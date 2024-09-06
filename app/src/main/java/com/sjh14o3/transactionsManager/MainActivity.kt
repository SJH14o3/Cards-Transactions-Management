package com.sjh14o3.transactionsManager

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.sjh14o3.transactionsManager.data.DebitCard
import com.sjh14o3.transactionsManager.databinding.ActivityMainBinding

//application starts from here
class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{
    private lateinit var parent: DrawerLayout
    private lateinit var recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var addCardButton: FloatingActionButton
    private lateinit var adaptor: MainAdaptor
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var cards: Array<DebitCard>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        //setting every static variables in start of application
        parent= findViewById(R.id.main)
        coordinatorLayout = findViewById(R.id.coordinate_layout)
        Statics.setVariables(applicationContext, packageName, this)
        //setting up drawer
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.main, binding.toolbar,
            R.string.add_card, R.string.expiry_placeholder)
        binding.main.addDrawerListener(toggle)
        toggle.syncState()
        binding.navMain.setNavigationItemSelectedListener(this)
        fragmentManager = supportFragmentManager
        openFragment(HomeFragment())

        //setting up recycler view
        setContentView(binding.root)
        recyclerview = findViewById(R.id.mainRecyclerView)
        cards = Statics.getCardDatabase().getAll()
        adaptor = MainAdaptor(cards, applicationContext, this)
        recyclerview.adapter = adaptor
        recyclerview.layoutManager = LinearLayoutManager(this)

        //setting up add card floating button
        addCardButton = findViewById(R.id.addCardFloatingButton)
        addCardButton.setOnClickListener { _ -> //switching to adding card activity
            adaptor.addCard()
        }
    }

    //navigation for drawer items selection, currently will show a toast message
    //TODO: add functionality to all menu drawer buttons
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_all_cards -> {
                val intent = Intent(this, ReportActivity::class.java)
                intent.putExtra("CardID", 0)
                startActivity(intent)
            }
            //R.id.nav_settings -> Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show() //Maybe in future
            //R.id.nav_accesses -> Toast.makeText(applicationContext, "Accesses", Toast.LENGTH_SHORT).show() //Maybe in future
            R.id.nav_add_card -> {
                adaptor.addCard()
            }
            R.id.nav_refresh -> {
                refreshCards()
            }
        }
        binding.main.closeDrawer(GravityCompat.START)
        return true
    }

    //when back is pressed, depending on if drawer is open, different action will be taken
    fun backPressed() {
        if (binding.main.isDrawerOpen(GravityCompat.START)) {
            binding.main.closeDrawer(GravityCompat.START)
        }
        else {
            super.onBackPressedDispatcher.onBackPressed()
        }
    }

    //when drawer opens, this function will run
    private fun openFragment(fragment: Fragment) {
        val fragmentTransition: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.main_frame, fragment)
        fragmentTransition.commit()
    }

    //before using data base, this was used for testing the recycler view of cards and image checking
    /*private fun getSampleCards(): Array<DebitCard> {
        val card1 = DebitCard("Income Card", "5022 2901 2345 6789", "123456789012345678901234", 1, 1407, "Michael Hamilton Smith")
        val card2 = DebitCard("Safe Spending Card", "6219 8601 2345 6789", "123456789012345678901234", 2, 1408, "Michael Hamilton Smith")
        val card3 = DebitCard("Deposit Card", "6063 7301 2345 6789", "123456789012345678901234", 11, 1409, "Michael Hamilton Smith")
        val card4 = DebitCard("Loan Card", "6395 9901 2345 6789", "123456789012345678901234", 7, 1410, "Michael Hamilton Smith")
        val card6 = DebitCard("Bill card", "6274 1201 2345 6789", "123456789012345678901234", 11, 1411, "Michael Hamilton Smith")
        val card7 = DebitCard("Kid Card", "6273 8101 2345 6789", "123456789012345678901234", 12, 1412, "Michael Hamilton Smith")
        val card8 = DebitCard("my card8", "0000 0601 2345 6789", "123456789012345678901234", 4, 1413, "Michael Hamilton Smith")
        val card9 = DebitCard("my card9", "6362 1401 2345 6789", "123456789012345678901234", 9, 1414, "Michael Hamilton Smith")
        return arrayOf(card1, card2, card3, card4, card6, card7, card8, card9)
    }*/
    //refresh all the cards. since notifyDataSetChanged() was causing issues, this had be done unfortunately.
    fun refreshCards() {
        cards = Statics.getCardDatabase().getAll()
        adaptor = MainAdaptor(cards, applicationContext, this)
        recyclerview.adapter = adaptor
    }
}