package ru.netology.nework

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.netology.nework.core.session.TokenStore
import ru.netology.nework.databinding.ActivityMainBinding
import ru.netology.nework.feature.auth.LoginFragment
import ru.netology.nework.feature.auth.RegisterFragment
import ru.netology.nework.feature.common.MapFragment
import ru.netology.nework.feature.events.EventsFragment
import ru.netology.nework.feature.posts.EditPostFragment
import ru.netology.nework.feature.posts.PostsFragment
import ru.netology.nework.feature.users.UserProfileFragment
import ru.netology.nework.feature.users.UsersFragment
import ru.netology.nework.feature.users.UsersRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenStore: TokenStore

    @Inject
    lateinit var usersRepository: UsersRepository

    private lateinit var binding: ActivityMainBinding
    private var isLoggedIn = false
    private var authScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, windowInsets ->
            val statusBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, statusBarInsets.top, view.paddingRight, view.paddingBottom)
            windowInsets
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_app_bar, menu)
                updateAuthMenu(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.actionLogin -> {
                        openLogin()
                        return true
                    }
                    R.id.actionRegister -> {
                        openRegister()
                        return true
                    }
                    R.id.actionLogout -> {
                        logout()
                        return true
                    }
                    R.id.actionProfile -> {
                        openMyProfile()
                        return true
                    }
                }
                return false
            }
        }, this, Lifecycle.State.RESUMED)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenStore.tokenFlow().collect { token ->
                    isLoggedIn = !token.isNullOrBlank()
                    invalidateOptionsMenu()
                }
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.container)
            authScreen = current is LoginFragment || current is RegisterFragment
            updateScreenState()
        }

        if (savedInstanceState == null) {
            openPosts()
        } else {
            updateScreenState()
        }

        binding.toolbar.setNavigationOnClickListener {
            supportFragmentManager.popBackStack()
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
                R.id.menu_map -> {
                    openMap()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateAuthMenu(menu: Menu) {
        val showAuthActions = !isLoggedIn && !authScreen
        menu.findItem(R.id.actionLogin).isVisible = showAuthActions
        menu.findItem(R.id.actionRegister).isVisible = showAuthActions
        menu.findItem(R.id.actionLogout).isVisible = isLoggedIn && !authScreen
        menu.findItem(R.id.actionProfile).isVisible = isLoggedIn && !authScreen
    }

    private fun updateScreenState() {
        val hasBackStack = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(hasBackStack)
        binding.bottomNavigation.isVisible = !hasBackStack
        applyToolbarStyle()
        invalidateOptionsMenu()
    }

    private fun applyToolbarStyle() {
        if (authScreen) {
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black))
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = true
            binding.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))
        } else {
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
            window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)
            WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false
            binding.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            tokenStore.clear()
            while (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStackImmediate()
            }
            authScreen = false
            openPosts()
        }
    }

    private fun openMyProfile() {
        lifecycleScope.launch {
            val userId = tokenStore.userIdFlow().first()
            if (userId.isNullOrBlank()) {
                openLogin()
                return@launch
            }
            try {
                val user = usersRepository.loadUser(userId)
                val fragment = UserProfileFragment()
                val arguments = Bundle()
                arguments.putString("userId", user.id)
                arguments.putString("name", user.name ?: user.login)
                arguments.putString("login", user.login)
                arguments.putString("avatar", user.avatarUrl)
                fragment.arguments = arguments
                supportActionBar?.title = getString(R.string.menu_profile)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack("profile")
                    .commit()
                updateScreenState()
            } catch (exception: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    exception.message ?: "не удалось открыть профиль",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openPosts() {
        authScreen = false
        supportActionBar?.title = getString(R.string.app_name)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, PostsFragment())
            .commit()
        updateScreenState()
    }

    private fun openEvents() {
        authScreen = false
        supportActionBar?.title = getString(R.string.menu_events)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, EventsFragment())
            .commit()
        updateScreenState()
    }

    private fun openUsers() {
        authScreen = false
        supportActionBar?.title = getString(R.string.menu_users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, UsersFragment())
            .commit()
        updateScreenState()
    }

    private fun openMap() {
        authScreen = false
        supportActionBar?.title = getString(R.string.menu_map)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MapFragment.newInstance(pickLocation = false))
            .commit()
        updateScreenState()
    }

    fun openLogin(replaceOnly: Boolean = false) {
        authScreen = true
        supportActionBar?.title = getString(R.string.login_title)
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.container, LoginFragment())
        if (!replaceOnly) {
            transaction.addToBackStack("login")
        }
        transaction.commit()
        updateScreenState()
    }

    fun openRegister(replaceOnly: Boolean = false) {
        authScreen = true
        supportActionBar?.title = getString(R.string.register_title)
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.container, RegisterFragment())
        if (!replaceOnly) {
            transaction.addToBackStack("register")
        }
        transaction.commit()
        updateScreenState()
    }

    fun openEditPost() {
        authScreen = false
        supportActionBar?.title = getString(R.string.edit_post_title)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, EditPostFragment())
            .addToBackStack("edit_post")
            .commit()
        updateScreenState()
    }
}
