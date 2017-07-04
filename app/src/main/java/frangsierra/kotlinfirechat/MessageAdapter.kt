package frangsierra.kotlinfirechat

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.util.*


class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val messageList: MutableList<Pair<String, Message>> = ArrayList()

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        with(messageList[position].second) {
            holder.messageTextView.text = text
            holder.authorTextView.text = name
//            holder.photoImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun addMessage(key: String, newMessage: Message) {
        val newMessages = emptyList<Pair<String,Message>>().plus(messageList).plus(Pair(key, newMessage))
        val diffResult = DiffUtil.calculateDiff(MessageDiff(this.messageList, newMessages))
        messageList.clear()
        messageList.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateMessage(key: String, newMessage: Message) {
        val newMessages = emptyList<Pair<String,Message>>().plus(messageList).filterNot { it.first == key }.plus(Pair(key, newMessage))
        val diffResult = DiffUtil.calculateDiff(MessageDiff(this.messageList, newMessages))
        messageList.clear()
        messageList.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val photoImageView = v.findViewById(R.id.photoImageView) as ImageView
        val messageTextView = v.findViewById(R.id.messageTextView) as TextView
        val authorTextView = v.findViewById(R.id.nameTextView) as TextView
    }

    fun deleteMessage(key: String) {
        val newMessages = emptyList<Pair<String,Message>>().plus(messageList).filter { it.first != key }
        val diffResult = DiffUtil.calculateDiff(MessageDiff(this.messageList, newMessages))
        messageList.clear()
        messageList.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }
}

 class MessageDiff(val oldList: List<Pair<String, Message>>, val newList: List<Pair<String, Message>>) : DiffUtil.Callback() {

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
