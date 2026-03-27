package com.example.appestudio.data.models

// Chat list item returned from GET /api/chats/:userId
data class ChatDto(
    val _id: String,
    val name: String,
    val isGroup: Boolean,
    val participants: List<String>,
    val participantNames: List<String>,
    val lastMessage: String,
    val lastMessageAt: String
)

// Message returned from GET /api/chats/:chatId/messages
data class MessageDto(
    val _id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val mediaUrl: String?,
    val mediaType: String?,
    val createdAt: String
)

// Request body for POST /api/chats/:chatId/messages
data class SendMessageRequest(
    val senderId: String,
    val senderName: String,
    val text: String
)

// Request body for POST /api/chats
data class CreateChatRequest(
    val userId: String,
    val userId2: String,
    val userName: String,
    val userName2: String,
    val isGroup: Boolean = false,
    val groupName: String = ""
)

// Request body for PUT /api/chats/:chatId
data class UpdateChatRequest(
    val name: String,
    val participants: List<String>,
    val participantNames: List<String>
)
