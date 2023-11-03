package com.example.scheduleapp.models

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot

interface FirebaseRepository {

    fun downloadByReference(reference: String): Task<DataSnapshot>

    fun <T> uploadByReference(reference: String, info: T): Task<Void>

    fun getCurrentUser(): FirebaseUser?

    fun sendResetMessage(email: String): Task<Void>

    fun signIn(email: String, password: String, newAccount: Boolean): Task<AuthResult>

    fun signOut()
}