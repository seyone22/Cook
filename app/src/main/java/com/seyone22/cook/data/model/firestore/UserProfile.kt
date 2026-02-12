package com.seyone22.cook.data.model.firestore

data class UserProfile(
    val uid: String,
    val displayName: String? = null,
    val profilePictureUrl: String? = null,
    val bio: String? = null
)