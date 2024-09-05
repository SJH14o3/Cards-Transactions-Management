package com.sjh14o3.transactionsManager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.sjh14o3.transactionsManager.data.DebitCard
import com.sjh14o3.transactionsManager.data.MyDate
import com.sjh14o3.transactionsManager.data.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar

//this activity will show a calendar and by default all of the transactions in this months ordered by newest
class CardOverviewActivity : AppCompatActivity() {
    companion object {
        //this values will be sent to adding or editing an transaction
        val ADD_TRANSACTION_REQUEST_CODE = 1
        val EDIT_TRANSACTION_REQUEST_CODE = 2

        val SIMPLE_TRANSACTION_EDIT_SUCCESS = 10 //edit only note or category of a transaction
        val SEMI_COMPLEX_TRANSACTION_EDIT_SUCCESS = 20 //edit change or remain of a transaction (basically a reset of remain)

        //this is used for converting start and end of selected custom interval range
        fun convertTimeToDate(time: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            return SimpleDateFormat("yyyyMMdd").format(calendar.time)
        }
    }

    private lateinit var calendar: CalendarView
    private lateinit var reportButton: Button
    private lateinit var addTransaction: Button
    private lateinit var selectInterval: Button
    private lateinit var selectMonth: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptor: TransactionsAdaptor
    private lateinit var noTransaction: TextView
    private lateinit var transactions: Array<Transaction>
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var remainText: TextView
    private var cardID: Int = -1
    private var selectedDate = ""
    private var currentStart: Long = -1
    private var currentEnd: Long = -1
    private var isFirstCalendarRefresh = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_overview)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cardID = intent.getSerializableExtra("CardID") as Int //it is important for transaction of what card to show up
        val cardNumber = intent.getSerializableExtra("CardNumber") as String //to set two fancy image on top, this is needed
        injectComponents(cardNumber)
        //by default, transactions of current IRL months will shown
        getCurrentMonthTransactions()
        refreshWholeData()
        setCalendarRange()
        setListeners(cardNumber)
        val formatted = SimpleDateFormat("yyyyMMdd")
        //selected date is used for "Select This Month" button, this is actually to prevent a crash,
        // since if this is empty and button is pressed app will crash
        selectedDate = formatted.format(calendar.date)
    }
    //refresh recycler view with the inserted range
    private fun setTransactionsRange(start: Long, end: Long) {
        currentStart = start
        currentEnd = end
        transactions = Statics.getTransactionDatabase().getCardAllTransactionsCustomRange(cardID ,start, end, 2)
        refreshWholeData()
    }

    //to be able to refresh items from adaptor, this public function is here
    fun refreshFromAdaptor() {
        transactions = Statics.getTransactionDatabase().getCardAllTransactionsCustomRange(cardID, currentStart, currentEnd, 2)
        refreshWholeData()
        setCalendarRange()
    }

    /*set minimum calendar range to first transaction time and max date to today
    * NOTE: I have tried using this method for some scenarios (like removing first transaction)
    *  to kinda dynamically refresh the min and max date but it there is a bug where it won't do it
    * dynamically, but it you refresh this activity is will work. I spent a long time trying to fix
    * the issue but I didn't find any fix. I gave up.*/
    private fun setCalendarRange() {
        if (isFirstCalendarRefresh) {
            isFirstCalendarRefresh = false
        } else Toast.makeText(this, "If calendar is bugged, reopen this page", Toast.LENGTH_LONG).show()
        //to set minimum date the calendar shows, time of the first transaction will be extracted
        val first = Statics.getTransactionDatabase().getFirstDateAndTime(cardID).toString()
        calendar.maxDate = System.currentTimeMillis() - 1000 //max date is today by default
        if (first == "0") { //if no transaction exist for that card, first is 0, so the only selectable date is today
            calendar.minDate = System.currentTimeMillis() - 2000
        } else { //if not, min date will be set
            val calendarInstance = Calendar.getInstance()
            //first (transaction time in database) is in this format: yyyyMMddHHmm, as a long, also months starts from 0 in android calendar
            calendarInstance.set(first.substring(0,4).toInt(), first.substring(4,6).toInt() - 1, first.substring(6,8).toInt())
            calendar.minDate = calendarInstance.timeInMillis
        }
        calendar.date = System.currentTimeMillis()
        calendar.invalidate()
    }

    //after adding or editing transaction is done, this function will run
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*if adding a new transaction was successful and there were no transaction after this
        * at first, this would have refresh only the new element for the recycler view but due to
        * complications [either new transaction was not in the selected range or it's right place was
        *  in the middle of the recycler view], it is the same*/
        if (requestCode == ADD_TRANSACTION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Displaying current month transactions", Toast.LENGTH_LONG).show()
            getCurrentMonthTransactions()
            refreshWholeData()
        //if adding a new transaction was successful but there transaction after this and user also commanded to change next transaction remain
        } else if (requestCode == ADD_TRANSACTION_REQUEST_CODE && resultCode == -10) {
            Toast.makeText(this, "Displaying current month transactions", Toast.LENGTH_LONG).show()
            getCurrentMonthTransactions()
            refreshWholeData()
        } else if (requestCode == EDIT_TRANSACTION_REQUEST_CODE && resultCode == SIMPLE_TRANSACTION_EDIT_SUCCESS) {
            adaptor.updateRow()
        } else if (requestCode == EDIT_TRANSACTION_REQUEST_CODE && resultCode == SEMI_COMPLEX_TRANSACTION_EDIT_SUCCESS) {
            Toast.makeText(this, "Displaying current month transactions", Toast.LENGTH_LONG).show()
            getCurrentMonthTransactions()
            refreshWholeData()
        }
        setCalendarRange()
    }

    private fun getCurrentMonthTransactions() {
        val today= MyDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, 0)
        currentStart = "${today.getYear()}${today.getMonthFormatted()}000000".toLong()
        currentEnd = "${today.getYear()}${today.getMonthFormatted()}990000".toLong()
        transactions = Statics.getTransactionDatabase().getCardAllTransactionsCustomRange(cardID,currentStart, currentEnd, 2)
    }

    private fun injectComponents(cardNumber: String) {
        calendar = findViewById(R.id.calendar)
        reportButton = findViewById(R.id.month_report)
        addTransaction = findViewById(R.id.add_transaction)
        selectInterval = findViewById(R.id.interval)
        selectMonth = findViewById(R.id.monthly_overall)
        recyclerView = findViewById(R.id.transactions_recycler_view)
        noTransaction = findViewById(R.id.no_transaction)
        image1 = findViewById(R.id.image1)
        image2 = findViewById(R.id.image2)
        remainText = findViewById(R.id.text_remain)
        DebitCard.getBankLogo(cardNumber, image1)
        DebitCard.getBankLogo(cardNumber, image2)
    }

    //all of the transactions will be received again, this is the unfortunate result of a change in vast amount of data.
    @SuppressLint("SetTextI18n")
    private fun refreshWholeData() {
        adaptor = TransactionsAdaptor(transactions, applicationContext, this, cardID)
        recyclerView.adapter = adaptor
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        if (transactions.size == 2) {
            noTransaction.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noTransaction.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        val remain = Statics.getTransactionDatabase().getLastRemain(cardID)
        remainText.text = "Remain: ${Transaction.getSeparatedDigits(remain)}T"
    }

    @SuppressLint("SimpleDateFormat")
    private fun setListeners(cardNumber: String) {
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = MyDate(year, month+1, dayOfMonth-1)
            val date2 = MyDate(year, month+1, dayOfMonth)
            val start = "${date.getYear()}${date.getMonthFormatted()}${date.getDayFormatted()}2500".toLong()
            val end = "${date.getYear()}${date.getMonthFormatted()}${date2.getDayFormatted()}2500".toLong()
            selectedDate = "${date.getYear()}${date.getMonthFormatted()}${date2.getDayFormatted()}"
            setTransactionsRange(start, end)
        }
        addTransaction.setOnClickListener {
            val intent = Intent(this, TransactionModifyActivity::class.java)
            intent.putExtra("CardID", cardID)
            intent.putExtra("EDIT", false)
            intent.putExtra("Date", selectedDate) //current date selected on calendar will be sent to automatically set date
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE)
        }
        selectMonth.setOnClickListener {
            val start = "${selectedDate.substring(0,6)}000000".toLong()
            val end = "${selectedDate.substring(0,6)}990000".toLong()
            setTransactionsRange(start, end)
        }
        selectInterval.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Range").setSelection(Pair(null, null)).build()
            picker.show(this.supportFragmentManager, "TAG")
            picker.addOnPositiveButtonClickListener {
                val start = "${convertTimeToDate(it.first)}0000".toLong()
                val end = "${convertTimeToDate(it.second)}2500".toLong()
                setTransactionsRange(start, end)
            }
            picker.addOnNegativeButtonClickListener {
                picker.dismiss()
            }
        }
        //bring up report activity
        reportButton.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("CardID", cardID)
            intent.putExtra("CardNumber", cardNumber)
            intent.putExtra("Start", currentStart)
            intent.putExtra("End", currentEnd)
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE)
        }
    }
}