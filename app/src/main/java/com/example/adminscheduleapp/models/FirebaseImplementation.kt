package com.example.adminscheduleapp.models

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class FirebaseImplementation(
    private val fAuth: FirebaseAuth
) : FirebaseRepository {
    override fun getCurrentUser(): FirebaseUser? {
        return fAuth.currentUser
    }

    override fun signIn(email: String, password: String, newAccount: Boolean): Task<AuthResult> {
        return if (newAccount) {
            fAuth.createUserWithEmailAndPassword(email, password)
        } else {
            fAuth.signInWithEmailAndPassword(email, password)
        }
    }

    override fun sendResetMessage(email: String): Task<Void> {
        return fAuth.sendPasswordResetEmail(email)
    }

    override fun signOut() {
        fAuth.signOut()
    }
}