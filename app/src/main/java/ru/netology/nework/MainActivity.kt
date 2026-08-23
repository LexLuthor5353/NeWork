package ru.netology.nework

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.ActivityMainBinding
import ru.netology.nework.feature.events.EventsFragment
import ru.netology.nework.feature.posts.PostsFragment
import ru.netology.nework.feature.users.UsersFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_app_bar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, this, Lifecycle.State.RESUMED)

        if (savedInstanceState == null) {
            openPosts()
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_posts -> {
                    openPosts()
                    true
                }
                R.id.menu_events -> {
                    openEvents()
                    true
                }
                R.id.menu_users -> {
                    openUsers()
                    true
                }
                else -> false
            }
        }
    }

    private fun openPosts() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, PostsFragment())
            .commit()
    }

    private fun openEvents() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, EventsFragment())
            .commit()
    }

    private fun openUsers() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, UsersFragment())
            .commit()
    }
}
