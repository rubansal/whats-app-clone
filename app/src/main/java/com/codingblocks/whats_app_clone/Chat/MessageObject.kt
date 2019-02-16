package com.codingblocks.whats_app_clone.Chat

data class MessageObject(var messageId:String, var senderId:String, var message:String, var mediaUrlList:ArrayList<String>)