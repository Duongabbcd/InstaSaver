package com.ezt.video.instasaver.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.video.instasaver.MyApplication
import com.ezt.video.instasaver.local.Carousel
import com.ezt.video.instasaver.model.Items
import com.ezt.video.instasaver.model.MediaItem
import com.ezt.video.instasaver.remote.repository.InstagramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val instagramRepository: InstagramRepository) :
    ViewModel() {
    val allPosts = instagramRepository.getRecentDownload
    val postExits = MutableLiveData<Boolean>()
    var downloadID = MutableLiveData<MutableList<Long>>()
    var downloadId = MutableLiveData<Long>()

    val allUserPosts = MutableLiveData<MutableList<Items>>(mutableListOf())
    val isLoading = MutableLiveData(false)

    private val _stats = MutableLiveData<Triple<Int, Int, Int>>()
    val stats: LiveData<Triple<Int, Int, Int>> = _stats

    private suspend fun doesPostExits(url: String): Boolean = withContext(Dispatchers.IO) {
        instagramRepository.doesPostExits(url)
    }

    fun downloadPost(
        url: String,
        map: String,
        inputItems: Items? = null,
        onCheckPostExit: (Boolean) -> Unit
    ) {
        println("downloadIdList: $url")
        viewModelScope.launch(Dispatchers.IO) {
            var downloadIdList = mutableListOf<Long>()
            val postDoesNotExits = withContext(Dispatchers.IO) { !doesPostExits(url) }
            if (postDoesNotExits || url.isEmpty()) {
                downloadIdList = instagramRepository.fetchPost(url, map, inputItems)
                onCheckPostExit(false)
            } else {
                onCheckPostExit(true)
                postExits.postValue(true)
            }
            println("downloadIdList: $postDoesNotExits and $downloadIdList")
            downloadID.postValue(downloadIdList)
        }
    }

    fun downloadPost2(url: String, path: String, title: String) {
        Log.d("tagg", "going to download")
        viewModelScope.launch(Dispatchers.IO) {
            downloadId.postValue(instagramRepository.directDownload(url, path, title))
        }
    }

    fun getCarousel(giveMeTheLink: String): LiveData<List<Carousel>> {
        return instagramRepository.getCarousel(giveMeTheLink)
    }


    fun deletePost(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            instagramRepository.deletePost(url)
        }
    }

    fun deleteCarousel(url: String) {
        viewModelScope.launch {
            instagramRepository.deleteCarousel(url)
        }
    }

    suspend fun isCurrentUserCookieValid(cookie: String): Boolean {
        return instagramRepository.getCurrentUser(cookie)
    }


    fun loadMorePosts(userId: Long, cookies: String) {
        if (!instagramRepository.hasMorePosts()) return
        isLoading.value = true
        viewModelScope.launch {
            val response = instagramRepository.getAllPosts(userId, cookies)
            response.let { newItems ->
                val current = allUserPosts.value ?: mutableListOf()
                current.addAll(newItems)
                allUserPosts.postValue(current)
                isLoading.postValue(false)
            }
        }
    }


    fun refresh(userId: Long, cookies: String) {
        instagramRepository.resetPagination()
        allUserPosts.value = mutableListOf()
        loadMorePosts(userId, cookies)
    }

    fun loadUserStats(pk: Long) {
        viewModelScope.launch {
            try {
                val result = instagramRepository.getUserStats(pk)
                _stats.value = result

            } catch (e: HttpException) {
                println("Server error (${e.code()})")

            } catch (e: IOException) {
                println("Network error")

            } catch (e: Exception) {
                println("Unexpected error: ${e.message}")
            }
        }
    }


}