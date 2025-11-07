package com.ezt.video.instasaver.screen.home.fragment

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentHomeBinding
import com.ezt.video.instasaver.screen.home.adapter.DownloadViewAdapter
import com.ezt.video.instasaver.screen.login.InstagramLoginActivity
import com.ezt.video.instasaver.screen.login.RequestLoginActivity
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import kotlin.toString
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val viewModel: HomeViewModel by viewModels()

    private var cookies: String? = null
    private var downloadID = mutableListOf<Long>()
    private var size: Int = 0
    private var load: Boolean = false
    private lateinit var navigation: DownloadNavigation
    private lateinit var downloadViewAdapter: DownloadViewAdapter

    private val onCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != -1L && downloadID.contains(id)) {
                downloadID.remove(id)

                if (downloadID.isEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    val holder = binding.downloadView.findViewHolderForAdapterPosition(0)
                            as? DownloadViewAdapter.DownloadViewHolder
                    holder?.loadingViewStub?.visibility = View.GONE
                    holder?.layout?.isClickable = true
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = activity?.getSharedPreferences("Cookies", 0)
        cookies = sharedPreferences?.getString("loginCookies", null)
        setOnClickListeners()
        binding.downloadView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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

        binding.dpButton.setOnClickListener {
            if (cookies == null) {
                startActivity(Intent(context, RequestLoginActivity::class.java))
            } else {
                Navigation.findNavController(it)
                    .navigate(HomeFragmentDirections.actionHomeToDPViewerFragment(cookies ?: ""))
            }

        }

        binding.downloadButton.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            val postLink = binding.editText.text.toString()
            if (isInstagramLink(postLink)) {
                download(postLink)
                hideKeyBoard(binding.downloadButton)
            } else {
                binding.editText.text.clear()
                Toast.makeText(ctx, "Invalid Link!", Toast.LENGTH_SHORT).show()
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

    private fun checkClipboard() {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == true && clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val link = item?.text.toString()

            if (isInstagramLink(link)) {
                binding.editText.setText(link)
                download(link)
            }
        }
    }

    private fun download(link: String) {
        binding.progressBar.visibility = View.VISIBLE
        if (cookies == null) {
            startActivity(Intent(context, RequestLoginActivity::class.java))
        } else {
            viewModel.downloadPost(link, cookies!!)
        }
        binding.editText.text.clear()
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
        val isPostLink = link.contains("/p/") || link.contains("/tv/") || link.contains("/reel/")
        return isInstagramLink && isPostLink
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigation = context as DownloadNavigation
    }


    override fun onStop() {
        super.onStop()
        // Always unregister
//        requireContext().unregisterReceiver(onCompleteReceiver)
    }


    override fun onStart() {
        super.onStart()
        // Register with requireContext() to ensure it's non-null
//        requireContext().registerReceiver(
//            onCompleteReceiver,
//            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//        )
    }

    override fun onResume() {
        super.onResume()
        observeData(context)

    }

    private fun observeData(context: Context?) {
        viewModel.allPosts.observe(viewLifecycleOwner) {
            binding.downloadView.adapter = DownloadViewAdapter(load, it)
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

    interface DownloadNavigation {
        fun navigateToDownload()
    }

}