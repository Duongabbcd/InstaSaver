package com.ezt.video.instasaver.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.video.instasaver.local.DPRecent
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.model.SearchUser
import com.ezt.video.instasaver.model.User
import com.ezt.video.instasaver.remote.repository.InstagramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DPViewerViewModel @Inject constructor(private val instagramRepository: InstagramRepository) : ViewModel()  {
    var searchResult= MutableLiveData<List<SearchUser>>()
    var recents= MutableLiveData<List<DPRecent>>()
    var profilePicUrl= MutableLiveData<String>()



    fun searchUser(query:String,cookie: String){
        viewModelScope.launch {
            searchResult.postValue(instagramRepository.searchUsers(query,cookie))
        }
    }

    fun insertRecent(user: User){
        viewModelScope.launch {
            instagramRepository.insertDPRecent(user)
        }
    }

    fun getRecentSearches(){
        viewModelScope.launch {
            recents.postValue(instagramRepository.getRecentDPSearch())
        }
    }

    fun insertDP(post: Post){
        viewModelScope.launch {
            instagramRepository.insertPost(post)
        }
    }

    fun getDP(userId: Long, cookies: String){
        viewModelScope.launch {
            val profilePic=instagramRepository.getDP(userId,cookies)
            profilePicUrl.postValue(profilePic)
        }
    }

}