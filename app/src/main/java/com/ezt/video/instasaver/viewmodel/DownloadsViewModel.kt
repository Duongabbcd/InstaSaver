package com.ezt.video.instasaver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.remote.repository.InstagramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(private val instagramRepository: InstagramRepository) : ViewModel(){
    val allPosts = instagramRepository.getAllPost

    private var _postByUser = MutableLiveData<List<Post>>()
    val postByUser : LiveData<List<Post>> = _postByUser

    fun fetchPostsByUser(userName: String, isVideo: Boolean, isPhoto: Boolean, isCarousel: Boolean) {
        viewModelScope.launch {
            _postByUser.value = instagramRepository.fetchPostByUser(userName)

            if(isVideo) {
                _postByUser.value = instagramRepository.fetchVideoPostByUser(userName)
            }
            if(isPhoto) {
                _postByUser.value = instagramRepository.fetchPhotoPostByUser(userName)
            }

            if(isCarousel) {
                _postByUser.value = instagramRepository.fetchCarouselPostByUser(userName)
            }
        }
    }

//    fun fetchCarouselPosts() {
//        viewModelScope.launch {
//            _postByUser.value = instagramRepository.fetchCarouselPosts()
//        }
//    }
//    fun fetchPhotoPosts() {
//        viewModelScope.launch {
//            _postByUser.value = instagramRepository.fetchPhotoPosts()
//        }
//    }
//    fun fetchVideoPosts() {
//        viewModelScope.launch {
//            _postByUser.value = instagramRepository.fetchVideoPosts()
//        }
//    }
}