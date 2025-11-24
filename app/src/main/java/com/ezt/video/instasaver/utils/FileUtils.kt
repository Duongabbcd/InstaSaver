package com.ezt.video.instasaver.utils

import android.content.Context
import java.io.File

object FileUtils {
    fun getDefaultImagePath(context: Context) : String{
        val basePath = getDefaultApplicationPath(context)
        val commandDir = File(basePath, "InstaImage")
        if (!commandDir.exists()) commandDir.mkdirs()
        return commandDir.absolutePath
    }

    fun getDefaultVideoPath(context: Context) : String{
        val basePath = getDefaultApplicationPath(context)
        val commandDir = File(basePath, "InstaVideo")
        if (!commandDir.exists()) commandDir.mkdirs()
        return commandDir.absolutePath
    }

    fun getDefaultStoryPath(context: Context) : String{
        val basePath = getDefaultApplicationPath(context)
        val commandDir = File(basePath, "InstaStory")
        if (!commandDir.exists()) commandDir.mkdirs()
        return commandDir.absolutePath
    }

    fun getDefaultAvatarUserPath(context: Context) : String{
        val basePath = getDefaultApplicationPath(context)
        val commandDir = File(basePath, "InstaProfileImage")
        if (!commandDir.exists()) commandDir.mkdirs()
        return commandDir.absolutePath
    }

    fun getDefaultCommandPath(context: Context): String {
        val basePath = getDefaultApplicationPath(context)
        val commandDir = File(basePath, "Command")
        if (!commandDir.exists()) commandDir.mkdirs()
        return commandDir.absolutePath
    }

    fun getDefaultApplicationPath(context: Context) : String {
        val appFolder = File(context.getExternalFilesDir(null), ".VideoDownloader")
        if (!appFolder.exists()) appFolder.mkdirs()
        return appFolder.absolutePath
    }
}