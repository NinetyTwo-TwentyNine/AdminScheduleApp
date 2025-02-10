package com.example.adminscheduleapp.models

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot

interface FirebaseRepository {

    fun getCurrentUser(): FirebaseUser?

    fun sendResetMessage(email: String): Task<Void>

    fun signIn(email: String, password: String, newAccount: Boolean): Task<AuthResult>

    fun signOut()
}