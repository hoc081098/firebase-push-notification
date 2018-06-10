package com.hoc.firebasepushnotification

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.notification_item_layout.view.*

/**
 * Created by Peter Hoc on 04/01/2018.
 */


class NotificationAdapter(
        options: FirestoreRecyclerOptions<Notification>
) : FirestoreRecyclerAdapter<Notification, NotificationAdapter.NotificationHolder>(options) {
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NotificationHolder(parent inflate R.layout.notification_item_layout, firestore)

    override fun onBindViewHolder(holder: NotificationHolder, position: Int, model: Notification) = holder.bind(model)

    class NotificationHolder(itemView: View, val firestore: FirebaseFirestore) : RecyclerView.ViewHolder(itemView) {
        fun bind(model: Notification) {
            text_message.text = model.message
            firestore.document("$USERS_COLLECTION/${model.sendId}")
                    .get()
                    .addOnSuccessListener {
                        userNameText.text = it[USER_NAME].toString()
                        Picasso.get()
                                .load(it[USER_IMAGE_URL].toString())
                                .noFade()
                                .into(userImage)
                    }
        }

        private val userNameText = itemView.user_name_text!!
        private val userImage = itemView.image_user!!
        private val text_message = itemView.text_message!!
    }
}

class NotificationsFragment : Fragment() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var notificationAdapter: NotificationAdapter
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_notifications, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = firebaseFirestore
                .collection("$USERS_COLLECTION/${firebaseAuth.currentUser?.uid}/$NOTIFICATIONS_COLLECTION")
        val options = FirestoreRecyclerOptions.Builder<Notification>()
                .setQuery(query, Notification::class.java)
                .build()
        notificationAdapter = NotificationAdapter(options)

        recycler_notifications.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        notificationAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        notificationAdapter.stopListening()
    }
}