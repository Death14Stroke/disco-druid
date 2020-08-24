package com.andruid.magic.discodruid.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.data.ACTION_PREPARE_QUEUE
import com.andruid.magic.discodruid.data.EXTRA_TRACK_MODE
import com.andruid.magic.discodruid.data.MODE_ALL
import com.andruid.magic.discodruid.databinding.ActivityMainBinding
import com.andruid.magic.discodruid.service.MusicService
import com.andruid.magic.discodruid.ui.adapter.POSITION_ALBUMS
import com.andruid.magic.discodruid.ui.adapter.POSITION_TRACKS
import com.andruid.magic.discodruid.ui.adapter.TabsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private val askStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                Log.i("permLog", "permission granted")
                initTabs()
                initBottomSheet()
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

        binding.bottomSheetLayout.thumbnailImage.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
                .setAction(ACTION_PREPARE_QUEUE)
                .putExtra(EXTRA_TRACK_MODE, MODE_ALL)
            startService(intent)
        }
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

    private fun initBottomSheet() {
        val sheetBehaviour = BottomSheetBehavior.from(binding.bottomSheetLayout.motionLayout)
        sheetBehaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.bottomSheetLayout.motionLayout.progress = slideOffset

                binding.bottomSheetLayout.songNameTv.alpha = min(1f - 2f * slideOffset, 1f)
                binding.bottomSheetLayout.bottomSheetArrow.rotation = slideOffset * 180
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })
    }
}