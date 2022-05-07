package com.redeyesncode.amazonchimekotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.redeyesncode.amazonchimekotlin.R
import com.redeyesncode.amazonchimekotlin.data.Message
import com.redeyesncode.amazonchimekotlin.databinding.RowMessageBinding
import com.redeyesncode.amazonchimekotlin.utils.inflate

class MessageAdapter(
    private val messages: Collection<Message>,var context: Context
) :
    RecyclerView.Adapter<MessageHolder>() {

    private lateinit var binding:RowMessageBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        binding = RowMessageBinding.inflate(LayoutInflater.from(context),parent,false)
        val inflatedView = parent.inflate(R.layout.row_message, false)
        return MessageHolder(inflatedView,binding)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = messages.elementAt(position)
        holder.bindMessage(message)
    }
}

class MessageHolder(inflatedView: View, binding: RowMessageBinding) :
    RecyclerView.ViewHolder(inflatedView) {
    private var view: View = inflatedView
    private var binding = RowMessageBinding.bind(inflatedView)

    fun bindMessage(message: Message) {
        binding.senderName.text = message.senderName
        binding.messageTimestamp.text = message.displayTime
        binding.messageText.text = message.text
        binding.messageText.contentDescription = message.text
        binding.messageText.textAlignment =
            if (message.isLocal) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START
    }
}
