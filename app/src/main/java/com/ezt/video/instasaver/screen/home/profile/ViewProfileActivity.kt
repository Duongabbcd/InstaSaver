package com.ezt.video.instasaver.screen.home.profile

import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityViewProfileBinding
import com.ezt.video.instasaver.screen.home.profile.adapter.UserPostAdapter
import com.ezt.video.instasaver.screen.login.RequestLoginActivity
import com.ezt.video.instasaver.utils.Common.gone
import com.ezt.video.instasaver.utils.Common.visible
import com.ezt.video.instasaver.utils.Constants.AVATAR_FOLDER_NAME
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
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
            val size = userPostAdapter.getSelectedPosts().size
            binding.selectAll.text =
                resources.getString(R.string.select_all).plus(" $size")

            binding.selectAll.setOnClickListener {
                if (size < 1) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.select_none),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    executeDownloadFiles()
                }
            }
        }
    }

    private lateinit var loadingDialog: Dialog

    private fun executeDownloadFiles() {
        checkDuplicateAvatars()

        val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        try {
            clipboard.clearPrimaryClip()  // Preferred
        } catch (e: Exception) {
            e.printStackTrace()
            clipboard.setPrimaryClip(ClipData.newPlainText(null, null))
        }

        Toast.makeText(this, resources.getString(R.string.please_wait), Toast.LENGTH_SHORT)
            .show()
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.download_loading_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.show()
        userPostAdapter.getSelectedPosts().onEach { item ->
            val base = if (item.product_type == "clips")
                "https://www.instagram.com/reel/"
            else
                "https://www.instagram.com/p/"

            item.code?.let {
                println("getSelectedPosts: ${base.plus("$it/")}")
                viewModel.downloadPost("$base$it/", cookies) { output ->
                    if (output) {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.file_already_exist),
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.file_downloaded_successfully),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }

        }
    }

    private fun checkDuplicateAvatars() {
        val folder = File(AVATAR_FOLDER_NAME)
        val files = folder.listFiles() ?: return

        // Group by base name
        val groups = files.groupBy { file ->
            file.name.replace(Regex("-\\d+(?=\\.)"), "")
        }

        for ((baseName, fileGroup) in groups) {

            // Find original file (the one WITHOUT -1, -2, etc.)
            val original = fileGroup.find { !it.name.matches(Regex(".*-\\d+\\..+")) }

            if (original != null) {
                println("Keeping original: ${original.name}")

                // Delete all duplicates
                fileGroup.filter { it != original }.forEach { dup ->
                    val deleted = dup.delete()
                    println("Deleting duplicate: ${dup.name} → $deleted")
                }

            } else {
                // No clean original file exists → keep the smallest-numbered one
                val fallback = fileGroup.sortedBy { it.name }.first()
                println("Fallback keep: ${fallback.name}")

                fileGroup.filter { it != fallback }.forEach { dup ->
                    val deleted = dup.delete()
                    println("Deleting duplicate(FALLBACK): ${dup.name} → $deleted")
                }
            }
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

//            userAllPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(rv, dx, dy)
//                    if (dy <= 0) return
//
//                    val layoutManager = rv.layoutManager as GridLayoutManager
//                    val lastVisible = layoutManager.findLastVisibleItemPosition()
//                    val totalItems = userPostAdapter.itemCount
//                    val visibleThreshold = 4
//
//                    if (lastVisible + visibleThreshold >= totalItems) {
//                        viewModel.loadMorePosts(userId, cookies)
//                    }
//                }
//            })
//
//            viewModel.allUserPosts.observe(this@ViewProfileActivity) { mediaItems ->
//                userPostAdapter.submitList(mediaItems)
//            }

            viewModel.downloadID.observe(this@ViewProfileActivity) {
                if (it.isNotEmpty() && it[0] == 3L) {
                    startActivity(
                        Intent(
                            this@ViewProfileActivity,
                            RequestLoginActivity::class.java
                        )
                    )
                    Toast.makeText(
                        this@ViewProfileActivity,
                        "JsonEncodingException",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    downloadID = it
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.getAllPosts(userId, cookies).collectLatest { pagingData ->
                userPostAdapter.submitData(pagingData)
            }
        }

        userPostAdapter.addLoadStateListener { loadState ->
            binding.apply {
                // Show progressBar only when loading the initial page or appending next pages
                loading.isVisible = loadState.source.refresh is LoadState.Loading
                        || loadState.append is LoadState.Loading

                // Optionally handle error state
                val errorState = when {
                    loadState.source.refresh is LoadState.Error -> loadState.source.refresh as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    else -> null
                }

                errorState?.let {
                    Toast.makeText(
                        this@ViewProfileActivity,
                        "Error: ${it.error.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        viewModel.isLoading.observe(this) { loading ->
            if (loading) binding.loading.visible() else binding.loading.gone()
        }
    }


    override fun onStop() {
        super.onStop()
        // Always unregister
        this.unregisterReceiver(onCompleteReceiver)
    }


    override fun onStart() {
        super.onStart()
        // Register with requireContext() to ensure it's non-null
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(
                onCompleteReceiver, intentFilter, Context.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                this,
                onCompleteReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    private var downloadID = mutableListOf<Long>()
    private val onCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            println("onCompleteReceiver: $downloadID and $id")
            if (id != -1L && downloadID.contains(id)) {
                loadingDialog.dismiss()

                binding.selectAll.text = resources.getString(R.string.select_all).plus(" 0")
                userPostAdapter.clearSelectedPosts()

                userPostAdapter.notifyDataSetChanged()
            }

        }
    }
}