package com.ezt.video.instasaver.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(post: Post)

    @Delete
    fun delete(post: Post)

    @Query("SELECT * FROM post_table")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM post_table ORDER BY id DESC limit 5")
    fun getRecentDownloads(): LiveData<List<Post>>

    @Query("SELECT * FROM post_table WHERE username LIKE '%' || :name || '%' ORDER BY id")
    fun getPostsByUsername(name: String): List<Post>
    @Query("SELECT * FROM post_table WHERE username LIKE '%' || :name || '%'   AND media_type = 1 ORDER BY id")
    fun getPhotoPostsByUsername(name: String): List<Post>

    @Query("SELECT * FROM post_table WHERE username LIKE '%' || :name || '%'   AND media_type = 2 ORDER BY id")
    fun getVideoPostsByUsername(name: String): List<Post>

    @Query("SELECT * FROM post_table WHERE username LIKE '%' || :name || '%'   AND media_type = 8 ORDER BY id")
    fun getCarouselPostsByUsername(name: String): List<Post>


    @Query("SELECT * FROM post_table WHERE media_type = 8 ORDER BY id")
    fun getCarouselPosts(): List<Post>

    @Query("SELECT * FROM post_table WHERE media_type = 2 ORDER BY id")
    fun getVideoPosts(): List<Post>

    @Query("SELECT * FROM post_table WHERE media_type = 1 ORDER BY id")
    fun getPhotoPosts(): List<Post>

    @Query("SELECT COUNT(id) FROM post_table ")
    fun getFileCount(): LiveData<Int>

    @Query("SELECT EXISTS(SELECT media_type FROM post_table WHERE link= :url)")
    fun doesPostExits(url:String): Boolean

    @Query("SELECT EXISTS(SELECT media_type FROM carousel_table WHERE link= :url)")
    fun doesPostExits2(url:String): Boolean

    @Query("SELECT * FROM carousel_table WHERE link=:url")
    fun getCarousel(url:String): LiveData<List<Carousel>>

    @Query("SELECT * FROM carousel_table WHERE link=:url")
    fun getCarousel2(url:String): List<Carousel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCarousel(carousel: Carousel)

    @Query("DELETE FROM post_table WHERE link= :url")
    fun deletePost(url: String)

    @Query("DELETE FROM carousel_table WHERE link= :url")
    fun deleteCarousel(url: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecent(storyRecent: StoryRecent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDPRecent(dpRecent: DPRecent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfileRecent(profileRecent: ProfileRecent)

    @Query("SELECT * FROM RECENT_TABLE ORDER BY ID DESC LIMIT 10")
    fun getRecentSearch(): List<StoryRecent>

    @Query("SELECT * FROM DP_RECENT_TABLE ORDER BY ID DESC LIMIT 10")
    fun getRecentDPSearch(): List<DPRecent>

    @Query("SELECT * FROM PROFILE_RECENT_TABLE ORDER BY ID DESC LIMIT 10")
    fun getRecentProfileSearch(): List<ProfileRecent>

    @Query("DELETE FROM PROFILE_RECENT_TABLE WHERE username= :username")
    fun deleteRecentProfileSearch(username: String)
}