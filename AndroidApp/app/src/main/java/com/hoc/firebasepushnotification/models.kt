package com.hoc.firebasepushnotification

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
class User(
        val name: String = "",
        val email: String = "",
        @get:PropertyName(USER_IMAGE_URL) @set:PropertyName(USER_IMAGE_URL) var imageUrl: String = "",
        var id: String = ""
)

@IgnoreExtraProperties
class Notification(
        val message: String = "",
        @get:PropertyName(NOTI_SEND_ID) @set:PropertyName(NOTI_SEND_ID) var sendId: String = ""
)