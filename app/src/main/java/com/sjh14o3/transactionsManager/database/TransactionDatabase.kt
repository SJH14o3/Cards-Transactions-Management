package com.sjh14o3.transactionsManager.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sjh14o3.transactionsManager.data.MyDate
import com.sjh14o3.transactionsManager.data.Transaction
import java.util.Calendar

//this is the database of transactions
class TransactionDatabase(context: Context):  SQLiteOpenHelper(context, "transactions.db", null , 1) {
    companion object {
        const val TRANSACTION_TABLE = "TRANSACTION_TABLE"
        const val COLUMN_ID = "ID"
        const val BANK_ID = "BANK_ID"
        const val COLUMN_DATE = "DATE" //time format in a long is: yyyy/mm/dd/hh/mm
        const val COLUMN_CHANGE = "CHANGE"
        const val COLUMN_NOTE = "NOTE"
        const val COLUMN_TYPE = "TYPE"
        const val COLUMN_REMAIN = "REMAIN"
    }
    override fun onCreate(p0: SQLiteDatabase?) {
        val createDatabase = "CREATE TABLE $TRANSACTION_TABLE ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $BANK_ID INTEGER, $COLUMN_DATE INTEGER, $COLUMN_CHANGE INTEGER," +
                " $COLUMN_NOTE TEXT, $COLUMN_TYPE INTEGER, $COLUMN_REMAIN, INTEGER)"
        p0!!.execSQL(createDatabase)
        println(createDatabase)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    fun makeFormattedDate(year:Int, formattedMonth: Int, formattedDay: Int): Long {
        return "$year${formattedMonth}${formattedDay}0000".toLong()
    }

    //with inserted two ranges, all transaction between that time will be returned
    fun getCardAllTransactionsCustomRange(cardID: Int, start: Long, end: Long): Array<Transaction> {
        val out: ArrayList<Transaction>
        val sql = "SELECT * FROM $TRANSACTION_TABLE WHERE $BANK_ID = $cardID AND $COLUMN_DATE >= $start AND $COLUMN_DATE < $end ORDER BY $COLUMN_DATE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(sql, null)
        out = duplicateGetAll(cursor, cardID)
        db.close()
        cursor.close()
        return out.toTypedArray()
    }

    //get current month and return all of the transaction in this month
    fun getCardAllTransactionsThisMonth(cardID: Int): Array<Transaction> {
        val out: ArrayList<Transaction>
        val today= MyDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, 0)
        val current = "${today.getYear()}${today.getMonthFormatted()}${today.getDayFormatted()}0000".toLong()
        val sql = "SELECT * FROM $TRANSACTION_TABLE WHERE $BANK_ID = $cardID AND $COLUMN_DATE > $current ORDER BY $COLUMN_DATE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(sql, null)
        out = duplicateGetAll(cursor, cardID)
        db.close()
        cursor.close()
        return out.toTypedArray()
    }

    //since last functions where similar, this function is here
    private fun duplicateGetAll(cursor: Cursor, cardID: Int): ArrayList<Transaction> {
        val out: ArrayList<Transaction>
        if (cursor.moveToFirst()) {
            val count = cursor.count
            out = java.util.ArrayList(count + 2) //two dummies transactions will be added at the end that's why count + 2 cards will be returned
            out.add(Transaction.createDummyTransaction()) //this is a dummy transaction to fix nested scroll view not showing all transactions
            out.add(Transaction.createDummyTransaction())
            do {
                val id = cursor.getInt(0)
                val rawDate = cursor.getLong(2).toString()
                val date = "${rawDate.substring(0,4)}-${rawDate.substring(4,6)}-${rawDate.substring(6,8)}"
                val time = "${rawDate.substring(8,10)}:${rawDate.substring(10,12)}"
                val change = cursor.getLong(3)
                val note = cursor.getString(4)
                val type = cursor.getShort(5).toByte()
                val remain = cursor.getLong(6)
                out.add(Transaction(date, time, change, remain, note, type, id, cardID))
            } while(cursor.moveToNext())
        } else { //there was no result in the query
            out = java.util.ArrayList(2)
            out.add(Transaction.createDummyTransaction()) //this is a dummy transaction to fix nested scroll view not showing all transactions
            out.add(Transaction.createDummyTransaction())
        }
        return out
    }

    //add a transaction to database
    fun addTransaction(transaction: Transaction): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(BANK_ID, transaction.getBankId())
        cv.put(COLUMN_DATE, transaction.getDateAndTimeAsLong())
        cv.put(COLUMN_CHANGE, transaction.getChange())
        cv.put(COLUMN_NOTE, transaction.getNote())
        cv.put(COLUMN_TYPE, transaction.getType())
        cv.put(COLUMN_REMAIN, transaction.getRemain())
        val out = db.insert(TRANSACTION_TABLE, null, cv)
        db.close()
        return out > 0
    }

    //will return remain of the card
    fun getLastRemain(bankID: Int): Long {
        val out: Long
        val query = "SELECT $COLUMN_REMAIN FROM $TRANSACTION_TABLE WHERE $BANK_ID = $bankID ORDER BY $COLUMN_DATE DESC LIMIT 1"
        return getLastRemainDuplicate(query)
    }

    //will return remain of the card but before the inserted time
    fun getLastRemainBeforeTime(cardId: Int, time: Long): Long {
        val out: Long
        val query = "SELECT $COLUMN_REMAIN FROM $TRANSACTION_TABLE WHERE $BANK_ID = $cardId AND $COLUMN_DATE < $time ORDER BY $COLUMN_DATE DESC LIMIT 1"
        return getLastRemainDuplicate(query)
    }

    private fun getLastRemainDuplicate(query: String): Long {
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        val out = if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else 0L
        cursor.close()
        db.close()
        return out
    }


    //get the newest transaction time
    fun getLastDateAndTime(bankID: Int): Long {
        val query = "SELECT $COLUMN_DATE FROM $TRANSACTION_TABLE WHERE $BANK_ID = $bankID ORDER BY $COLUMN_DATE DESC LIMIT 1"
        return getAnEndDuplicate(query)
    }

    //get the first transaction of card
    fun getFirstDateAndTime(cardID: Int): Long {
        val query = "SELECT $COLUMN_DATE FROM $TRANSACTION_TABLE WHERE $BANK_ID = $cardID ORDER BY $COLUMN_DATE LIMIT 1"
        return getAnEndDuplicate(query)
    }

    //last functions where similar so this function is here, also if there isn't any transaction, this function returns 0
    private fun getAnEndDuplicate(query: String): Long {
        val db = this.readableDatabase
        val out: Long
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            out = cursor.getLong(0)
        } else return 0L
        cursor.close()
        db.close()
        return out
    }

    //it is not allowed to have two transaction at the same exact time (year-month-day-hour-minute)
    fun hasDateConflict(date: Long, bankID: Int): Boolean {
        val out: Boolean
        val query = "SELECT $COLUMN_TYPE FROM $TRANSACTION_TABLE WHERE $COLUMN_DATE = $date AND $BANK_ID = $bankID LIMIT 1"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        out = cursor.moveToFirst()
        cursor.close()
        db.close()
        return out
    }

    /*this function will reset the remain of the transaction based of the new inserted remain.
    if a transaction new remain become negative, the operation will fail and all the changes will be rolled back*/
    fun resetNextRowsRemain(date: Long, remainInput: Long, cardID: Int): Boolean {
        val cursor: Cursor
        var newRemain = remainInput
        val query = "SELECT $COLUMN_CHANGE, $COLUMN_ID FROM $TRANSACTION_TABLE WHERE $BANK_ID = $cardID AND $COLUMN_DATE > $date"
        val db: SQLiteDatabase = this.writableDatabase
        db.beginTransaction()
        cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val change = cursor.getLong(0)
                val id = cursor.getInt(1)
                newRemain += change
                if (newRemain < 0) {
                    cursor.close()
                    db.endTransaction()
                    db.close()
                    return false
                }
                val updateQuery = "UPDATE $TRANSACTION_TABLE SET $COLUMN_REMAIN = $newRemain WHERE $COLUMN_ID = $id"
                db.execSQL(updateQuery)
            } while (cursor.moveToNext())
            db.setTransactionSuccessful()
        }
        cursor.close()
        db.endTransaction()
        db.close()
        return true
    }
    /*this function is called when a row is deleted and user checked updating the remain of next transactions.
    * it won't reset remains, it will update them but still it is forbidden to have negative remain*/
    fun updateNextRowsRemain(date: Long, changeInput: Long, cardID: Int): Boolean {
        println("LOG: DELETED CHANGE: $changeInput")
        val db = this.writableDatabase
        val query = "SELECT $COLUMN_REMAIN, $COLUMN_ID FROM $TRANSACTION_TABLE WHERE $BANK_ID = $cardID AND $COLUMN_DATE > $date"
        db.beginTransaction()
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val remain = cursor.getLong(0)
                println("LOG: old remain: $remain")
                val id = cursor.getInt(1)
                println("LOG: WTF!: $changeInput")
                val newRemain = remain - changeInput
                println("LOG: new remain: $newRemain")
                if (newRemain < 0) {
                    cursor.close()
                    db.endTransaction()
                    db.close()
                    return false
                }
                val updateQuery = "UPDATE $TRANSACTION_TABLE SET $COLUMN_REMAIN = $newRemain WHERE $COLUMN_ID = $id"
                db.execSQL(updateQuery)
            } while (cursor.moveToNext())
            db.setTransactionSuccessful()
        }
        cursor.close()
        db.endTransaction()
        db.close()
        return true
    }

    //this function will delete all of the given card transactions
    fun deleteAllCardTransactions(bankID: Int) {
        val query = "DELETE FROM $TRANSACTION_TABLE WHERE $BANK_ID = $bankID"
        val db = this.writableDatabase
        db.execSQL(query)
        db.close()
    }

    fun getCount(): Int {
        val query = "SELECT $COLUMN_ID FROM $TRANSACTION_TABLE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count
    }

    //will simply delete a transaction
    fun deleteTransaction(id: Int): Boolean {
        val db = this.writableDatabase
        val out = db.delete(TRANSACTION_TABLE, "$COLUMN_ID = $id", null)
        db.close()
        return out > 0
    }

}