package com.ezt.video.instasaver.screen.home.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityViewProfileBinding
import com.ezt.video.instasaver.screen.download.adapter.StoryHighlightViewAdapter
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import com.ezt.video.instasaver.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ViewProfileActivity :
    BaseActivity<ActivityViewProfileBinding>(ActivityViewProfileBinding::inflate) {
    private val userName by lazy {
        intent.getStringExtra("username")
    }
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var cookies: String
    private val storyViewModel: StoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cookies = intent.getStringExtra("cookie") ?: ""
        binding.apply {
            backButton.setOnClickListener { finish() }

            instaAce.text = userName

            binding.storyHighlightView.layoutManager =
                LinearLayoutManager(this@ViewProfileActivity, LinearLayoutManager.HORIZONTAL, false)

        }
    }

    override fun onResume() {
        super.onResume()
        val userId = intent.getLongExtra("userId", 0)
        storyViewModel.fetchStoryHighlights(userId, cookies)

        storyViewModel.storyHighlights.observe(this) { highlights ->
            highlights.onEach {
                println("storyHighlights: $it")
            }
            binding.storyHighlightView.adapter =
                StoryHighlightViewAdapter(this@ViewProfileActivity, highlights, cookies)
        }

    }

    private fun cookieIsStillValid(cookie: String?, onValid: (Boolean) -> Unit) {

        cookie?.let {
            lifecycleScope.launch {
                val x: Boolean = viewModel.isCurrentUserCookieValid(cookie)
                onValid(x)
            }
        }
    }
}