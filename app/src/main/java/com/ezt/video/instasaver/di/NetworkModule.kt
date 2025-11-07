package com.ezt.video.instasaver.di

import android.content.Context
import com.ezt.video.instasaver.insta.Downloader
import com.ezt.video.instasaver.insta.PostDownloader
import com.ezt.video.instasaver.insta.ProfileDownloader
import com.ezt.video.instasaver.insta.StoryDownloader
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.remote.network.InstagramAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideInstagramAPI(): InstagramAPI{
        return Retrofit.Builder()
            .baseUrl("https://i.instagram.com/api/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(InstagramAPI::class.java)

    }

    @Provides
    @Singleton
    fun provideDownloader(@ApplicationContext appContext : Context): Downloader {
        return Downloader(appContext)
    }

    @Provides
    @Singleton
    fun providePostDownloader(@ApplicationContext appContext : Context,instagramAPI: InstagramAPI,postDao: PostDao): PostDownloader {
        return PostDownloader(appContext,instagramAPI,postDao)
    }

    @Provides
    @Singleton
    fun provideStoryDownloader(@ApplicationContext appContext : Context,instagramAPI: InstagramAPI,postDao: PostDao): StoryDownloader {
        return StoryDownloader(appContext,instagramAPI,postDao)
    }

    @Provides
    @Singleton
    fun provideProfileDownloader(@ApplicationContext appContext : Context,instagramAPI: InstagramAPI,postDao: PostDao): ProfileDownloader {
        return ProfileDownloader(appContext,instagramAPI,postDao)
    }




}