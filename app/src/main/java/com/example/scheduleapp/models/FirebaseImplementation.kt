package com.example.adminscheduleapp.models

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class FirebaseImplementation(
    private val fDatabase: FirebaseDatabase,
    private val fAuth: FirebaseAuth
) : FirebaseRepository {
    override fun downloadByReference(reference: String): Task<DataSnapshot> {
        return fDatabase.getReference(reference).get()
    }

    override fun <T> uploadByReference(reference: String, info: T): Task<Void> {
        return fDatabase.getReference(reference).setValue(info)
    }

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

    /*fun something()
    {
        fDatabase.getReference("FlatScheduleDetailed303/schedule_test1").setValue(null).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("UPLOAD_TEST", "uploaded data successfully")
            }
            else {
                Log.d("UPLOAD_TEST", "data upload error: " + it.exception!!.message.toString())
            }
        }
    }*/

    override fun signOut() {
        fAuth.signOut()
    }
}