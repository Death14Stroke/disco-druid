package com.andruid.magic.discodruid.ui.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.palette.graphics.Palette
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.FragmentHomeBinding
import com.andruid.magic.discodruid.eventbus.ViewTracksEvent
import com.andruid.magic.discodruid.ui.adapter.TabPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(), CoroutineScope {
    private lateinit var binding: FragmentHomeBinding

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("navlog", "HomeFragment onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        NavigationUI.setupActionBarWithNavController(
            requireActivity() as AppCompatActivity,
            findNavController(),
            AppBarConfiguration(findNavController().graph)
        )
        setViewPager()
        selectTab(0)
        return binding.root
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onViewTracksEvent(event: ViewTracksEvent) {
        findNavController().navigate(HomeFragmentDirections.actionHomeToTrack())
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun setViewPager() {
        val tabPagerAdapter = TabPagerAdapter(this)
        binding.apply {
            viewPager.adapter = tabPagerAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = getString(
                    when (position) {
                        TabPagerAdapter.POS_TRACKS -> R.string.tracks
                        TabPagerAdapter.POS_ALBUM -> R.string.albums
                        TabPagerAdapter.POS_ARTIST -> R.string.artists
                        else -> R.string.playlists
                    }
                )
            }.attach()
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    selectTab(tab.position)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    private fun selectTab(pos: Int) {
        binding.apply {
            val imgRes = when (pos) {
                TabPagerAdapter.POS_TRACKS -> R.drawable.track_bg
                TabPagerAdapter.POS_ALBUM -> R.drawable.album_bg
                TabPagerAdapter.POS_ARTIST -> R.drawable.artist_bg
                else -> R.drawable.playlist_bg
            }
            launch {
                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeResource(resources, imgRes)
                }
                tabImageView.setImageBitmap(bitmap)
                Palette.Builder(bitmap)
                    .generate { palette ->
                        palette?.let {
                            val vibrantDarkColor =
                                it.getDarkVibrantColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.colorPrimaryDark
                                    )
                                )
                            collapseToolBar.setContentScrimColor(vibrantDarkColor)
                            collapseToolBar.setStatusBarScrimColor(vibrantDarkColor)
                        }
                    }
            }
        }
    }
}