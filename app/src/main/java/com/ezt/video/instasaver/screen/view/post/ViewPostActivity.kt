package com.ezt.video.instasaver.screen.view.post

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.app.RecoverableSecurityException
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuInflater
import android.view.View
import android.view.ViewStub
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.MyApplication
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityViewPostBinding
import com.ezt.video.instasaver.local.Carousel
import com.ezt.video.instasaver.model.CarouselMedia
import com.ezt.video.instasaver.screen.home.fragment.HomeFragment
import com.ezt.video.instasaver.screen.view.post.adapter.CarouselViewPagerAdapter
import com.ezt.video.instasaver.utils.Constants
import com.ezt.video.instasaver.utils.Constants.IMAGE_FOLDER_NAME
import com.ezt.video.instasaver.utils.Constants.STORY_FOLDER_NAME
import com.ezt.video.instasaver.utils.Constants.VIDEO_FOLDER_NAME
import com.ezt.video.instasaver.utils.FileUtils.getDefaultVideoPath
import com.ezt.video.instasaver.utils.InvalidLinkException
import com.ezt.video.instasaver.utils.sdk29AndUp
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.regex.Pattern
import androidx.core.net.toUri

@AndroidEntryPoint
class ViewPostActivity : BaseActivity<ActivityViewPostBinding>(ActivityViewPostBinding::inflate) {
    private var viewMore = true
    private lateinit var uri: Uri
    private val homeViewModel: HomeViewModel by viewModels()
    private var captionDialog: Dialog? = null
    private lateinit var mediaList: List<CarouselMedia>
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var deletedImageUri: Uri? = null
    private var notDeleted = true
    private var isPost: Boolean = false

    private val post by lazy {
        intent.extras
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HomeFragment.load = false
        val name: String = post?.getString("name") ?: "lol"
        val mediaType = post?.getInt("media_type", 1)
        val caption = post?.getString("caption") ?: ""
        val username = post?.getString("username") ?: ""
        val defaultProfilePic = post?.getString("profilePicture")
        val profilePicture = File(Constants.AVATAR_FOLDER_NAME, username.plus(".jpg"))
        println("DownloadViewHolder file path 1 $username $profilePicture")

        if (profilePicture.exists()) {
            Glide.with(this@ViewPostActivity).load(Uri.fromFile(profilePicture))
                .into(binding.profilePicView)
        } else {
            Glide.with(this@ViewPostActivity).load(defaultProfilePic)
                .into(binding.profilePicView)
        }


        val instagramURL = post?.getString("instagram_url") ?: Constants.INSTAGRAM_HOMEPAGE_LINK
        isPost = post?.getBoolean("isStory") ?: false
        var isImage = true
        var isCarousel = false

        binding.shareToIG.isVisible = !isPost
        when (mediaType) {
            1 -> {
                binding.videoView.visibility = View.GONE
                binding.imageView.visibility = View.VISIBLE
                val photos = loadPhoto(name)
                if (photos != null) {
                    binding.imageView.setImageURI(photos)
                    uri = photos
                }
            }

            2 -> {
                binding.videoView.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                setVideo(loadVideo(name))
                isImage = false
            }

            else -> {
                loadCarousel(instagramURL)
                binding.imageView.visibility = View.GONE
                binding.videoView.visibility = View.GONE
                isCarousel = true

            }
        }

        binding.captionView.text = caption
        binding.usernameView.text = username
        setOnClickListeners(isImage, instagramURL, caption, username ?: "", isCarousel)
        initIntentSenderLauncher()
    }

    private fun loadCarousel(link: String) {
        val viewStub = binding.viewStub.inflate()
        val viewPager: ViewPager2 = viewStub.findViewById(R.id.viewpager)
        homeViewModel.getCarousel(link).observe(this) {
            if (it != null && notDeleted) {
                mediaList = fetchCarousel(it)
                viewPager.adapter = CarouselViewPagerAdapter(this@ViewPostActivity, mediaList)
                val tabLayout: TabLayout = viewStub.findViewById(R.id.tabLayout)
                TabLayoutMediator(tabLayout, viewPager) { _: TabLayout.Tab, _: Int -> }.attach()
                uri = mediaList[0].uri!!
            }
        }
    }

    private fun initIntentSenderLauncher() {
        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        deleteFile(deletedImageUri ?: return@registerForActivityResult)
                    }
                } else {
                    throw InvalidLinkException("Unable to delete post")
                }
            }
    }


    private fun fetchCarousel(carousels: List<Carousel>): List<CarouselMedia> {
        val medias = mutableListOf<CarouselMedia>()
        for (carousel in carousels) {
            val title = carousel.title ?: "lol"
            if (carousel.media_type == 1) {
                val uri = loadPhoto(title)
                medias.add(CarouselMedia(uri, 1))
            } else {
                val uri = loadVideo(title)
                medias.add(CarouselMedia(uri, 2))
            }
        }
        return medias
    }


    private fun setOnClickListeners(
        isImage: Boolean,
        instagramUrl: String,
        caption: String,
        username: String,
        isCarousel: Boolean
    ) {
        val viewMore2 = "View More"
        val viewLess = "View Less"
        binding.viewMore.setOnClickListener {
            if (viewMore) {
                binding.captionView.maxLines = 30
                binding.viewMore.text = viewLess
            } else {
                binding.captionView.maxLines = 3
                binding.viewMore.text = viewMore2
            }
            viewMore = !viewMore
        }
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.repost.setOnClickListener {
            repost(caption, username, isImage)
        }

        binding.caption.setOnClickListener {
            launchCaptionDialog(caption, username)
        }

        binding.shareToIG.setOnClickListener {
            val uri = Uri.parse(instagramUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage(Constants.INSTAGRAM)
            startActivity(intent)
        }

        binding.delete.setOnClickListener {
            try {
                deletePost(instagramUrl, isCarousel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.menu.setOnClickListener {
            val popup = PopupMenu(this, it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.view_post_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.openInInstagram -> {
                        val uri = Uri.parse(instagramUrl)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage(Constants.INSTAGRAM)
                        startActivity(intent)
                    }

                    R.id.delete -> {
                        deletePost(instagramUrl, isCarousel)
                    }
                }
                true
            }
            popup.show()
        }

        binding.share.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val intentType = if (isImage) {
                "image/jpeg"
            } else {
                "video/mp4"
            }
            intent.type = intentType
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(intent, "Share"))
        }

    }


    private fun loadPhoto(name: String): Uri? {
        // Your app's private folder: /Android/data/<package>/files/.VideoDownloader/InstaPhoto (or InstaVideo)
        val photoDir = File(
            if (isPost) STORY_FOLDER_NAME else IMAGE_FOLDER_NAME

        )

        val photoFile = File(photoDir, name)

        return if (photoFile.exists()) {
            Uri.fromFile(photoFile)
        } else {
//            AlertDialog.Builder(this@ViewPostActivity)
//                .setTitle("Photo Not Found")
//                .setMessage("The photo may have been deleted, renamed, or moved.")
//                .setNegativeButton("Close") { d, _ -> d.dismiss() }
//                .show()
            post?.getString("imageUrl")?.toUri()
        }
    }


    private fun loadVideo(name: String): Uri? {
        println("loadVideo: $name")
        val videoDir = File(
            if (isPost) STORY_FOLDER_NAME else VIDEO_FOLDER_NAME
        )

        val videoFile = File(videoDir, name)

        return if (videoFile.exists()) {
            Uri.fromFile(videoFile)
        } else {
//            AlertDialog.Builder(this@ViewPostActivity)
//                .setTitle("Video Not Found")
//                .setMessage("The video may have been deleted, renamed, or moved.")
//                .setNegativeButton("Close") { d, _ -> d.dismiss() }
//                .show()
            post?.getString("videoUrl")?.toUri()
        }
    }


    private fun repost(caption: String, username: String, isImage: Boolean) {
        val repostCaption = Constants.REPOST + "@" + username + " \n" + caption
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", repostCaption)
        clipboardManager.setPrimaryClip(clipData)
        val share = Intent(Intent.ACTION_SEND)
        share.type = if (isImage) "image/*" else "video/*"
        share.setPackage(Constants.INSTAGRAM)
        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share to"))
        Toast.makeText(this@ViewPostActivity, "Caption copied to clipboard", Toast.LENGTH_SHORT)
            .show()
    }

    private fun launchCaptionDialog(caption: String, username: String) {
        if (captionDialog == null) {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            captionDialog = Dialog(this@ViewPostActivity)
            captionDialog!!.setContentView(R.layout.caption_dialog_layout)
            captionDialog!!.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@ViewPostActivity,
                    R.drawable.rounded_corner_white_rectangle
                )
            )
            captionDialog!!.findViewById<TextView>(R.id.captionView).text = caption

            captionDialog!!.findViewById<TextView>(R.id.copyAll).setOnClickListener {
                val clipData = ClipData.newPlainText("text", caption)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this@ViewPostActivity, "Caption copied!", Toast.LENGTH_SHORT).show()
            }

            captionDialog!!.findViewById<TextView>(R.id.copyHashtags).setOnClickListener {
                val loadingViewStub = captionDialog!!.findViewById<ViewStub>(R.id.loadingViewStub)
                loadingViewStub.inflate()
                val stringBuilder = StringBuilder()
                val pattern = Pattern.compile("#(\\w+)")
                val match = pattern.matcher(caption)
                while (match.find()) {
                    stringBuilder.append("#" + match.group(1) + " ")
                }
                val clipData = ClipData.newPlainText("text", stringBuilder)
                clipboardManager.setPrimaryClip(clipData)
                loadingViewStub.visibility = View.GONE
                Toast.makeText(this@ViewPostActivity, "Hashtags copied!", Toast.LENGTH_SHORT).show()
            }

            captionDialog!!.findViewById<TextView>(R.id.copyUsername).setOnClickListener {
                val clipData = ClipData.newPlainText("text", username)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this@ViewPostActivity, "Username copied!", Toast.LENGTH_SHORT).show()
            }
        }
        captionDialog!!.show()
    }

    private fun setVideo(videoUri: Uri?) {
        if (videoUri != null) {
            binding.videoView.setMediaController(MediaController(this@ViewPostActivity))
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()
            uri = videoUri
            binding.videoView.setOnCompletionListener {
                it.seekTo(0)
                it.start()
            }
        }
    }

    private fun deleteFile(uri: Uri) {
        if (uri.scheme == "file") {
            val file = File(uri.path!!)
            val deleted = file.delete()
            println("Deleted FILE: ${file.absolutePath} → $deleted")
            return
        }

        // Fallback for content:// URIs (MediaStore)
        try {
            contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            // For Android Q+ recoverable deletion
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(contentResolver, listOf(uri)).intentSender
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    (e as? RecoverableSecurityException)
                        ?.userAction
                        ?.actionIntent
                        ?.intentSender
                }

                else -> null
            }

            intentSender?.let {
                intentSenderLauncher.launch(IntentSenderRequest.Builder(it).build())
            }
        }
    }


    private fun deletePost(instagramUrl: String, isCarousel: Boolean) {
        notDeleted = false
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val primaryClip = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
        if (primaryClip == instagramUrl) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("text", ""))
        }
        homeViewModel.deletePost(instagramUrl)
        if (isCarousel) {
            for (media in mediaList) {
                homeViewModel.deleteCarousel(instagramUrl)
                val uri = media.uri
                if (uri != null) {
                    deletedImageUri = uri
                    deleteFile(uri)
                }
            }
        } else {
            deletedImageUri = uri
            deleteFile(uri)
        }
        Toast.makeText(this@ViewPostActivity, "Post Delete successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

}