package com.hoc.firebasepushnotification

import android.content.Context
import android.content.Intent
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.Task
import kotlin.coroutines.experimental.suspendCoroutine

fun Context.toast(charSequence: CharSequence) = Toast.makeText(
        this,
        charSequence,
        Toast.LENGTH_SHORT
).show()

inline fun <reified T> Context.startActivity() = startActivity(Intent(this, T::class.java))

infix fun ViewGroup.inflate(@LayoutRes resource: Int): View {
    val layoutInflater = LayoutInflater.from(context)
    return layoutInflater.inflate(resource, this, false)
}

suspend fun <TResult> Task<TResult>.await() = suspendCoroutine<TResult> { continuation ->
    addOnSuccessListener {
        continuation.resume(it)
    }.addOnFailureListener {
        continuation.resumeWithException(it)
    }
}


operator fun <T> Intent.get(name: String): T {
    @Suppress("UNCHECKED_CAST")
    return extras.get(name) as? T ?: throw TypeCastException()
}

fun String.isValidEmail() = """^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,6}$""".toRegex(RegexOption.IGNORE_CASE).matches(this)