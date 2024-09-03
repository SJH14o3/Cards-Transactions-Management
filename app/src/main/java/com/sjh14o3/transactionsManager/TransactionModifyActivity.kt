@file:SuppressLint("SetTextI18n")
package com.sjh14o3.transactionsManager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sjh14o3.transactionsManager.data.MyDate
import com.sjh14o3.transactionsManager.data.MyTime
import com.sjh14o3.transactionsManager.data.Transaction

//the activity for adding or modifying a transaction
class TransactionModifyActivity : AppCompatActivity() {
    private lateinit var activityName: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButtonIncome: RadioButton
    private lateinit var radioButtonSpend: RadioButton
    private lateinit var showerDate: TextView
    private lateinit var showerTime: TextView
    private lateinit var categoryIcon: ImageView
    private lateinit var remainCheck: CheckBox
    private lateinit var remainLayout: RelativeLayout

    private lateinit var errorChange: TextView
    private lateinit var errorDate: TextView
    private lateinit var errorTime: TextView
    private lateinit var errorCategory: TextView
    private lateinit var errorRemain: TextView
    private lateinit var warningCustomRemain: TextView

    private lateinit var inputChange: EditText
    private lateinit var inputNotes: EditText
    private lateinit var inputRemain: EditText

    private lateinit var buttonDatePicker: Button
    private lateinit var buttonTimePicker: Button
    private lateinit var buttonCategoryPicker: Button
    private lateinit var buttonCancel: Button
    private lateinit var buttonConfirm: Button

    private var pickedDate: MyDate? = null
    private var pickedTime: MyTime? = null
    private var category: Byte = -1
    private var remainWarningBroughtUp = false
    private var cardID: Int = -1
    private var lastRemain = -1L
    private var lastTransactionTime = -1L
    private var mandatoryRemain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_modify)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cardID = intent.getSerializableExtra("CardID") as Int
        injectComponents()
        //to automatically update remain, last remain will be received
        lastRemain = Statics.getTransactionDatabase().getLastRemain(cardID)
        //to figure if this transaction is newest or not, newest transaction time will be extracted
        lastTransactionTime = Statics.getTransactionDatabase().getLastDateAndTime(cardID)
        inputRemain.isEnabled = false //by default, inputting remain is disabled
        setListeners()
        //if back or cancel is pressed, a confirmation dialog will be shown
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
    }
    //to prevent accidental back pressing or canceling, this dialog will be shown
    private fun backPressed() {
        AlertDialog.Builder(this).setTitle("Discard Operation?").
        setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }.setPositiveButton("Discard") { dialog, _ ->
            dialog.dismiss()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }.create().show()
    }

    private fun injectComponents() {
        errorChange = findViewById(R.id.alert_change)
        errorDate = findViewById(R.id.alert_date)
        errorTime = findViewById(R.id.alert_time)
        errorCategory = findViewById(R.id.alert_category)
        errorRemain = findViewById(R.id.alert_remain)
        warningCustomRemain = findViewById(R.id.alert_custom_remain)

        inputChange = findViewById(R.id.input_change)
        inputNotes = findViewById(R.id.input_note)
        inputRemain = findViewById(R.id.input_remain)

        buttonDatePicker = findViewById(R.id.pick_date)
        buttonTimePicker = findViewById(R.id.pick_time)
        buttonCategoryPicker = findViewById(R.id.pick_category)
        buttonCancel = findViewById(R.id.cancel)
        buttonConfirm = findViewById(R.id.confirm)

        activityName = findViewById(R.id.textView)
        radioGroup = findViewById(R.id.radioGroup)
        showerDate = findViewById(R.id.text_date_shower)
        showerTime = findViewById(R.id.text_time_shower)
        categoryIcon = findViewById(R.id.category_icon)
        remainCheck = findViewById(R.id.remain_checkbox)
        remainLayout = findViewById(R.id.remain_panel)
        radioButtonIncome = findViewById(R.id.radio_gain)
        radioButtonSpend = findViewById(R.id.radio_lost)
    }

    //after category was selected, image view will show that category
    private fun changeCategoryIcon(categoryStr: String) {
        val res = Statics.getApplicationContext().resources
        val iconName = when(categoryStr) {
            "Cafe/Restaurant" -> "cafe_or_restaurant"
            else -> categoryStr.lowercase()
        }
        val directory = "ic_tr_$iconName"
        category = Transaction.iconNameToType(iconName)
        val resID = res.getIdentifier(directory, "drawable", Statics.getPackageName())
        categoryIcon.setImageDrawable(res.getDrawable(resID))
    }

    /*it won't be easy to just change the remain since it will makes some inaccuracy.
    * I can say this was not necessary but I did it anyway.*/
    private fun showCustomRemainWarning() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning").setMessage("Custom Remain may result in inaccuracy.\n\n" +
                    "-If you are adding multiple transactions at once, we suggest adding transactions in order.\n" +
                    "-if you made a custom remain, the next transactions will calculate remain from this transaction.")
            .setNegativeButton("Cancel") { dialog, _ ->
                remainCheck.isChecked = false
            }.setPositiveButton("Acknowledge") { dialog, _ ->
                remainWarningBroughtUp = true
                inputRemain.setText("")
            }.setOnDismissListener {
                if (!remainWarningBroughtUp) {
                    remainCheck.isChecked = false
                }
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //function to validate if inserted data makes sense
    private fun validateData(): Boolean {
        makeErrorMessagesGone()
        val sb = StringBuilder()
        var out = true
        //change was not inserted or 0 was inserted
        if (inputChange.text.isEmpty() || inputChange.text.toString().toLong() == 0L) {
            out = false
            sb.append("-insert change")
            errorChange.visibility = View.VISIBLE
        }
        //date was not picked
        if (pickedDate == null) {
            if (!out) sb.append("\n")
            out = false
            sb.append("-pick a date")
            errorDate.visibility = View.VISIBLE
        }
        //time was not picked
        if (pickedTime == null) {
            if (!out) sb.append("\n")
            out = false
            sb.append("-pick a time")
            errorTime.visibility = View.VISIBLE
        }
        //category was not selected
        if (category == (-1).toByte()) {
            if (!out) sb.append("\n")
            out = false
            sb.append("-pick a category")
            errorCategory.visibility = View.VISIBLE
        }
        //if manual remain mode is enabled, then remain should not be empty, 0 is allowed.
        if (remainCheck.isChecked && inputRemain.text.toString().isEmpty()) {
            if (!out) sb.append("\n")
            out = false
            sb.append("-insert remain")
            errorRemain.visibility = View.VISIBLE
        }
        //if change will make remain negative, it will be prevented
        if (inputRemain.text.isNotEmpty() && inputRemain.text.toString().toLong() < 0) {
            if (!out) sb.append("\n")
            out = false
            sb.append("-remain cannot be negative, check Custom Remain if you believe automatic remain calculation is wrong.")
        }
        if (!out) {
            showValidationFailedDialog(sb)
        }
        return out
    }

    //when user is typing change, remain will change dynamically
    private fun autoSetRemain() {
        var newRemain = 0L
        if (inputChange.text.toString().isEmpty()) {
            inputRemain.setText("")
            return
        }
        if (radioButtonSpend.isChecked) {
            newRemain = lastRemain - inputChange.text.toString().toLong()
        } else newRemain = lastRemain + inputChange.text.toString().toLong()
        inputRemain.setText(newRemain.toString())
    }

    //picking time dialog
    private fun showTimeDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                pickedTime = MyTime(selectedHour, selectedMinute)
                showerTime.text = "${pickedTime!!.getHourFormatted()}:${pickedTime!!.getMinuteFormatted()}"
                showerTime.visibility = View.VISIBLE
                if (errorTime.visibility == View.VISIBLE) {
                    errorTime.visibility = View.GONE
                }
                checkIfLastTransaction()
            },0,
            0,
            true
        )
        timePickerDialog.show()
    }

    /*this is an important function, if the current inserted time is not after the newest saved transaction,
    * inputting remain will become mandatory, and user will also be asked to change the remain of the next transactions
    * most important thing is remain cannot be negative so if a transaction remain become negative, user won't be
    * allowed to insert the transaction. I did test some scenarios but I can't be sure if this still works properly*/
    private fun checkIfLastTransaction() {
        if (pickedDate == null) {
            errorDate.visibility = View.VISIBLE
            return
        }
        if (pickedTime == null) {
            errorTime.visibility = View.VISIBLE
            return
        }
        val current = ("${pickedDate!!.getYear()}${pickedDate!!.getMonthFormatted()}${pickedDate!!.getDayFormatted()}" +
                "${pickedTime!!.getHourFormatted()}${pickedTime!!.getMinuteFormatted()}").toLong()
        if (current < lastTransactionTime) {
            warningCustomRemain.visibility = View.VISIBLE
            mandatoryRemain = true
            remainCheck.isChecked = true
            inputRemain.setText("")
            remainCheck.isEnabled = false
        }
        else {
            warningCustomRemain.visibility = View.GONE
            mandatoryRemain = false
            remainCheck.isChecked = false
            remainCheck.isEnabled = true
        }
    }

    //date picker dialog
    private fun showDateDialog() {
        val date = Statics.getDate()
        val year = date.getYear()
        val month = date.getMonth()
        val day = date.getDay()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                pickedDate = MyDate(selectedYear, selectedMonth+1, selectedDay)
                showerDate.text = "$selectedDay/${selectedMonth+ 1}/$selectedYear"
                showerDate.visibility = View.VISIBLE
                if (pickedTime == null) {
                    showTimeDialog()
                } else checkIfLastTransaction()
                errorDate.visibility = View.GONE
            },
            year,
            month-1,
            day
        )
        datePickerDialog.show()
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 7_776_000_000
    }

    //if inserted data were not valid, an alert dialog with errors will be shown
    private fun showValidationFailedDialog(sb: StringBuilder) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Validation Failed").setMessage(sb.toString())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //basically will remove all error text views
    private fun makeErrorMessagesGone() {
        errorChange.visibility = View.GONE
        errorRemain.visibility = View.GONE
        errorCategory.visibility = View.GONE
        errorDate.visibility = View.GONE
        errorTime.visibility = View.GONE
    }

    //when transaction was successful, this dialog will be shown an activity will be closed
    private fun showSuccessDialog(mode: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Success").setMessage("Transaction was $mode successfully").setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }.setOnDismissListener {
            if (mandatoryRemain) {
                setResult(-10)
            } else {
                setResult(Activity.RESULT_OK)
            }
            finish()
        }.create().show()
    }

    private fun setListeners() {
        radioButtonIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputChange.setTextColor(resources.getColor(R.color.income_text))
                buttonCategoryPicker.isEnabled = false
                buttonCategoryPicker.setBackgroundColor(ContextCompat.getColor(this, R.color.disabled_background))
                categoryIcon.setImageResource(R.drawable.ic_tr_income)
                category = 0
                //function Auto Set Remain was not changing remain properly during this event so I had to do it manually
                if (!remainCheck.isChecked && inputChange.text.isNotEmpty()) {
                    val newRemain = lastRemain + inputChange.text.toString().toLong()
                    inputRemain.setText(newRemain.toString())
                }
            }
        }
        radioButtonSpend.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputChange.setTextColor(resources.getColor(R.color.spent_text))
                buttonCategoryPicker.isEnabled = true
                categoryIcon.setImageResource(R.drawable.ic_question_mark)
                buttonCategoryPicker.setBackgroundColor(ContextCompat.getColor(this, R.color.lavender))
                category = -1
                if (!remainCheck.isChecked && inputChange.text.isNotEmpty()) {
                    val newRemain = lastRemain - inputChange.text.toString().toLong()
                    inputRemain.setText(newRemain.toString())
                }
            }
        }

        buttonDatePicker.setOnClickListener {
            showDateDialog()
        }

        buttonTimePicker.setOnClickListener {
            showTimeDialog()
        }

        buttonCategoryPicker.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonCategoryPicker)
            popupMenu.menuInflater.inflate(R.menu.transactions_types, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                changeCategoryIcon(menuItem.title.toString())
                true
            }
            popupMenu.show()
        }

        remainCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!remainWarningBroughtUp && !mandatoryRemain) {
                    showCustomRemainWarning()
                }
                remainLayout.setBackgroundDrawable(null)
                inputRemain.setText("")
                inputRemain.isEnabled = true
            } else {
                remainLayout.setBackgroundColor(resources.getColor(R.color.disabled_background))
                inputRemain.isEnabled = false
                autoSetRemain()
            }
        }

        inputChange.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun afterTextChanged(p0: Editable?) {
                if (errorChange.visibility == View.VISIBLE) {
                    errorChange.visibility = View.GONE
                }
                if (!remainCheck.isChecked) {
                    autoSetRemain()
                }
            }
        })

        inputRemain.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (errorRemain.visibility == View.VISIBLE) {
                    errorRemain.visibility = View.GONE
                }
            }

        })

        buttonCancel.setOnClickListener {
            backPressed()
        }

        buttonConfirm.setOnClickListener {
            val result = validateData()
            if (result) {
                val current = ("${pickedDate!!.getYear()}${pickedDate!!.getMonthFormatted()}${pickedDate!!.getDayFormatted()}" +
                        "${pickedTime!!.getHourFormatted()}${pickedTime!!.getMinuteFormatted()}").toLong()
                if (current < lastTransactionTime) { //this is not the last transaction
                    AlertDialog.Builder(this).setTitle("Update remain for next transactions?")
                        .setMessage("Since this is not the last transaction, do you want to update remain on next transactions?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            addAndUpdateTransactions()
                        }.setNegativeButton("No") { dialog, _ ->
                            addTransaction()
                        }.setCancelable(false).create().show()
                } else {
                    addTransaction()
                }
            }
        }
    }

    //it is not possible to have two transactions with same exact time.
    private fun showConflictingDialog() {
        AlertDialog.Builder(this).setTitle("Failed")
            .setMessage("Cannot have two transaction with same time, try increasing minute by 1")
            .setPositiveButton("OK") { _, _ -> }
            .create().show()
    }

    //transaction will be added
    private fun addTransaction() {
        val transaction = createTransaction()
        if (Statics.getTransactionDatabase().hasDateConflict(transaction.getDateAndTimeAsLong())) {
            showConflictingDialog()
        } else {
            if (Statics.getTransactionDatabase().addTransaction(transaction)) {
                showSuccessDialog("added")
            } else Toast.makeText(this, "Failed, try again later", Toast.LENGTH_LONG).show()
        }
    }

    //if the next transactions remain need to be updated, this function will operate
    private fun addAndUpdateTransactions() {
        val transaction = createTransaction()
        if (Statics.getTransactionDatabase().hasDateConflict(transaction.getDateAndTimeAsLong())) {
            showConflictingDialog()
        } else {
            if (Statics.getTransactionDatabase().updateNextRowsRemain(transaction.getDateAndTimeAsLong(),
                    transaction.getRemain(), transaction.getBankId(), this)) {
                addTransaction()
            }
        }
    }

    //create transaction with the all of the given input
    private fun createTransaction(): Transaction {
        val change = if (category != 0.toByte()) {
            "-${inputChange.text}"
        } else inputChange.text.toString()
        return Transaction("${pickedDate!!.getYear()}-${pickedDate!!.getMonthFormatted()}-${pickedDate!!.getDayFormatted()}",
            "${pickedTime!!.getHourFormatted()}:${pickedTime!!.getMinuteFormatted()}",
            change.toLong(), inputRemain.text.toString().toLong(),inputNotes.text.toString(), category, cardID)
    }
    //TODO:warn user if there is a transaction after new one and auto change of remain for this scenario and editing scenario
}