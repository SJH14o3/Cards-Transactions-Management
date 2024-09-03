package com.sjh14o3.transactionsManager

import android.content.Context
import android.content.Intent
import com.sjh14o3.transactionsManager.data.MyDate
import com.sjh14o3.transactionsManager.database.CardDatabase
import com.sjh14o3.transactionsManager.database.TransactionDatabase
import java.util.Calendar

//this class contains some useful variable and will be initiated in the start of application
class Statics {
    companion object {
        private lateinit var applicationContext: Context
        private lateinit var packageName: String
        private lateinit var cardDatabase: CardDatabase
        private lateinit var mainActivity: MainActivity
        private lateinit var transactionDatabase: TransactionDatabase

        fun setVariables(context: Context, name: String, main: MainActivity) {
            applicationContext = context
            packageName = name
            cardDatabase = CardDatabase(context)
            mainActivity = main
            transactionDatabase = TransactionDatabase(context)
        }

        fun getApplicationContext(): Context {
            return applicationContext
        }

        fun getPackageName(): String {
            return packageName
        }

        fun getCardDatabase(): CardDatabase {
            return cardDatabase
        }

        fun getMainActivity(): MainActivity {
            return mainActivity
        }

        fun getTransactionDatabase(): TransactionDatabase {
            return transactionDatabase
        }

        fun switchActivity(current: Context, target: Class<*>) {
            val intent = Intent(current, target)
            current.startActivity(intent)
        }

        fun getDate(): MyDate {
            val currentDate = Calendar.getInstance()
            return MyDate(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) + 1, currentDate.get(Calendar.DAY_OF_MONTH))
        }
    }
}