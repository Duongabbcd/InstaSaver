package com.ezt.video.instasaver.screen.home.download

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentDownloadsBinding
import com.ezt.video.instasaver.screen.home.download.adapter.DownloadViewAdapter2
import com.ezt.video.instasaver.viewmodel.DownloadsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadsFragment : BaseFragment<FragmentDownloadsBinding>(FragmentDownloadsBinding::inflate){
    private val downloadViewModel : DownloadsViewModel by viewModels()
    private lateinit var downloadViewAdapter2: DownloadViewAdapter2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.downloadView.adapter = DownloadViewAdapter2()
        val ctx = context ?: return
        val layoutManager= LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd=true
        binding.downloadView.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()

        downloadViewModel.allPosts.observe(viewLifecycleOwner) {
            downloadViewAdapter2.submitList(it)
        }
    }
}