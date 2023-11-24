package com.example.scheduleapp.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.scheduleapp.data.AddPairItem
import com.example.scheduleapp.data.*
import com.example.scheduleapp.data.Date
import com.example.scheduleapp.utils.Utils.addPairToFlatSchedule
import com.example.scheduleapp.utils.Utils.convertPairToArrayOfAddPairItem
import com.example.scheduleapp.utils.Utils.getById
import com.example.scheduleapp.utils.Utils.getFlatScheduleDetailedDeepCopy
import com.example.scheduleapp.utils.Utils.getItemId
import com.example.scheduleapp.utils.Utils.moveDataFromScheduleToArray
import com.example.scheduleapp.utils.Utils.removeScheduleItemById
import kotlin.collections.ArrayList

//@HiltViewModel
class ScheduleFragmentViewModel : ViewModel() {
    private var savedFlatSchedule: FlatScheduleDetailed? = null
    private var chosenScheduleItem: ArrayList<AddPairItem>? = null
    private var chosenScheduleId: Int? = null
    private var chosenPairNumber: Int? = null

    private var chosenDate: String? = null
    private var chosenGroup: String? = null

    var chosenScheduleIdIsNew: Boolean? = null


    fun getScheduleByGroupAndDay(
        groupId: Int?, dayId: Int?, parameters: FlatScheduleParameters
    ): ArrayList<Schedule> {
        val resArray = arrayListOf<Schedule>()
        val detArray = getScheduleByGroupAndDayDetailed(groupId, dayId, parameters)
        detArray.forEach {
            resArray.add(Schedule())
        }

        finishScheduleArraySetup(resArray, detArray)
        return resArray
    }

    fun getScheduleByGroupAndDayDetailed(
        groupId: Int?, dayId: Int?, parameters: FlatScheduleParameters
    ): ArrayList<ScheduleDetailed> {
        val detArray = arrayListOf<ScheduleDetailed>()

        for (i in 1..14) {
            detArray.add(ScheduleDetailed(lessonNum = i))
        }

        if (groupId == null || dayId == null) {
            return detArray
        }

        var scheduleId: Int? = null

        val firstScheduleArray = getById(dayId, savedFlatSchedule!!.scheduleDay)
        val secondScheduleArray = getById(groupId, savedFlatSchedule!!.scheduleGroup)
        if (firstScheduleArray == null || secondScheduleArray == null) {
            Log.d("TAG", "getScheduleByGroupAndDay: One of the schedule arrays is missing!")
            return detArray
        }

        if (firstScheduleArray.scheduleId.size < secondScheduleArray.scheduleId.size) {
            for (item in firstScheduleArray.scheduleId) {
                if (secondScheduleArray.scheduleId.contains(item)) {
                    scheduleId = item
                    break
                }
            }
        } else {
            for (item in secondScheduleArray.scheduleId) {
                if (firstScheduleArray.scheduleId.contains(item)) {
                    scheduleId = item
                    break
                }
            }
        }

        if (scheduleId == null) {
            return detArray
        }

        moveDataFromScheduleToArray(savedFlatSchedule!!, parameters, scheduleId, detArray)
        return detArray
    }
    private fun finishScheduleArraySetup(resArray: ArrayList<Schedule>, detArray: ArrayList<ScheduleDetailed>) {
        for (i in 0 until detArray.size) {
            resArray[i] = checkForEquality(detArray[i])
        }
    }

    fun checkForEquality(scheduleDetailed: ScheduleDetailed): Schedule {
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

    private fun possibleIdFunctionality(currentIds: ArrayList<Int>): Int {
        currentIds.sort()

        var new_id = 0
        for(e: Int in currentIds){
            if(new_id == e){
                new_id += 1
            }
        }
        return new_id
    }

    @JvmName("getPossibleIdString")
    fun getPossibleId(originalList: ArrayList<Data_IntString>): Int {
        val currentIds = arrayListOf<Int>()
        originalList.forEach { currentIds.add(it.id!!) }
        return possibleIdFunctionality(currentIds)
    }
    @JvmName("getPossibleIdDate")
    fun getPossibleId(originalList: ArrayList<Data_IntDate>): Int {
        val currentIds = arrayListOf<Int>()
        originalList.forEach { currentIds.add(it.id!!) }
        return possibleIdFunctionality(currentIds)
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
            for (i in 0 until base.size) {
                if (base[i].title != new[i].title) {
                    same = false
                    break
                }
            }
        }

        return Pair(same, missing_ids)
    }

    private fun collectAllScheduleIds(): ArrayList<Int> {
        val scheduleIds = arrayListOf<Int>()
        for (i: Data_IntArray in savedFlatSchedule!!.scheduleDay) {
            i.scheduleId.forEach {
                if (!scheduleIds.contains(it)) {
                    scheduleIds.add(it)
                }
            }
        }
        for (i: Data_IntArray in savedFlatSchedule!!.scheduleGroup) {
            i.scheduleId.forEach {
                if (!scheduleIds.contains(it)) {
                    scheduleIds.add(it)
                }
            }
        }
        return scheduleIds
    }

    fun getSavedSchedule(): FlatScheduleDetailed? {
        return if (savedFlatSchedule == null) {
            null
        } else {
            getFlatScheduleDetailedDeepCopy(savedFlatSchedule!!)
        }
    }

    fun saveSchedule(newSchedule: FlatScheduleDetailed) {
        savedFlatSchedule = getFlatScheduleDetailedDeepCopy(newSchedule)
    }

    fun chooseScheduleItem(scheduleParams: FlatScheduleParameters, currentDate: Date, currentGroup: String, number: Int) {
        val currentGroupId = getItemId(scheduleParams.groupList, currentGroup)
        var currentDateId = getItemId(scheduleParams.dayList, currentDate)

        if (currentDateId == null) {
            currentDateId = getPossibleId(scheduleParams.dayList)
            scheduleParams.dayList.add(Data_IntDate(currentDate, currentDateId))
        }


        if (getById(currentDateId, savedFlatSchedule!!.scheduleDay) == null) {
            savedFlatSchedule!!.scheduleDay.add(Data_IntArray(currentDateId, arrayListOf()))
        }
        if (getById(currentGroupId!!, savedFlatSchedule!!.scheduleGroup) == null) {
            savedFlatSchedule!!.scheduleGroup.add(Data_IntArray(currentDateId, arrayListOf()))
        }

        var scheduleId: Int? = null
        val firstScheduleArray = getById(currentDateId, savedFlatSchedule!!.scheduleDay)
        val secondScheduleArray = getById(currentGroupId, savedFlatSchedule!!.scheduleGroup)

        if (firstScheduleArray!!.scheduleId.size < secondScheduleArray!!.scheduleId.size) {
            for (item in firstScheduleArray.scheduleId) {
                if (secondScheduleArray.scheduleId.contains(item)) {
                    scheduleId = item
                    break
                }
            }
        } else {
            for (item in secondScheduleArray.scheduleId) {
                if (firstScheduleArray.scheduleId.contains(item)) {
                    scheduleId = item
                    break
                }
            }
        }

        chosenScheduleIdIsNew = (scheduleId == null)
        if (scheduleId == null) {
            scheduleId = possibleIdFunctionality(collectAllScheduleIds())
            firstScheduleArray.scheduleId.add(scheduleId)
            secondScheduleArray.scheduleId.add(scheduleId)
        }

        convertChosenScheduleToAddPairItem(scheduleParams, currentDateId, currentGroupId, number)
        chosenScheduleId = scheduleId
        chosenPairNumber = number

        chosenDate = currentDate.toString()
        chosenGroup = currentGroup
    }

    private fun convertChosenScheduleToAddPairItem(scheduleParams: FlatScheduleParameters, currentDateId: Int, currentGroupId: Int, number: Int) {
        val schedule = getScheduleByGroupAndDayDetailed(currentGroupId, currentDateId, scheduleParams)
        val subPair1 = schedule[number*2]
        val subPair2 = schedule[number*2+1]

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

    fun removeScheduleItem(scheduleParams: FlatScheduleParameters, currentDate: Date, currentGroup: String, number: Int) {
        val currentGroupId = getItemId(scheduleParams.groupList, currentGroup)
        val currentDateId = getItemId(scheduleParams.dayList, currentDate)

        if (currentDateId == null || currentGroupId == null) {
            Log.d("ADMIN_EDITOR_CHECKER", "No date or group ID was found.")
            return
        }

        var scheduleId: Int? = null
        val firstScheduleArray = getById(currentDateId, savedFlatSchedule!!.scheduleDay)
        val secondScheduleArray = getById(currentGroupId, savedFlatSchedule!!.scheduleGroup)
        if (firstScheduleArray == null || secondScheduleArray == null) {
            Log.d("ADMIN_EDITOR_CHECKER", "No date or group array was found.")
            return
        }

        if (firstScheduleArray.scheduleId.size < secondScheduleArray.scheduleId.size) {
            for (item in firstScheduleArray.scheduleId) {
                if (secondScheduleArray.scheduleId.contains(item)) {
                    scheduleId = item
                    break
                }
            }
        } else {
            for (item in secondScheduleArray.scheduleId) {
                if (firstScheduleArray.scheduleId.contains(item)) {
                    scheduleId = item
                    break
                }
            }
        }

        if (scheduleId == null) {
            Log.d("ADMIN_EDITOR_CHECKER", "No schedule ID was identified.")
            return
        }

        removeScheduleItemById(savedFlatSchedule!!, scheduleId, number+1, true)
    }

    fun saveScheduleEdits(scheduleParams: FlatScheduleParameters, pair: Pair<ScheduleDetailed, ScheduleDetailed>) {
        removeScheduleItemById(savedFlatSchedule!!, chosenScheduleId!!, chosenPairNumber!!+1, false)
        addPairToFlatSchedule(savedFlatSchedule!!, scheduleParams, chosenScheduleId!!, pair)
        chosenScheduleItem = convertPairToArrayOfAddPairItem(Pair(pair.first, pair.second))
        chosenScheduleIdIsNew = false
    }
}
