package com.andruid.magic.discodruid.ui.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.palette.graphics.Palette
import coil.api.load
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.FragmentHomeBinding
import com.andruid.magic.discodruid.ui.adapter.TabPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        requireActivity().findViewById<AppBarLayout>(R.id.home_app_bar).visibility = View.GONE
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        setViewPager()
        selectTab(0)

        return binding.root
    }

    private fun setViewPager() {
        val tabPagerAdapter = TabPagerAdapter(
            requireContext(), requireFragmentManager(),
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        binding.apply {
            viewPager.adapter = tabPagerAdapter
            viewPager.offscreenPageLimit = TabPagerAdapter.NUMBER_OF_TABS

            tabLayout.setupWithViewPager(viewPager)
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) = selectTab(tab.position)

                override fun onTabUnselected(p0: TabLayout.Tab?) {}
                override fun onTabReselected(p0: TabLayout.Tab?) {}
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    fun selectTab(pos: Int) {
        binding.apply {
            viewPager.currentItem = pos
            val imgRes = when (pos) {
                TabPagerAdapter.POS_TRACKS -> R.drawable.track_bg
                TabPagerAdapter.POS_ALBUM -> R.drawable.album_bg
                TabPagerAdapter.POS_ARTIST -> R.drawable.artist_bg
                else -> R.drawable.playlist_bg
            }
            launch {
                withContext(Dispatchers.IO) { tabImageView.load(imgRes) }
                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeResource(resources, imgRes)
                }
                Palette.Builder(bitmap)
                    .generate { palette ->
                        palette?.let {
                            val vibrantLightColor =
                                it.getLightVibrantColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.colorPrimary
                                    )
                                )
                            val vibrantDarkColor =
                                it.getDarkVibrantColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.colorPrimaryDark
                                    )
                                )
                            collapseToolBar.setContentScrimColor(vibrantDarkColor)
                            collapseToolBar.setStatusBarScrimColor(vibrantLightColor)
                        }
                    }
            }
        }
    }
}