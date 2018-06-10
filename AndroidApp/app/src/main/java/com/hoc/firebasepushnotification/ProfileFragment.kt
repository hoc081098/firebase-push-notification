package com.hoc.firebasepushnotification

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by Peter Hoc on 04/01/2018.
 */

class ProfileFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var registration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_logout.setOnClickListener {
            launch(UI) {
                firestore.document("$USERS_COLLECTION/${auth.currentUser?.uid}")
                        .update(mapOf(TOKEN to null))
                        .await()
                auth.signOut()
                this@ProfileFragment.context?.startActivity<LoginActivity>()
                activity?.finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val uid = auth.currentUser?.uid
        registration = firestore
                .document("$USERS_COLLECTION/$uid")
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException !== null) {
                        context?.toast(firebaseFirestoreException.message ?: "An error occurred")
                        return@addSnapshotListener
                    }

                    documentSnapshot?.toObject(User::class.java)?.let {
                        Picasso.get()
                                .load(it.imageUrl)
                                .noFade()
                                .into(profile_image)
                        name_text.text = it.name
                        textViewEmail.text = it.email
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        registration?.remove()
    }
}