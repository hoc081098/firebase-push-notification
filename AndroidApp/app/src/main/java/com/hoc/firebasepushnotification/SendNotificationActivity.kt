package com.hoc.firebasepushnotification

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_send_notification.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

/**
 * Created by Peter Hoc on 08/01/2018.
 */

class SendNotificationActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var currentId: String
    private lateinit var receiverId: String
    private lateinit var receiverName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_notification)

        currentId = auth.currentUser!!.uid
        receiverId = intent[RECEIVER_ID]
        receiverName = intent[RECEIVER_NAME]

        text_send_to.text = getString(R.string.send_to, receiverName)

        button_send.setOnClickListener {
            val message = edit_message.text.toString()
            when {
                message.isBlank() -> toast("Message cannot be blank")
                else -> sendMessage(currentId, message, receiverId)
            }
        }

        firestore.document("$USERS_COLLECTION/$receiverId")
                .addSnapshotListener(this) { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException !== null) {
                        toast("Error: ${firebaseFirestoreException.message}")
                        return@addSnapshotListener
                    }
                    getString(R.string.send_to, documentSnapshot[USER_NAME])
                }
    }

    private fun sendMessage(currentId: String?, message: String, receiverId: String?) {
        if (currentId === null) return

        val sendTask = async(UI) {
            progress_bar.visibility = VISIBLE

            val notification = Notification(message, currentId)

            firestore.collection("$USERS_COLLECTION/$receiverId/$NOTIFICATIONS_COLLECTION")
                    .add(notification)
                    .await()

            toast("Sent successfully")
            edit_message.setText("")

            progress_bar.visibility = INVISIBLE
        }

        launch(UI) {
            try {
                sendTask.await()
            } catch (exception: Throwable) {
                progress_bar.visibility = INVISIBLE
                toast("Error: ${exception.message}. Try again")
            }
        }
    }
}
