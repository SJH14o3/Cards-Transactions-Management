package com.sjh14o3.transactionsManager

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.sjh14o3.transactionsManager.data.DebitCard
import com.sjh14o3.transactionsManager.data.Transaction

//activity to show report
class ReportActivity : AppCompatActivity() {
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var range: TextView
    private lateinit var income: TextView
    private lateinit var spent: TextView
    private lateinit var rangePicker: Button
    private lateinit var rangeHeadsUp: TextView
    private lateinit var listView: ListView

    private lateinit var transactions: Array<Transaction>
    private var cardId: Int = 0 //if cardID is 0, it will show transactions from all cards.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        injectComponents()
        cardId = intent.getSerializableExtra("CardID") as Int
        if (cardId != 0) { //if cardID is 0, it means all the cards
            val cardNumber = intent.getSerializableExtra("CardNumber") as String
            val start = intent.getSerializableExtra("Start") as Long
            val end = intent.getSerializableExtra("End") as Long
            DebitCard.getBankLogo(cardNumber, image1)
            DebitCard.getBankLogo(cardNumber, image2)
            if (end != -1L) {
                fetchInformation(start, end)
            }
        }
        rangePicker.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Range").setSelection(Pair(null, null)).build()
            picker.show(this.supportFragmentManager, "TAG")
            picker.addOnPositiveButtonClickListener {
                val start = "${CardOverviewActivity.convertTimeToDate(it.first)}0000".toLong()
                val end = "${CardOverviewActivity.convertTimeToDate(it.second)}2500".toLong()
                fetchInformation(start, end)
            }
            picker.addOnNegativeButtonClickListener {
                picker.dismiss()
            }
        }
    }

    //after range was selected, this function will do all the calculations to show information
    @SuppressLint("SetTextI18n")
    private fun fetchInformation(start: Long, end: Long) {
        range.text = "${getFormattedDate(start.toString())} to ${getFormattedDate(end.toString())}"
        transactions = if (cardId == 0) {
            Statics.getTransactionDatabase().getAllCardsTransactionsLowDetails(start, end, 0)
        } else {
            Statics.getTransactionDatabase().getCardAllTransactionsLowDetail(cardId, start, end, 0)
        }
        //if there isn't any transaction, this part will run
        if (transactions.isEmpty()) {
            this.income.text = "+0.0T"
            this.spent.text = "-0.0T"
            listView.visibility = View.VISIBLE
            rangeHeadsUp.visibility = View.GONE
            return
        }
        println("MYLOG: length = {${transactions.size}}")
        var income = 0L
        var spent = 0L
        val splitCategories = LongArray(Transaction.CATEGORIES_COUNT + 1) { 0 }
        for (transaction in transactions) {
            val change = transaction.getChange()
            if (change > 0) { //income transaction
                income += change
            } else { //spent transaction
                spent += change
                if (transaction.getType() == 127.toByte()) { //127 is other category
                    splitCategories[splitCategories.size-1] -= change
                } else {
                    splitCategories[transaction.getType().toInt()] -= change
                }
            }
        }
        this.income.text = "+${Transaction.getSeparatedDigits(income)}T"
        this.spent.text = "${Transaction.getSeparatedDigits(spent)}T"
        val arrayList = ArrayList<String>()
        for (i in splitCategories.indices) {
            if (splitCategories[i] == 0L) { //if there was no spent on a category, that won't show up
                continue
            }
            val percent = ((splitCategories[i].toDouble() / spent.toDouble()) * - 100)
            val str = "${Transaction.getTypeName(i.toByte())}: ${Transaction.getSeparatedDigits(splitCategories[i])}T , ${"%.2f".format(percent)}%"
            arrayList.add(str)
        }
        //sorting the result by
        val sortedList = arrayList.sortedBy {
            // Extract the percentage value using regex and convert it to Double
            val percentString = it.substringAfter(", ").substringBefore("%")
            percentString.toDouble()
        }
        //getting all the categories
        val adaptor = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, sortedList.toTypedArray().reversedArray())
        listView.adapter = adaptor
        listView.visibility = View.VISIBLE
        rangeHeadsUp.visibility = View.GONE
    }

    private fun getFormattedDate(input: String): String {
        return "${input.substring(0,4)}/${input.substring(4,6)}/${input.substring(6,8)}"
    }

    private fun injectComponents() {
        image1 = findViewById(R.id.image1)
        image2 = findViewById(R.id.image2)
        range = findViewById(R.id.range)
        income = findViewById(R.id.text_income)
        spent = findViewById(R.id.text_spent)
        rangePicker = findViewById(R.id.select_interval)
        rangeHeadsUp = findViewById(R.id.select_range_heads_up)
        listView = findViewById(R.id.list_view)
    }
}