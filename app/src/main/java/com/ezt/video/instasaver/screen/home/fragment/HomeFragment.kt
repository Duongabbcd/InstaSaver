package com.ezt.video.instasaver.screen.home.fragment

import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentHomeBinding
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.screen.home.adapter.DownloadViewAdapter
import com.ezt.video.instasaver.screen.login.InstagramLoginActivity
import com.ezt.video.instasaver.screen.login.RequestLoginActivity
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import kotlin.toString
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.ezt.video.instasaver.utils.Common.gone
import com.ezt.video.instasaver.utils.Common.visible
import com.ezt.video.instasaver.utils.Constants.AVATAR_FOLDER_NAME
import com.ezt.video.instasaver.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val viewModel: HomeViewModel by viewModels()
    private val storyViewModel: StoryViewModel by viewModels()

    private var cookies: String? = null
    private var downloadID = mutableListOf<Long>()
    private var size: Int = 0
    private lateinit var navigation: DownloadNavigation

    //    private lateinit var downloadViewAdapter: DownloadViewAdapter
    private lateinit var loadingDialog: Dialog

    private val onCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            println("onCompleteReceiver: $downloadID and $id")
            val holder = binding.downloadView.findViewHolderForAdapterPosition(0)
                    as? DownloadViewAdapter.DownloadViewHolder
            holder?.loadingViewStub?.visible()

            if (id != -1L && downloadID.contains(id)) {
                downloadID.remove(id)
                viewModel.allPosts.observe(viewLifecycleOwner) {
                    load = false
                    binding.downloadView.adapter = DownloadViewAdapter(load, it)
                    size = it.size
                }

                loadingDialog.dismiss()

                binding.progressBar.visibility = View.GONE
                holder?.layout?.isClickable = true
            }
            holder?.loadingViewStub?.gone()

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editText.text.clear()
        val sharedPreferences = activity?.getSharedPreferences("Cookies", 0)
        cookies = sharedPreferences?.getString("loginCookies", null)
        setOnClickListeners()
        binding.downloadView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        downloadViewAdapter = DownloadViewAdapter()
//        binding.downloadView.adapter = downloadViewAdapter
        view.viewTreeObserver?.addOnWindowFocusChangeListener {
            if (cookies != null) {
                checkClipboard()
            }
        }
    }

    private fun setOnClickListeners() {
        binding.faqButton.setOnClickListener {
            startActivity(Intent(context, InstagramLoginActivity::class.java))
        }

        binding.storyButton.setOnClickListener {
            if (cookies == null) {
                startActivity(Intent(context, RequestLoginActivity::class.java))
            } else {
                Navigation.findNavController(it)
                    .navigate(HomeFragmentDirections.actionHomeToStoryFragment(cookies ?: ""))
            }
        }

        binding.textView11.setOnClickListener {
            if (cookies == null) {
                startActivity(Intent(context, RequestLoginActivity::class.java))
            } else {
                Navigation.findNavController(it)
                    .navigate(HomeFragmentDirections.actionHomeToStoryFragment(cookies ?: ""))
            }
        }

        binding.dpButton.setOnClickListener {
            if (cookies == null) {
                startActivity(Intent(context, RequestLoginActivity::class.java))
            } else {
                Navigation.findNavController(it)
                    .navigate(HomeFragmentDirections.actionHomeToDPViewerFragment(cookies ?: ""))
            }

        }

         binding.textView12.setOnClickListener {
            if (cookies == null) {
                startActivity(Intent(context, RequestLoginActivity::class.java))
            } else {
                Navigation.findNavController(it)
                    .navigate(HomeFragmentDirections.actionHomeToDPViewerFragment(cookies ?: ""))
            }

        }

        binding.profileButton.setOnClickListener  {
            if (cookies == null) {
                startActivity(Intent(context, RequestLoginActivity::class.java))
            } else {
                Navigation.findNavController(it)
                    .navigate(HomeFragmentDirections.actionHomeToProfileFragment(cookies ?: ""))
            }
        }

        binding.textView13.setOnClickListener  {
                if (cookies == null) {
                    startActivity(Intent(context, RequestLoginActivity::class.java))
                } else {
                    Navigation.findNavController(it)
                        .navigate(HomeFragmentDirections.actionHomeToProfileFragment(cookies ?: ""))
                }
        }


        binding.downloadButton.setOnClickListener {
            withSafeContext { ctx ->
                loadingDialog = Dialog(ctx)
                loadingDialog.setContentView(R.layout.download_loading_dialog)
                loadingDialog.setCancelable(false)
                loadingDialog.show()

                Toast.makeText(ctx, resources.getString(R.string.please_wait), Toast.LENGTH_SHORT)
                    .show()
                val postLink = binding.editText.text.toString()
                if (isInstagramLink(postLink)) {
                    download(postLink)
                    hideKeyBoard(binding.downloadButton)
                } else {
                    Toast.makeText(ctx, resources.getString(R.string.invalid_link), Toast.LENGTH_SHORT)
                        .show()
                }
                binding.editText.text.clear()
            }

        }

        binding.instagramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        }

        binding.viewAll.setOnClickListener {
            navigation.navigateToDownload()
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

    private fun checkClipboard() {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == true && clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val link = item?.text.toString()
            println("checkClipboard: $link")
            if (isInstagramLink(link)) {
                binding.editText.setText(link)
            }
        }
    }

    private fun download(link: String) {
        binding.progressBar.visibility = View.VISIBLE
        if (cookies == null) {
            startActivity(Intent(context, RequestLoginActivity::class.java))
        } else {
            if (link.contains("stories")) {
                val searchUser = getStoryUsername(link) ?: ""
                cookies?.let { cookie ->
                    storyViewModel.searchUser(searchUser, cookie)
                    storyViewModel.searchResult.observe(viewLifecycleOwner) {
                        val result = it.first()
                        storyViewModel.fetchStory(result.user.pk, cookie)
                    }
                    storyViewModel.stories.observe(viewLifecycleOwner) {
                        it.onEach { url ->
                            storyViewModel.downloadStory(url)
                        }
                    }
                }

            } else {
                viewModel.downloadPost(link, cookies!!)
            }

        }

        binding.editText.text.clear()
        binding.progressBar.visibility = View.GONE
        load = true
    }

    private fun isInstagramLink(link: String): Boolean {
        var isInstagramLink = false
        val containsInstagram = link.contains("instagram.com/")
        if (link.length > 26 && containsInstagram) {
            val index = link.indexOf("instagram.com")
            val totalIndex = index + 13
            val url = link.substring(totalIndex, link.length)
            isInstagramLink = url.length >= 14
        }
        val isPostLink =
            link.contains("/p/") || link.contains("/tv/") || link.contains("/reel/") || link.contains(
                "/stories/"
            )
        return isInstagramLink && isPostLink
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigation = context as DownloadNavigation
    }


    override fun onStop() {
        super.onStop()
        // Always unregister
        requireContext().unregisterReceiver(onCompleteReceiver)
    }


    override fun onStart() {
        super.onStart()
        // Register with requireContext() to ensure it's non-null
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(
                onCompleteReceiver, intentFilter, Context.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                ctx,
                onCompleteReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    override fun onResume() {
        super.onResume()
        observeData(context)

    }

    private fun observeData(context: Context?) {
        viewModel.allPosts.observe(viewLifecycleOwner) {
            load = false
            binding.downloadView.adapter = DownloadViewAdapter(load, it.take(5))
            size = it.size
        }

        viewModel.postExits.observe(viewLifecycleOwner) {
            if (it) {
                binding.editText.text.clear()
                viewModel.postExits.value = false
                binding.progressBar.visibility = View.GONE
                load = false
            }
        }

        viewModel.downloadID.observe(viewLifecycleOwner) {
            if (it.isNotEmpty() && it[0] == 3L) {
                binding.progressBar.visibility = View.GONE
                startActivity(Intent(context, RequestLoginActivity::class.java))
                Toast.makeText(context, "JsonEncodingException", Toast.LENGTH_SHORT).show()
            } else {
                downloadID = it
            }

        }

    }

    companion object {
        var load: Boolean = false

        fun getStoryUsername(url: String): String? {
            val regex = Regex("https?://(www\\.)?instagram\\.com/stories/([^/]+)/")
            return regex.find(url)?.groupValues?.get(2)
        }
    }

    interface DownloadNavigation {
        fun navigateToDownload()
    }

}