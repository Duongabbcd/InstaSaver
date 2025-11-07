package com.ezt.video.instasaver.di

import android.app.Application
import androidx.room.Room
import com.ezt.video.instasaver.local.PostDao
import com.ezt.video.instasaver.local.db.InstagramDatabase
import com.ezt.video.instasaver.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun providePostDB(application: Application): InstagramDatabase {
        return Room.databaseBuilder(
            application,
            InstagramDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }


    @Provides
    @Singleton
    fun providePostDao(postDB: InstagramDatabase): PostDao {
        return postDB.postDao()
    }

}