package com.sjh14o3.transactionsManager.data

import android.annotation.SuppressLint
import android.widget.ImageView
import com.sjh14o3.transactionsManager.R
import com.sjh14o3.transactionsManager.Statics
import java.io.Serializable

//transaction class is here
class Transaction: Serializable {
    private val date: String
    private val time: String
    private val change: Long
    private val remain: Long
    private val note: String
    private val type: Byte
    private var id: Int = -1
    private val bankId: Int

    constructor(date: String, time: String, change: Long, remain: Long, note: String, type: Byte, bankId: Int) {
        this.date = date
        this.time = time
        this.change = change
        this.remain = remain
        this.note = note
        this.type = type
        this.bankId = bankId
    }

    constructor(date: String, time: String, change: Long, remain: Long, note: String, type: Byte, id: Int, bankId: Int) {
        this.date = date
        this.time = time
        this.change = change
        this.remain = remain
        this.note = note
        this.type = type
        this.id = id
        this.bankId = bankId
    }

    fun getDate(): String {
        return date
    }

    fun getTime(): String {
        return time
    }

    fun getChange(): Long {
        return change
    }

    fun getRemain(): Long {
        return remain
    }

    fun getNote(): String {
        return note
    }

    fun getId(): Int {
        return id
    }

    fun getType(): Byte {
        return type
    }

    fun getDay(): String {
        return date.split("-")[2]
    }

    fun getBankId(): Int {
        return bankId
    }

    //this function will return full time with this format: yyyyMMddHHmm
    fun getDateAndTimeAsLong(): Long {
        val dateInfo = date.split("-")
        val timeInfo = time.split(":")
        val outStr = "${dateInfo[0]}${dateInfo[1]}${dateInfo[2]}${timeInfo[0]}${timeInfo[1]}"
        return outStr.toLong()
    }

    fun getOrdinalDay(): String {
        val day = getDay()
        val append = when(day) {
            "1" -> "st"
            "21" -> "st"
            "31" -> "st"
            "2" -> "nd"
            "22" -> "nd"
            else -> "th"
        }
        return day + append
    }

    fun getOrdinalDayAndMonth(): String {
        val month = when(date.split("-")[1].toInt()) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "INVALID MONTH"
        }
        return month + getOrdinalDay()
    }

    fun getOrdinalAll(): String {
        return getOrdinalDayAndMonth() + date.split("-")[0]
    }

    override fun toString(): String {
        return "Transaction(bankId=$bankId, type=$type, note='$note', remain=$remain, change=$change, time='$time', date='$date')"
    }

    companion object {
        const val CATEGORIES_COUNT = 14
        //integer to month
        fun convertToMonth(number: Int): String {
            return when(number) {
                1 -> "January"
                2 -> "February"
                3 -> "March"
                4 -> "April"
                5 -> "May"
                6 -> "June"
                7 -> "July"
                8 -> "August"
                9 -> "September"
                10 -> "October"
                11 -> "November"
                12 -> "December"
                else -> "INVALID MONTH"
            }
        }
        //a function to exchange type from byte to string
        fun getTypeName(type: Byte): String {
            return when(type.toInt()) {
                0 -> "Income"
                1 -> "Groceries"
                2 -> "Cafe or Restaurant"
                3 -> "Entertainment"
                4 -> "Bill"
                5 -> "Transportation"
                6 -> "Insurance"
                7 -> "Healthcare"
                8 -> "Education"
                9 -> "Clothing"
                10 -> "Investments"
                11 -> "Debt"
                12 -> "Tax"
                13 -> "Housing"
                else -> "Other"
            }
        }
        //same as last function but in reverse
        fun iconNameToType(str: String): Byte {
            return when(str) {
                "income" -> 0
                "groceries" -> 1
                "cafe_or_restaurant" -> 2
                "entertainment" -> 3
                "bill" -> 4
                "transportation" -> 5
                "insurance" -> 6
                "healthcare" -> 7
                "education" -> 8
                "clothing" -> 9
                "investment" -> 10
                "debt" -> 11
                "tax" -> 12
                "housing" -> 13
                else -> 127
            }
        }
        //this will return transaction icon URI
        fun getTypeIconName(type: Byte): String {
            return "ic_tr_" + when(type.toInt()) {
                0 -> "income"
                1 -> "groceries"
                2 -> "cafe_or_restaurant"
                3 -> "entertainment"
                4 -> "bill"
                5 -> "transportation"
                6 -> "insurance"
                7 -> "healthcare"
                8 -> "education"
                9 -> "clothing"
                10 -> "investment"
                11 -> "debt"
                12 -> "tax"
                13 -> "housing"
                else -> "other"
            }
        }
        //this will set the input image with the type icon
        @SuppressLint("UseCompatLoadingForDrawables")
        fun setIconType(type: Byte, image: ImageView) {
            try {
                //loading in bank logo to identify bank
                val res = Statics.getApplicationContext().resources
                val directory = getTypeIconName(type)
                val resID = res.getIdentifier(directory, "drawable", Statics.getPackageName())
                image.setImageDrawable(res.getDrawable(resID))
                //if somehow  the bank logo was not available, a default icon will be used instead
            } catch (e: Exception) {
                e.printStackTrace()
                image.setImageResource(R.drawable.ic_tr_other)
            }
        }
        //a function to add commas between digits to improve reading, and also all digits will be shown as TOMAN
        fun getSeparatedDigits(number: Long): String {
            //TODO: consider adding an option to toggle between Toman and Rial
            var toman = number / 10
            if (number < 0) toman *= -1
            val sb: StringBuilder = StringBuilder(toman.toString())
            val length = sb.length
            // Start from the rightmost position and insert commas every 3 characters
            var i = length - 3
            while (i > 0) {
                sb.insert(i, ',')
                i -= 3
            }
            val first = number % 10
            sb.append(".${first}")
            return if (number<0) "-$sb"
            else sb.toString()
        }
        //used for invisible transaction card in recycler view to fix the stupid bug of not showing all items
        fun createDummyTransaction(): Transaction {
            return Transaction("","",0,0,"",20, 0)
        }
        //if a transaction is more than 3 months old, it cannot be deleted or edited.
        fun allowedForMoreOperations(time: String): Boolean {
            var year = time.substring(0,4).toInt()
            var month = time.substring(4,6).toInt() + 3
            if (month > 12) {
                month -= 12
                year += 1
            }
            val formattedMonth = if (month < 10) "0$month" else month.toString()
            val limit = "${year}${formattedMonth}${time.substring(6)}".toLong()
            println("MYLOG: LIMIT = $limit")
            println("MYLOG: current = ${Statics.getExactTime()}")
            return limit > Statics.getExactTime()
        }
    }
}