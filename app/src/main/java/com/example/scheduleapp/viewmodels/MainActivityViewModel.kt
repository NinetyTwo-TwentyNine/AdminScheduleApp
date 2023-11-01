package com.example.scheduleapp.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.data.AuthenticationStatus
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_CABINET_NAME
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_DISCIPLINE_NAME
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_GROUP_NAME
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_TEACHER_NAME
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_GROUP_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_SCHEDULE_LIST
import com.example.scheduleapp.data.Constants.APP_CALENDER_DAY_OF_WEEK
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.data.Date
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleDetailed
import com.example.scheduleapp.models.FirebaseRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val rImplementation: FirebaseRepository,
    private val sPreferences: SharedPreferences
) : ViewModel() {
    var authState: MutableLiveData<AuthenticationStatus> = MutableLiveData()
    var groupsDownloadState: MutableLiveData<DownloadStatus<ArrayList<Data_IntString>>> = MutableLiveData()
    var scheduleDownloadState: MutableLiveData<DownloadStatus<FlatScheduleDetailed>> = MutableLiveData()

    private var groupList = arrayListOf<Data_IntString>()
    private var flatSchedule = FlatScheduleDetailed()

    private lateinit var timer: Timer
    private lateinit var listenerToRemove: OnCompleteListener<DataSnapshot>

    init {
        Log.d("TAG", "Created a view model for the outer app segment successfully.")
    }

    fun resetAuthState() {
        authState = MutableLiveData()
    }

    fun resetDownloadState(onlyGroups: Boolean) {
        if (onlyGroups) {
            groupsDownloadState = MutableLiveData()
        } else {
            scheduleDownloadState = MutableLiveData()
        }
    }

    fun downloadGroupList() {
        groupsDownloadState.value = DownloadStatus.Progress
        setTimeout(5000L, true)

        listenerToRemove = getDownloadListener(true)
        rImplementation.downloadByReference(APP_BD_PATHS_GROUP_LIST)
            .addOnCompleteListener(listenerToRemove)
    }

    fun downloadSchedule() {
        scheduleDownloadState.value = DownloadStatus.Progress
        setTimeout(8000L, false)

        listenerToRemove = getDownloadListener(false)
        rImplementation.downloadByReference(APP_BD_PATHS_SCHEDULE_LIST)
            .addOnCompleteListener(listenerToRemove)
    }

    fun getDownloadListener(onlyGroups: Boolean): OnCompleteListener<DataSnapshot> {
        val listener = OnCompleteListener<DataSnapshot> { task ->
            if (task.isSuccessful) {
                timer.cancel()
                Log.d("TAG", "Successfully downloaded data from the database:")
                Log.d("TAG", task.result.value.toString())

                try {
                    if (onlyGroups) {
                        groupList = Gson().fromJson(
                            task.result.value.toString(),
                            object : TypeToken<ArrayList<Data_IntString>>() {}.type
                        )
                        groupsDownloadState.value = DownloadStatus.Success(groupList)
                    } else {
                        flatSchedule = Gson().fromJson(
                            task.result.value.toString(),
                            FlatScheduleDetailed::class.java
                        )
                        scheduleDownloadState.value = DownloadStatus.Success(flatSchedule)
                    }
                    Log.d("TAG", "Successfully read and converted the data.")
                } catch (e: Exception) {
                    if (onlyGroups) {
                        groupsDownloadState.value = DownloadStatus.Error(e.message.toString())
                    } else {
                        scheduleDownloadState.value = DownloadStatus.Error(e.message.toString())
                    }
                    Log.d("TAG", "Failed to convert the data: ${e.message}")
                }
            } else {
                if (onlyGroups) {
                    groupsDownloadState.value = DownloadStatus.Error("Connection or network error.")
                } else {
                    scheduleDownloadState.value = DownloadStatus.Error("Connection or network error.")
                }
                Log.d("TAG", "Failed to download data from the database.")
            }
        }
        return listener
    }

    private fun setTimeout(time: Long, onlyGroups: Boolean) {
        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    if (onlyGroups) {
                        groupsDownloadState.value = DownloadStatus.WeakProgress("Looks like there are some problems with connection...")
                    } else {
                        scheduleDownloadState.value = DownloadStatus.WeakProgress("Looks like there are some problems with connection...")
                    }
                }
            }
        }
        timer.schedule(timerTask, time)
    }

    fun getSchedule(): FlatScheduleDetailed {
        return flatSchedule
    }

    fun getGroupNames(): ArrayList<String> {
        val groupNames = arrayListOf<String>()
        for (item in groupList) {
            groupNames.add(item.title!!)
        }
        return groupNames
    }

    fun getDayWithOffset(index: Int): Date {
        var position = index - PAGE_COUNT/2
        val c = Calendar.getInstance()

        if (position != 0) {
            c.add(Calendar.DATE, position)
        }

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        return Date(year, month, day)
    }

    fun getDayToTab(index: Int): String {
        val position = index - PAGE_COUNT/2
        val c = Calendar.getInstance()

        if (position != 0) {
            c.add(Calendar.DATE, position)
        }

        val weekDay = APP_CALENDER_DAY_OF_WEEK[c.get(Calendar.DAY_OF_WEEK) - 1]
        var day = c.get(Calendar.DAY_OF_MONTH).toString()
        if (day.length < 2) { day += "0" }

        return "$weekDay${System.getProperty("line.separator")}$day"
    }

    fun isUserSingedIn(): Boolean {
        if (rImplementation.getCurrentUser() != null) {
            return true
        }
        return false
    }

    fun getUserEmail(): String? {
        if (!isUserSingedIn()) {
            return null
        }
        return rImplementation.getCurrentUser()!!.email
    }

    fun signIn(email: String, password: String, newAccount: Boolean) {
        authState.value = AuthenticationStatus.Progress
        rImplementation.signIn(email, password, newAccount).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                authState.value = AuthenticationStatus.Success
            } else {
                authState.value = AuthenticationStatus.Error(task.exception!!.message.toString())
            }
        }
    }

    fun signOut() {
        rImplementation.signOut()
    }

    fun sendResetMessage(email: String) {
        authState.value = AuthenticationStatus.Progress
        rImplementation.sendResetMessage(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                authState.value = AuthenticationStatus.Success
            } else {
                authState.value = AuthenticationStatus.Error(task.exception!!.message.toString())
            }
        }
    }

    fun getParametersByName(name: String): ArrayList<Data_IntString> {
        return when(name) {
            APP_ADMIN_PARAMETERS_DISCIPLINE_NAME -> flatSchedule.lessonList
            APP_ADMIN_PARAMETERS_TEACHER_NAME -> flatSchedule.teacherList
            APP_ADMIN_PARAMETERS_GROUP_NAME -> flatSchedule.groupList
            APP_ADMIN_PARAMETERS_CABINET_NAME -> flatSchedule.cabinetList
            else -> arrayListOf()
        }
    }

    fun <T> editPreferences(preference: String, value: T) {
        val sEdit: SharedPreferences.Editor
        if (preference.contains("_BOOL")) {
            sEdit = sPreferences.edit().putBoolean(preference, (value as Boolean))
        } else {
            sEdit = sPreferences.edit().putString(preference, (value as String))
        }
        sEdit.apply()
    }

    fun <T> getPreference(preference: String, defValue: T): T {
        if (preference.contains("_BOOL")) {
            return (sPreferences.getBoolean(preference, (defValue as Boolean)) as T)
        } else {
            return (sPreferences.getString(preference, (defValue as String)) as T)
        }
    }
}