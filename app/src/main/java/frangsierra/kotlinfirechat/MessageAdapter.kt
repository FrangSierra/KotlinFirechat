package frangsierra.kotlinfirechat

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.util.*


class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val messageList: MutableList<Message> = ArrayList()

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        with(messageList[position]) {
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

    fun addMessage(message: Message) {
        if (messageList.contains(message)) return
        messageList.add(message)
        notifyItemInserted(messageList.size.minus(1))
    }

    inner class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val photoImageView = v.findViewById(R.id.photoImageView) as ImageView
        val messageTextView = v.findViewById(R.id.messageTextView) as TextView
        val authorTextView = v.findViewById(R.id.nameTextView) as TextView
    }
}
