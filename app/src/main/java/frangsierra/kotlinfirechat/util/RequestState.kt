package frangsierra.kotlinfirechat.util

import mini.Grove

data class TypedTask<out T>(
    val status: Status = TypedTask.Status.IDLE,
    val lastUpdate: Long = System.currentTimeMillis(),
    val data: T? = null,
    val error: Throwable? = null,
    val progress: Float? = null) {

    enum class Status {
        IDLE, RUNNING, SUCCESS, FAILURE
    }

    fun isRunning() = this.status == Status.RUNNING
    fun isTerminal() = this.status == Status.SUCCESS || this.status == Status.FAILURE
    fun isSuccessful() = this.status == Status.SUCCESS
    fun isFailure() = this.status == Status.FAILURE
}

fun <T> taskRunning(): TypedTask<T> {
    return TypedTask(status = TypedTask.Status.RUNNING)
}

fun <T> taskSuccess(data: T? = null): TypedTask<T> {
    return TypedTask(status = TypedTask.Status.SUCCESS, data = data)
}

fun <T> taskFailure(error: Throwable? = null): TypedTask<T> {
    Grove.e(error) { "Task error" }
    return TypedTask(status = TypedTask.Status.FAILURE, error = error)
}

fun <T> taskFailure(error: Throwable? = null, data: T): TypedTask<T> {
    Grove.e(error) { "Task error" }
    return TypedTask(status = TypedTask.Status.FAILURE, error = error, data = data)
}

typealias Task = TypedTask<Nothing?>