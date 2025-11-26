package com.ezt.video.instasaver.screen.home.story

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentStoryBinding
import com.ezt.video.instasaver.model.ReelTray
import com.ezt.video.instasaver.screen.download.DownloadStoryActivity
import com.ezt.video.instasaver.screen.home.dpviewer.adapter.StorySearchViewAdapter
import com.ezt.video.instasaver.screen.home.story.adapter.RecentViewAdapter
import com.ezt.video.instasaver.screen.home.story.adapter.ReelTrayViewAdapter
import com.ezt.video.instasaver.utils.Common.gone
import com.ezt.video.instasaver.utils.Common.visible
import com.ezt.video.instasaver.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoryFragment : BaseFragment<FragmentStoryBinding>(FragmentStoryBinding::inflate) {
    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var cookies: String
    private lateinit var reelTrays: MutableList<ReelTray>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: StoryFragmentArgs by navArgs()
        cookies = args.cookie
        storyViewModel.fetchReelTray(cookies)

        observeData()
        setOnClickListeners()
        binding.apply {
            recentView.setOnClickListener {
                hideKeyBoard(recentView)
            }
            downloadView.setOnClickListener {
                hideKeyBoard(downloadView)
            }

            loading.visible()
            recentView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            editText.doOnTextChanged { text, _, _, count ->
                if (count > 0) {
                    storyViewModel.getRecentSearches()
                    storyViewModel.searchUser(text.toString(), cookies)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.searching.visibility = View.VISIBLE
                    binding.cancelButton.visibility = View.VISIBLE
                } else {
                    binding.cancelButton.visibility = View.GONE
                }
            }
        }
    }

    private fun observeData() {
        storyViewModel.searchResult.observe(viewLifecycleOwner) {
            binding.downloadView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.downloadView.adapter = StorySearchViewAdapter(it) { user ->
                val intent = Intent(context, DownloadStoryActivity::class.java)
                intent.putExtra("username", user.username)
                intent.putExtra("full_name", user.full_name)
                intent.putExtra("profilePicUrl", user.profile_pic_url)
                intent.putExtra("userId", user.pk)
                intent.putExtra("cookie", cookies)
                context?.startActivity(intent)
                storyViewModel.insertRecent(user)
            }
            binding.searching.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            if (it.isEmpty()) {
                Toast.makeText(context, "No Account Found!", Toast.LENGTH_SHORT).show()
            }
        }

        storyViewModel.recents.observe(viewLifecycleOwner) {
            binding.recentView.adapter = RecentViewAdapter( it, cookies)
        }

        storyViewModel.reelTray.observe(viewLifecycleOwner) {
            reelTrays = it.toMutableList()
            setReelTrayRecyclerView()
        }

    }

    private fun setOnClickListeners() {
        binding.fetchButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            hideKeyBoard(binding.fetchButton)
            binding.editText.text.clear()
        }

        binding.backButton.setOnClickListener {
            NavHostFragment.findNavController(this@StoryFragment).navigateUp()
        }

        binding.cancelButton.setOnClickListener {
            binding.editText.text.clear()
            setReelTrayRecyclerView()
        }
    }


    private fun setReelTrayRecyclerView() {
        val recyclerView: RecyclerView = binding.downloadView
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = ReelTrayViewAdapter(reelTrays, cookies)
        binding.loading.gone()
    }

    override fun onResume() {
        super.onResume()
        storyViewModel.getRecentSearches()
    }
}