package com.ezt.video.instasaver.insta

import android.content.Context
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.remote.network.InstagramAPI
import com.ezt.video.instasaver.utils.Constants
import javax.inject.Inject

class ProfileDownloader @Inject constructor(private val context: Context, private val instagramAPI: InstagramAPI, private val postDao:PostDao) {

    suspend fun getDP(userId: Long,cookies: String): String{
        val link= Constants.DP.format(userId)
        var result = ""
        try {
            result= instagramAPI.getDP(link, cookies,Constants.USER_AGENT).user.hd_profile_pic_url_info.url
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

}

