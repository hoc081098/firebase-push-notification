package com.hoc.firebasepushnotification

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.message_layout.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Peter Hoc on 13/01/2018.
 */


class NotificationActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private var registration: ListenerRegistration? = null

    private lateinit var senderId: String
    private lateinit var senderName: String
    private lateinit var senderImageUrl: String
    private lateinit var body: String
    private var sendTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        senderId =intent[SENDER_ID]
        senderName = intent[SENDER_NAME]
        senderImageUrl = intent[SENDER_IMAGE_URL]
        body = intent[BODY]
        sendTime = intent[SEND_TIME]


        text_send_user.text = senderName
        text_body.text = body
        val date = Date(sendTime)
        text_time.text = SimpleDateFormat("hh:mm:ss, dd-MM-yyyy").format(date)
        Picasso.get()
                .load(senderImageUrl)
                .noFade()
                .into(image_view)

        buttonReply.setOnClickListener {
            val intent = Intent(this, SendNotificationActivity::class.java).apply {
                putExtra(RECEIVER_ID, senderId)
                putExtra(RECEIVER_NAME, senderName)
            }
            startActivity(intent)
        }
    }


    override fun onStart() {
        super.onStart()
        registration = firestore
                .document("$USERS_COLLECTION/$senderId")
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException !== null) {
                        toast(firebaseFirestoreException.message ?: "An error occurred")
                        return@addSnapshotListener
                    }

                    documentSnapshot?.toObject(User::class.java)?.let {
                        text_send_user.text = it.name
                        Picasso.get()
                                .load(it.imageUrl)
                                .noFade()
                                .into(image_view)
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        registration?.remove()
    }
}