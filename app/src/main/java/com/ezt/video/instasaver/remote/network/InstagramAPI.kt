package com.ezt.video.instasaver.remote.network

import com.ezt.video.instasaver.model.InstagramResponse
import com.ezt.video.instasaver.model.ReelMediaResponse
import com.ezt.video.instasaver.model.ReelTrayResponse
import com.ezt.video.instasaver.model.StoryHighlightResponse
import com.ezt.video.instasaver.model.StoryResponse
import com.ezt.video.instasaver.model.UserResponse
import com.ezt.video.instasaver.model.Users
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface InstagramAPI {
    @GET("media/{mediaId}/info")
    suspend fun getData(@Path("mediaId") mediaId: Long, @Header("Cookie") map :String,@Header("User-Agent") userAgent:String): InstagramResponse

    @GET
    suspend fun getData(@Url url: String, @Header("Cookie") map: String, @Header("User-Agent") userAgent: String) : InstagramResponse

    @GET
    suspend fun searchUsers(@Url url: String, @Header("Cookie") map: String) : Users

    @GET
    suspend fun getStories(@Url url: String, @Header("Cookie") map :String,@Header("User-Agent") userAgent:String): StoryResponse

    @GET
    suspend fun getReelsTray(@Url url: String, @Header("Cookie") map: String, @Header("User-Agent") userAgent:String): ReelTrayResponse

    @GET
    suspend fun getReelMedia(@Url url: String, @Header("Cookie") map: String, @Header("User-Agent") userAgent:String): ReelMediaResponse

    @GET
    suspend fun getStoryHighlights(@Url url: String, @Header("Cookie") map: String, @Header("User-Agent") userAgent:String): StoryHighlightResponse

    @GET
    suspend fun getDP(@Url url: String, @Header("Cookie",) map: String,@Header("User-Agent") userAgent:String): UserResponse

    @GET("accounts/current_user/")
    suspend fun getCurrentUser(
        @Header("Cookie") cookie: String,
        @Header("User-Agent") userAgent: String
    ): CurrentUserResponse

}

data class CurrentUserResponse(
    val status: String?,
    val message: String?
)