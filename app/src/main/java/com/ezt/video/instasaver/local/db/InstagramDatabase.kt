package com.ezt.video.instasaver.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ezt.video.instasaver.local.Carousel
import com.ezt.video.instasaver.local.DPRecent
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.local.ProfileRecent
import com.ezt.video.instasaver.local.StoryRecent

@Database(entities = [Post::class,Carousel::class,StoryRecent::class, DPRecent::class, ProfileRecent::class], version = 3, exportSchema = false)
abstract class InstagramDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}