package com.example.adminscheduleapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.adminscheduleapp.data.AddPairItem
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_CABINET_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_DISCIPLINE_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_GROUP_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_TEACHER_NAME
import com.example.adminscheduleapp.data.Constants.APP_CALENDER_DAY_OF_WEEK
import com.example.adminscheduleapp.data.Data_IntArray
import com.example.adminscheduleapp.data.Data_IntString
import com.example.adminscheduleapp.data.Date
import com.example.adminscheduleapp.data.FlatScheduleBase
import com.example.adminscheduleapp.data.FlatScheduleDetailed
import com.example.adminscheduleapp.data.FlatScheduleParameters
import com.example.adminscheduleapp.data.Schedule
import com.example.adminscheduleapp.data.ScheduleDetailed
import com.example.adminscheduleapp.utils.Utils.convertPairToArrayOfAddPairItem
import com.example.adminscheduleapp.utils.Utils.getById
import com.example.adminscheduleapp.utils.Utils.getEmptyId
import com.example.adminscheduleapp.utils.Utils.getFlatScheduleBaseDeepCopy
import com.example.adminscheduleapp.utils.Utils.getFlatScheduleDetailedDeepCopy
import com.example.adminscheduleapp.utils.Utils.getItemArrayDeepCopy
import com.example.adminscheduleapp.utils.Utils.getItemId
import com.example.adminscheduleapp.utils.Utils.getScheduleIdByGroupAndDate
import com.example.adminscheduleapp.utils.Utils.getScheduleIdByGroupDateAndBaseScheduleId
import com.example.adminscheduleapp.utils.Utils.moveDataFromScheduleToArray

//@HiltViewModel
class ScheduleViewModel : ViewModel() {
    private var savedFlatScheduleDetailed: FlatScheduleDetailed? = null
    private var scheduleCurrentComparisonStatus: Pair<Boolean?, Boolean?> = Pair(null, null)

    private var savedFlatScheduleBase: FlatScheduleBase? = null
    private var scheduleBaseComparisonStatus: Pair<Boolean?, Boolean?> = Pair(null, null)

    private var chosenBaseSchedule: Int? = null
    private var chosenScheduleItem: ArrayList<AddPairItem>? = null
    private var chosenScheduleId: Int? = null
    private var chosenPairNumber: Int? = null

    private var chosenDate: String? = null
    private var chosenGroup: String? = null

    var chosenScheduleIdIsNew: Boolean? = null


    fun getScheduleByGroupAndDay(
        groupId: Int?, dayId: Int?, parameters: FlatScheduleParameters, editMode: Int
    ): ArrayList<Schedule> {
        val resArray = arrayListOf<Schedule>()
        val detArray: ArrayList<ScheduleDetailed> = when (editMode) {
            APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> {
                getCurrentScheduleByGroupAndDayDetailed(groupId, dayId, parameters)
            }
            APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> {
                getBaseScheduleByGroupAndDayDetailed(groupId, dayId, parameters)
            }
            else -> {
                throw(IllegalStateException("Unknown edit mode."))
            }
        }
        detArray.forEach {
            resArray.add(Schedule())
        }

        finishScheduleArraySetup(resArray, detArray)
        return resArray
    }
    private fun getCurrentScheduleByGroupAndDayDetailed(
        groupId: Int?, dayId: Int?, parameters: FlatScheduleParameters
    ): ArrayList<ScheduleDetailed> {
        val detArray = arrayListOf<ScheduleDetailed>()

        for (i in 1..14) {
            detArray.add(ScheduleDetailed(lessonNum = i))
        }

        if (groupId == null || dayId == null) {
            return detArray
        }

        val scheduleId: Int? = getScheduleIdByGroupAndDate(savedFlatScheduleDetailed!!, dayId, groupId)

        if (scheduleId == null) {
            return detArray
        }

        moveDataFromScheduleToArray(savedFlatScheduleDetailed!!, parameters, scheduleId, detArray)
        return detArray
    }

    private fun getBaseScheduleByGroupAndDayDetailed(
        groupId: Int?, dayId: Int?, parameters: FlatScheduleParameters
    ): ArrayList<ScheduleDetailed> {
        val detArray = arrayListOf<ScheduleDetailed>()

        for (i in 1..14) {
            detArray.add(ScheduleDetailed(lessonNum = i))
        }

        if (groupId == null || dayId == null) {
            return detArray
        }

        val scheduleId: Int? = getScheduleIdByGroupDateAndBaseScheduleId(savedFlatScheduleBase!!, dayId, groupId, chosenBaseSchedule!!)

        if (scheduleId == null) {
            return detArray
        }

        moveDataFromScheduleToArray(savedFlatScheduleBase!!, parameters, scheduleId, detArray)
        return detArray
    }

    private fun finishScheduleArraySetup(resArray: ArrayList<Schedule>, detArray: ArrayList<ScheduleDetailed>) {
        for (i in 0 until detArray.size) {
            resArray[i] = checkForEquality(detArray[i])
        }
    }

    private fun checkForEquality(scheduleDetailed: ScheduleDetailed): Schedule {
        val scheduleObject = Schedule()

        scheduleObject.lessonNum = scheduleDetailed.lessonNum

        scheduleObject.discipline = compareStringParams(scheduleDetailed.discipline1!!, scheduleDetailed.discipline2!!, scheduleDetailed.discipline3!!)
        scheduleObject.cabinet = compareStringParams(scheduleDetailed.cabinet1!!, scheduleDetailed.cabinet2!!, scheduleDetailed.cabinet3!!)
        scheduleObject.teacher = compareStringParams(scheduleDetailed.teacher1!!, scheduleDetailed.teacher2!!, scheduleDetailed.teacher3!!)

        return scheduleObject
    }

    private fun compareStringParams(param1: String, param2: String, param3: String): String {
        return if (param3 != "-") {
            if (param1 != param2 || param2 != param3 || param1 != param3) {
                param1 + System.getProperty("line.separator") + param2 + System.getProperty("line.separator") + param3
            } else {
                param1
            }
        } else {
            if (param1 != param2) {
                param1 + System.getProperty("line.separator") + param2
            } else {
                param1
            }
        }
    }

    fun compareParametersLists(base: ArrayList<Data_IntString>, new: ArrayList<Data_IntString>): Pair<Boolean, Boolean> {
        var same: Boolean = base.size == new.size
        val base_ids = arrayListOf<Int>()
        val new_ids = arrayListOf<Int>()
        base.forEach { base_ids.add(it.id!!) }
        new.forEach { new_ids.add(it.id!!) }

        var missing_ids = false
        for (id: Int in base_ids) {
            if (!new_ids.contains(id)) {
                missing_ids = true
                same = false
                break
            }
        }

        if (same) {
            for (i in 0 until base_ids.size) {
                if (getById(base_ids[i], base)!!.title != getById(base_ids[i], new)!!.title) {
                    same = false
                    break
                }
            }
        }

        Log.d("APP_DEBUGGER", "Comparison function result: ${Pair(same, missing_ids)}.")
        return Pair(same, missing_ids)
    }

    fun checkIfParameterIsNecessary(reference: String, id: Int): Boolean {
        when(reference) {
            APP_ADMIN_PARAMETERS_DISCIPLINE_NAME -> {
                savedFlatScheduleDetailed?.scheduleLesson?.forEach { if (it.specialId == id) { return true } }
                savedFlatScheduleBase?.scheduleLesson?.forEach { if (it.specialId == id) { return true } }
            }
            APP_ADMIN_PARAMETERS_TEACHER_NAME -> {
                savedFlatScheduleDetailed?.teacherLesson?.forEach { if (it.specialId == id) { return true } }
                savedFlatScheduleBase?.teacherLesson?.forEach { if (it.specialId == id) { return true } }
            }
            APP_ADMIN_PARAMETERS_GROUP_NAME -> {
                savedFlatScheduleDetailed?.scheduleGroup?.forEach { if (it.specialId == id) { return true } }
                savedFlatScheduleBase?.scheduleGroup?.forEach { if (it.specialId == id) { return true } }
            }
            APP_ADMIN_PARAMETERS_CABINET_NAME -> {
                savedFlatScheduleDetailed?.cabinetLesson?.forEach { if (it.specialId == id) { return true } }
                savedFlatScheduleBase?.cabinetLesson?.forEach { if (it.specialId == id) { return true } }
            }
        }
        return false
    }

    private fun collectAllCurrentScheduleIds(): ArrayList<Int> {
        val scheduleIds = arrayListOf<Int>()
        for (i: Data_IntArray in savedFlatScheduleDetailed!!.scheduleDay) {
            i.scheduleId.forEach {
                if (!scheduleIds.contains(it)) {
                    scheduleIds.add(it)
                }
            }
        }
        for (i: Data_IntArray in savedFlatScheduleDetailed!!.scheduleGroup) {
            i.scheduleId.forEach {
                if (!scheduleIds.contains(it)) {
                    scheduleIds.add(it)
                }
            }
        }
        return scheduleIds
    }
    private fun collectAllBaseScheduleIds(): ArrayList<Int> {
        val scheduleIds = arrayListOf<Int>()
        for (i: Data_IntArray in savedFlatScheduleBase!!.scheduleDay) {
            i.scheduleId.forEach {
                if (!scheduleIds.contains(it)) {
                    scheduleIds.add(it)
                }
            }
        }
        for (i: Data_IntArray in savedFlatScheduleBase!!.scheduleGroup) {
            i.scheduleId.forEach {
                if (!scheduleIds.contains(it)) {
                    scheduleIds.add(it)
                }
            }
        }
        for (i: Data_IntArray in savedFlatScheduleBase!!.scheduleName) {
            i.scheduleId.forEach {
                if (!scheduleIds.contains(it)) {
                    scheduleIds.add(it)
                }
            }
        }
        return scheduleIds
    }

    private fun updateScheduleComparisonStatus(oldStatus: Pair<Boolean?, Boolean?>, newStatus: Pair<Boolean?, Boolean?>): Pair<Boolean?, Boolean?> {
        var resultStatus = oldStatus
        if (newStatus.first != null) {
            resultStatus = Pair(newStatus.first, resultStatus.second)
        }
        if (newStatus.second != null) {
            resultStatus = Pair(resultStatus.first, newStatus.second)
        }
        return resultStatus
    }

    fun getSavedCurrentSchedule(): FlatScheduleDetailed? {
        return if (savedFlatScheduleDetailed == null) {
            null
        } else {
            getFlatScheduleDetailedDeepCopy(savedFlatScheduleDetailed!!)
        }
    }

    fun saveCurrentSchedule(newSchedule: FlatScheduleDetailed, newStatus: Pair<Boolean?, Boolean?>? = null) {
        savedFlatScheduleDetailed = getFlatScheduleDetailedDeepCopy(newSchedule)
        if (newStatus != null) {
            updateCurrentScheduleComparisonStatus(newStatus)
        }
    }

    fun updateCurrentScheduleComparisonStatus(newStatus: Pair<Boolean?, Boolean?>) {
        scheduleCurrentComparisonStatus = updateScheduleComparisonStatus(scheduleCurrentComparisonStatus, newStatus)
    }

    fun isStagedScheduleCurrentSame(): Pair<Boolean?, Boolean?> {
        return scheduleCurrentComparisonStatus
    }

    fun clearCurrentSchedule() {
        savedFlatScheduleDetailed = null
        scheduleCurrentComparisonStatus = Pair(null, null)
    }


    fun getSavedBaseSchedule(): FlatScheduleBase? {
        return if (savedFlatScheduleBase == null) {
            null
        } else {
            getFlatScheduleBaseDeepCopy(savedFlatScheduleBase!!)
        }
    }

    fun saveBaseSchedule(newSchedule: FlatScheduleBase, newStatus: Pair<Boolean?, Boolean?>? = null) {
        savedFlatScheduleBase = getFlatScheduleBaseDeepCopy(newSchedule)
        if (newStatus != null) {
            updateBaseScheduleComparisonStatus(newStatus)
        }
    }

    fun updateBaseScheduleComparisonStatus(newStatus: Pair<Boolean?, Boolean?>) {
        scheduleBaseComparisonStatus = updateScheduleComparisonStatus(scheduleBaseComparisonStatus, newStatus)
    }

    fun isStagedScheduleBaseSame(): Pair<Boolean?, Boolean?> {
        return scheduleBaseComparisonStatus
    }

    fun clearBaseSchedule() {
        savedFlatScheduleBase = null
        scheduleBaseComparisonStatus = Pair(null, null)
    }

    fun chooseBaseSchedule(scheduleNum: Int) {
        chosenBaseSchedule = scheduleNum
    }

    fun getChosenBaseSchedule(): Int? {
        return chosenBaseSchedule
    }

    fun chooseScheduleItemCurrent(scheduleParams: FlatScheduleParameters, currentDate: Date, currentGroup: String, pairNumber: Int, baseSchedule: FlatScheduleDetailed? = null): Boolean {
        val currentGroupId = getItemId(scheduleParams.groupList, currentGroup)
        val currentDateId = getItemId(scheduleParams.dayList, currentDate)

        if (currentDateId == null) {
            Log.d("ADMIN_EDITOR_CHECKER", "No date ID was found (${currentDate}).")
            return false
        }


        if (getById(currentDateId, savedFlatScheduleDetailed!!.scheduleDay) == null) {
            savedFlatScheduleDetailed!!.scheduleDay.add(Data_IntArray(currentDateId, arrayListOf()))
        }
        if (getById(currentGroupId!!, savedFlatScheduleDetailed!!.scheduleGroup) == null) {
            savedFlatScheduleDetailed!!.scheduleGroup.add(Data_IntArray(currentGroupId, arrayListOf()))
        }

        var scheduleId: Int? = getScheduleIdByGroupAndDate(savedFlatScheduleDetailed!!, currentDateId, currentGroupId)

        chosenScheduleIdIsNew = (scheduleId == null)
        if (scheduleId == null) {
            if (baseSchedule != null) {
                scheduleId = getScheduleIdByGroupAndDate(baseSchedule, currentDateId, currentGroupId)
            }
            if (scheduleId == null) {
                scheduleId = getEmptyId(collectAllCurrentScheduleIds())
            }
            val dayScheduleArray = getById(currentDateId, savedFlatScheduleDetailed!!.scheduleDay)
            val groupScheduleArray = getById(currentGroupId, savedFlatScheduleDetailed!!.scheduleGroup)

            dayScheduleArray!!.scheduleId.add(scheduleId)
            groupScheduleArray!!.scheduleId.add(scheduleId)
        }

        convertChosenScheduleToAddPairItem(scheduleParams, currentDateId, currentGroupId, pairNumber, APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE)
        chosenScheduleId = scheduleId
        chosenPairNumber = pairNumber

        chosenDate = currentDate.toString()
        chosenGroup = currentGroup

        return true
    }

    fun chooseScheduleItemBase(scheduleParams: FlatScheduleParameters, dayId: Int, currentGroup: String, pairNumber: Int, baseSchedule: FlatScheduleBase? = null): Boolean {
        val currentGroupId = getItemId(scheduleParams.groupList, currentGroup)


        if (getById(dayId, savedFlatScheduleBase!!.scheduleDay) == null) {
            savedFlatScheduleBase!!.scheduleDay.add(Data_IntArray(dayId, arrayListOf()))
        }
        if (getById(currentGroupId!!, savedFlatScheduleBase!!.scheduleGroup) == null) {
            savedFlatScheduleBase!!.scheduleGroup.add(Data_IntArray(currentGroupId, arrayListOf()))
        }
        if (getById(chosenBaseSchedule!!, savedFlatScheduleBase!!.scheduleName) == null) {
            savedFlatScheduleBase!!.scheduleName.add(Data_IntArray(chosenBaseSchedule!!, arrayListOf()))
        }

        var scheduleId: Int? = getScheduleIdByGroupDateAndBaseScheduleId(savedFlatScheduleBase!!, dayId, currentGroupId, chosenBaseSchedule!!)

        chosenScheduleIdIsNew = (scheduleId == null)
        if (scheduleId == null) {
            if (baseSchedule != null) {
                scheduleId = getScheduleIdByGroupDateAndBaseScheduleId(baseSchedule, dayId, currentGroupId, chosenBaseSchedule!!)
            }
            if (scheduleId == null) {
                scheduleId = getEmptyId(collectAllBaseScheduleIds())
            }
            val dayScheduleArray = getById(dayId, savedFlatScheduleBase!!.scheduleDay)
            val groupScheduleArray = getById(currentGroupId, savedFlatScheduleBase!!.scheduleGroup)
            val nameScheduleArray = getById(chosenBaseSchedule!!, savedFlatScheduleBase!!.scheduleName)

            dayScheduleArray!!.scheduleId.add(scheduleId)
            groupScheduleArray!!.scheduleId.add(scheduleId)
            nameScheduleArray!!.scheduleId.add(scheduleId)
        }

        convertChosenScheduleToAddPairItem(scheduleParams, dayId, currentGroupId, pairNumber, APP_ADMIN_BASE_SCHEDULE_EDIT_MODE)
        chosenScheduleId = scheduleId
        chosenPairNumber = pairNumber

        chosenDate = APP_CALENDER_DAY_OF_WEEK[dayId]
        chosenGroup = currentGroup

        return true
    }

    private fun convertChosenScheduleToAddPairItem(scheduleParams: FlatScheduleParameters, currentDateId: Int, currentGroupId: Int, pairNumber: Int, editMode: Int) {
        val schedule: ArrayList<ScheduleDetailed> = when(editMode) {
            APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> {
                getCurrentScheduleByGroupAndDayDetailed(currentGroupId, currentDateId, scheduleParams)
            }
            APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> {
                getBaseScheduleByGroupAndDayDetailed(currentGroupId, currentDateId, scheduleParams)
            }
            else -> {
                throw(IllegalStateException("Unknown edit mode."))
            }
        }
        val subPair1 = schedule[pairNumber*2]
        val subPair2 = schedule[pairNumber*2+1]

        chosenScheduleItem = convertPairToArrayOfAddPairItem(Pair(subPair1, subPair2))
    }

    fun getChosenScheduleItem(): ArrayList<AddPairItem>? {
        return chosenScheduleItem
    }

    fun getChosenPairNum(): Int? {
        return chosenPairNumber
    }

    fun getChosenGroup(): String? {
        return chosenGroup
    }

    fun saveNewPair(pair: ArrayList<AddPairItem>) {
        chosenScheduleItem = getItemArrayDeepCopy(pair)
    }

    fun checkBaseScheduleIdValidity(baseFlatSchedule: FlatScheduleBase, baseScheduleId: Int = chosenBaseSchedule!!): Boolean {
        val idsArray: ArrayList<Int> = arrayListOf()
        baseFlatSchedule.nameList.forEach { idsArray.add(it.id!!) }

        return idsArray.contains(baseScheduleId)
    }

    fun getBaseScheduleName(baseScheduleId: Int = chosenBaseSchedule!!): String {
        return getById(baseScheduleId, savedFlatScheduleBase!!.nameList)!!.title!!
    }
}