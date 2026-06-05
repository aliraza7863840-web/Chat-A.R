package com.paisachat.data

data class LocalMessage(
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val messageText: String
)
