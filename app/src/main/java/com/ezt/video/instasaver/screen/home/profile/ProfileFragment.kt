package com.ezt.video.instasaver.screen.home.profile

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
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentProfileBinding
import com.ezt.video.instasaver.screen.home.dpviewer.adapter.StorySearchViewAdapter
import com.ezt.video.instasaver.screen.home.story.StoryFragmentArgs
import com.ezt.video.instasaver.screen.home.story.adapter.RecentProfileAdapter
import com.ezt.video.instasaver.screen.home.story.adapter.RecentViewAdapter
import com.ezt.video.instasaver.screen.view.ViewDPActivity
import com.ezt.video.instasaver.utils.Utils.hideKeyBoard
import com.ezt.video.instasaver.viewmodel.DPViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {
    private val dpViewerViewModel: DPViewerViewModel by viewModels()
    private lateinit var cookies: String
    private lateinit var adapter: StorySearchViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: ProfileFragmentArgs by navArgs()
        cookies = args.cookie

        binding.apply {
            binding.backButton.setOnClickListener {
                NavHostFragment.findNavController(this@ProfileFragment).navigateUp()
            }

            dpViewerViewModel.profileRecent.observe(viewLifecycleOwner) {
                binding.recentView.adapter = RecentProfileAdapter( it, cookies)
            }

            binding.fetchButton.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                hideKeyBoard(binding.fetchButton)
                binding.editText.text.clear()
            }

            binding.cancelButton.setOnClickListener {
                binding.editText.text.clear()
                adapter.clearData()
                hideKeyBoard(binding.cancelButton)
            }


            recentView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            searchView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            editText.doOnTextChanged { text, _, _, count ->
                dpViewerViewModel.getRecentProfileSearches()
                if (count > 0) {
                    dpViewerViewModel.searchUser(text.toString(), cookies)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.searching.visibility = View.VISIBLE
                    binding.cancelButton.visibility = View.VISIBLE

                    binding.recentView.visibility = View.GONE
                    binding.searchView.visibility = View.VISIBLE
                    binding.textView.visibility = View.GONE
                } else {
                    binding.recentView.visibility = View.VISIBLE
                    binding.searchView.visibility = View.GONE
                    binding.textView.visibility = View.VISIBLE

                    binding.cancelButton.visibility = View.GONE
                }
            }

            searchView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    withSafeContext { ctx ->
                        ctx.hideKeyBoard(editText)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dx != 0 || dy != 0) {
                        withSafeContext { ctx ->
                            ctx.hideKeyBoard(editText)
                        }
                    }
                }
            })


        }
    }

    override fun onResume() {
        super.onResume()
        dpViewerViewModel.getRecentProfileSearches()

        dpViewerViewModel.searchResult.observe(viewLifecycleOwner) { searchResult ->
            searchResult.onEach {
                println("searchResult: $it")
            }
            adapter = StorySearchViewAdapter(searchResult) { user ->
                val intent = Intent(context, ViewProfileActivity::class.java)
                intent.putExtra("username", user.username)
                intent.putExtra("fullName", user.full_name)
                intent.putExtra("cookies", cookies)
                intent.putExtra("userId", user.pk)
                context?.startActivity(intent)
                dpViewerViewModel.insertProfileRecent(user)
            }

            binding.searchView.adapter = adapter
            binding.searching.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            if (searchResult.isEmpty()) {
                Toast.makeText(
                    context,
                    resources.getString(R.string.no_acc_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}