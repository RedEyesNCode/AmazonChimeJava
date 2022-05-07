package com.redeyesncode.amazonchimekotlin.data

import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val senderName: String,
    val timestamp: Long,
    val text: String,
    val isLocal: Boolean
) {
    val displayTime: String
        get() {
            return SimpleDateFormat("HH:mm").format(Date(timestamp))
        }
}