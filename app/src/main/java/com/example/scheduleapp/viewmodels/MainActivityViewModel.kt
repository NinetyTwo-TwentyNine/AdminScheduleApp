package com.example.scheduleapp.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.data.*
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_CABINET_NAME
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_DISCIPLINE_NAME
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_GROUP_NAME
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_TEACHER_NAME
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_CABINET_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_DISCIPLINE_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_GROUP_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_SCHEDULE_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_TEACHER_LIST
import com.example.scheduleapp.data.Constants.APP_CALENDER_DAY_OF_WEEK
import com.example.scheduleapp.data.Constants.APP_WEAK_CONNECTION_WARNING
import com.example.scheduleapp.data.Date
import com.example.scheduleapp.models.FirebaseRepository
import com.example.scheduleapp.utils.Utils
import com.google.android.gms.tasks.OnCompleteListener
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
    private var flatSchedule = FlatScheduleDetailed()

    init {
        Log.d("TAG", "Created a view model for the outer app segment successfully.")
    }

    fun getReferenceByIndex(id: Int): String {
        return when(APP_ADMIN_PARAMETERS_LIST[id]) {
            APP_ADMIN_PARAMETERS_DISCIPLINE_NAME -> APP_BD_PATHS_DISCIPLINE_LIST
            APP_ADMIN_PARAMETERS_TEACHER_NAME -> APP_BD_PATHS_TEACHER_LIST
            APP_ADMIN_PARAMETERS_GROUP_NAME -> APP_BD_PATHS_GROUP_LIST
            APP_ADMIN_PARAMETERS_CABINET_NAME -> APP_BD_PATHS_CABINET_LIST
            else -> APP_BD_PATHS_SCHEDULE_LIST
        }
    }

    fun getParametersByReference(reference: String, link: Boolean = false): ArrayList<Data_IntString> {
        val table = when(reference) {
            APP_BD_PATHS_DISCIPLINE_LIST -> flatSchedule.lessonList
            APP_BD_PATHS_TEACHER_LIST -> flatSchedule.teacherList
            APP_BD_PATHS_GROUP_LIST -> flatSchedule.groupList
            APP_BD_PATHS_CABINET_LIST -> flatSchedule.cabinetList
            else -> arrayListOf()
        }

        if (link) {
            return table
        }
        return Utils.getDataIntStringArrayDeepCopy(table)
    }

    fun getParametersByIndex(id: Int, link: Boolean = false): ArrayList<Data_IntString> {
        return getParametersByReference(getReferenceByIndex(id), link)
    }

    fun updateParametersByReference(reference: String, paramsArr: ArrayList<Data_IntString>) {
        val table_link = getParametersByReference(reference, true)
        updateParameters(table_link, paramsArr)
    }
    fun updateParametersByIndex(id: Int, paramsArr: ArrayList<Data_IntString>) {
        val table_link = getParametersByIndex(id, true)
        updateParameters(table_link, paramsArr)
    }
    private fun updateParameters(table_link: ArrayList<Data_IntString>, paramsArr: ArrayList<Data_IntString>) {
        table_link.clear()
        paramsArr.forEach {
            table_link.add(Data_IntString(it.id, it.title))
        }
    }

    fun downloadParametersList(parametersDownloadState: MutableLiveData<DownloadStatus<ArrayList<Data_IntString>>>, reference: String) {
        parametersDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(parametersDownloadState, 5000L, true)

        val listener = getParametersDownloadListener(parametersDownloadState, timer, reference)
        rImplementation.downloadByReference(reference)
            .addOnCompleteListener(listener)
    }

    fun downloadSchedule(scheduleDownloadState: MutableLiveData<DownloadStatus<FlatScheduleDetailed>>) {
        scheduleDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(scheduleDownloadState, 8000L, false)

        val listener = getScheduleDownloadListener(scheduleDownloadState, timer)
        rImplementation.downloadByReference(APP_BD_PATHS_SCHEDULE_LIST)
            .addOnCompleteListener(listener)
    }

    private fun <T> getDownloadListener(downloadState: MutableLiveData<DownloadStatus<T>>, timer: Timer, reference: String? = null): OnCompleteListener<DataSnapshot> {
        val listener = OnCompleteListener<DataSnapshot> { task ->
            timer.cancel()
            if (task.isSuccessful) {
                Log.d("TAG", "Successfully downloaded data from the database:")
                Log.d("TAG", task.result.value.toString())

                try {
                    if (reference != null) {
                        val table: ArrayList<Data_IntString> = Gson().fromJson(
                            task.result.value.toString(),
                            object : TypeToken<ArrayList<Data_IntString>>() {}.type
                        )
                        updateParametersByReference(reference, table)
                        downloadState.value = (DownloadStatus.Success(table) as DownloadStatus.Success<T>)
                    } else {
                        flatSchedule = Gson().fromJson(
                            task.result.value.toString(),
                            FlatScheduleDetailed::class.java
                        )
                        downloadState.value = (DownloadStatus.Success(flatSchedule) as DownloadStatus.Success<T>)
                    }
                    Log.d("TAG", "Successfully read and converted the data.")
                } catch (e: Exception) {
                    if (reference != null) {
                        downloadState.value = DownloadStatus.Error(e.message.toString())
                    } else {
                        downloadState.value = DownloadStatus.Error(e.message.toString())
                    }
                    Log.d("TAG", "Failed to convert the data: ${e.message}")
                }
            } else {
                if (reference != null) {
                    downloadState.value = DownloadStatus.Error("Connection or network error.")
                } else {
                    downloadState.value = DownloadStatus.Error("Connection or network error.")
                }
                Log.d("TAG", "Failed to download data from the database.")
            }
        }
        return listener
    }
    private fun getParametersDownloadListener(downloadState: MutableLiveData<DownloadStatus<ArrayList<Data_IntString>>>, timer: Timer, reference: String): OnCompleteListener<DataSnapshot> {
        return getDownloadListener(downloadState, timer, reference)
    }
    private fun getScheduleDownloadListener(downloadState: MutableLiveData<DownloadStatus<FlatScheduleDetailed>>, timer: Timer): OnCompleteListener<DataSnapshot> {
        return getDownloadListener(downloadState, timer)
    }

    private fun <T> setDownloadTimeout(downloadStatus: MutableLiveData<DownloadStatus<T>>, time: Long, onlyParams: Boolean): Timer {
        return performTimerEvent(
            {
                if (onlyParams) {
                    downloadStatus.value =
                        DownloadStatus.WeakProgress(APP_WEAK_CONNECTION_WARNING)
                } else {
                    downloadStatus.value =
                        DownloadStatus.WeakProgress(APP_WEAK_CONNECTION_WARNING)
                }
            }, time)
    }

    private fun setUploadTimeout(uploadState: MutableLiveData<UploadStatus>, time: Long, onlyParams: Boolean): Timer {
        return performTimerEvent(
            { uploadState.value = UploadStatus.WeakProgress(APP_WEAK_CONNECTION_WARNING) }, time
        )
    }

    fun performTimerEvent(function: ()->Unit, time: Long): Timer {
        val event_timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    function()
                }
            }
        }
        event_timer.schedule(timerTask, time)
        return event_timer
    }

    fun <T> uploadData(uploadState: MutableLiveData<UploadStatus>, reference: String, info: T) {
        uploadState.value = UploadStatus.Progress
        val timer = setUploadTimeout(uploadState, 5000L, false)

        rImplementation.uploadByReference(reference, info).addOnCompleteListener { task ->
            timer.cancel()
            if (task.isSuccessful) {
                uploadState.value = UploadStatus.Success
            } else {
                uploadState.value = UploadStatus.Error(task.exception!!.message.toString())
            }
        }
    }

    fun uploadParameters(uploadState: MutableLiveData<UploadStatus>, index: Int, paramsArr: ArrayList<Data_IntString>) {
        uploadData(uploadState, getReferenceByIndex(index), paramsArr)
    }

    fun getSchedule(): FlatScheduleDetailed {
        return flatSchedule
    }

    fun getParametersList(reference: String): ArrayList<String> {
        val table: ArrayList<String> = arrayListOf()
        getParametersByReference(reference).forEach { table.add(it.title!!) }
        return table
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
        if (day.length < 2) { day = "0$day" }

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

    fun signIn(authState: MutableLiveData<AuthenticationStatus>, email: String, password: String, newAccount: Boolean) {
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

    fun sendResetMessage(authState: MutableLiveData<AuthenticationStatus>, email: String) {
        authState.value = AuthenticationStatus.Progress
        rImplementation.sendResetMessage(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                authState.value = AuthenticationStatus.Success
            } else {
                authState.value = AuthenticationStatus.Error(task.exception!!.message.toString())
            }
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