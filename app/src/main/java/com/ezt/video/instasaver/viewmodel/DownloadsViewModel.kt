package com.ezt.video.instasaver.viewmodel

import androidx.lifecycle.ViewModel
import com.ezt.video.instasaver.remote.repository.InstagramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(private val instagramRepository: InstagramRepository) : ViewModel(){
    val allPosts = instagramRepository.getAllPost
}