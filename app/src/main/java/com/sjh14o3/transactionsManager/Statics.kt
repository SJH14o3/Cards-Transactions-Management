package com.sjh14o3.transactionsManager

import android.content.Context
import android.content.Intent

//this class contains some useful variable and will be initiated in the start of application
class Statics {
    companion object {
        private lateinit var applicationContext: Context
        private lateinit var packageName: String

        fun setVariables(context: Context, name: String) {
            applicationContext = context
            packageName = name
        }

        fun getApplicationContext(): Context {
            return applicationContext
        }

        fun getPackageName(): String {
            return packageName
        }

        fun switchActivity(current: Context, target: Class<*>) {
            val intent = Intent(current, target)
            current.startActivity(intent)
        }
    }
}