package com.ezt.video.instasaver.insta

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.model.ReelTray
import com.ezt.video.instasaver.model.SearchUser
import com.ezt.video.instasaver.model.Story
import com.ezt.video.instasaver.model.StoryHighlight
import com.ezt.video.instasaver.remote.network.InstagramAPI
import com.ezt.video.instasaver.utils.Constants
import retrofit2.HttpException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class StoryDownloader @Inject constructor(
    private val context: Context,
    private val instagramAPI: InstagramAPI,
    private val postDao: PostDao
) {
    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    suspend fun fetchFromUrl(userId: Long, cookie: String): MutableList<Story> {
        val stories = mutableListOf<Story>()
        try {
            val link = Constants.STORY_DOWNLOAD.format(userId)
            val reel = instagramAPI.getStories(link, cookie, Constants.USER_AGENT).reel
            val items = reel.items
            val user = reel.user
            for (item in items) {
                stories.add(
                    Story(
                        item.code,
                        item.media_type,
                        item.image_versions2.candidates[0].url,
                        item.video_versions?.get(0)?.url,
                        user.username,
                        user.profile_pic_url,
                        name = null
                    )
                )
            }
        }catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API_ERROR", "Error body: $errorBody")
            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()

        }
        return stories
    }

    fun downloadStory(story: Story): Long {
        println("downloadStory: $story")
        val extension: String
        val downloadLink = if (story.mediaType == 1) {
            extension = ".jpg"
            story.imageUrl
        } else {
            extension = ".mp4"
            story.videoUrl
        }
        val username = story.username
        val code = story.code
        val currentTime = System.currentTimeMillis()
        val title = "${username}_${currentTime}$extension"
        story.name = title
        val caption = "${username}'s story from ${formatter.format(currentTime)}"


        val avatarFile = File(Constants.AVATAR_FOLDER_NAME, story.username + ".jpg")
        if (avatarFile.exists()) {
            avatarFile.delete() // delete old file
        }
        download(story.profilePicUrl, Constants.AVATAR_FOLDER_NAME, story.username + ".jpg")

        postDao.insertPost(
            Post(
                0,
                story.mediaType,
                username,
                story.profilePicUrl,
                story.imageUrl,
                story.videoUrl,
                caption,
                null,
                null,
                extension,
                title,
                code,
                isCarousel = false, isStory = true
            )
        )
        return download(downloadLink, Constants.STORY_FOLDER_NAME, title)
    }

    fun downloadStoryDirect(story: Story, extension: String, downloadLink: String): Long {
        val username = story.username
        val currentTime = System.currentTimeMillis()
        val title = "${username}_${currentTime}$extension"
        val caption = "${username}'s story from ${formatter.format(currentTime)}"
        postDao.insertPost(
            Post(
                0,
                story.mediaType,
                username,
                story.profilePicUrl,
                story.imageUrl,
                story.videoUrl,
                caption,
                null,
                null,
                extension,
                title,
                story.code,
                isCarousel = false, isStory = true
            )
        )
        return download(downloadLink, Constants.STORY_FOLDER_NAME, title)
    }

    suspend fun fetchReelTray(cookie: String): List<ReelTray> {
        val result = mutableListOf<ReelTray>()
        try {
            result.addAll(
                instagramAPI.getReelsTray(
                    Constants.REEL_TRAY,
                    cookie,
                    Constants.USER_AGENT
                ).tray
            )

        } catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API_ERROR", "Error body: $errorBody")
            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()

        }
        return result
    }

    fun download(downloadLink: String?, path: String?, title: String?): Long {
        val uri: Uri = Uri.parse(downloadLink)
        val request = DownloadManager.Request(uri)

        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
        )
        request.setTitle(title)

        // Use absolute path with Uri.fromFile
        val file = File(path, title ?: "")
        request.setDestinationUri(Uri.fromFile(file))

        return (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    suspend fun getReelMedia(reelId: Long, cookie: String): ReelTray? {
        val link = Constants.REEL_MEDIA.format(reelId)
        return try {
            instagramAPI.getReelMedia(link, cookie, Constants.USER_AGENT).reels[reelId.toString()]
        } catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API_ERROR", "Error body: $errorBody")
            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
            null
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            null
        }

    }

    suspend fun getReelMedia(reelId: String, cookie: String): MutableList<Story> {
        val stories = mutableListOf<Story>()
        try {
            val link = Constants.REEL_MEDIA.format(reelId)
            val reel: ReelTray? =
                instagramAPI.getReelMedia(link, cookie, Constants.USER_AGENT).reels[reelId]
            if (reel != null) {
                val items = reel.items
                val user = reel.user
                for (item in items) {
                    stories.add(
                        Story(
                            item.code,
                            item.media_type,
                            item.image_versions2.candidates[0].url,
                            item.video_versions?.get(0)?.url,
                            user.username,
                            user.profile_pic_url,
                            name = null
                        )
                    )
                }
            }
        }catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API_ERROR", "Error body: $errorBody")
            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()

        }
        return stories
    }

    suspend fun getStoryHighlights(userId: Long, cookie: String): List<StoryHighlight> {
        val link = Constants.STORY_HIGHLIGHTS.format(userId)

        val storyHighlights = mutableListOf<StoryHighlight>()

        try {
            storyHighlights.addAll(
                instagramAPI.getStoryHighlights(
                    link,
                    cookie,
                    Constants.USER_AGENT
                ).tray
            )
        } catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API_ERROR", "Error body: $errorBody")
            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()

        }

        return storyHighlights
    }

    suspend fun searchUsers(query: String, cookie: String): List<SearchUser> {
        try {
            val url = Constants.SEARCH_USER.format(query)
            return instagramAPI.searchUsers(url, cookie).users
        } catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API_ERROR", "Error body: $errorBody")
            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
            return mutableListOf()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()

            return mutableListOf()
        }

    }
}
