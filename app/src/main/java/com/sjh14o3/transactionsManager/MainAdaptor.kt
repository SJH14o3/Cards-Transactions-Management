@file:Suppress("DEPRECATION")
@file:SuppressLint("UseCompatLoadingForDrawables", "DiscouragedApi")


package com.sjh14o3.transactionsManager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sjh14o3.transactionsManager.data.DebitCard

//adaptor for cards recycler view
class MainAdaptor(cards: Array<DebitCard>, private val context: Context, private val activity: MainActivity): RecyclerView.Adapter<MainAdaptor.ViewHolder>() {
    private lateinit var cards: Array<DebitCard>

    init {
        setCards(cards)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setCards(input: Array<DebitCard>) {
        this.cards = input
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.card_title)
        var cardNumber: TextView = itemView.findViewById(R.id.card_number)
        var shaba: TextView = itemView.findViewById(R.id.shaba_number)
        var expiry: TextView = itemView.findViewById(R.id.expiry)
        var name: TextView = itemView.findViewById(R.id.owner_name)
        var parent: ConstraintLayout = itemView.findViewById(R.id.constraintLayoutCards)
        var more: ImageButton = itemView.findViewById(R.id.moreOperations)
        var logo: ImageView = itemView.findViewById(R.id.bank_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bank_card_view, parent, false))
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //in real application, I will add a card at the end as a big button for adding a card
        //that's why things are assigning differently and no more functionality is needed
        if (position == cards.size-1) {
            drawAddCard(holder)
            return
        }
        val card = cards[position]
        //assigning values to the card view
        drawCard(holder, card)
        //getting bank name using a simple process
        val bankName = card.getBankName()
        //a pop up menu will show up when more button is clicked
        holder.more.setOnClickListener {
            showMorePopUp(holder, card)
        }
        //this will bring transactions history of clicked card
        holder.parent.setOnClickListener {
            val intent = Intent(activity, CardOverviewActivity::class.java)
            //some extra information need to be sent to the activity
            intent.putExtra("CardID", card.getId())
            intent.putExtra("CardNumber", card.getCardNumber())
            activity.startActivity(intent)
        }
        //if background of card is customized, the text color will be changed too
        val color = DebitCard.getColor(bankName)
        if (color != "#FFFFFF") {
            customCardStyle(holder, color, bankName)
        }
        else {
            defaultCardStyle(holder, bankName)
        }
    }
    //when sharing card is clicked, a dialog will show up to ask the share method
    //btw, this dialog and sharing took a long time, a lot of wierd bugs kept happening
    private fun showShareDialog(card: DebitCard, holder: ViewHolder) {
        val methods = arrayOf("Card Number", "Shaba Number", "Image")
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder
            .setTitle("Choose Sharing Method")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setItems(methods) { _, which ->
                shareOption(methods, which, card, holder)
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //switch-case for selected sharing method
    private fun shareOption(methods: Array<String>, which: Int, card: DebitCard, holder: ViewHolder) {
        when(methods[which]) {
            "Card Number" -> { //share the card number and owner name
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${card.getCardNumber()}\n${card.getOwnerName()}")
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(activity ,shareIntent, null)
            }

            "Shaba Number" -> { //share the card shaba number and owner name
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${card.getShaba()}\n${card.getOwnerName()}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(activity ,shareIntent, null)
            }

            "Image" -> {
                /*the coolest sharing method, as an image! the exact look of the card view
                 excluding card title, expiry and more option button*/
                holder.title.visibility = View.INVISIBLE
                holder.expiry.visibility = View.INVISIBLE
                holder.more.visibility = View.INVISIBLE
                val bitmap = holder.parent.drawToBitmap(Bitmap.Config.ARGB_8888)
                sharePalette(bitmap)
                holder.title.visibility = View.VISIBLE
                holder.expiry.visibility = View.VISIBLE
                holder.more.visibility = View.VISIBLE
            }
        }
    }

    //called for sharing image. this is the source: https://gist.github.com/Binary-Finery/0ae4bc4c0fd14c04ab4f9229f7ee87fa
    private fun sharePalette(bitmap: Bitmap) {
        val bitmapPath = MediaStore.Images.Media.insertImage(
            activity.contentResolver,
            bitmap,
            "palette",
            "share palette",
        )
        val bitmapUri = Uri.parse(bitmapPath)

        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("image/png")
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        startActivity(activity,Intent.createChooser(intent, "Share"), null)
    }
    //this function handles displaying the expiry month, if month number is less than 10, a 0 will concat to start
    private fun displayMonth(month: Byte): String {
        if (month < 10) return "0${month}"
        return month.toString()
    }
    private fun customCardStyle(holder: ViewHolder, color: String, bankName: String) {
        holder.title.setTextColor(Color.parseColor(color))
        holder.cardNumber.setTextColor(Color.parseColor(color))
        holder.shaba.setTextColor(Color.parseColor(color))
        holder.expiry.setTextColor(Color.parseColor(color))
        holder.name.setTextColor(Color.parseColor(color))
        val res = Statics.getApplicationContext().resources
        val threeDots = DrawableCompat.wrap(res.getDrawable(R.drawable.ic_three_dots))
        threeDots.setTint(context.resources.getColor(R.color.mehr_text_fill))
        holder.more.setImageDrawable(threeDots)
        val directory2 = "bg_" + bankName.lowercase()
        holder.parent.background = res.getDrawable(res.getIdentifier(directory2, "drawable", Statics.getPackageName()))
        holder.logo.visibility = View.GONE
    }
    private fun defaultCardStyle(holder: ViewHolder, bankName: String) {
            val res = Statics.getApplicationContext().resources
            val directory = "ic_" + bankName.lowercase()
            val resID = res.getIdentifier(directory, "drawable", Statics.getPackageName())
            try {
                //loading in bank logo to identify bank
                holder.logo.setImageDrawable(res.getDrawable(resID))
                //if somehow  the bank logo was not available, a default icon will be used instead
            } catch (e: NotFoundException) {
                return
            }
    }
    private fun drawAddCard(holder: ViewHolder) {
        holder.title.visibility = View.INVISIBLE
        holder.cardNumber.visibility = View.INVISIBLE
        holder.shaba.visibility = View.INVISIBLE
        holder.expiry.visibility = View.INVISIBLE
        holder.name.visibility = View.INVISIBLE
        holder.parent.background = ContextCompat.getDrawable(context, R.drawable.add_card)
        holder.more.visibility = View.GONE
        holder.logo.visibility = View.GONE
        holder.parent.setOnClickListener {
            addCard()
        }
        return
    }
    private fun drawCard(holder: ViewHolder, card: DebitCard) {
        holder.title.text = card.getTitle()
        holder.cardNumber.text = card.getCardNumber()
        holder.shaba.text = activity.getString(R.string.shaba, card.getShaba())
        val year = card.getExpiryYear()
        val month = displayMonth(card.getExpiryMonth())
        holder.expiry.text = activity.getString(R.string.expiry_date, year,month)
        holder.name.text = card.getOwnerName()
    }
    private fun showMorePopUp(holder: ViewHolder, card: DebitCard) {
        val moreMenu = PopupMenu(context, holder.more)
        moreMenu.menuInflater.inflate(R.menu.card_more, moreMenu.menu)
        moreMenu.setOnMenuItemClickListener { item ->
            when(item.title) {
                "Edit" -> {
                    editCard(card)
                }
                "Share" -> showShareDialog(card, holder)
                "Delete" -> { //an alert for confirmation will be shown first
                    val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                    builder
                        .setTitle("Warning").setIcon(R.drawable.ic_alert)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Delete") { dialog, _ ->
                            Statics.getCardDatabase().deleteCard(card.getId())
                            //all of the card transactions will be deleted
                            Statics.getTransactionDatabase().deleteAllCardTransactions(card.getId())
                            activity.refreshCards()
                            dialog.dismiss()
                            Snackbar.make(activity.window.decorView.findViewById(R.id.coordinate_layout), "Card was deleted", Snackbar.LENGTH_INDEFINITE).setAction("OK"
                            ) {
                            }.show()
                        }.setMessage("Do you want to delete ${card.getTitle()}?\nAll of the information" +
                                "including transactions of the card will be lost forever")
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
            true
        }
        moreMenu.show()
    }
    private fun editCard(card: DebitCard) {
        val intent = Intent(activity, CardAddOrEditActivity::class.java)
        intent.putExtra("Card", card)
        activity.startActivity(intent)
    }
    fun addCard() {
        Statics.switchActivity(activity, CardAddOrEditActivity::class.java)
    }
}