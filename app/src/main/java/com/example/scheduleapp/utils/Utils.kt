package com.example.scheduleapp.utils

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.example.scheduleapp.data.Data_IntIntIntArrayArray
import com.example.scheduleapp.data.Data_IntString
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import java.util.concurrent.Executor

object Utils {

    fun getBlankStringsChecker(textInput: EditText, setButtonVisibility: ()->Unit): TextWatcher {
        return object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (textInput.text.toString().replace(" ", "") == textInput.text.toString()) {
                    setButtonVisibility()
                } else {
                    textInput.setText(textInput.text.toString().replace(" ", ""))
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }
    }

    fun getDataIntStringArrayDeepCopy(origArr: ArrayList<Data_IntString>): ArrayList<Data_IntString> {
        val newArr = arrayListOf<Data_IntString>()
        origArr.forEach { newArr.add(Data_IntString(it.id, it.title)) }
        return newArr
    }

    /**
     * As firebase doesn't give us error on timeout and just keeps retrying,
     * in the earlier versions of the app we would just generate our own Unsuccessful Event.
     * Note that no other function is called except for isSuccessful().
     * However, this method, of course, wasn't exactly architecturally correct in any way,
     * as we essentially were just faking a server response through code.

    fun createUnsuccessfulTask(): Task<DataSnapshot> {
        return object : Task<DataSnapshot>() {
            override fun addOnFailureListener(p0: OnFailureListener): Task<DataSnapshot> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Activity,
                p1: OnFailureListener
            ): Task<DataSnapshot> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Executor,
                p1: OnFailureListener
            ): Task<DataSnapshot> {
                TODO("Not yet implemented")
            }

            override fun getException(): java.lang.Exception? {
                TODO("Not yet implemented")
            }

            override fun getResult(): DataSnapshot {
                TODO("Not yet implemented")
            }

            override fun <X : Throwable?> getResult(p0: Class<X>): DataSnapshot {
                TODO("Not yet implemented")
            }

            override fun isCanceled(): Boolean {
                TODO("Not yet implemented")
            }

            override fun isComplete(): Boolean {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Executor,
                p1: OnSuccessListener<in DataSnapshot>
            ): Task<DataSnapshot> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Activity,
                p1: OnSuccessListener<in DataSnapshot>
            ): Task<DataSnapshot> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(p0: OnSuccessListener<in DataSnapshot>): Task<DataSnapshot> {
                TODO("Not yet implemented")
            }

            override fun isSuccessful(): Boolean {
                return false
            }

        }
    }
    */
}