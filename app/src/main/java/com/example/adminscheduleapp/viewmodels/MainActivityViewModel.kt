package com.example.adminscheduleapp.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminscheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.adminscheduleapp.data.AddPairItem
import com.example.adminscheduleapp.data.AuthenticationStatus
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_CABINET_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_DISCIPLINE_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_GROUP_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_TEACHER_NAME
import com.example.adminscheduleapp.data.Constants.APP_CALENDER_DAY_OF_WEEK
import com.example.adminscheduleapp.data.Constants.APP_TOAST_WEAK_CONNECTION
import com.example.adminscheduleapp.data.Data_IntString
import com.example.adminscheduleapp.data.Date
import com.example.adminscheduleapp.data.DownloadStatus
import com.example.adminscheduleapp.data.FlatSchedule
import com.example.adminscheduleapp.data.FlatScheduleAnswer
import com.example.adminscheduleapp.data.FlatScheduleBase
import com.example.adminscheduleapp.data.FlatScheduleDetailed
import com.example.adminscheduleapp.data.FlatScheduleParameters
import com.example.adminscheduleapp.data.ScheduleDetailed
import com.example.adminscheduleapp.data.UploadStatus
import com.example.adminscheduleapp.models.FirebaseRepository
import com.example.adminscheduleapp.retrofit.ScheduleService
import com.example.adminscheduleapp.utils.Utils.convertArrayOfAddPairItemToPair
import com.example.adminscheduleapp.utils.Utils.getFlatScheduleBaseDeepCopy
import com.example.adminscheduleapp.utils.Utils.getFlatScheduleDetailedDeepCopy
import com.example.adminscheduleapp.utils.Utils.getItemArrayDeepCopy
import com.example.adminscheduleapp.utils.Utils.getItemId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.InputMismatchException
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val rImplementation: FirebaseRepository,
    private val sPreferences: SharedPreferences,
    private val sService: ScheduleService
) : ViewModel() {
    private var flatScheduleDetailed = FlatScheduleDetailed()
    private var flatScheduleBase = FlatScheduleBase()
    private var flatScheduleParameters = FlatScheduleParameters()
    private var chosenDayIndex = PAGE_COUNT/2
    private var editMode: Int = APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
    private var shouldStagePair: Boolean = false

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
        return APP_ADMIN_PARAMETERS_LIST[id]
    }

    fun getParametersByReference(reference: String, link: Boolean = false): ArrayList<Data_IntString> {
        val table = when(reference) {
            APP_ADMIN_PARAMETERS_DISCIPLINE_NAME -> flatScheduleParameters.lessonList
            APP_ADMIN_PARAMETERS_TEACHER_NAME -> flatScheduleParameters.teacherList
            APP_ADMIN_PARAMETERS_GROUP_NAME -> flatScheduleParameters.groupList
            APP_ADMIN_PARAMETERS_CABINET_NAME -> flatScheduleParameters.cabinetList
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


    fun setNextUpload(should: Boolean) {
        shouldStagePair = should
    }

    fun shouldDataBeUploaded(): Boolean {
        return shouldStagePair
    }


    fun <T> downloadParametersList(parametersDownloadState: MutableLiveData<DownloadStatus<T>>, reference: String? = null) {
        parametersDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(parametersDownloadState, 5000L)

        viewModelScope.launch {
            try {
                var scheduleResponse: T
                if (reference == null) {
                    scheduleResponse = (sService.getScheduleParameters() as T)
                    flatScheduleParameters = scheduleResponse as FlatScheduleParameters
                } else if (APP_ADMIN_PARAMETERS_LIST.indexOf(reference) != -1) {
                    scheduleResponse = (sService.getSpecificParameters(APP_ADMIN_PARAMETERS_LIST.indexOf(reference)) as T)
                    updateParametersByReference(reference, scheduleResponse as ArrayList<Data_IntString>)
                } else {
                    throw(InputMismatchException("No matching reference was found to initiate the download: $reference."))
                }
                Log.d("APP_DEBUGGER_SCHEDULE", "Download successful: $scheduleResponse.")
                parametersDownloadState.value = DownloadStatus.Success(scheduleResponse)
            } catch (e: Exception) {
                Log.d("APP_DEBUGGER_SCHEDULE", "Failed attempt to download and convert schedule data. Error = ${e.message}.")
                parametersDownloadState.value = DownloadStatus.Error(e.message!!)
            } finally {
                timer.cancel()
                Log.d("APP_DEBUGGER_SCHEDULE", "End of an attempt to download schedule data.")
            }
        }
    }

    fun uploadParameters(uploadState: MutableLiveData<UploadStatus>, index: Int, paramsArr: ArrayList<Data_IntString>) {
        val downloadableParamsArray = getItemArrayDeepCopy(paramsArr)

        val timer = setUploadTimeout(uploadState, 5000L)
        viewModelScope.launch {
            try {
                sService.uploadSpecificParameters(index, downloadableParamsArray)
                Log.d("APP_DEBUGGER", "Upload successful.")
                uploadState.value = UploadStatus.Success
            } catch (e: Exception) {
                Log.d("APP_DEBUGGER", "Failed attempt to upload parameters (index = $index). Error = ${e.message}.")
                uploadState.value = UploadStatus.Error(e.message!!)
            }
        }.invokeOnCompletion {
            Log.d("APP_DEBUGGER", "Upload parameters function finished.")
            timer.cancel()
        }
        updateParametersByIndex(index, paramsArr)
    }

    fun downloadEverything(scheduleDownloadState: MutableLiveData<DownloadStatus<FlatSchedule>>) {
        scheduleDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(scheduleDownloadState, 8000L)

        viewModelScope.launch {
            try {
                val scheduleResponse = sService.getEntireSchedule()
                flatScheduleParameters = scheduleResponse.BaseParameters
                updateCurrentSchedule(scheduleResponse.CurrentSchedules)
                updateBaseSchedule(scheduleResponse.BaseSchedules)
                Log.d("APP_DEBUGGER_SCHEDULE", "Download successful: $scheduleResponse.")
                scheduleDownloadState.value = DownloadStatus.Success(scheduleResponse)
            } catch (e: Exception) {
                Log.d("APP_DEBUGGER_SCHEDULE", "Failed attempt to download and convert schedule data. Error = ${e.message}.")
                scheduleDownloadState.value = DownloadStatus.Error(e.message!!)
            } finally {
                timer.cancel()
                Log.d("APP_DEBUGGER_SCHEDULE", "End of an attempt to download schedule data.")
            }
        }
    }


    private inline fun <reified T> processScheduleResponse(scheduleResponse: FlatScheduleAnswer<T>, scheduleViewModel: ScheduleViewModel) {
        when (T::class.java) {
            FlatScheduleDetailed::class.java -> {
                if (scheduleResponse.scheduleCurrent != null) {
                    updateCurrentSchedule(scheduleResponse.scheduleCurrent!! as FlatScheduleDetailed)
                }
                if (scheduleResponse.scheduleStaged != null) {
                    scheduleViewModel.saveCurrentSchedule(scheduleResponse.scheduleStaged!! as FlatScheduleDetailed)
                }
                scheduleViewModel.updateCurrentScheduleComparisonStatus(Pair(scheduleResponse.comparisonGeneral, scheduleResponse.comparisonSpecific))
            }
            FlatScheduleBase::class.java -> {
                if (scheduleResponse.scheduleCurrent != null) {
                    updateBaseSchedule(scheduleResponse.scheduleCurrent!! as FlatScheduleBase)
                }
                if (scheduleResponse.scheduleStaged != null) {
                    scheduleViewModel.saveBaseSchedule(scheduleResponse.scheduleStaged!! as FlatScheduleBase)
                }
                scheduleViewModel.updateBaseScheduleComparisonStatus(Pair(scheduleResponse.comparisonGeneral, scheduleResponse.comparisonSpecific))
            }
            else -> {
                throw(InputMismatchException("Attempt to process schedule response of an unknown type: ${T::class.java}."))
            }
        }
    }

    private inline fun <reified T> processScheduleUploadResult(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleResponse: FlatScheduleAnswer<T>, scheduleViewModel: ScheduleViewModel) {
        processScheduleResponse(scheduleResponse, scheduleViewModel)
        scheduleUploadState.value = UploadStatus.Success
    }

    private inline fun <reified T> processScheduleDownloadResult(scheduleDownloadState: MutableLiveData<DownloadStatus<T>>, scheduleResponse: FlatScheduleAnswer<T>, scheduleViewModel: ScheduleViewModel) {
        val info: T
        if (scheduleResponse.scheduleCurrent != null && scheduleResponse.scheduleStaged == null) {
            info = scheduleResponse.scheduleCurrent!!
        } else if (scheduleResponse.scheduleCurrent == null && scheduleResponse.scheduleStaged != null) {
            info = scheduleResponse.scheduleStaged!!
        } else if (scheduleResponse.scheduleStaged != null) {
            info = scheduleResponse.scheduleStaged!!
        } else {
            throw(RuntimeException("Got a schedule response with no schedule data in it: ${scheduleResponse} (processScheduleDownloadResult)."))
        }
        processScheduleResponse(scheduleResponse, scheduleViewModel)
        scheduleDownloadState.value = DownloadStatus.Success(info)
    }


    private inline fun <reified T> downloadFunctionGeneric(scheduleDownloadState: MutableLiveData<DownloadStatus<T>>, scheduleViewModel: ScheduleViewModel, progressTime: Long, crossinline initFunc: suspend ()->FlatScheduleAnswer<T>) {
        scheduleDownloadState.value = DownloadStatus.Progress
        val timer = setDownloadTimeout(scheduleDownloadState, progressTime)

        viewModelScope.launch {
            try {
                val scheduleResponse = initFunc()
                Log.d("APP_DEBUGGER_SCHEDULE", "Download successful: $scheduleResponse.")
                processScheduleDownloadResult(scheduleDownloadState, scheduleResponse, scheduleViewModel)
            } catch (e: Exception) {
                Log.d("APP_DEBUGGER_SCHEDULE", "Failed attempt to download and convert schedule data. Error = ${e.message}.")
                scheduleDownloadState.value = DownloadStatus.Error(e.message!!)
            } finally {
                timer.cancel()
                Log.d("APP_DEBUGGER_SCHEDULE", "End of an attempt to download schedule data.")
            }
        }
    }

    fun downloadCurrentSchedule(scheduleDownloadState: MutableLiveData<DownloadStatus<FlatScheduleDetailed>>, scheduleViewModel: ScheduleViewModel, dateId: Int = -1) {
        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleDetailed> = {
            sService.getScheduleCurrent(dateId)
        }
        downloadFunctionGeneric(scheduleDownloadState, scheduleViewModel, 8000L, initFunc)
    }

    fun downloadBaseSchedule(scheduleDownloadState: MutableLiveData<DownloadStatus<FlatScheduleBase>>, scheduleViewModel: ScheduleViewModel, dayNum: Int = -1, nameId: Int = -1) {
        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleBase> = {
            sService.getScheduleBase(nameId, dayNum)
        }
        downloadFunctionGeneric(scheduleDownloadState, scheduleViewModel, 8000L, initFunc)
    }


    private inline fun <reified T> uploadFunctionGeneric(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, progressTime: Long, crossinline initFunc: suspend ()->FlatScheduleAnswer<T>) {
        scheduleUploadState.value = UploadStatus.Progress
        val timer = setUploadTimeout(scheduleUploadState, progressTime)

        viewModelScope.launch {
            try {
                val scheduleResponse = initFunc()
                Log.d("APP_DEBUGGER_SCHEDULE", "Download successful: $scheduleResponse.")
                processScheduleUploadResult(scheduleUploadState, scheduleResponse, scheduleViewModel)
            } catch (e: Exception) {
                Log.d("APP_DEBUGGER_SCHEDULE", "Failed attempt to download and convert schedule data. Error = ${e.message}.")
                scheduleUploadState.value = UploadStatus.Error(e.message!!)
            } finally {
                timer.cancel()
                Log.d("APP_DEBUGGER_SCHEDULE", "End of an attempt to download schedule data.")
            }
        }
    }

    fun stageCurrentSchedulePair(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, groupId: Int, dateId: Int, pairNum: Int, body: ArrayList<AddPairItem>? = null) {
        val subPair1 = ScheduleDetailed(pairNum * 2 + 1)
        val subPair2 = ScheduleDetailed(pairNum * 2 + 2)
        if (body != null) {
            convertArrayOfAddPairItemToPair(body, Pair(subPair1, subPair2))
        }

        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleDetailed> = {
            sService.stageCurrentSchedulePair(groupId, dateId, Pair(subPair1, subPair2))
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
    }

    fun applyBaseScheduleToCurrent(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, date: Date = getDateWithOffset(), baseScheduleName: String, day: String = getDayToTab()) {
        val dateId = getItemId(flatScheduleParameters.dayList, date)
        val nameId = getItemId(flatScheduleBase.nameList, baseScheduleName)

        val dayOfWeek = (day.split(System.lineSeparator()))[0]
        val dayId = APP_CALENDER_DAY_OF_WEEK.indexOf(dayOfWeek)

        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleDetailed> = {
            sService.applyBaseScheduleToCurrent(nameId!!, dayId, dateId!!)
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
    }

    fun stageBaseSchedulePair(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, groupId: Int, dayNum: Int, pairNum: Int, nameId: Int, body: ArrayList<AddPairItem>? = null) {
        val subPair1 = ScheduleDetailed(pairNum * 2 + 1)
        val subPair2 = ScheduleDetailed(pairNum * 2 + 2)
        if (body != null) {
            convertArrayOfAddPairItemToPair(body, Pair(subPair1, subPair2))
        }
        Log.d("APP_DEBUGGER", "Sending schedule pair: ${Pair(subPair1, subPair2)}.")

        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleBase> = {
            sService.stageBaseSchedulePair(groupId, dayNum, nameId, Pair(subPair1, subPair2))
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
    }

    fun stageBaseScheduleList(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, body: Data_IntString) {
        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleBase> = {
            sService.stageBaseScheduleList(body)
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
    }


    fun applyStagedChangesToScheduleCurrent(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, dateId: Int = -1, updateVersion: Boolean = true) {
        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleDetailed> = {
            if (updateVersion) {
                sService.applyStagedChangesToScheduleCurrent(dateId)
            } else {
                sService.applyStagedChangesToScheduleCurrent(dateId, 0)
            }
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
    }

    fun applyStagedChangesToScheduleBase(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, dayNum: Int = -1, nameId: Int = -1) {
        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleBase> = {
            sService.applyStagedChangesToScheduleBase(dayNum, nameId)
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
    }

    fun resetStagedChangesToScheduleCurrent(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, dateId: Int = -1) {
        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleDetailed> = {
            sService.resetStagedChangesToScheduleCurrent(dateId)
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
    }

    fun resetStagedChangesToScheduleBase(scheduleUploadState: MutableLiveData<UploadStatus>, scheduleViewModel: ScheduleViewModel, dayNum: Int = -1, nameId: Int = -1) {
        val initFunc: suspend ()->FlatScheduleAnswer<FlatScheduleBase> = {
            sService.resetStagedChangesToScheduleBase(dayNum, nameId)
        }
        uploadFunctionGeneric(scheduleUploadState, scheduleViewModel, 8000L, initFunc)
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
            APP_ADMIN_PARAMETERS_DISCIPLINE_NAME -> {
                flatScheduleDetailed.scheduleLesson.forEach { if (it.specialId == id) { return true } }
                flatScheduleBase.scheduleLesson.forEach { if (it.specialId == id) { return true } }
            }
            APP_ADMIN_PARAMETERS_TEACHER_NAME -> {
                flatScheduleDetailed.teacherLesson.forEach { if (it.specialId == id) { return true } }
                flatScheduleBase.teacherLesson.forEach { if (it.specialId == id) { return true } }
            }
            APP_ADMIN_PARAMETERS_GROUP_NAME -> {
                flatScheduleDetailed.scheduleGroup.forEach { if (it.specialId == id) { return true } }
                flatScheduleBase.scheduleGroup.forEach { if (it.specialId == id) { return true } }
            }
            APP_ADMIN_PARAMETERS_CABINET_NAME -> {
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