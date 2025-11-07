package com.ezt.video.instasaver.model

data class Users(val users: List<SearchUser>)

data class SearchUser(val position: Int,val user: User)

data class HDUser(val profile_pic_url_hd: String)
