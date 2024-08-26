package com.sjh14o3.transactionsManager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sjh14o3.transactionsManager.data.DebitCard

class CardAddOrEditActivity : AppCompatActivity() {
    private lateinit var textTitle: TextView
    private lateinit var textCardNumber: TextView
    private lateinit var textShabaNumber: TextView
    private lateinit var textExpiry: TextView
    private lateinit var textOwnerName: TextView
    private lateinit var activityTitle: TextView

    private lateinit var inputTitle: EditText
    private lateinit var inputCardNumber1: EditText
    private lateinit var inputCardNumber2: EditText
    private lateinit var inputCardNumber3: EditText
    private lateinit var inputCardNumber4: EditText
    private lateinit var inputShabaNumber: EditText
    private lateinit var inputMonth: EditText
    private lateinit var inputYear: EditText
    private lateinit var inputOwnerName: EditText

    private lateinit var logo: ImageView
    private lateinit var buttonConfirm: Button
    private lateinit var buttonCancel: Button
    private var card: DebitCard? = null
    private var editMode = false
    private lateinit var mainView: ConstraintLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
        //This function will initialize every component that are marked with late initialize
        injectComponents()
        try {
            //if through editing this activity is launched, all the EditTexts will have their content as the card
            //but if activity is launched through adding, an exception will happen and EditTexts won't have any value
            card = intent.getSerializableExtra("Card") as DebitCard
            editMode = true
            fillComponents(card!!)
            inputYear.clearFocus()
            DebitCard.getBankLogo("${inputCardNumber1.text} ${inputCardNumber2.text}", logo)
            activityTitle.text = "Edit Card"
        } catch (e: Exception) {
            //add mode
        }
    }
    //as I said, this function will inject components
    private fun injectComponents() {
        textTitle = findViewById(R.id.card_title_text)
        textCardNumber = findViewById(R.id.card_number_text)
        textShabaNumber = findViewById(R.id.card_shaba_text)
        textExpiry = findViewById(R.id.card_expiry_text)
        textOwnerName = findViewById(R.id.card_owner_text)
        activityTitle = findViewById(R.id.textView)

        inputTitle = findViewById(R.id.card_title_input)
        inputCardNumber1 = findViewById(R.id.card_number_input1)
        inputCardNumber2 = findViewById(R.id.card_number_input2)
        inputCardNumber3 = findViewById(R.id.card_number_input3)
        inputCardNumber4 = findViewById(R.id.card_number_input4)
        inputShabaNumber = findViewById(R.id.card_shaba_input)
        inputMonth = findViewById(R.id.card_expiry_input_month)
        inputYear = findViewById(R.id.card_expiry_input_year)
        inputOwnerName = findViewById(R.id.card_owner_input)

        logo = findViewById(R.id.bank_logo)
        buttonConfirm = findViewById(R.id.addCard_confirm)
        buttonCancel = findViewById(R.id.addCard_cancel)

        mainView = findViewById(R.id.main)

        buttonConfirm.setOnClickListener {
            confirm()
        }
        buttonCancel.setOnClickListener {
            cancel()
        }
        addTextListeners()
    }
    //when confirm button is clicked, first all TextViews will go back to normal (if user failed to validate data before)
    //then data will be processed to confirm is validation, if validation failed, some warning and a dialog will shown
    //if it was successful, depending on adding card or editing card, database will be affected
    private fun confirm() {
        resetTexts()
        if (!validateInput()) return
        //action is ready to be done, first we create the class
        val newCard = DebitCard(inputTitle.text.toString(), "${inputCardNumber1.text} ${inputCardNumber2.text}" +
                " ${inputCardNumber3.text} ${inputCardNumber4.text}", inputShabaNumber.text.toString(),
            inputMonth.text.toString().toByte(), inputYear.text.toString().toShort(), inputOwnerName.text.toString())
        //separating editing and adding
        if (!editMode) {
            //if card is added to database successfully, a dialog will be shown
            if (Statics.getCardDatabase().addCard(newCard)) {
                showSuccessDialog("added")
            } else { //if it didn't success, a toast message will show up
                Toast.makeText(this, "Failed, Try Again Later", Toast.LENGTH_LONG).show()
            }
        }
        else {
            //we check first if any values were changed. if not a warning dialog will be shown
            if (card!! == newCard) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Warning").setMessage("No changes were made").setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            else { //card will be updated
                Statics.getCardDatabase().editCard(card!!, newCard)
                showSuccessDialog("edited")

            }
        }
    }
    //cancel button and back button act the same
    private fun cancel() {
        backPressed()
    }
    //this function will check if our data is valid
    //I used the Text View for determining location of each card input as the error message too
    private fun validateInput(): Boolean {
        //all of the errors will be shown in a dialog, to save the errors, we will need this string builder
        val sb = StringBuilder()
        //out is the output of this function
        var out = true
        //since year and month use the same Text View and Year is processed first, this boolean will help
        // to concat year and month errors if that happen
        var correctYear = true
        //title is a simple process, just checking if its not empty
        if (inputTitle.text.isEmpty()) {
            sb.append("Enter Title") //this message will be shown in the dialog
            textTitle.setText(R.string.error_title) //an error will be shown in the Text View of Corresponding Component
            textTitle.setTextColor(resources.getColor(R.color.error)) //changing color to add more effect
            out = false //validation of data has failed
        }

        //each cell of card number is limited to 4 characters
        //program check if all cells are filled with 4 digits
        //even though it might not be possible to input other than digits, program will still check it
        if (inputCardNumber1.text.isEmpty() && inputCardNumber2.text.isEmpty() &&
            inputCardNumber3.text.isEmpty() && inputCardNumber4.text.isEmpty()) {
            //if there was an error already, a new line must be added to the string builder
            if (!out) sb.append("\n")
            sb.append("Enter Card Number")
            textCardNumber.setText(R.string.error_card_empty)
            textCardNumber.setTextColor(resources.getColor(R.color.error))
            out = false
        } else if (inputCardNumber1.length() != 4 || inputCardNumber2.length() != 4 &&
            inputCardNumber3.length() != 4 && inputCardNumber4.length() != 4) {
            if (!out) sb.append("\n")
            sb.append("Fill all card number cells")
            textCardNumber.setText(R.string.error_card_not_filled)
            textCardNumber.setTextColor(resources.getColor(R.color.error))
            out = false
        } else if (!validateDigits(inputCardNumber1.text.toString(), 4) ||
            !validateDigits(inputCardNumber2.text.toString(), 4) ||
            !validateDigits(inputCardNumber3.text.toString(), 4) ||
            !validateDigits(inputCardNumber4.text.toString(), 4)) {
                if (!out) sb.append("\n")
                sb.append("Enter only digits for card number")
                textCardNumber.setText(R.string.error_mismatch_number)
                textCardNumber.setTextColor(resources.getColor(R.color.error))
                out = false
        }
        //shaba is like card number but there is only one cell and 24 digits
        if (inputShabaNumber.text.isEmpty()) {
            if (!out) sb.append("\n")
            sb.append("Enter Shaba")
            textShabaNumber.setText(R.string.error_shaba_empty)
            textShabaNumber.setTextColor(resources.getColor(R.color.error))
            out = false
        } else if(inputShabaNumber.length() != 24) {
            if (!out) sb.append("\n")
            sb.append("Insert 24 digits for shaba")
            textShabaNumber.setText(R.string.error_shaba_not_filled)
            textShabaNumber.setTextColor(resources.getColor(R.color.error))
            out = false

        } else if(!validateDigits(inputShabaNumber.text.toString(), 24)) {
            if (!out) sb.append("\n")
            sb.append("Enter only digits for Shaba Number")
            textShabaNumber.setText(R.string.error_mismatch_number)
            textShabaNumber.setTextColor(resources.getColor(R.color.error))
            out = false
        }
        //year must be four digits
        if (inputYear.text.isEmpty()) {
            if (!out) sb.append("\n")
            sb.append("Enter Year")
            textExpiry.setText(R.string.error_year_empty)
            textExpiry.setTextColor(resources.getColor(R.color.error))
            correctYear = false
            out = false
        } else if (inputYear.length() != 4) {
            if (!out) sb.append("\n")
            sb.append("Fill year cell")
            textExpiry.setText(R.string.error_year_not_filled)
            textExpiry.setTextColor(resources.getColor(R.color.error))
            correctYear = false
            out = false
        } else if (!validateDigits(inputYear.text.toString(), 4)) {
            if (!out) sb.append("\n")
            sb.append("Insert only digits for year")
            textExpiry.setText(R.string.error_year_mismatch_number)
            textExpiry.setTextColor(resources.getColor(R.color.error))
            correctYear = false
            out = false
        }
        //month must be 1 or 2 digits and it must be between 1 to 12
        if (inputMonth.text.isEmpty()) {
            if (!out) sb.append("\n")
            sb.append("Enter Month")
            //Text View label will provide both errors if year had a problem too
            if (!correctYear) textExpiry.setText("" + textExpiry.text + " and " + resources.getString(R.string.error_month_empty))
            else textExpiry.setText(R.string.error_month_empty)
            textExpiry.setTextColor(resources.getColor(R.color.error))
            out = false
        } else if (!validateDigits(inputMonth.text.toString(), 2) &&
            !validateDigits(inputMonth.text.toString(), 1)) {
            if (!out) sb.append("\n")
            sb.append("Insert only digits for Month")
            if (!correctYear) textExpiry.setText("" + textExpiry.text + " and " + resources.getString(R.string.error_month_mismatch_number))
            else textExpiry.setText(R.string.error_month_mismatch_number)
            textExpiry.setTextColor(resources.getColor(R.color.error))
            out = false
        } else if (!(inputMonth.text.toString().toByte() in 1..12)) {
            if (!out) sb.append("\n")
            sb.append("Month of Expiry must be from 1 to 12")
            if (!correctYear) textExpiry.setText("" + textExpiry.text + " and " + resources.getString(R.string.error_month_invalid_input))
            else textExpiry.setText(R.string.error_month_invalid_input)
            textExpiry.setTextColor(resources.getColor(R.color.error))
            out = false
        }
        //same as title
        if (inputOwnerName.text.isEmpty()) {
            if (!out) sb.append("\n")
            sb.append("Enter Owner Name")
            textOwnerName.setText(R.string.error_owner_empty)
            textOwnerName.setTextColor(resources.getColor(R.color.error))
            out = false
        }
        //if validation failed, a dialog containing all errors will be shown
        if (!out) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Input Validation Failed").setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.setMessage(sb.toString())
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        return out
    }
    //this function has two usage, first for all digit cells when they reached their limit, next cell will be focused
    //second, when 6 first digits of card are entered, program will try to determine corresponding bank and show the bank logo
    private fun addTextListeners() {
        inputCardNumber1.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4) {
                    if (inputCardNumber2.length() > 1) {
                        //if two character of next cell are in, determining bank will happen
                        DebitCard.getBankLogo("${inputCardNumber1.text} ${inputCardNumber2.text}", logo)
                    }
                    inputCardNumber2.requestFocus()
                }
            }
        })
        inputCardNumber2.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! < 3) {
                    //determining bank here
                    DebitCard.getBankLogo("${inputCardNumber1.text} ${inputCardNumber2.text}", logo)
                }
                else if (s.length == 4) {
                    inputCardNumber3.requestFocus() // Switch focus to the next EditText
                }
                return
            }
        })
        inputCardNumber3.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4) {
                    inputCardNumber4.requestFocus()
                }
            }
        })
        inputCardNumber4.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4) {
                    inputShabaNumber.requestFocus()
                }
            }
        })
        inputShabaNumber.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 24) {
                    inputMonth.requestFocus() // Switch focus to the next EditText
                }
            }
        })
        inputMonth.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2) {
                    inputYear.requestFocus() // Switch focus to the next EditText
                }
            }
        })
        inputYear.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4) {
                    inputOwnerName.requestFocus() // Switch focus to the next EditText
                }
            }
        })
    }
    //when back or cancel is pressed, a dialog will be shown to prevent accidents
    private fun backPressed() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Discard Operation?").setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }.setPositiveButton("Discard") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    //when activity is launched for editing, all EditTexts will be filled
    private fun fillComponents(card: DebitCard) {
        inputTitle.setText(card.getTitle())
        val cardNumberStrings = card.getCardNumber().split(" ")
        inputCardNumber1.setText(cardNumberStrings[0])
        inputCardNumber2.setText(cardNumberStrings[1])
        inputCardNumber3.setText(cardNumberStrings[2])
        inputCardNumber4.setText(cardNumberStrings[3])
        inputShabaNumber.setText(card.getShaba())
        inputYear.setText(card.getExpiryYear().toString())
        inputMonth.setText(card.getExpiryMonthWithFormat())
        inputOwnerName.setText(card.getOwnerName())
    }
    //is called when
    private fun resetTexts() {
        textTitle.setText(R.string.card_title)
        textTitle.setTextColor(resources.getColor(R.color.text_fill_default))
        textCardNumber.setText(R.string.card_number)
        textCardNumber.setTextColor(resources.getColor(R.color.text_fill_default))
        textShabaNumber.setText(R.string.shaba_number)
        textShabaNumber.setTextColor(resources.getColor(R.color.text_fill_default))
        textExpiry.setText(R.string.expiry)
        textExpiry.setTextColor(resources.getColor(R.color.text_fill_default))
        textOwnerName.setText(R.string.owner_name)
        textOwnerName.setTextColor(resources.getColor(R.color.text_fill_default))
    }
    //a function to determine if a string is only consist of determined amount of digits using a simple regex
    private fun validateDigits(input: String, digits: Int): Boolean {
        println("\\d{${digits}")
        val pattern = Regex("\\d{${digits}}")
        return pattern.matches(input)
    }
    //when operation was successful, a dialog will be shown. because notifyDataSetChanged() was not working properly,
    //unfortunately all the cards will be read from database again.
    private fun showSuccessDialog(mode: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Success").setMessage("Card has been $mode").setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            Statics.getMainActivity().refreshCards()
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}