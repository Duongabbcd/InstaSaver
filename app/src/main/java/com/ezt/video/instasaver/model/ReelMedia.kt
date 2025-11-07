package com.ezt.video.instasaver.model

data class ReelMedia(
    val uri:String,
    val isImage:Boolean,
)

data class ReelResponse(val reels: List<Reel>)

