package com.stream.baba

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.stream.baba.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object{
        private var nextSearchQuery: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.apply {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _: NavController, navDestination: NavDestination, bundle: Bundle? ->
            // Intercept search and add a query
            if (navDestination.matchDestination(R.id.navigation_search) && !nextSearchQuery.isNullOrBlank()) {
                bundle?.apply {
                    // this.putString(SearchFragment.SEARCH_QUERY, nextSearchQuery)
                    nextSearchQuery = null
                }
            }
        }
        navView.setupWithNavController(navController)
            navView.setOnItemSelectedListener { item ->
                onNavDestinationSelected(
                    item,
                    navController
                )
            }
            navController.addOnDestinationChangedListener { _, destination, _ ->
                updateNavBar(destination)
            }
    }
    }
    private fun onNavDestinationSelected(item: MenuItem, navController: NavController): Boolean {
        val builder = NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(true)
            .setEnterAnim(R.anim.pop_in)
            .setExitAnim(R.anim.pop_out)
            .setPopEnterAnim(R.anim.pop_in)
            .setPopExitAnim(R.anim.pop_out)
        if (item.order and Menu.CATEGORY_SECONDARY == 0) {
            builder.setPopUpTo(
                navController.graph.findStartDestination().id,
                inclusive = false,
                saveState = true
            )
        }
        val options = builder.build()
        return try {
            navController.navigate(item.itemId, null, options)
            navController.currentDestination?.matchDestination(item.itemId) == true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun updateNavBar(destination: NavDestination) {
        val isAtHome = destination.matchDestination(R.id.navigation_home)
        if (isAtHome){
            binding.navView.selectedItemId = R.id.navigation_home
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController
        val isAtHome =
            navController?.currentDestination?.matchDestination(R.id.navigation_home) == true

        if (!isAtHome){
            onBackPressedDispatcher.onBackPressed()
        }
        if (isAtHome) {
            onBackPressedDispatcher.onBackPressed()
        }

    }


    private fun NavDestination.matchDestination(@IdRes destId: Int): Boolean =
        hierarchy.any { it.id == destId }
}