package com.sjh14o3.transactionsManager.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sjh14o3.transactionsManager.data.DebitCard

class CardDatabase(context: Context):  SQLiteOpenHelper(context, "cards.db", null , 1){
    companion object {
        //statics values for table name and columns
        const val CARDS_TABLE = "CARDS_TABLE"
        const val COLUMN_ID = "ID"
        const val COLUMN_TITLE = "TITLE"
        const val COLUMN_CARD_NUMBER = "CARD_NUMBER"
        const val COLUMN_SHABA = "SHABA_NUMBER"
        const val COLUMN_EXPIRY_MONTH = "EXPIRY_MONTH"
        const val COLUMN_EXPIRY_YEAR = "EXPIRY_YEAR"
        const val COLUMN_OWNER_NAME = "EXPIRY_OWNER_NAME"
    }
    //when database doesn't exist, it will be created
    override fun onCreate(p0: SQLiteDatabase?) {
        val createDatabase = "CREATE TABLE $CARDS_TABLE ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $COLUMN_TITLE TEXT, $COLUMN_CARD_NUMBER INTEGER, $COLUMN_SHABA TEXT," +
                " $COLUMN_EXPIRY_MONTH INTEGER, $COLUMN_EXPIRY_YEAR INTEGER, $COLUMN_OWNER_NAME, TEXT)"
        p0!!.execSQL(createDatabase)
    }
    //when changes were made to database for example new columns this function will be executed
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
    //return all of the cards
    fun getAll(): Array<DebitCard> {
        val out: ArrayList<DebitCard>
        val sql = "SELECT * FROM $CARDS_TABLE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            val count = cursor.count
            out = java.util.ArrayList(count + 1) //a dummy card will be added at the end that's why count + 1 cards will be returned
            do {
                val id = cursor.getInt(0)
                val cardName = cursor.getString(1)
                val cardNumberSure = cursor.getLong(2)
                val cardNumberSize = cardNumberSure.toString().length
                val cardNumberLong = if (cardNumberSize != 16) { //if card starts with some zeros, this needs to be handled.
                    "0".repeat(16-cardNumberSize) + cardNumberSure.toString()
                } else cardNumberSure.toString()

                val cardNumber = "${cardNumberLong.substring(0,4)} ${cardNumberLong.substring(4,8)} " +
                        "${cardNumberLong.substring(8,12)} ${cardNumberLong.substring(12,16)}"
                val cardShaba = cursor.getString(3)
                val cardExpiryMonth = cursor.getInt(4).toByte()
                val cardExpiryYear = cursor.getShort(5)
                val cardOwner = cursor.getString(6)
                out.add(DebitCard(cardName, cardNumber, cardShaba, cardExpiryMonth, cardExpiryYear, cardOwner, id))
            } while(cursor.moveToNext())
        } else { //there was no result in the query
            out = java.util.ArrayList(1)
        }
        out.add(DebitCard("","","",0,0,"")) //this is a dummy card that will be replaced with a add card button
        db.close()
        cursor.close()
        return out.toTypedArray()
    }
    //adding card happens here
    fun addCard(card: DebitCard): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_TITLE, card.getTitle())
        cv.put(COLUMN_CARD_NUMBER, card.getCardNumberPlain().toLong())
        cv.put(COLUMN_SHABA, card.getShaba())
        cv.put(COLUMN_EXPIRY_MONTH, card.getExpiryMonth())
        cv.put(COLUMN_EXPIRY_YEAR, card.getExpiryYear())
        cv.put(COLUMN_OWNER_NAME, card.getOwnerName())
        val out = db.insert(CARDS_TABLE, null, cv)
        db.close()
        return out > 0
    }
    //editing card here, only columns with changes will be updated
    fun editCard(old: DebitCard, new: DebitCard): Boolean{
        val db = this.writableDatabase
        val cv = ContentValues()
        if (old.getTitle() != new.getTitle()) {
            cv.put(COLUMN_TITLE, new.getTitle())
        }
        if (old.getCardNumber() != new.getCardNumber()) {
            cv.put(COLUMN_CARD_NUMBER, new.getCardNumberPlain().toLong())
        }
        if (old.getShaba() != new.getShaba()) {
            cv.put(COLUMN_SHABA, new.getShaba())
        }
        if (old.getExpiryMonth() != new.getExpiryMonth()) {
            cv.put(COLUMN_EXPIRY_MONTH, new.getExpiryMonth())
        }
        if (old.getExpiryYear() != new.getExpiryYear()) {
            cv.put(COLUMN_EXPIRY_YEAR, new.getExpiryYear())
        }
        if (old.getOwnerName() != new.getOwnerName()) {
            cv.put(COLUMN_OWNER_NAME, new.getOwnerName())
        }
        val out = db.update(CARDS_TABLE, cv, "$COLUMN_ID = ${old.getId()}", null)
        db.close()
        return out > 0
    }
    //card will be deleted here
    fun deleteCard(id: Int): Boolean {
        val db = writableDatabase
        val sql = "DELETE FROM $CARDS_TABLE WHERE $COLUMN_ID = $id"
        val cursor = db.rawQuery(sql, null)
        val out = (cursor.count != 0)
        cursor.close()
        return out
    }

    //this function will return the debit card when id is passed
    fun getCard(id: Int): DebitCard {
        val query = "SELECT * FROM $CARDS_TABLE WHERE $COLUMN_ID = $id"
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val cardNumberSure = cursor.getLong(2)
        val cardNumberSize = cardNumberSure.toString().length
        val cardNumberLong = if (cardNumberSize != 16) { //if card starts with some zeros, this needs to be handled.
            "0".repeat(16-cardNumberSize) + cardNumberSure.toString()
        } else cardNumberSure.toString()

        val cardNumber = "${cardNumberLong.substring(0,4)} ${cardNumberLong.substring(4,8)} " +
                "${cardNumberLong.substring(8,12)} ${cardNumberLong.substring(12,16)}"
        val card = DebitCard(cursor.getString(1), cardNumber, cursor.getString(3),
            cursor.getShort(4).toByte(), cursor.getShort(5), cursor.getString(6), id)
        cursor.close()
        db.close()
        return card
    }
}