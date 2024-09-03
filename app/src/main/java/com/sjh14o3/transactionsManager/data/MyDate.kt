package com.sjh14o3.transactionsManager.data

//In this data class I will save date, since I needed to implement two custom functions, this class exist
class MyDate(private val year: Int, private val month: Int, private val day: Int) {
    fun getYear(): Int { //since year is at least 4 digits, there is no need for a formatted year
        return year
    }
    fun getMonth(): Int {
        return month
    }
    fun getDay(): Int {
        return day
    }
    fun getMonthFormatted(): String { //if a month is less than 10 e.g.: 6, it will return as 0x e.g: 06
        return if (month < 10) "0${month}"
        else month.toString()
    }
    fun getDayFormatted(): String { //same for day too
        return if (day < 10) "0${day}"
        else day.toString()
    }

    override fun toString(): String {
        return "MyDate(year=$year, month=$month, day=$day)"
    }
}