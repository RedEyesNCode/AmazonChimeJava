package com.redeyesncode.chimer.utils;

import com.amazonaws.services.chime.sdk.meetings.utils.logger.LogLevel;

public class LogEntry {
    private int sequenceNumber;
    private String message;
    private Long timeStampNs;
    private LogLevel logLevel;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeStampNs() {
        return timeStampNs;
    }

    public void setTimeStampNs(Long timeStampNs) {
        this.timeStampNs = timeStampNs;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public LogEntry(int sequenceNumber, String message, Long timeStampns, LogLevel logLevel){
        this.sequenceNumber=sequenceNumber;
        this.message=message;
        this.timeStampNs=timeStampns;
        this.logLevel=logLevel;


    }
}
