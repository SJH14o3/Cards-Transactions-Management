package com.sjh14o3.transactionsManager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.sjh14o3.transactionsManager.data.Transaction

//transactions recycler views adaptor
class TransactionsAdaptor(private var transactions: Array<Transaction>, private val context: Context,
                          private val activity: CardOverviewActivity, private val cardID: Int): RecyclerView.Adapter<TransactionsAdaptor.ViewHolder>() {
      private var changedRow = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var parent: CardView = itemView.findViewById(R.id.transaction_card_parent)
        var date: TextView = itemView.findViewById(R.id.date)
        var time: TextView = itemView.findViewById(R.id.time)
        var change: TextView = itemView.findViewById(R.id.change)
        var categoryIcon: ImageView = itemView.findViewById(R.id.category)
        var viewButton: ImageButton = itemView.findViewById(R.id.view_button)
        var moreButton: ImageButton = itemView.findViewById(R.id.more)
        var dynamicLayout: ConstraintLayout = itemView.findViewById(R.id.dynamic_layout)
        var note: TextView = itemView.findViewById(R.id.note)
        var remain: TextView = itemView.findViewById(R.id.remain)
        var canBeModified = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false))
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        if (position == 0 || position == 1) {
            holder.parent.visibility = View.INVISIBLE
            return
        }
        drawTransaction(holder, transaction, position)
    }
    @SuppressLint("SetTextI18n")
    private fun drawTransaction(holder: ViewHolder, transaction: Transaction, position: Int) {
        //odd cards and even cards will have separate background color
        if (position % 2 == 1) holder.parent.setBackgroundColor(activity.resources.getColor(R.color.transaction_background_even))
        holder.date.text = transaction.getDate() //TODO: test three modes
        holder.time.text = transaction.getTime()
        Transaction.setIconType(transaction.getType(), holder.categoryIcon)
        //income will be shown green, but spend will be shown red
        if (transaction.getChange() > 0) {
            holder.change.text = "+" + Transaction.getSeparatedDigits(transaction.getChange()) + "T"
            holder.change.setTextColor(context.resources.getColor(R.color.income_text))
        } else {
            holder.change.text = Transaction.getSeparatedDigits(transaction.getChange()) + "T"
            holder.change.setTextColor(context.resources.getColor(R.color.spent_text))
        }
        val note = transaction.getNote()
        holder.note.text = note.ifEmpty {
            "No Note was Provided"
        }
        holder.remain.text = "Remain: ${Transaction.getSeparatedDigits(transaction.getRemain())}T"
        //cards can be expanded to show note and remain in two ways. expanded card will be minimized if trigger is pressed again
        holder.viewButton.setOnClickListener {  //one:clicking expand button
            toggleMoreInformation(holder.dynamicLayout, holder.viewButton)
        }
        holder.parent.setOnClickListener { //two: clicking transaction
            toggleMoreInformation(holder.dynamicLayout, holder.viewButton)
        }

        holder.moreButton.setOnClickListener {
            showTransactionMore(holder, transaction, position)
        }
        holder.canBeModified = Transaction.allowedForMoreOperations(transaction.getDateAndTimeAsLong().toString())
    }
    //expand the minimized card, minimize the expanded card
    private fun toggleMoreInformation(layout: ConstraintLayout, button: ImageButton) {
        if (layout.visibility == View.GONE) {
            layout.visibility = View.VISIBLE
            button.setImageResource(R.drawable.ic_minimize)
        }
        else {
            layout.visibility = View.GONE
            button.setImageResource(R.drawable.ic_expand)
        }
    }

    //when more button on a transaction card is clicked, it will show a pop up menu
    private fun showTransactionMore(holder: ViewHolder, transaction: Transaction, position: Int) {
        val moreMenu = PopupMenu(context, holder.moreButton)
        if (holder.canBeModified) {
            moreMenu.menuInflater.inflate(R.menu.transaction_more, moreMenu.menu)
        }
        else {
            moreMenu.menuInflater.inflate(R.menu.transaction_one_option, moreMenu.menu)
        }
        moreMenu.setOnMenuItemClickListener { item ->
            changedRow = position
            when(item.title) {
                "Edit" -> {
                    val intent = Intent(activity, TransactionModifyActivity::class.java)
                    intent.putExtra("CardID", cardID)
                    intent.putExtra("EDIT", true)
                    intent.putExtra("transaction", transaction)
                    activity.startActivityForResult(intent, CardOverviewActivity.EDIT_TRANSACTION_REQUEST_CODE)
                }
                "Share" -> {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        val debitCard = Statics.getCardDatabase().getCard(cardID)
                        putExtra(Intent.EXTRA_TEXT, "Transaction From card: ${debitCard.getCardNumber()}\n" +
                                "Time: ${holder.date.text} ${holder.time.text}\nChange: ${holder.change.text}\n" +
                                "Remain: ${holder.remain.text}\n" +
                                "Note: ${holder.note.text}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(activity ,shareIntent, null)
                }
                "Delete" -> { //an alert for confirmation will be shown first
                    AlertDialog.Builder(activity)
                        .setTitle("Warning").setIcon(R.drawable.ic_alert)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Delete") { dialog, _ ->
                            deleteShowAskForUpdateRowsTransaction(transaction)
                        }.setMessage("This Transaction Will be deleted for ever!")
                        .create().show()
                }
            }
            true
        }
        moreMenu.show()
    }

    private fun deleteShowAskForUpdateRowsTransaction(transaction: Transaction) {
        //this is not the last transaction
        if (Statics.getTransactionDatabase().getLastDateAndTime(cardID) != transaction.getDateAndTimeAsLong()) {
            AlertDialog.Builder(activity).setTitle("Choose Action")
                .setMessage("This transaction which you are willing to delete, is not the newest one." +
                        "Do you want to automatically change remain of next transactions?" +
                        "be aware that if during modifying next transactions, if a transaction remain" +
                        "become negative, this will failed and you have to change them manually")
                .setPositiveButton("Update") { dialog, _ ->
                    if (Statics.getTransactionDatabase().updateNextRowsRemain(transaction.getDateAndTimeAsLong(),
                            transaction.getChange(), cardID)) {
                        Statics.getTransactionDatabase().deleteTransaction(transaction.getId())
                        Toast.makeText(activity, "Deletion and auto fix were successful", Toast.LENGTH_LONG).show()
                        activity.refreshFromAdaptor()
                    } else {
                        AlertDialog.Builder(activity).setMessage("Failed").
                        setMessage("With the deletion, some of the next transactions became negative!" +
                                " try deleting again but don't update the next rows and fix the remains manually.")
                            .setPositiveButton("OK") { _, _ -> }.create().show()
                    }
                }.setNegativeButton("Don't Update") { _, _ ->
                    Statics.getTransactionDatabase().deleteTransaction(transaction.getId())
                    activity.refreshFromAdaptor()
                }.setNeutralButton("Cancel Deletion") { dialog, _ ->
                    dialog.dismiss()
                }.setCancelable(false).create().show()
        } else {
            Statics.getTransactionDatabase().deleteTransaction(transaction.getId())
            Toast.makeText(activity, "Deletion was successful", Toast.LENGTH_LONG).show()
            activity.refreshFromAdaptor()
        }
    }

    fun updateRow() {
        transactions[changedRow] = Statics.getTransactionDatabase().getTransaction(transactions[changedRow].getId())
        notifyItemChanged(changedRow)
    }
}