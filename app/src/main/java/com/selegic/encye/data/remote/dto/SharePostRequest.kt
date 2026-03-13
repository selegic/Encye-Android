package com.selegic.encye.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SharePostRequest(
    val content: String? = null
)
