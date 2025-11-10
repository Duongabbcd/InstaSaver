package com.ezt.video.instasaver.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.video.instasaver.local.StoryRecent
import com.ezt.video.instasaver.model.ReelTray
import com.ezt.video.instasaver.model.SearchUser
import com.ezt.video.instasaver.model.Story
import com.ezt.video.instasaver.model.StoryHighlight
import com.ezt.video.instasaver.model.User
import com.ezt.video.instasaver.remote.repository.InstagramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(private val instagramRepository: InstagramRepository) : ViewModel() {
    var stories= MutableLiveData<MutableList<Story>>()
    var reelTray= MutableLiveData<List<ReelTray>>()
    var storyHighlights= MutableLiveData<List<StoryHighlight>>()
    var reelMedia= MutableLiveData<ReelTray?>()
    var recents= MutableLiveData<List<StoryRecent>>()
    //    var storyHighlightsStories= MutableLiveData<MutableList<Story>>()
    var downloadId= MutableLiveData<Long>()
    var searchResult= MutableLiveData<List<SearchUser>>()

    fun fetchStory(userId: Long, map: String) {
        viewModelScope.launch(Dispatchers.IO) {
            stories.postValue(instagramRepository.fetchStory(userId, map))
        }
    }

    fun downloadStory(story: Story){
        viewModelScope.launch(Dispatchers.IO) {
            val downloadIdList= instagramRepository.downloadStory(story)
            downloadId.postValue(downloadIdList)
        }
    }

    fun downloadStoryDirect(story: Story,extension: String, downloadLink:String){
        viewModelScope.launch(Dispatchers.IO) {
            val downloadIdList= instagramRepository.downloadStoryDirect(story,extension,downloadLink)
            downloadId.postValue(downloadIdList)
        }
    }

    fun fetchReelTray(cookie: String){
        viewModelScope.launch(Dispatchers.IO) {
            reelTray.postValue(instagramRepository.fetchReelTray(cookie))
        }
    }

    fun fetchReelMedia(reelId: Long,cookie: String){
        viewModelScope.launch {
            reelMedia.postValue(instagramRepository.fetchReelMedia(reelId, cookie))
        }
    }

    fun fetchStoryHighlightsStories(reelId: String,cookie: String){
        viewModelScope.launch {
            stories.postValue(instagramRepository.fetchStoryHighlightsStories(reelId, cookie))
        }
    }

    fun fetchStoryHighlights(userId: Long,cookie: String){
        viewModelScope.launch {
            storyHighlights.postValue(instagramRepository.fetchStoryHighlights(userId, cookie))
        }
    }

    fun searchUser(query:String,cookie: String){
        viewModelScope.launch {
            searchResult.postValue(instagramRepository.searchUsers(query,cookie))
        }
    }

    fun insertRecent(user: User){
        viewModelScope.launch {
            instagramRepository.insertRecent(user)
        }
    }

    fun getRecentSearches(){
        viewModelScope.launch {
            recents.postValue(instagramRepository.getRecentSearch())
        }
    }


}