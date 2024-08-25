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
        injectComponents()
        try {
            card = intent.getSerializableExtra("Card") as DebitCard
            editMode = true
            fillComponents(card!!)
            inputYear.clearFocus()
            DebitCard.getBankLogo("${inputCardNumber1.text} ${inputCardNumber2.text}", logo)
            activityTitle.text = "Edit Card"
        } catch (e: Exception) {
            Toast.makeText(this, "Add mode", Toast.LENGTH_SHORT).show()
        }
    }
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

        buttonConfirm.setOnClickListener {
            confirm()
        }
        buttonCancel.setOnClickListener {
            cancel()
        }
        addTextListeners()
    }
    private fun confirm() {
        resetTexts()
        if (!validateInput()) return
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        //TODO: Implement confirmation part
    }
    private fun cancel() {
        backPressed()
    }
    private fun validateInput(): Boolean {
        val sb = StringBuilder()
        var out = true
        var correctYear = true
        if (inputTitle.text.isEmpty()) {
            sb.append("Enter Title")
            textTitle.setText(R.string.error_title)
            textTitle.setTextColor(resources.getColor(R.color.error))
            out = false
        }

        if (inputCardNumber1.text.isEmpty() && inputCardNumber2.text.isEmpty() &&
            inputCardNumber3.text.isEmpty() && inputCardNumber4.text.isEmpty()) {
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

        if (inputMonth.text.isEmpty()) {
            if (!out) sb.append("\n")
            sb.append("Enter Month")
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

        if (inputOwnerName.text.isEmpty()) {
            if (!out) sb.append("\n")
            sb.append("Enter Owner Name")
            textOwnerName.setText(R.string.error_owner_empty)
            textOwnerName.setTextColor(resources.getColor(R.color.error))
            out = false
        }

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
                    inputCardNumber4.requestFocus() // Switch focus to the next EditText
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
                    inputShabaNumber.requestFocus() // Switch focus to the next EditText
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

    private fun validateDigits(input: String, digits: Int): Boolean {
        println("\\d{${digits}")
        val pattern = Regex("\\d{${digits}}")
        return pattern.matches(input)
    }
}