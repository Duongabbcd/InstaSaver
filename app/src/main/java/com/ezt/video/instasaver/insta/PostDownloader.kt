package com.ezt.video.instasaver.insta

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.ezt.video.instasaver.local.Carousel
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.model.Items
import com.ezt.video.instasaver.model.ShortCodeMedia
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.remote.network.InstagramAPI
import com.ezt.video.instasaver.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class PostDownloader @Inject constructor(
    private val context: Context,
    private val instagramAPI: InstagramAPI,
    private val postDao: PostDao
) {

    suspend fun fetchDownloadLink(url: String, map: String): MutableList<Long> {
        try {
            val postID = getPostCode(url)
            val items: Items = if (postID.length > 23) {
                instagramAPI.getData(
                    Constants.PRIVATE_POST_URL.format(postID),
                    map,
                    Constants.USER_AGENT
                ).items[0]
            } else {
                val mediaId = getMediaId(postID)
                val logd = "$mediaId \n $map \n ${Constants.USER_AGENT} \n $url"
                Log.d("tagg", logd)
                instagramAPI.getData(mediaId, map, Constants.USER_AGENT).items[0]
            }
            println("fetchDownloadLink: $postID and $items")

            val post: Post
            val downloadId = mutableListOf<Long>()

            if (items.media_type == 8) {
                for ((index, item) in items.carousel_media.withIndex()) {
                    if (index == 0) {
                        item.user = items.user
                        item.caption = items.caption
                        val currentPost = downloadPost(item)
                        currentPost.isCarousel = true
                        downloadId.add(
                            download(
                                currentPost.downloadLink,
                                currentPost.path,
                                currentPost.title
                            )
                        )
                        currentPost.link = url
                        currentPost.media_type = 8
                        postDao.insertPost(currentPost)
                        val carousel = Carousel(
                            0,
                            item.media_type,
                            currentPost.image_url,
                            currentPost.video_url,
                            currentPost.extension,
                            currentPost.link,
                            currentPost.title
                        )
                        postDao.insertCarousel(carousel)
                    } else {
                        item.user = items.user
                        item.caption = items.caption
                        val currentPost = downloadPost(item)
                        downloadId.add(
                            download(
                                currentPost.downloadLink,
                                currentPost.path,
                                currentPost.title
                            )
                        )
                        val carousel = Carousel(
                            0,
                            item.media_type,
                            currentPost.image_url,
                            currentPost.video_url,
                            currentPost.extension,
                            url,
                            currentPost.title
                        )
                        postDao.insertCarousel(carousel)
                    }
                }
            } else {
                post = downloadPost(items)
                post.link = url
                postDao.insertPost(post)
                downloadId.add(download(post.downloadLink, post.path, post.title))
            }
            return downloadId
        }catch (e: HttpException) {
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

    suspend fun isCookieValid(cookie: String): Boolean {
        return try {
            val res = instagramAPI.getCurrentUser(cookie, Constants.USER_AGENT)
            println("cookieIsStillValid 1: $res")
            // Valid session
            if (res.status == "ok") return true

            // Invalid or logged out
            if (res.message == "login_required") return false

            false
        } catch (e: Exception) {
            // Error also means cookie probably invalid
            false
        }
    }


    private fun downloadPost(item: Items): Post {
        var videoUrl: String? = null
        val path: String
        val extension: String
        val downloadLink = when (item.media_type) {
            1 -> {
                extension = ".jpg"
                path = Constants.IMAGE_FOLDER_NAME
                item.image_versions2.candidates[0].url

            }

            2 -> {
                extension = ".mp4"
                path = Constants.VIDEO_FOLDER_NAME
                videoUrl = item.video_versions[0].url
                videoUrl
            }

            else -> {
                extension = ".jpg"
                path = Constants.IMAGE_FOLDER_NAME
                item.image_versions2.candidates[0].url
            }
        }
        val title = item.user.username + "_" + System.currentTimeMillis().toString() + extension
        val caption: String? = item.caption?.text



        val avatarFile = File(Constants.AVATAR_FOLDER_NAME, item.user.username + ".jpg")
        if (avatarFile.exists()) {
            avatarFile.delete()
        }
        println("downloadPost: ${item.user.username}  and $avatarFile")
        download(item.user.profile_pic_url, Constants.AVATAR_FOLDER_NAME, item.user.username + ".jpg")

        return Post(
            0,
            item.media_type,
            item.user.username,
            item.user.profile_pic_url,
            item.image_versions2.candidates[0].url,
            videoUrl,
            caption,
            path,
            downloadLink,
            extension,
            title,
            avatarFile.absolutePath ,
            false, false
        )
    }

    fun download(downloadLink: String?, path: String?, title: String?): Long {
        println("download: path $path  title $title")
        val uri: Uri = Uri.parse(downloadLink)
        val request = DownloadManager.Request(uri)

        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
        )
        request.setTitle(title)

        // Use absolute path with Uri.fromFile
        val file = File(path, title)
        if(file.exists()) {
            println("The file is exist")
            file.delete()
        }
        request.setDestinationUri(Uri.fromFile(file))

        return (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    suspend fun getUserStatsByPk(pk: Long): Triple<Int, Int, Int> {
        try {
            val response = instagramAPI.getUserInfo(pk).user

            val posts = response.media_count
            val followers = response.follower_count
            val following = response.following_count

            return Triple(posts, followers, following)
        }

        catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
            return Triple(0,0,0)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()

            return Triple(0,0,0)
        }
    }


    private fun getPostCode(link: String): String {
        val index = link.indexOf("instagram.com")
        val totalIndex = index + 13
        var url = link.substring(totalIndex, link.length)
        url = url.split("/?")[0]
        url = url.split("/")[2]
        return url
    }

    private fun getMediaId(shortcode: String): Long {
        var mediaId = 0L
        val alphabets = Constants.SHORTCODE_CHARACTERS
        for (letter in shortcode) {
            mediaId = (mediaId * 64) + alphabets.indexOf(letter)
        }
        return mediaId
    }

//    private fun getPostCode2(link: String): String {
//        return link.split("/")[4]
//    }
}
