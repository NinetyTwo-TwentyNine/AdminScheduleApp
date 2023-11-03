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
    var parametersDownloadState: MutableLiveData<DownloadStatus<ArrayList<Data_IntString>>> = MutableLiveData()
    var scheduleDownloadState: MutableLiveData<DownloadStatus<FlatScheduleDetailed>> = MutableLiveData()
    var uploadState: MutableLiveData<UploadStatus> = MutableLiveData()

    private var flatSchedule = FlatScheduleDetailed()

    private lateinit var timer: Timer
    private lateinit var listenerToRemove: OnCompleteListener<DataSnapshot>

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

    fun resetAuthState() {
        authState = MutableLiveData()
    }

    fun resetDownloadState(onlyParams: Boolean = false) {
        if (onlyParams) {
            parametersDownloadState = MutableLiveData()
        } else {
            scheduleDownloadState = MutableLiveData()
        }
    }

    fun resetUploadState() {
        uploadState = MutableLiveData()
    }

    fun downloadParametersList(reference: String) {
        parametersDownloadState.value = DownloadStatus.Progress
        setDownloadTimeout(3000L, true)

        listenerToRemove = getDownloadListener(reference)
        rImplementation.downloadByReference(reference)
            .addOnCompleteListener(listenerToRemove)
    }

    fun downloadSchedule() {
        scheduleDownloadState.value = DownloadStatus.Progress
        setDownloadTimeout(8000L, false)

        listenerToRemove = getDownloadListener()
        rImplementation.downloadByReference(APP_BD_PATHS_SCHEDULE_LIST)
            .addOnCompleteListener(listenerToRemove)
    }

    fun getDownloadListener(reference: String? = null): OnCompleteListener<DataSnapshot> {
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
                        parametersDownloadState.value = DownloadStatus.Success(table)
                    } else {
                        flatSchedule = Gson().fromJson(
                            task.result.value.toString(),
                            FlatScheduleDetailed::class.java
                        )
                        scheduleDownloadState.value = DownloadStatus.Success(flatSchedule)
                    }
                    Log.d("TAG", "Successfully read and converted the data.")
                } catch (e: Exception) {
                    if (reference != null) {
                        parametersDownloadState.value = DownloadStatus.Error(e.message.toString())
                    } else {
                        scheduleDownloadState.value = DownloadStatus.Error(e.message.toString())
                    }
                    Log.d("TAG", "Failed to convert the data: ${e.message}")
                }
            } else {
                if (reference != null) {
                    parametersDownloadState.value = DownloadStatus.Error("Connection or network error.")
                } else {
                    scheduleDownloadState.value = DownloadStatus.Error("Connection or network error.")
                }
                Log.d("TAG", "Failed to download data from the database.")
            }
        }
        return listener
    }

    private fun setDownloadTimeout(time: Long, onlyParams: Boolean) {
        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    if (onlyParams) {
                        parametersDownloadState.value = DownloadStatus.WeakProgress(APP_WEAK_CONNECTION_WARNING)
                    } else {
                        scheduleDownloadState.value = DownloadStatus.WeakProgress(APP_WEAK_CONNECTION_WARNING)
                    }
                }
            }
        }
        timer.schedule(timerTask, time)
    }

    private fun setUploadTimeout(time: Long, onlyParams: Boolean) {
        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    uploadState.value = UploadStatus.WeakProgress(APP_WEAK_CONNECTION_WARNING)
                }
            }
        }
        timer.schedule(timerTask, time)
    }

    fun <T> uploadData(reference: String, info: T) {
        uploadState.value = UploadStatus.Progress
        setDownloadTimeout(5000L, false)

        rImplementation.uploadByReference(reference, info).addOnCompleteListener { task ->
            timer.cancel()
            if (task.isSuccessful) {
                uploadState.value = UploadStatus.Success
            } else {
                authState.value = AuthenticationStatus.Error(task.exception!!.message.toString())
            }
        }
    }

    fun uploadParameters(index: Int, paramsArr: ArrayList<Data_IntString>) {
        uploadData(getReferenceByIndex(index), paramsArr)
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