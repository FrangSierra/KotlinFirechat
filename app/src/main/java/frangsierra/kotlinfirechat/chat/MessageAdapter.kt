package frangsierra.kotlinfirechat.chat

import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.calculateDiff
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import frangsierra.kotlinfirechat.common.firebase.Message
import kotlinx.android.synthetic.main.item_message.view.*

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val messageList: MutableList<Pair<String, Message>> = java.util.ArrayList()

    override fun onBindViewHolder(holder: frangsierra.kotlinfirechat.chat.MessageAdapter.MessageViewHolder, position: Int) {
        with(messageList[position].second) {
            holder.messageTextView.text = text
            holder.authorTextView.text = name
            //TODO implement image
//            holder.photoImageView
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): frangsierra.kotlinfirechat.chat.MessageAdapter.MessageViewHolder {
        val v = android.view.LayoutInflater.from(parent.context).inflate(frangsierra.kotlinfirechat.R.layout.item_message, parent, false)
        return MessageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun updateMessages(messages : List<Pair<String, Message>>) {
        val diffResult = calculateDiff(MessageDiff(this.messageList, messages))
        messageList.clear()
        messageList.addAll(messages)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class MessageViewHolder(v: android.view.View) : RecyclerView.ViewHolder(v) {
        val photoImageView: ImageView = v.photoImageView
        val messageTextView: TextView = v.messageTextView
        val authorTextView: TextView = v.nameTextView
    }

    inner class MessageDiff(val oldList: List<Pair<String, Message>>, val newList: List<Pair<String, Message>>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].first == newList[newItemPosition].first
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].second == newList[newItemPosition].second
        }
    }
}