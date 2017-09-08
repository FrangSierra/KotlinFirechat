package frangsierra.kotlinfirechat.common.log

import android.content.Context
import android.support.v4.util.Pools
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

/**
 * Factory for [FileTree].
 * Multiple instances of this controller over the same folder are not safe.
 *
 * @param relativeLogFolderPath Path for the folder containing multiple log files.
 */
class FileLogController(
        val context: Context,
        val relativeLogFolderPath: String = "logs",
        val minLogLevel: Int = Log.VERBOSE) {

    companion object {
        private val FILE_NAME_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
    }

    private var _currentFileTree: FileTree? = null
    val currentFileTree: FileTree
        get() = _currentFileTree!!

    private val logsFolder: File? by lazy {
        var root = context.getExternalFilesDir(null)
        if (root == null) {
            //Fall back to private directory
            root = context.filesDir
        }
        val logRootDirectory = File(root.absolutePath, relativeLogFolderPath)
        if (!logRootDirectory.exists()) {
            if (!logRootDirectory.mkdir()) {
                Grove.e { "Unable to create log directory, nothing will be written on disk" }
                return@lazy null
            }
        }
        return@lazy logRootDirectory
    }

    /**
     * Create a new fileTree and close the previous one if present.
     *
     * This operation may block, consider executing it in another thread.
     *
     * @return The logger, or null if the file could not be created.
     */
    fun newFileTree(): FileTree? {
        if (logsFolder == null) return null

        val logFileName = String.format("log-${FILE_NAME_DATE_FORMAT.format(Date())}.txt")
        val logFile = File(logsFolder, logFileName)
        Grove.d { "New session, logs will be stored in: ${logFile.absolutePath}" }
        _currentFileTree?.exit()
        _currentFileTree = FileTree(logFile, minLogLevel)
        return _currentFileTree
    }

    /**
     * Delete any log files created under [logsFolder] older that `maxAge` in ms.
     *
     * This operation may block, consider executing it in another thread.
     *
     * Current log file wont be deleted.
     *
     * @return Deleted files count.
     */
    fun deleteOldLogs(maxAge: Long, maxCount: Int = Int.MAX_VALUE): Int {
        var deleted = 0
        val files = logsFolder?.listFiles()
        files?.sortedByDescending(File::lastModified)?.apply {
            for ((i, file) in this.withIndex()) {
                val age = System.currentTimeMillis() - file.lastModified()
                if (age > maxAge || i > maxCount) {
                    val isCurrentLogFile = _currentFileTree?.file?.absolutePath == file.absolutePath
                    if (!isCurrentLogFile && file.delete()) deleted++
                }
            }
        }
        return deleted
    }
}

/**
 * Logger that writes asynchronously to a file.
 * Automatically infers the tag from the calling class.
 */
class FileTree
/**
 * Create a new FileTree instance that will write in a background thread
 * any incoming logs as long as the level is at least `minLevel`.

 * @param file     The file this logger will write.
 * @param minLevel The minimum message level that will be written (inclusive).
 */
(val file: File, private val minLevel: Int) : Tree {
    private val queue = ArrayBlockingQueue<LogLine>(100)
    private val pool = Pools.SynchronizedPool<LogLine>(20)

    private val backgroundThread: Thread
    private val writer: Writer?

    init {
        var writer: Writer?
        this.backgroundThread = Thread(Runnable { this.loop() })
        try {
            //Not buffered, we want to write on the spot
            writer = FileWriter(file.absolutePath, true)
            this.backgroundThread.start()
        } catch (e: IOException) {
            writer = null
            Grove.e(e) { "Failed to create writer, nothing will be done" }
        }

        this.writer = writer
    }

    /**
     * Flush the file, this call is required before application dies or the file will be empty.
     */
    fun flush() {
        if (writer != null) {
            try {
                writer.flush()
            } catch (e: IOException) {
                Grove.e(e) { "Flush failed" }
            }

        }
    }

    /**
     * Close the file and exit. This method does not block.
     */
    fun exit() {
        this.backgroundThread.interrupt()
    }

    override fun log(priority: Int, tag: String, message: String) {
        enqueueLog(priority, tag, message)
    }

    private fun enqueueLog(priority: Int, tag: String, message: String) {
        var logLine: LogLine? = pool.acquire()
        if (logLine == null) {
            logLine = LogLine()
        }

        logLine.tag = tag
        logLine.message = message
        logLine.level = priority
        logLine.date.time = System.currentTimeMillis()

        queue.offer(logLine)
    }

    private fun loop() {
        while (true) {
            try {
                val logLine = queue.take()
                if (writer != null) {
                    val lines = logLine.format()
                    for (line in lines) {
                        writer.write(line)
                    }
                }
                logLine.clear()
                pool.release(logLine)
            } catch (e: InterruptedException) {
                break //We are done
            } catch (e: IOException) {
                Grove.e(e) { "Failed to write line" }
                break
            }

        }
        closeSilently()
    }

    private fun closeSilently() {
        if (writer != null) {
            try {
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun toString(): String {
        return "FileTree{" +
                "file=" + file.absolutePath +
                '}'
    }

    private class LogLine {
        companion object {
            private val LOG_FILE_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.US)
        }

        internal val date = Date()
        internal var level: Int = 0
        internal var message: String? = null
        internal var tag: String? = null

        internal fun clear() {
            message = null
            tag = null
            date.time = 0
            level = 0
        }

        internal fun format(): List<String> {
            message?.let {
                val lines = it.split('\n').dropLastWhile(String::isEmpty)
                val levelString: String
                when (level) {
                    Log.DEBUG -> levelString = "D"
                    Log.INFO -> levelString = "I"
                    Log.WARN -> levelString = "W"
                    Log.ERROR -> levelString = "E"
                    else -> levelString = "V"
                }
                //[29-04-1993 01:02:34.567 D/SomeTag: The value to Log]
                val prelude = "[${LOG_FILE_DATE_FORMAT.format(date)}] $levelString/$tag"
                return lines.map { "$prelude $it \r\n" }
            }
            return emptyList()
        }
    }
}
