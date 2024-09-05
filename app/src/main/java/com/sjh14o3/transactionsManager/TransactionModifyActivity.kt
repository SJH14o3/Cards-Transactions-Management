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
    private lateinit var inputToman: TextView
    private lateinit var remainToman: TextView

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
    private var lastRemainBefore = -1L
    private var lastTransactionTime = -1L
    private var notLastTransaction = false
    private var isEditMode = true
    private lateinit var transaction: Transaction
    private var simpleChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_modify)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cardID = intent.getSerializableExtra("CardID") as Int
        isEditMode = intent.getSerializableExtra("EDIT") as Boolean
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
        if (isEditMode) {
            transaction = intent.getSerializableExtra("transaction") as Transaction
            fillComponents(transaction)
        } else {
            val date = intent.getSerializableExtra("Date") as String
            if (Transaction.allowedForMoreOperations("${date}0000")) {
                pickedDate = MyDate(date.substring(0,4).toInt(), date.substring(4,6).toInt(), date.substring(6,8).toInt())
                showerDate.visibility = View.VISIBLE
                showerDate.text = "${date.substring(0,4)}-${date.substring(4,6)}-${date.substring(6,8)}"
            }
        }
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

        inputToman = findViewById(R.id.changeToman)
        remainToman = findViewById(R.id.remainToman)
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

    /*it won't be easy to just change the remain since it will makes some inaccuracy since it will reset remain
    * I can say this was not necessary but I did it anyway.*/
    private fun showCustomRemainWarning() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning").setMessage("Custom Remain may result in inaccuracy.\n" +
                    "Next transactions remain will be calculated from this transaction remain (meaning it would reset remain kinda like this is the first transaction)\n" +
                    "If you are adding multiple transactions at once, we suggest adding transactions in order to prevent some complications")
            .setNegativeButton("Cancel") { dialog, _ ->
                remainCheck.isChecked = false
            }.setPositiveButton("Acknowledge") { dialog, _ ->
                remainWarningBroughtUp = true
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
        //user can't set a remain where income change is more than remain!
        if (remainCheck.isChecked && radioButtonIncome.isChecked &&
            inputRemain.text.toString().toLong() < inputChange.text.toString().toLong()) {
            if (!out) sb.append("\n")
            out = false
            sb.append("-Inserted income is more than inserted remain? NONSENSE!")
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
        if (!notLastTransaction) {
            inputRemain.setText(newRemain.toString())
        } else {
            checkIfLastTransaction()
        }
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
    * allowed to insert the transaction. I did test some scenarios but I can't be sure if this still works properly\
    * UPDATE: I made a change where remain will automatically updates to the last remain, this might help user better*/
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
            notLastTransaction = true
            lastRemainBefore = Statics.getTransactionDatabase().getLastRemainBeforeTime(cardID, current)
            setRemainForNotLastTransaction()

        }
        else {
            warningCustomRemain.visibility = View.GONE
            notLastTransaction = false
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
            if (!isEditMode) {
                if (notLastTransaction) {
                    setResult(-10)
                } else {
                    setResult(Activity.RESULT_OK)
                }
            } else {
                if (simpleChange) {
                    setResult(CardOverviewActivity.SIMPLE_TRANSACTION_EDIT_SUCCESS)
                } else {
                    setResult(CardOverviewActivity.SEMI_COMPLEX_TRANSACTION_EDIT_SUCCESS)
                }
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
                    if (notLastTransaction) {
                        inputRemain.setText((lastRemainBefore + inputChange.text.toString().toLong()).toString())
                    } else {
                        val newRemain = lastRemain + inputChange.text.toString().toLong()
                        inputRemain.setText(newRemain.toString())
                    }
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
                    if (notLastTransaction) {
                        inputRemain.setText((lastRemainBefore - inputChange.text.toString().toLong()).toString())
                    } else {
                        val newRemain = lastRemain - inputChange.text.toString().toLong()
                        inputRemain.setText(newRemain.toString())
                    }
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
                if (!remainWarningBroughtUp) {
                    showCustomRemainWarning()
                }
                remainLayout.setBackgroundDrawable(null)
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
                giveTomanDescription(inputChange.text.toString(), inputToman)
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
                giveTomanDescription(inputRemain.text.toString(), remainToman)
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
                if (!isEditMode) {
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
                else {
                    val new = createTransaction()
                    val editChange = (transaction.getChange() != new.getChange())
                    val editTime = (transaction.getDateAndTimeAsLong() != new.getDateAndTimeAsLong())
                    val editCategory = (transaction.getType() != new.getType())
                    val editNote = (transaction.getNote() != new.getNote())
                    val editRemain = (transaction.getRemain() != new.getRemain())
                    //checking if there was any change
                    if (!editChange && !editTime && !editCategory && !editNote && !editRemain) {
                        AlertDialog.Builder(this).setTitle("Failed").setMessage("No Change was detected")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }.create().show()
                    }
                    //checking if simple things where changed
                    else if ((editCategory || editNote) && !editChange && !editRemain && !editTime) {
                        if (Statics.getTransactionDatabase().updateRowSimple(transaction.getId(), editCategory, editNote, new))  {
                            simpleChange = true
                            showSuccessDialog("edited")
                        } else Toast.makeText(this, "Failed, try again later", Toast.LENGTH_LONG).show()
                    }

                    else if ((editChange || editRemain) && !editTime) {
                        checkIfLastTransaction()
                        if (current < lastTransactionTime) {
                            AlertDialog.Builder(this)
                                .setTitle("Update remain for next transactions?")
                                .setMessage("Since this is not the last transaction, do you want to update remain on next transactions?")
                                .setPositiveButton("Yes") { dialog, _ ->
                                    if (Statics.getTransactionDatabase().resetNextRowsRemain(transaction.getDateAndTimeAsLong(), new.getRemain(), transaction.getBankId())) {
                                        Statics.getTransactionDatabase().updateRowComplex(transaction.getId(), editCategory, editNote, editChange, editRemain, false, new)
                                        simpleChange = false
                                        showSuccessDialog("edited")
                                    } else {
                                        simpleChange = true
                                        AlertDialog.Builder(this).setMessage("Failed").
                                        setMessage("During updating rows, some of the next transactions became negative!" +
                                                " try editing again but don't update the next rows and fix the remains manually.")
                                            .setPositiveButton("OK") { _, _ -> }.create().show()
                                    }
                                }.setNegativeButton("No") { dialog, _ ->
                                    simpleChange = true
                                    Statics.getTransactionDatabase().updateRowComplex(transaction.getId(), editCategory, editNote, editChange, editRemain, false, new)
                                    showSuccessDialog("Edited")
                                }.setCancelable(false).create().show()
                        } else {
                            Statics.getTransactionDatabase().updateRowComplex(transaction.getId(), editCategory, editNote, editChange, editRemain, false, new)
                        }
                    } else {
                        //case where newest card time is edited and still is above other cards
                        if (!notLastTransaction && (current > Statics.getTransactionDatabase().getSecondLastDateAndTime(cardID))) {
                            if (Statics.getTransactionDatabase().updateRowComplex(transaction.getId(), editCategory, editNote, editChange, editRemain, true, new)) {
                                showSuccessDialog("Success")
                                simpleChange = true
                            } else Toast.makeText(this, "Failed, Try again later", Toast.LENGTH_LONG).show()
                        } else {
                            AlertDialog.Builder(this)
                                .setTitle("NOTICE")
                                .setMessage("Since you are changing time of transaction, it is not possible to" +
                                        " automatically update remain, either continue without updating, or" +
                                        "delete this transaction and insert the correct time.")
                                .setPositiveButton("Edit without update") { dialog, _ ->
                                    simpleChange = true
                                    Statics.getTransactionDatabase().updateRowComplex(transaction.getId(), editCategory, editNote, editChange, editRemain, false, new)
                                    showSuccessDialog("Edited")
                                }.setNegativeButton("Cancel") { dialog, _ ->
                                    dialog.dismiss()
                                }.create().show()
                        }
                    }
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
        if (Statics.getTransactionDatabase().hasDateConflict(transaction.getDateAndTimeAsLong(), cardID)) {
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
        if (Statics.getTransactionDatabase().hasDateConflict(transaction.getDateAndTimeAsLong(), cardID)) {
            showConflictingDialog()
        } else {
            if (Statics.getTransactionDatabase().resetNextRowsRemain(transaction.getDateAndTimeAsLong(),
                    transaction.getRemain(), transaction.getBankId())) {
                addTransaction()
            } else {
                AlertDialog.Builder(this).setMessage("Failed").
                setMessage("With the custom remain, some of the next transactions became negative!\ncannot do your transaction")
                    .setPositiveButton("OK") { _, _ -> }.create().show()
            }
        }
    }

    private fun setRemainForNotLastTransaction() {
        if (inputChange.text.isNotEmpty()) {
            val remain = if (radioButtonIncome.isChecked) {
                lastRemainBefore + inputChange.text.toString().toLong()
            } else {
                lastRemainBefore - inputChange.text.toString().toLong()
            }
            inputRemain.setText(remain.toString())
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

    private fun giveTomanDescription(input: String, view: TextView) {
        if (input.isEmpty()) {
            view.visibility = View.GONE
            view.text = ""
        } else {
            view.visibility = View.VISIBLE
            view.text = "${Transaction.getSeparatedDigits(input.toLong())}T"
        }
    }
    //during edit mode, all of the input components will automatically filled with the transaction to be edited
    private fun fillComponents(transaction: Transaction) {
        val change = transaction.getChange()
        if (change > 0) {
            inputChange.setText(change.toString())
            radioButtonIncome.isChecked = true
        } else {
            inputChange.setText((change * -1).toString())
            category = transaction.getType()
            Transaction.setIconType(transaction.getType(), categoryIcon)
        }
        val time = transaction.getDateAndTimeAsLong().toString()
        pickedDate = MyDate(time.substring(0,4).toInt(), time.substring(4,6).toInt(), time.substring(6,8).toInt())
        pickedTime = MyTime(time.substring(8,10).toInt(), time.substring(10).toInt())
        showerDate.visibility = View.VISIBLE
        showerDate.text = "${time.substring(0,4)}-${time.substring(4,6)}-${time.substring(6,8)}"
        showerTime.visibility = View.VISIBLE
        showerTime.text = "${time.substring(8,10)}:${time.substring(10,12)}"
        inputNotes.setText(transaction.getNote())
        inputRemain.setText(transaction.getRemain().toString())
        checkIfLastTransaction()
        if (!notLastTransaction) {
            lastRemain = transaction.getRemain() - transaction.getChange()
        }
    }
}