package com.example.tracsitv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.tracsitv2.databinding.ActivityPrimaryBinding

class PrimaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrimaryBinding
    lateinit var toggle : ActionBarDrawerToggle
    private val fragmentHistory = ArrayDeque<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrimaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///side nav drawer

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.side_menu_open, R.string.side_menu_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.miItem1 -> startActivity(Intent(this, MainActivity::class.java))
                R.id.miItem2 -> startActivity(Intent(this, RegisterActivity::class.java))
            }
            true
        }

        ///bottom nav bar

        val notificationsFragment = NotificationsFragment()
        setCurrentFragment(notificationsFragment)
        var prevId = R.id.bnbHome

        binding.bottomNavigationView.setOnItemSelectedListener {
            val currentId = it.itemId
            setCurrentFragment(getFragment(currentId))
            if (fragmentHistory.isNotEmpty()) {
                if (fragmentHistory.last() != prevId) {
                    fragmentHistory.add(prevId)
                }
            } else fragmentHistory.add(prevId)
            prevId = currentId
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFragment(itemId : Int) : Fragment {
        when(itemId) {
            R.id.bnbHome -> {
                binding.bottomNavigationView.menu.getItem(0).setChecked(true)
                return NotificationsFragment()
            }
            R.id.bnbNotifications -> {
                binding.bottomNavigationView.menu.getItem(1).setChecked(true)
                return CalendarFragment()}
            R.id.bnbSettings -> {
                binding.bottomNavigationView.menu.getItem(2).setChecked(true)
                return SettingsFragment()}
        }
        binding.bottomNavigationView.menu.getItem(0).setChecked(true)
        return NotificationsFragment()
    }

    private fun setCurrentFragment(newFragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, newFragment)
            commit()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        else if (fragmentHistory.isNotEmpty()) {
            setCurrentFragment(getFragment(fragmentHistory.last()))
            fragmentHistory.removeLast()
        } else finish()
    }

    fun addFragmentToHistory(prevId: Int) {
        fragmentHistory.add(prevId)
    }

}