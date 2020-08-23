package com.andruid.magic.discodruid.ui.activity

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.ActivityMainBinding
import com.andruid.magic.discodruid.ui.adapter.POSITION_ALBUMS
import com.andruid.magic.discodruid.ui.adapter.POSITION_TRACKS
import com.andruid.magic.discodruid.ui.adapter.TabsAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private val askStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            Log.i("permLog", "permission granted")
            initTabs()
        } else {
            Log.i("permLog", "permission denied")
            finish()
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        askStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        //initTabs()
    }

    private fun initTabs() {
        binding.viewPager.adapter = TabsAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                POSITION_TRACKS -> getString(R.string.tab_tracks)
                POSITION_ALBUMS -> getString(R.string.tab_albums)
                else -> getString(R.string.tab_artists)
            }
        }.attach()
    }
}