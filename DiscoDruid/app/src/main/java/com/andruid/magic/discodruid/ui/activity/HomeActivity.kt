package com.andruid.magic.discodruid.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.andruid.magic.discodruid.R
import com.andruid.magic.discodruid.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.apply {
            setSupportActionBar(toolBar)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}