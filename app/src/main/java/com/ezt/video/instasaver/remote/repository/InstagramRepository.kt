package com.ezt.video.instasaver.remote.repository

import androidx.lifecycle.LiveData
import com.ezt.video.instasaver.insta.PostDownloader
import com.ezt.video.instasaver.insta.ProfileDownloader
import com.ezt.video.instasaver.insta.StoryDownloader
import com.ezt.video.instasaver.local.Carousel
import com.ezt.video.instasaver.local.DPRecent
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.local.ProfileRecent
import com.ezt.video.instasaver.local.StoryRecent
import com.ezt.video.instasaver.model.ReelTray
import com.ezt.video.instasaver.model.SearchUser
import com.ezt.video.instasaver.model.Story
import com.ezt.video.instasaver.model.StoryHighlight
import com.ezt.video.instasaver.model.User
import com.ezt.video.instasaver.utils.Constants
import javax.inject.Inject

class InstagramRepository @Inject constructor(private val postDao: PostDao,private val postDownloader: PostDownloader, private val storyDownloader: StoryDownloader, private val profileDownloader: ProfileDownloader){
    val getAllPost = postDao.getAllPosts()
    val getRecentDownload = postDao.getRecentDownloads()

    suspend fun fetchPostByUser(
        userName: String,
    ) : List<Post> {
        return postDao.getPostsByUsername(userName)
    }

    suspend fun fetchVideoPostByUser(
        userName: String,
    ) : List<Post> {
        return postDao.getPhotoPostsByUsername(userName)
    }

    suspend fun fetchPhotoPostByUser(
        userName: String,
    ) : List<Post> {
        return postDao.getVideoPostsByUsername(userName)
    }

    suspend fun fetchCarouselPostByUser(
        userName: String,
    ) : List<Post> {
        return postDao.getCarouselPostsByUsername(userName)
    }

    suspend fun fetchCarouselPosts(
    ) : List<Post> {
        return postDao.getCarouselPosts()
    }

    suspend fun fetchPhotoPosts(
    ) : List<Post> {
        return postDao.getPhotoPosts()
    }

    suspend fun fetchVideoPosts(
    ) : List<Post> {
        return postDao.getVideoPosts()
    }

    suspend fun fetchPost(url: String, map: String): MutableList<Long>{
        return postDownloader.fetchDownloadLink(url,map)
    }

    fun downloadStory(story: Story): Long{
        return storyDownloader.downloadStory(story)
    }


    fun downloadStoryDirect(story: Story,extension: String, downloadLink: String): Long{
        return storyDownloader.downloadStoryDirect(story,extension, downloadLink)
    }

    suspend fun fetchStory(userId: Long, map: String): MutableList<Story>{
        return storyDownloader.fetchFromUrl(userId,map)
    }

    fun directDownload(link:String,path:String,title:String): Long{
        return postDownloader.download(link,path,title)
    }

    fun doesPostExits(url: String): Boolean{
        return postDao.doesPostExits(url)
    }

    fun getCarousel(giveMeTheLink: String): LiveData<List<Carousel>> {
        return postDao.getCarousel(giveMeTheLink)
    }

    fun deletePost(url:String){
        postDao.deletePost(url)
    }

    fun deleteCarousel(url:String){
        println("deleteCarousel: $url")
        postDao.deletePost(url)
        postDao.deleteCarousel(url)
    }

    suspend fun fetchReelTray(cookie: String): List<ReelTray>{
        return storyDownloader.fetchReelTray(cookie)
    }

    suspend fun fetchReelMedia(reelId: Long,cookie: String): ReelTray?{
        return storyDownloader.getReelMedia(reelId,cookie)
    }

    suspend fun fetchStoryHighlightsStories(reelId: String,cookie: String): MutableList<Story>{
        return storyDownloader.getReelMedia(reelId,cookie)
    }

    suspend fun fetchStoryHighlights(userId: Long,cookie: String): List<StoryHighlight>{
        return storyDownloader.getStoryHighlights(userId,cookie)
    }

    suspend fun searchUsers(query: String,cookie: String): List<SearchUser>{
        return storyDownloader.searchUsers(query, cookie)
    }

    fun insertRecent(user: User){
        postDownloader.download(user.profile_pic_url, Constants.AVATAR_FOLDER_NAME, user.username.plus(".jpg"))
        postDao.insertRecent(StoryRecent(user.pk,user.username,user.full_name,user.profile_pic_url,null))
    }

    fun insertDPRecent(user: User){
        postDownloader.download(user.profile_pic_url, Constants.AVATAR_FOLDER_NAME, user.username.plus(".jpg"))
        postDao.insertDPRecent(DPRecent(user.pk,user.username,user.full_name,user.profile_pic_url))
    }

    fun insertProfileRecent(user: User){
        postDownloader.download(user.profile_pic_url, Constants.AVATAR_FOLDER_NAME, user.username.plus(".jpg"))
        postDao.insertProfileRecent(ProfileRecent(user.pk,user.username,user.full_name,user.profile_pic_url))
    }

    fun getRecentSearch(): List<StoryRecent>{
        return postDao.getRecentSearch()
    }

    fun getRecentDPSearch(): List<DPRecent>{
        return postDao.getRecentDPSearch()
    }

    fun getRecentProfileSearch() : List<ProfileRecent> {
        return postDao.getRecentProfileSearch()
    }

    fun insertPost(post: Post){
        postDao.insertPost(post)
    }

    suspend fun getDP(userId: Long,cookies: String): String{
        return profileDownloader.getDP(userId, cookies)
    }


    suspend fun getCurrentUser(cookie: String) : Boolean {
        return postDownloader.isCookieValid(cookie)
    }



}