package com.ezt.video.instasaver.screen.home.profile

import android.os.Bundle
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityViewProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewProfileActivity : BaseActivity<ActivityViewProfileBinding>(ActivityViewProfileBinding::inflate) {
    private val userName by lazy {
        intent.getStringExtra("username")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            backButton.setOnClickListener { finish() }

            instaAce.text = userName
        }
    }
}