package com.ezt.video.instasaver.screen.home.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityViewProfileBinding
import com.ezt.video.instasaver.model.Items
import com.ezt.video.instasaver.screen.home.profile.adapter.UserPostAdapter
import com.ezt.video.instasaver.utils.Common.gone
import com.ezt.video.instasaver.utils.Common.visible
import com.ezt.video.instasaver.viewmodel.HomeViewModel
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

    private val userId by lazy {
        intent.getLongExtra("userId", 0)
    }
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var cookies: String

    private val userPostAdapter: UserPostAdapter by lazy {
        UserPostAdapter { item ->
            binding.selectAll.text =
                resources.getString(R.string.select_all).plus(" ${userPostAdapter.getSelectedPosts().size}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = this@ViewProfileActivity.getSharedPreferences("Cookies", 0)
        cookies = sharedPreferences?.getString("loginCookies", "").toString()
        binding.apply {
            backButton.setOnClickListener { finish() }

            instaAce.text = userName
            instaFullName.text = resources.getString(R.string.full_name).plus(": $fullName")
            selectAll.text = resources.getString(R.string.select_all).plus(" 0")

            userAllPosts.layoutManager =
                GridLayoutManager(this@ViewProfileActivity, 3)
            userAllPosts.adapter = userPostAdapter
            userAllPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    if (dy <= 0) return

                    val layoutManager = rv.layoutManager as GridLayoutManager
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val totalItems = userPostAdapter.itemCount
                    val visibleThreshold = 4

                    if (lastVisible + visibleThreshold >= totalItems) {
                        viewModel.loadMorePosts(userId, cookies)
                    }
                }
            })

            viewModel.allUserPosts.observe(this@ViewProfileActivity) { mediaItems ->
                mediaItems.onEach {
                    println("allUserPosts: $it")
                }
                val newItems = mediaItems.drop(userPostAdapter.itemCount)
                userPostAdapter.submitList(newItems)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMorePosts(userId, cookies)

        viewModel.isLoading.observe(this) { loading ->
            if (loading) binding.loading.visible() else binding.loading.gone()
        }
    }
}