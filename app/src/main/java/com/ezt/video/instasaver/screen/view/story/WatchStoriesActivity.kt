package com.ezt.video.instasaver.screen.view.story

import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.customview.StoriesProgressView
import com.ezt.video.instasaver.databinding.ActivityWatchStoriesBinding
import com.ezt.video.instasaver.model.ReelMedia
import com.ezt.video.instasaver.model.ReelTray
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.model.Story
import com.ezt.video.instasaver.viewmodel.StoryViewModel
import com.squareup.picasso.Picasso
import com.squareup.picasso.Callback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WatchStoriesActivity :
    BaseActivity<ActivityWatchStoriesBinding>(ActivityWatchStoriesBinding::inflate),
    StoriesProgressView.StoriesListener {
    private val viewModel: StoryViewModel by viewModels()
    private val reelMediaList: MutableList<ReelMedia> = mutableListOf()
    private var counter = 0
    private var imageUrl = ""
    private lateinit var loadingDialog: Dialog
    private var videoUrl: String? = null
    private lateinit var reelTray: ReelTray
    private lateinit var onDownloadComplete: BroadcastReceiver
    private var downloadID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reelId = intent.getLongExtra("reelId", 0L)
        val cookie: String = intent.getStringExtra("cookie") ?: ""
        viewModel.fetchReelMedia(reelId, cookie)
        binding.stories.setStoriesListener(this@WatchStoriesActivity)
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.download_loading_dialog)
        loadingDialog.setCancelable(false)
        observeData()
        setOnClickListener()
        onDownloadComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadID) {
                    reelTray.items[counter].downloaded = true
                    loadingDialog.dismiss()
                    binding.stories.resume()
                    Toast.makeText(context, resources.getString(R.string.download_complete), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun observeData() {
        viewModel.reelMedia.observe(this) {
            if (it != null) {
                reelTray = it
                val user = it.user
                Picasso.get().load(user.profile_pic_url).into(binding.profilePicView)
                binding.usernameView.text = user.username
                val items = it.items
                for (item in items) {
                    val uri: String
                    val mediaType = if (item.media_type == 1) {
                        uri = item.image_versions2.candidates[0].url
                        true
                    } else {
                        uri = item.video_versions?.get(0)?.url ?: ""
                        false
                    }
                    reelMediaList.add(ReelMedia(uri, mediaType))
                }
                setStory()
            }
        }


        viewModel.downloadId.observe(this) {
            downloadID = it
        }

    }

    private fun setOnClickListener() {
        binding.profilePicView.setOnClickListener {
            binding.stories.pause()
        }

        binding.usernameView.setOnClickListener {
            binding.stories.resume()
        }

        binding.downloadButton.setOnClickListener {
            binding.stories.pause()
            loadingDialog.show()
            val item = reelTray.items[counter]
            if (item.downloaded) {
                Toast.makeText(this, resources.getString(R.string.already_downloaded), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.downloadStory(
                    Story(
                        item.code,
                        item.media_type,
                        item.image_versions2.candidates[0].url,
                        videoUrl,
                        reelTray.user.username,
                        reelTray.user.profile_pic_url,
                        isSelected = false,
                        downloaded = false,
                        name = null
                    )
                )
            }
        }

    }

    private fun setStory() {
        val size = reelMediaList.size
        val storiesProgressView = binding.stories
        storiesProgressView.setStoriesCount(size)
        storiesProgressView.setStoryDuration(7000L)
        binding.reverse.setOnClickListener {
            storiesProgressView.reverse()
        }
        binding.skip.setOnClickListener {
            storiesProgressView.skip()
        }
        storiesProgressView.startStories()
        changeMedia(reelMediaList[counter], 0)

    }

    private fun changeMedia(reelMedia: ReelMedia, index: Int) {
        binding.progressBar.visibility = View.VISIBLE
        if (reelMedia.isImage) {
            binding.videoView.visibility = View.GONE
            binding.imageView.visibility = View.VISIBLE
            imageUrl = reelMedia.uri
            Picasso.get().load(reelMedia.uri).into(binding.imageView, object : Callback {
                override fun onSuccess() {
                    binding.progressBar.visibility = View.GONE
                    binding.stories.resume()

                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                }

            })
        } else {
            binding.imageView.visibility = View.GONE
            videoUrl = reelMedia.uri
            val videoUri: Uri? = Uri.parse(reelMedia.uri)
            setVideo(videoUri, index)
        }
    }

    private fun setVideo(videoUri: Uri?, index: Int) {
        if (videoUri != null) {
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.visibility = View.VISIBLE
            binding.videoView.setOnPreparedListener {
                val duration: Long = it.duration.toLong()
                binding.stories.progressBars[index].animation.duration = duration
                binding.videoView.start()
                binding.progressBar.visibility = View.GONE
                binding.stories.resume()
            }
        }
    }

    override fun onNext() {
        counter += 1
        changeMedia(reelMediaList[counter], counter)
    }

    override fun onPrev() {
        if ((counter - 1) < 0) return
        changeMedia(reelMediaList[--counter], counter)
    }

    override fun onComplete() {
        finish()
    }

    override fun onDestroy() {
        binding.stories.destroy()
        super.onDestroy()
        if (this::onDownloadComplete.isInitialized) {
            unregisterReceiver(onDownloadComplete)
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(onDownloadComplete, intentFilter, RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                this@WatchStoriesActivity,
                onDownloadComplete,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )

        }
    }
    
}