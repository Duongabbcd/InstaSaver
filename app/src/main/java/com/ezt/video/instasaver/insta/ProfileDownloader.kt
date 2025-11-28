package com.ezt.video.instasaver.insta

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.model.Items
import com.ezt.video.instasaver.remote.network.InstagramAPI
import com.ezt.video.instasaver.utils.Constants
import retrofit2.HttpException
import javax.inject.Inject

class ProfileDownloader @Inject constructor(private val context: Context, private val instagramAPI: InstagramAPI, private val postDao:PostDao) {

    suspend fun getDP(userId: Long,cookies: String): String{
        val link= Constants.DP.format(userId)
        var result = ""
        try {
            result= instagramAPI.getDP(link, cookies,Constants.USER_AGENT).user.hd_profile_pic_url_info.url
        }catch (e: HttpException) {
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

    suspend fun getAllPosts(userId: Long, cookies: String) : List<Items> {
        val response = instagramAPI.getUserPosts(userId, cookie = cookies, userAgent = Constants.USER_AGENT)

       try {
           if (response.isSuccessful) {
               return response.body()?.items ?: emptyList()
           }

           Log.e("API", "Error: ${response.errorBody()?.string()}")
       } catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.code()} - ${e.message}")

            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API_ERROR", "Error body: $errorBody")
            Toast.makeText(context, context.resources.getString(R.string.cookie_expired), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
        return emptyList()
    }

}

