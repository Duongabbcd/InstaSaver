package com.ezt.video.instasaver.screen.home.dpviewer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentDpViewerBinding
import com.ezt.video.instasaver.screen.home.dpviewer.adapter.DPRecentViewAdapter
import com.ezt.video.instasaver.screen.home.dpviewer.adapter.StorySearchViewAdapter
import com.ezt.video.instasaver.screen.home.story.StoryFragmentArgs
import com.ezt.video.instasaver.screen.view.ViewDPActivity
import com.ezt.video.instasaver.viewmodel.DPViewerViewModel
import com.google.android.material.internal.ViewUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import kotlin.text.clear

@AndroidEntryPoint
class DPViewerFragment  : BaseFragment<FragmentDpViewerBinding>(FragmentDpViewerBinding::inflate){
    private val dpViewerViewModel: DPViewerViewModel by viewModels()
    private lateinit var adapter: StorySearchViewAdapter
    private lateinit var cookies: String


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: StoryFragmentArgs by navArgs()
        cookies= args.cookie
        dpViewerViewModel.getRecentSearches()

        observeData()
        setOnClickListeners()

        binding.apply {
            recentView.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            editText.doOnTextChanged { text, _, _, count ->
                if(count>0){
                    dpViewerViewModel.searchUser(text.toString(),cookies)
                    binding.progressBar.visibility=View.VISIBLE
                    binding.searching.visibility=View.VISIBLE
                    binding.cancelButton.visibility=View.VISIBLE
                }else{
                    binding.cancelButton.visibility=View.GONE
                }
            }
        }
    }

    private fun setOnClickListeners() {
        binding.fetchButton.setOnClickListener{
            binding.progressBar.visibility=View.VISIBLE
            hideKeyBoard(binding.fetchButton)
            binding.editText.text.clear()
        }

        binding.backButton.setOnClickListener{
            NavHostFragment.findNavController(this@DPViewerFragment).navigateUp()
        }

        binding.cancelButton.setOnClickListener{
            binding.editText.text.clear()
            adapter.clearData()
            hideKeyBoard(binding.cancelButton)
        }
    }

    private fun observeData() {
        dpViewerViewModel.searchResult.observe(viewLifecycleOwner){
            binding.downloadView.layoutManager=LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = StorySearchViewAdapter(it){ user->
                val intent= Intent(context, ViewDPActivity::class.java)
                intent.putExtra("username",user.username)
                intent.putExtra("cookies",cookies)
                intent.putExtra("userId",user.pk)
                context?.startActivity(intent)
                dpViewerViewModel.insertRecent(user)
                adapter.clearData()
            }

            binding.downloadView.adapter=adapter
            binding.searching.visibility=View.GONE
            binding.progressBar.visibility=View.GONE
            if(it.isEmpty()){
                Toast.makeText(context,"No Account Found!", Toast.LENGTH_SHORT).show()
            }
        }

        dpViewerViewModel.recents.observe(viewLifecycleOwner){
            binding.recentView.adapter= DPRecentViewAdapter(it,cookies)
        }
    }


}