package com.example.mymovies.views.activities

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.mymovies.R
import com.example.mymovies.databinding.ActivityMainBinding
import com.example.mymovies.utils.toGone
import com.example.mymovies.utils.toVisible
import com.example.mymovies.views.fragments.InfoFragment
import com.example.mymovies.views.fragments.MovieFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationComponent()
    }

    private lateinit var appBarConfigurationDrawer: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private fun initNavigationComponent() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(onDestinationChangeListener)
    }

    private val onDestinationChangeListener = NavController.OnDestinationChangedListener { controller, destination, arguments ->
        when (destination.id) {
            R.id.splashFragment, R.id.watchFragment, R.id.infoFragment -> {
                binding.bottomNav.toGone()
            }
            else -> binding.bottomNav.toVisible()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfigurationDrawer) || super.onSupportNavigateUp()

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        with(navHostFragment!!.childFragmentManager.fragments[0]) {
            when (this){
                is MovieFragment -> checkCloseApp(this.onBackPressed())
                is InfoFragment -> checkCloseApp(this.onBackPressed())
                else -> super.onBackPressed()
            }
        }
    }

    private fun checkCloseApp(close: Boolean){
        if(close)
            finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
