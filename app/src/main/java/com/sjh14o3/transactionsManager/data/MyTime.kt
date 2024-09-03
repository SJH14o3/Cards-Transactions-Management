package com.sjh14o3.transactionsManager.data

//this class has same story with MyDate class
class MyTime(val HOUR: Int, val MINUTE: Int) {
    override fun toString(): String {
        return "MyTime(HOUR=$HOUR, MINUTE=$MINUTE)"
    }

    fun getHourFormatted(): String {
        return if (HOUR < 10) {
            "0$HOUR"
        } else HOUR.toString()
    }

    fun getMinuteFormatted(): String {
        return if (MINUTE < 10) "0$MINUTE"
        else MINUTE.toString()
    }
}