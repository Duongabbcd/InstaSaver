package com.ezt.video.instasaver.screen.home.download

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentDownloadsBinding
import com.ezt.video.instasaver.screen.home.download.adapter.DownloadViewAdapter2
import com.ezt.video.instasaver.screen.view.post.ViewPostActivity
import com.ezt.video.instasaver.viewmodel.DownloadsViewModel
import com.ezt.video.instasaver.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadsFragment :
    BaseFragment<FragmentDownloadsBinding>(FragmentDownloadsBinding::inflate) {
    private val downloadViewModel: DownloadsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var downloadViewAdapter2: DownloadViewAdapter2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = context ?: return
        val layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = true
        binding.downloadView.layoutManager = layoutManager
        downloadViewAdapter2 = DownloadViewAdapter2()
        binding.downloadView.adapter = downloadViewAdapter2
    }

    override fun onResume() {
        super.onResume()

        downloadViewModel.allPosts.observe(viewLifecycleOwner) { data ->
            println("FragmentDownloadsBinding: $data")
            downloadViewAdapter2.submitList(data)

            binding.faqButton.setOnClickListener {
                withSafeContext {
                    val ctx = context ?: return@setOnClickListener
                    val alertDialog = AlertDialog.Builder(ctx)
                        .setTitle("Delete your media files")
                        .setMessage("Do you want to clear all the IG files you downloaded?")
                        .setPositiveButton("Delete all") { dialogInterface, _ ->
                            data.onEach {
                                homeViewModel.deleteCarousel(it.link ?: "")
                            }
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                    alertDialog.show()
                }

            }
        }
    }
}