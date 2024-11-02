package com.example.adminscheduleapp.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.adminscheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.adminscheduleapp.data.*
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_CABINET_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_DISCIPLINE_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_GROUP_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_TEACHER_NAME
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_BASE
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_BASE_PARAMETERS
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_CABINET_LIST
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_DATE_LIST
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_DISCIPLINE_LIST
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_GROUP_LIST
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_SCHEDULE_BASE
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_SCHEDULE_BASE_NAME_LIST
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_SCHEDULE_CURRENT
import com.example.adminscheduleapp.data.Constants.APP_BD_PATHS_TEACHER_LIST
import com.example.adminscheduleapp.data.Constants.APP_CALENDER_DAY_OF_WEEK
import com.example.adminscheduleapp.data.Constants.APP_PREFERENCES_PUSHES
import com.example.adminscheduleapp.data.Constants.APP_TOAST_WEAK_CONNECTION
import com.example.adminscheduleapp.data.Date
import com.example.adminscheduleapp.models.FirebaseRepository
import com.example.adminscheduleapp.utils.Utils.checkIfItemArraysAreEqual
import com.example.adminscheduleapp.utils.Utils.getById
import com.example.adminscheduleapp.utils.Utils.getFlatScheduleBaseDeepCopy
import com.example.adminscheduleapp.utils.Utils.getFlatScheduleDetailedDeepCopy
import com.example.adminscheduleapp.utils.Utils.getItemArrayDeepCopy
import com.example.adminscheduleapp.utils.Utils.getPossibleId
import com.example.adminscheduleapp.utils.Utils.removeScheduleItemById
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
    private var flatScheduleDetailed = FlatScheduleDetailed()
    private var flatScheduleBase = FlatScheduleBase()
    private var flatScheduleParameters = FlatScheduleParameters()
    private var chosenDayIndex = PAGE_COUNT/2
    private var editMode: Int = APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE

    init {
        Log.d("TAG", "Created a view model for the outer app segment successfully.")
    }

    fun getEditMode(): Int {
        return editMode
    }

    fun setCurrentScheduleEditMode() {
        editMode = APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
    }

    fun setBaseScheduleEditMode() {
        editMode = APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
    }

    fun getReferenceByIndex(id: Int): String {
        return when(APP_ADMIN_PARAMETERS_LIST[id]) {
            APP_ADMIN_PARAMETERS_DISCIPLINE_NAME -> APP_BD_PATHS_DISCIPLINE_LIST
            APP_ADMIN_PARAMETERS_TEACHER_NAME -> APP_BD_PATHS_TEACHER_LIST
            APP_ADMIN_PARAMETERS_GROUP_NAME -> APP_BD_PATHS_GROUP_LIST
            APP_ADMIN_PARAMETERS_CABINET_NAME -> APP_BD_PATHS_CABINET_LIST
            else -> {throw(IllegalStateException("Specific reference not found."))}
        }
    }

    fun getParametersByReference(reference: String, link: Boolean = false): ArrayList<Data_IntString> {
        val table = when(reference) {
            APP_BD_PATHS_DISCIPLINE_LIST -> flatScheduleParameters.lessonList
            APP_BD_PATHS_TEACHER_LIST -> flatScheduleParameters.teacherList
            APP_BD_PATHS_GROUP_LIST -> flatScheduleParameters.groupList
            APP_BD_PATHS_CABINET_LIST -> flatScheduleParameters.cabinetList
            else -> arrayListOf()
        }

        if (link) {
            return table
        }
        return getItemArrayDeepCopy(table)
    }

    fun getParametersByIndex(id: Int, link: Boolean = false): ArrayList<Data_IntString> {
        return getParametersByReference(getReferenceByIndex(id), link)
    }

    private fun updateParametersByReference(reference: String, paramsArr: ArrayList<Data_IntString>) {
        val table_link = getParametersByReference(reference, true)
        updateParameters(table_link, paramsArr)
    }
    private fun updateParametersByIndex(id: Int, paramsArr: ArrayList<Data_IntString>) {
        val table_link = getParametersByIndex(id, true)
        updateParameters(table_link, paramsArr)
    }
    private fun updateParameters(table_link: ArrayList<Data_IntString>, paramsArr: ArrayList<Data_IntString>) {
        table_link.clear()
        paramsArr.forEach {
            table_link.add(Data_IntString(it.id, it.title))
        }
    }

    private fun updateCurrentSchedule(newFlatSchedule: FlatScheduleDetailed) {
        flatScheduleDetailed = getFlatScheduleDetailedDeepCopy(newFlatSchedule)
    }

    private fun updateBaseSchedule(newFlatSchedule: FlatScheduleBase) {
        flatScheduleBase = getFlatScheduleBaseDeepCopy(newFlatSchedule)
    }

    fun <T> downloadParametersList(parametersDownloadState: MutableLiveData<DownloadStatus<T>>, reference: String? = null) {
        parametersDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(parametersDownloadState, 5000L)

        if (reference == null) {
            val listener = getParametersDownloadListener(parametersDownloadState, timer, APP_BD_PATHS_BASE_PARAMETERS)
            rImplementation.downloadByReference(APP_BD_PATHS_BASE_PARAMETERS)
                .addOnCompleteListener(listener)
        } else {
            val listener = getParametersDownloadListener(parametersDownloadState, timer, reference)
            rImplementation.downloadByReference(reference)
                .addOnCompleteListener(listener)
        }
    }

    fun downloadCurrentSchedule(scheduleDownloadState: MutableLiveData<DownloadStatus<FlatScheduleDetailed>>) {
        scheduleDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(scheduleDownloadState, 8000L)

        val listener = getScheduleDownloadListener(scheduleDownloadState, timer, false)
        rImplementation.downloadByReference(APP_BD_PATHS_SCHEDULE_CURRENT)
            .addOnCompleteListener(listener)
    }

    fun downloadBaseSchedule(scheduleDownloadState: MutableLiveData<DownloadStatus<FlatScheduleBase>>) {
        scheduleDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(scheduleDownloadState, 8000L)

        val listener = getScheduleDownloadListener(scheduleDownloadState, timer, true)
        rImplementation.downloadByReference(APP_BD_PATHS_SCHEDULE_BASE)
            .addOnCompleteListener(listener)
    }

    fun downloadEverything(scheduleDownloadState: MutableLiveData<DownloadStatus<FlatSchedule>>) {
        scheduleDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(scheduleDownloadState, 8000L)

        val listener = getEverythingDownloadListener(scheduleDownloadState, timer)
        rImplementation.downloadByReference(APP_BD_PATHS_BASE)
            .addOnCompleteListener(listener)
    }

    private fun <T> getDownloadListener(downloadState: MutableLiveData<DownloadStatus<T>>, timer: Timer, reference: String): OnCompleteListener<DataSnapshot> {
        val listener = OnCompleteListener<DataSnapshot> { task ->
            timer.cancel()
            if (task.isSuccessful) {
                Log.d("TAG", "Successfully downloaded data from the database:")
                Log.d("TAG", task.result.value.toString())

                try {
                    when (reference) {
                        APP_BD_PATHS_BASE -> {
                            val flatScheduleAll = Gson().fromJson(
                                task.result.value.toString(),
                                FlatSchedule::class.java
                            )
                            flatScheduleBase = flatScheduleAll.BaseSchedules
                            flatScheduleDetailed = flatScheduleAll.CurrentSchedules
                            flatScheduleParameters = flatScheduleAll.BaseParameters
                            downloadState.value = DownloadStatus.Success(flatScheduleAll as T)
                        }
                        APP_BD_PATHS_SCHEDULE_CURRENT -> {
                            flatScheduleDetailed = Gson().fromJson(
                                task.result.value.toString(),
                                FlatScheduleDetailed::class.java
                            )
                            downloadState.value = DownloadStatus.Success(flatScheduleDetailed as T)
                        }
                        APP_BD_PATHS_SCHEDULE_BASE -> {
                            flatScheduleBase = Gson().fromJson(
                                task.result.value.toString(),
                                FlatScheduleBase::class.java
                            )
                            downloadState.value = DownloadStatus.Success(flatScheduleBase as T)
                        }
                        APP_BD_PATHS_BASE_PARAMETERS -> {
                            flatScheduleParameters = Gson().fromJson(
                                task.result.value.toString(),
                                FlatScheduleParameters::class.java
                            )
                            downloadState.value = DownloadStatus.Success(flatScheduleParameters as T)
                        }
                        else -> {
                            val table: ArrayList<Data_IntString> = Gson().fromJson(
                                task.result.value.toString(),
                                object : TypeToken<ArrayList<Data_IntString>>() {}.type
                            )
                            updateParametersByReference(reference, table)
                            downloadState.value = DownloadStatus.Success(table as T)
                        }
                    }
                    Log.d("TAG", "Successfully read and converted the data.")
                } catch (e: Exception) {
                    downloadState.value = DownloadStatus.Error(e.message.toString())
                    Log.d("TAG", "Failed to convert the data: ${e.message}")
                }
            } else {
                downloadState.value = DownloadStatus.Error("Connection or network error.")
                Log.d("TAG", "Failed to download data from the database.")
            }
        }
        return listener
    }
    private fun <T> getParametersDownloadListener(downloadState: MutableLiveData<DownloadStatus<T>>, timer: Timer, reference: String): OnCompleteListener<DataSnapshot> {
        return getDownloadListener(downloadState, timer, reference)
    }
    private fun <T> getScheduleDownloadListener(downloadState: MutableLiveData<DownloadStatus<T>>, timer: Timer, baseSchedule: Boolean): OnCompleteListener<DataSnapshot> {
        return if (baseSchedule) {
            getDownloadListener(downloadState, timer, APP_BD_PATHS_SCHEDULE_BASE)
        } else {
            getDownloadListener(downloadState, timer, APP_BD_PATHS_SCHEDULE_CURRENT)
        }
    }
    private fun getEverythingDownloadListener(downloadState: MutableLiveData<DownloadStatus<FlatSchedule>>, timer: Timer): OnCompleteListener<DataSnapshot> {
        return getDownloadListener(downloadState, timer, APP_BD_PATHS_BASE)
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

    private fun <T> setDownloadTimeout(downloadStatus: MutableLiveData<DownloadStatus<T>>, time: Long): Timer {
        return performTimerEvent(
            { downloadStatus.value = DownloadStatus.WeakProgress(APP_TOAST_WEAK_CONNECTION) }, time)
    }

    private fun setUploadTimeout(uploadState: MutableLiveData<UploadStatus>, time: Long): Timer {
        return performTimerEvent(
            { uploadState.value = UploadStatus.WeakProgress(APP_TOAST_WEAK_CONNECTION) }, time
        )
    }

    private fun <T> uploadData(uploadState: MutableLiveData<UploadStatus>, reference: String, info: T, updateFunc: ()->Unit) {
        uploadState.value = UploadStatus.Progress
        val timer = setUploadTimeout(uploadState, 5000L)

        rImplementation.uploadByReference(reference, info).addOnCompleteListener { task ->
            timer.cancel()
            if (task.isSuccessful) {
                uploadState.value = UploadStatus.Success
                updateFunc()
            } else {
                uploadState.value = UploadStatus.Error(task.exception!!.message.toString())
            }
        }
    }

    fun uploadParameters(uploadState: MutableLiveData<UploadStatus>, index: Int, paramsArr: ArrayList<Data_IntString>) {
        val downloadableParamsArray = getItemArrayDeepCopy(paramsArr)
        downloadableParamsArray.forEach { it.title = "'${it.title!!}'" }
        uploadData(uploadState, getReferenceByIndex(index), downloadableParamsArray) { updateParametersByIndex(index, paramsArr) }
    }

    fun uploadCurrentSchedule(uploadState: MutableLiveData<UploadStatus>, flatSchedule: FlatScheduleDetailed) {
        cleanScheduleFromUnnecessaryDates(flatSchedule)
        if (getPreference(APP_PREFERENCES_PUSHES, true)) {
            flatSchedule.version = Calendar.getInstance().timeInMillis
        } else {
            flatSchedule.version = flatScheduleDetailed.version
        }
        uploadData(uploadState, APP_BD_PATHS_SCHEDULE_CURRENT, flatSchedule) { updateCurrentSchedule(flatSchedule) }
    }

    fun uploadBaseSchedule(uploadState: MutableLiveData<UploadStatus>, flatSchedule: FlatScheduleBase) {
        val uploadSchedule = getFlatScheduleBaseDeepCopy(flatSchedule)
        uploadSchedule.nameList.forEach { it.title = "'${it.title!!}'" }
        uploadData(uploadState, APP_BD_PATHS_SCHEDULE_BASE, uploadSchedule) { updateBaseSchedule(flatSchedule) }
    }

    fun uploadBaseScheduleNames(uploadState: MutableLiveData<UploadStatus>, flatSchedule: FlatScheduleBase) {
        val uploadNameList = getItemArrayDeepCopy(flatSchedule.nameList)
        uploadNameList.forEach { it.title = "'${it.title!!}'" }
        uploadData(uploadState, APP_BD_PATHS_SCHEDULE_BASE_NAME_LIST, uploadNameList) { flatScheduleBase.nameList = getItemArrayDeepCopy(flatSchedule.nameList) }
    }

    private fun cleanScheduleFromUnnecessaryDates(flatSchedule: FlatScheduleDetailed) {
        val arrayToRemove: ArrayList<Int> = arrayListOf()
        flatSchedule.scheduleDay.forEach {curData ->
            val curDate = getById(curData.specialId!!, flatScheduleParameters.dayList)
            if (curDate == null) {
                curData.scheduleId.forEach {
                    if (!arrayToRemove.contains(it)) {
                        arrayToRemove.add(it)
                    }
                }
            }
        }

        arrayToRemove.forEach {
            removeScheduleItemById(flatSchedule, it)
        }
    }

    fun updateAndUploadTheDayList(uploadState: MutableLiveData<UploadStatus>): Boolean {
        val dayList = getItemArrayDeepCopy(flatScheduleParameters.dayList)

        for (i in 0 until PAGE_COUNT) {
            val date = getDateWithOffset(i)
            var dateExists = false
            dayList.forEach {
                if (it.date!! == date) {
                    dateExists = true
                }
            }

            if (!dateExists) {
                val newIndex = getPossibleId(dayList)
                dayList.add(Data_IntDate(newIndex, date))
            }
        }

        val arrayToRemove: ArrayList<Data_IntDate> = arrayListOf()
        dayList.forEach {
            if (getDateIndex(it.date!!) == -1) {
                arrayToRemove.add(it)
            }
        }
        arrayToRemove.forEach { dayList.remove(it) }
        arrayToRemove.clear()

        if (checkIfItemArraysAreEqual(flatScheduleParameters.dayList, dayList)) { return false }
        uploadData(uploadState, APP_BD_PATHS_DATE_LIST, dayList) { flatScheduleParameters.dayList = dayList }
        return true
    }

    fun getCurrentSchedule(): FlatScheduleDetailed {
        return getFlatScheduleDetailedDeepCopy(flatScheduleDetailed)
    }

    fun getBaseSchedule(): FlatScheduleBase {
        return getFlatScheduleBaseDeepCopy(flatScheduleBase)
    }

    fun getParameters(): FlatScheduleParameters {
        return flatScheduleParameters
    }

    fun getParametersList(reference: String): ArrayList<String> {
        val table: ArrayList<String> = arrayListOf()
        getParametersByReference(reference).forEach { table.add(it.title!!) }
        return table
    }

    fun checkIfParameterIsNecessary(reference: String, id: Int): Boolean {
        when(reference) {
            APP_BD_PATHS_DISCIPLINE_LIST -> {
                flatScheduleDetailed.scheduleLesson.forEach { if (it.specialId == id) { return true } }
                flatScheduleBase.scheduleLesson.forEach { if (it.specialId == id) { return true } }
            }
            APP_BD_PATHS_TEACHER_LIST -> {
                flatScheduleDetailed.teacherLesson.forEach { if (it.specialId == id) { return true } }
                flatScheduleBase.teacherLesson.forEach { if (it.specialId == id) { return true } }
            }
            APP_BD_PATHS_GROUP_LIST -> {
                flatScheduleDetailed.scheduleGroup.forEach { if (it.specialId == id) { return true } }
                flatScheduleBase.scheduleGroup.forEach { if (it.specialId == id) { return true } }
            }
            APP_BD_PATHS_CABINET_LIST -> {
                flatScheduleDetailed.cabinetLesson.forEach { if (it.specialId == id) { return true } }
                flatScheduleBase.cabinetLesson.forEach { if (it.specialId == id) { return true } }
            }
        }
        return false
    }

    fun getDateWithOffset(index: Int = chosenDayIndex): Date {
        val position = index - PAGE_COUNT/2
        val c = Calendar.getInstance()

        if (position != 0) {
            c.add(Calendar.DATE, position)
        }

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        return Date(year, month, day)
    }

    fun getDayToTab(index: Int = chosenDayIndex): String {
        when (editMode) {
            APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> {
                return APP_CALENDER_DAY_OF_WEEK[index]
            }
            APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> {
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
            else -> {
                throw(IllegalStateException("Unknown edit mode."))
            }
        }
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

    fun getChosenDayIndex(): Int {
        return chosenDayIndex
    }

    private fun getDateIndex(date: Date): Int {
        var index = -1
        for (i in 0 until PAGE_COUNT) {
            if (getDateWithOffset(i).equals(date)) {
                index = i
                break
            }
        }
        return index
    }

    fun chooseDay(date: Date) {
        if (editMode != APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE) {
            throw(Exception("Edit mode and chosen date mismatch."))
        }

        val index = getDateIndex(date)
        if (index == -1) {
            throw(Exception("Failed to calculate chosen date's index."))
        }
        chosenDayIndex = index
    }

    fun chooseDay(day: Int) {
        if (editMode != APP_ADMIN_BASE_SCHEDULE_EDIT_MODE) {
            throw(Exception("Edit mode and chosen day mismatch."))
        }

        if (day < 1 || day > 7) {
            throw(Exception("Invalid day number."))
        }
        chosenDayIndex = day
    }
}