package com.andruid.magic.discodruid.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_track -> {
                    Log.d("navlog", "nav track dest")
                }
                R.id.nav_home -> {
                    Log.d("navlog", "nav home dest")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }
}