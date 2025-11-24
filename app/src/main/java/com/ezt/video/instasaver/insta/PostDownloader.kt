package com.ezt.video.instasaver.insta

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.ezt.video.instasaver.local.Carousel
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.model.Items
import com.ezt.video.instasaver.model.ShortCodeMedia
import com.ezt.video.instasaver.remote.network.InstagramAPI
import com.ezt.video.instasaver.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                        val currentPost = downloadPost(item, false)
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
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableListOf()
        }
    }


    private fun downloadPost(item: Items, isSavedToFile : Boolean = true): Post {
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
        println("downloadPost: ${item.user.username}  and $avatarFile and $isSavedToFile")
        CoroutineScope(Dispatchers.IO).launch {
            downloadAvatar(context,item.user.profile_pic_url, Constants.AVATAR_FOLDER_NAME, item.user.username + ".jpg")
        }



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

    suspend fun downloadAvatar(
        context: Context,
        downloadUrl: String,
        folderName: String,
        fileName: String
    ): File? = withContext(Dispatchers.IO) {
        if (downloadUrl.isEmpty()) return@withContext null

        // Prepare folder and file
        val folder = File(context.getExternalFilesDir(null), folderName)
        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, fileName)
        if (file.exists()) file.delete() // overwrite safely

        // Setup download request
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
            )
            .setTitle(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(file))

        val downloadId = dm.enqueue(request)

        // Wait for download to complete
        var downloading = true
        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = dm.query(query)
            if (cursor.moveToFirst()) {
                val status =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                } else if (status == DownloadManager.STATUS_FAILED) {
                    cursor.close()
                    return@withContext null
                }
            }
            cursor.close()
            Thread.sleep(100) // small delay to prevent busy loop
        }

        return@withContext file
    }

    fun download(downloadLink: String?, path: String?, title: String?): Long {
        val uri: Uri = Uri.parse(downloadLink)
        val request = DownloadManager.Request(uri)

        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
        )
        request.setTitle(title)

        // Use absolute path with Uri.fromFile
        val file = File(path, title)
        if (file.exists()) file.delete()  // overwrite existing
        request.setDestinationUri(Uri.fromFile(file))

        return (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
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
