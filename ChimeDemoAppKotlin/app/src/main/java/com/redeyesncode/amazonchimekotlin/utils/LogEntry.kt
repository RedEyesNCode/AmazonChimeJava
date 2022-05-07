package com.redeyesncode.amazonchimekotlin.utils

import com.amazonaws.services.chime.sdk.meetings.utils.logger.LogLevel

data class LogEntry(
    val sequenceNumber: Int,
    val message: String,
    val timestampMs: Long,
    val logLevel: LogLevel
)