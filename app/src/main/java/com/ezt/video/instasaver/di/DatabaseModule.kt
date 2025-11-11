package com.ezt.video.instasaver.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
            .addMigrations(MIGRATION_1_2)
            .build()
    }


    @Provides
    @Singleton
    fun providePostDao(postDB: InstagramDatabase): PostDao {
        return postDB.postDao()
    }

    val  MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE post_table ADD COLUMN isStory INTEGER NOT NULL DEFAULT 0")
        }
    }

}