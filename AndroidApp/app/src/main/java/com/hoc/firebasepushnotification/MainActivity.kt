package com.hoc.firebasepushnotification

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textViews = listOf(profile_text, all_user_text, notifications_text)
                .apply {
                    forEachIndexed { index, textView ->
                        textView.setOnClickListener {
                            view_pager.currentItem = index
                        }
                    }
                }

        view_pager.run {
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) = changeTab(position, textViews)
            })
            adapter = ViewPagerAdapter(supportFragmentManager,
                    listOf(ProfileFragment(), AllUsersFragment(), NotificationsFragment()))
            offscreenPageLimit = 2
        }
    }

    private fun changeTab(position: Int, textViews: List<TextView>) {
        TransitionManager.beginDelayedTransition(tab_layout, ChangeBounds())
        textViews.forEachIndexed { index, textView ->
            if (position == index) {
                textView.textSize = 18f
                textView.setTextColor(
                        ContextCompat.getColor(this@MainActivity, R.color.textTabLight))
            } else {
                textView.textSize = 16f
                textView.setTextColor(
                        ContextCompat.getColor(this@MainActivity, R.color.textTabBright))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser === null) {
            startActivity<LoginActivity>()
            finish()
        }
    }
}


class ViewPagerAdapter(
        supportFragmentManager: FragmentManager?,
        private val fragments: List<Fragment>
) : FragmentPagerAdapter(supportFragmentManager) {
    override fun getItem(position: Int) = fragments[position]
    override fun getCount() = fragments.size
}