package com.ezt.video.instasaver.utils

object Constants {
    const val DATABASE_NAME="Insta_Download"
     var VIDEO_FOLDER_NAME = ""
     var IMAGE_FOLDER_NAME = ""
     var STORY_FOLDER_NAME = ""
     var AVATAR_FOLDER_NAME = ""
    const val INSTAGRAM_SAVE_LOGIN_LINK="https://www.instagram.com/accounts/onetap/?next=%2F"
    const val INSTAGRAM_HOMEPAGE_LINK="https://www.instagram.com/"
    const val INSTAGRAM="com.instagram.android"
    const val REPOST= "#Repost "
    const val CAROUSEL= "GraphSidecar"
    const val IMAGE="GraphImage"
    const val VIDEO="GraphVideo"
    const val REEL_MEDIA="https://i.instagram.com/api/v1/feed/reels_media/?reel_ids=%s"
    const val STORY_DOWNLOAD="https://i.instagram.com/api/v1/feed/user/%s/story/"
    const val USER_AGENT =
        "Instagram 254.0.0.19.109 Android (33/13; 420dpi; 1080x2400; Google; Pixel 7; panther; gs201; en_US; 450263394)"
    const val REEL_TRAY="https://i.instagram.com/api/v1/feed/reels_tray/"
    const val SEARCH_USER="https://www.instagram.com/web/search/topsearch/?context=blended&query=%s&rank_token=0.13538577328414725&include_reel=false"
    const val STORY_HIGHLIGHTS="https://i.instagram.com/api/v1/highlights/%s/highlights_tray/"
    const val DP="https://i.instagram.com/api/v1/users/%s/info/"
    const val SHORTCODE_CHARACTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    const val PRIVATE_POST_URL="https://www.instagram.com/p/%s/?__a=1&__d=dis"
}