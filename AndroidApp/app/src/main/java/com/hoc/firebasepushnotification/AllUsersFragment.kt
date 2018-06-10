package com.hoc.firebasepushnotification

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_all_users.*
import kotlinx.android.synthetic.main.user_item_layout.view.*

/**
 * Created by Peter Hoc on 04/01/2018.
 */

class UserAdapter(
        options: FirestoreRecyclerOptions<User>,
        private val onClickListener: (User) -> Unit
) : FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            UserViewHolder(parent inflate R.layout.user_item_layout)

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) =
            holder.bind(model, onClickListener)


    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameText = itemView.user_name_text!!
        private val userImage = itemView.image_user!!
        private val userEmailText = itemView.text_user_email!!

        fun bind(model: User, onClickListener: (User) -> Unit) {
            userNameText.text = model.name
            userEmailText.text = model.email
            Picasso.get()
                    .load(model.imageUrl)
                    .noFade()
                    .into(userImage)
            itemView.setOnClickListener { onClickListener(model) }
        }
    }
}

class AllUsersFragment : Fragment() {
    private lateinit var usersAdapter: UserAdapter
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_all_users, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = firebaseFirestore
                .collection(USERS_COLLECTION)
                .orderBy(USER_NAME)
        val options = FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query) { it.toObject(User::class.java).apply { id = it.id } }
                .build()
        usersAdapter = UserAdapter(options) {
            val intent = Intent(context, SendNotificationActivity::class.java).apply {
                putExtra(RECEIVER_ID, it.id)
                putExtra(RECEIVER_NAME, it.name)
            }
            startActivity(intent)
        }

        recycler_users.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = usersAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        usersAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        usersAdapter.stopListening()
    }
}




