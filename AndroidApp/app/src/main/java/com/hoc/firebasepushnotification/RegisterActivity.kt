package com.hoc.firebasepushnotification

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

/**
 * Created by Peter Hoc on 04/01/2018.
 */


class RegisterActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val imagesStorage = FirebaseStorage.getInstance().reference.child("images")
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        button_back_to_login.setOnClickListener {
            finish()
        }

        image.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE_CODE)
        }

        button_resgiter.setOnClickListener {
            if (imageUri === null) {
                toast("Please select an image")
                return@setOnClickListener
            }

            val name = edit_user.text.toString()
            val password = edit_password.text.toString()
            val email = edit_email.text.toString()
            when {
                name.isBlank() || email.isBlank() || password.isBlank() -> toast("Name, email, password cannot be blank")
                !email.isValidEmail() -> toast("Invalid email address")
                password.length < 6 -> toast("Password is too short. Minimize length is 6")
                else -> register(email, password, name)
            }
        }
    }

    private fun register(email: String, password: String, name: String) {

        val task = async(UI) {
            progress_bar_register.visibility = View.VISIBLE
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val userId = authResult.user.uid
            val uploadSnapshot = imagesStorage.child("$userId.jpg").putFile(imageUri!!).await()
            val token = FirebaseInstanceId.getInstance().token
            val map = mapOf(
                    USER_NAME to name,
                    USER_EMAIL to email,
                    USER_IMAGE_URL to uploadSnapshot.downloadUrl.toString(),
                    TOKEN to token
            )
            firestore.document("$USERS_COLLECTION/$userId").set(map).await()

            toast("Register successfully")
            progress_bar_register.visibility = View.INVISIBLE

            startActivity<MainActivity>()
            finish()
        }

        launch(UI) {
            try {
                task.await()
            } catch (exception: Throwable) {
                toast("Error ${exception.message}")
                progress_bar_register.visibility = View.INVISIBLE
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_CODE -> if (resultCode == Activity.RESULT_OK) {
                imageUri = data?.data
                Picasso.get()
                        .load(imageUri)
                        .noFade()
                        .into(image)

            }
        }
    }

    companion object {
        const val PICK_IMAGE_CODE = 1
    }
}