package com.neonnoir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.neonnoir.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Inflates the layout, sets up NavController, and wires the bottom navigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navHeight  = binding.bottomNav.measuredHeight
                .takeIf { it > 0 }
                ?: resources.getDimensionPixelSize(R.dimen.bottom_nav_height)

            view.updatePadding(
                top    = systemBars.top,
                bottom = navHeight
            )
            windowInsets
        }
    }

    // Connects BottomNavigationView to the NavController and hides it on detail screens
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id in bottomNavDestinations
        }
    }

    // Returns the set of destination IDs that should show the bottom navigation bar
    private val bottomNavDestinations = setOf(
        R.id.nav_home,
        R.id.nav_search,
        R.id.nav_library,
        R.id.nav_profile
    )
}
