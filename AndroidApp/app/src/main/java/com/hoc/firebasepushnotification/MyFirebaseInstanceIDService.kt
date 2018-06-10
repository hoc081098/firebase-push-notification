package com.hoc.firebasepushnotification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by Peter Hoc on 10/01/2018.
 */

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    private val auth = FirebaseAuth.getInstance()

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "Refreshed token: " + refreshedToken!!)
        sendRegistrationToServer(refreshedToken)
    }

    private fun sendRegistrationToServer(refreshedToken: String?) {
        val currentUid = auth.currentUser?.uid ?: return
        val map = mapOf(TOKEN to refreshedToken)
        FirebaseFirestore.getInstance()
                .document("$USERS_COLLECTION/$currentUid")
                .update(map)
                .addOnSuccessListener { Log.d(TAG, "Update token success") }
                .addOnFailureListener { Log.d(TAG, "Update token failure") }
    }

    companion object {
        private const val TAG = "MyFirebaseInstance"
    }
}
