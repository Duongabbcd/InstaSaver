package com.ezt.video.instasaver.model

data class UserFeedResponse(
    val items: List<Items>,
    val more_available: Boolean,
    val num_results: Int,
    val next_max_id: String? = null
)

data class MediaItem(
    val id: String,
    val media_type: Int,
    val caption: Caption? = null,
    val user: FeedUser? = null,
    val image_versions2: ImageVersion? = null,
    val video_versions: List<VideoVersion>? = null,
    val carousel_media: List<MediaItem>? = null,
)

data class VideoVersion(
    val url: String
)

