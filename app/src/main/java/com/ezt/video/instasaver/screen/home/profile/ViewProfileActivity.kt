package com.ezt.video.instasaver.screen.home.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityViewProfileBinding
import com.ezt.video.instasaver.screen.download.adapter.StoryHighlightViewAdapter
import com.ezt.video.instasaver.screen.home.profile.adapter.UserPostAdapter
import com.ezt.video.instasaver.utils.Common.gone
import com.ezt.video.instasaver.utils.Common.visible
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import com.ezt.video.instasaver.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ViewProfileActivity :
    BaseActivity<ActivityViewProfileBinding>(ActivityViewProfileBinding::inflate) {
    private val userName by lazy {
        intent.getStringExtra("username") ?: ""
    }

    private val fullName by lazy {
        intent.getStringExtra("fullName") ?: ""
    }

    private val userPk by lazy {
        intent.getLongExtra("userId", 0L)
    }
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var cookies: String
    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var userPostAdapter: UserPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = this@ViewProfileActivity.getSharedPreferences("Cookies", 0)
        cookies = sharedPreferences?.getString("loginCookies", "").toString()
//        viewModel.loadUserStats(userPk)
        binding.apply {
            backButton.setOnClickListener { finish() }

            instaAce.text = userName
            instaFullName.text = resources.getString(R.string.full_name).plus(": $fullName")

            binding.storyHighlightView.layoutManager =
                GridLayoutManager(this@ViewProfileActivity, 3 )
            userPostAdapter = UserPostAdapter()
            binding.storyHighlightView.adapter = userPostAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        binding.loading.visible()
        val userId = intent.getLongExtra("userId", 0)
        storyViewModel.storyHighlights.observe(this) { highlights ->
            highlights.onEach {
                println("storyHighlights: $it")
            }
            binding.storyHighlightView.adapter =
                StoryHighlightViewAdapter(this@ViewProfileActivity, highlights, cookies)
        }

        lifecycleScope.launch {
            viewModel.getAllPosts(userId, cookies)
        }
        viewModel.allUserPosts.observe(this) { mediaItems ->
            userPostAdapter.submitList(mediaItems)
            binding.loading.gone()
        }

        viewModel.stats.observe(this) { data ->
            binding.apply {
                postCount.text = data.first.toString()
                followerCount.text = data.second.toString()
                followingCount.text = data.third.toString()
            }
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