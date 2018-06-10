package com.hoc.firebasepushnotification

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

/**
 * Created by Peter Hoc on 04/01/2018.
 */

class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            val email = edit_email.text.toString()
            val password = edit_password.text.toString()
            when {
                email.isBlank() || email.isBlank() -> toast("Email and password cannot be blank")
                !email.isValidEmail() -> toast("Invalid email address")
                password.length < 6 -> toast("Password is too short. Minimize length is 6")
                else -> login(email, password)
            }
        }

        button_need_a_new_account.setOnClickListener {
            startActivity<RegisterActivity>()
        }
    }

    private fun login(email: String, password: String) {
        val task = async(UI) {
            progress_bar_login.visibility = View.VISIBLE
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val token = FirebaseInstanceId.getInstance().token
            firestore.document("$USERS_COLLECTION/${authResult.user.uid}")
                    .update(mapOf(TOKEN to token))
            progress_bar_login.visibility = View.INVISIBLE
            startActivity<MainActivity>()
            finish()
        }

        launch(UI) {
            try {
                task.await()
            } catch (exception: Throwable) {
                toast("Error: ${exception.message}")
                progress_bar_login.visibility = View.INVISIBLE
            }
        }
    }
}

