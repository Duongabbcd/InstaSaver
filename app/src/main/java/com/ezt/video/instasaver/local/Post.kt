package com.ezt.video.instasaver.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "post_table")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var media_type: Int,
    val username: String,
    val profile_pic_url: String,
    val image_url: String,
    val video_url: String?,
    val caption: String?,
    val path: String?,
    val downloadLink:String?,
    val extension:String?,
    val title:String?,
    var link:String?,
    var isCarousel: Boolean,
    var isStory: Boolean = false
)

@Entity(tableName = "carousel_table")
data class Carousel(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val media_type: Int,
    val image_url: String,
    val video_url: String?,
    val extension:String?,
    val link: String?,
    val title:String?,
)

@Entity(tableName="recent_table")
data class StoryRecent(
    val pk: Long,
    val username: String,
    val full_name: String,
    val profile_pic_url: String,
    val userId: String?
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(tableName="dp_recent_table",indices = [Index(value = ["username"], unique = true)])
data class DPRecent(
    val pk: Long,
    val username: String,
    val full_name: String,
    val profile_pic_url: String,
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(tableName="profile_recent_table",indices = [Index(value = ["username"], unique = true)])
data class ProfileRecent(
    val pk: Long,
    val username: String,
    val full_name: String,
    val profile_pic_url: String,
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}






