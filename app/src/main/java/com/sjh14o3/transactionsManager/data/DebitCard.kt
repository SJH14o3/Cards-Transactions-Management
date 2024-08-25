package com.sjh14o3.transactionsManager.data

import android.annotation.SuppressLint
import android.content.res.Resources.NotFoundException
import android.widget.ImageView
import com.sjh14o3.transactionsManager.R
import com.sjh14o3.transactionsManager.Statics
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date

class DebitCard(private val title: String ,private val cardNumber: String,private val shaba: String,
                private val expiryMonth: Byte, private val expiryYear: Short, private val ownerName: String): Serializable {
    private var isExpired = false

    init {
        checkExpired()
    }

    fun isExpired(): Boolean {
        return isExpired
    }

    fun getTitle(): String {
        return title
    }

    fun getCardNumber(): String {
        return cardNumber
    }
    fun getShaba(): String {
        return shaba
    }
    fun getExpiryMonth(): Byte {
        return expiryMonth
    }
    fun getExpiryMonthWithFormat(): String {
        if (expiryMonth < 10) return "0$expiryMonth"
        return expiryMonth.toString()
    }
    fun getExpiryYear(): Short {
        return expiryYear
    }
    fun getOwnerName(): String {
        return ownerName
    }
    fun getBankName(): String {
        return identifyBank(cardNumber)
    }

    companion object {
        fun identifyBank(str: String): String {
            val out: String = when(str.substring(0, 7)) {
                "6062 56" -> "Askariye" //No Icon
                "6274 12" -> "Eghtesad_Novin"
                "6273 81" -> "Ansar"
                "5057 85" -> "Iran_Zamin"
                "6362 14" -> "Ayande"
                "6221 06" -> "Parsian"
                "6391 94" -> "Parsian"
                "6278 84" -> "Parsian"
                "6393 47" -> "Pasargad"
                "5022 29" -> "Pasargad"
                "6273 53" -> "Tejarat"
                "5029 08" -> "Tose_e_Taavon"
                "2071 77" -> "Tose_e_Saderat"
                "6276 48" -> "Tose_e_Saderat"
                "6369 49" -> "Hekmat"
                "5029 38" -> "Dey"
                "5041 72" -> "Resalat"
                "5894 63" -> "Refah"
                "6219 86" -> "Saman"
                "5892 10" -> "Sepah"
                "6396 07" -> "Sarmaye"
                "6393 46" -> "Sina"
                "5028 06" -> "Shahr"
                "6037 69" -> "Saderat"
                "6279 61" -> "Sanaat_va_Maadan"
                "6063 73" -> "Mehr"
                "6395 99" -> "Qavamin"
                "6274 88" -> "Kar_Afarin"
                "5029 10" -> "Kar_Afarin"
                "6037 70" -> "Keshavarzi"
                "6392 17" -> "Keshavarzi"
                "5054 16" -> "Gardeshgari"
                "6367 95" -> "Markazi"
                "6280 23" -> "Maskan"
                "6104 33" -> "Mellat"
                "9919 75" -> "Mellat"
                "6037 99" -> "Melli"
                "6393 70" -> "Mehr_Eghtesad"
                "6067 37" -> "Mehr_Eghtesad"
                "6277 60" -> "Post"
                "6281 57" -> "Tose_e"
                "5058 01" -> "Kosar"
                else -> "Unknown bank"
            }
            return out
        }

        fun getColor(bankName: String): String {
            return when(bankName) {
                "Pasargad" -> "#F0C239"
                "Mehr" -> "#35D930"
                "Saman" -> "#66D6FF"
                else -> "#FFFFFF"
            }
        }

        @SuppressLint("DiscouragedApi", "UseCompatLoadingForDrawables")
        fun getBankLogo(cardNumber: String, image: ImageView) {
            try {
                //loading in bank logo to identify bank
                val res = Statics.getApplicationContext().resources
                val directory = "ic_" + identifyBank(cardNumber).lowercase()
                println(directory)
                val resID = res.getIdentifier(directory, "drawable", Statics.getPackageName())
                image.setImageDrawable(res.getDrawable(resID))
                //if somehow  the bank logo was not available, a default icon will be used instead
            } catch (e: Exception) {
                e.printStackTrace()
                image.setImageResource(R.drawable.ic_default)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun checkExpired() {
        val currentYear = SimpleDateFormat("yyyy").format(Date())
        val currentMonth = SimpleDateFormat("MM").format(Date())
        if (expiryYear < currentYear.toShort() || (expiryYear == currentYear.toShort() &&
                    currentMonth.toByte() >= expiryMonth)) {
            isExpired = true
            return
        }
    }

    override fun toString(): String {
        return "DebitCard(title='$title', cardNumber='$cardNumber', shaba='$shaba'," +
                " expiryMonth=$expiryMonth, expiryYear=$expiryYear, isExpired=$isExpired)"
    }

}
