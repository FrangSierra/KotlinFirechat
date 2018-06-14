package frangsierra.kotlinfirechat.home


import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.calculateDiff
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.chat.model.Message
import frangsierra.kotlinfirechat.util.getTimeAgoText
import frangsierra.kotlinfirechat.util.setCircularImage
import kotlinx.android.synthetic.main.item_message.view.*

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val messageList: MutableList<Message> = java.util.ArrayList()

    override fun onBindViewHolder(holder: MessageAdapter.MessageViewHolder, position: Int) {
        with(messageList[position]) {
            holder.messageTextView.text = message
            holder.authorTextView.text = author.username
            //holder.timeAgoTextView.text = timestamp.time
            if (author.photoUrl == null) {
                holder.photoImageView.visibility = View.GONE
            } else {
                holder.photoImageView.setCircularImage(author.photoUrl)
                holder.photoImageView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MessageAdapter.MessageViewHolder {
        val v = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun updateMessages(messages: List<Message>) {
        val diffResult = calculateDiff(MessageDiff(this.messageList, messages))
        messageList.clear()
        messageList.addAll(messages)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class MessageViewHolder(v: android.view.View) : RecyclerView.ViewHolder(v) {
        val photoImageView: ImageView = v.comment_author_picture
        val messageTextView: TextView = v.comment_description
        val authorTextView: TextView = v.comment_author_username
        val timeAgoTextView: TextView = v.comment_time_ago
    }

    inner class MessageDiff(val oldList: List<Message>, val newList: List<Message>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].uid == newList[newItemPosition].uid
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}