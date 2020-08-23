package com.andruid.magic.discodruid.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.ActivityMainBinding
import com.andruid.magic.discodruid.ui.adapter.POSITION_ALBUMS
import com.andruid.magic.discodruid.ui.adapter.POSITION_TRACKS
import com.andruid.magic.discodruid.ui.adapter.TabsAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        initTabs()
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