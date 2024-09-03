package com.sjh14o3.transactionsManager

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sjh14o3.transactionsManager.data.Transaction

//transactions recycler views adaptor
class TransactionsAdaptor(private var transactions: Array<Transaction>, private val context: Context,
                          private val activity: CardOverviewActivity, private val cardID: Int): RecyclerView.Adapter<TransactionsAdaptor.ViewHolder>() {

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false))
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        drawTransaction(holder, transaction, position)
    }
    @SuppressLint("SetTextI18n")
    private fun drawTransaction(holder: ViewHolder, transaction: Transaction, position: Int) {
        if (transaction.getDate().isEmpty()) {
            holder.parent.visibility = View.INVISIBLE
            return
        }
        //odd cards and even cards will have separate background color
        if (position % 2 == 1) holder.parent.setBackgroundColor(activity.resources.getColor(R.color.transaction_background_even))
        holder.date.text = transaction.getDate() //TODO: test three modes
        holder.time.text = transaction.getTime()
        Transaction.setIconType(transaction.getType(), holder.categoryIcon)
        //income will be shown green, but spend will be shown red
        if (transaction.getChange() > 0) {
            holder.change.text = "+" + Transaction.getSeparatedDigits(transaction.getChange())
            holder.change.setTextColor(context.resources.getColor(R.color.income_text))
        } else {
            holder.change.text = "-" + Transaction.getSeparatedDigits(transaction.getChange())
            holder.change.setTextColor(context.resources.getColor(R.color.spent_text))
        }
        val note = transaction.getNote()
        holder.note.text = note.ifEmpty {
            "No Note was Provided"
        }
        holder.remain.text = "Remain: ${Transaction.getSeparatedDigits(transaction.getRemain())}"
        //cards can be expanded to show note and remain in two ways. expanded card will be minimized if trigger is pressed again
        holder.viewButton.setOnClickListener {  //one:clicking expand button
            toggleMoreInformation(holder.dynamicLayout, holder.viewButton)
        }
        holder.parent.setOnClickListener { //two: clicking transaction
            toggleMoreInformation(holder.dynamicLayout, holder.viewButton)
        }

        holder.moreButton.setOnClickListener {
            //TODO: transactions more options
            Toast.makeText(context, "${Transaction.getTypeName(transaction.getType())} is clicked", Toast.LENGTH_SHORT).show()
        }
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
}