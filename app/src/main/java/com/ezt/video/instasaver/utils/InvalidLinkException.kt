package com.ezt.video.instasaver.utils

import android.os.Build
import java.lang.Exception

class InvalidLinkException(message:String): Exception(message) {

}

inline fun <T> sdk29AndUp(onSdk29: () -> T): T?{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        onSdk29()
    }else null
}