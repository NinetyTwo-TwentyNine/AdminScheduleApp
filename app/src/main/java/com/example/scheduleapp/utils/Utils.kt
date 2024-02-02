package com.example.scheduleapp.utils

import android.bluetooth.BluetoothSocket
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import com.example.scheduleapp.data.AddPairItem
import com.example.scheduleapp.data.*
import com.example.scheduleapp.data.Constants.APP_ADMIN_EDIT_PAIR_ARRAY

object Utils {

    //================================================================================================================
    //General utility stuff
    //================================================================================================================

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

    fun getById(id: Int, array: ArrayList<Data_IntString>): Data_IntString? {
        for (item in array) {
            if (item.id == id) {
                return item
            }
        }
        return null
    }
    fun getById(id: Int, array: ArrayList<Data_IntDate>): Data_IntDate? {
        for (item in array) {
            if (item.id == id) {
                return item
            }
        }
        return null
    }
    fun getById(id: Int, array: ArrayList<Data_IntArray>): Data_IntArray? {
        for (item in array) {
            if (item.specialId == id) {
                return item
            }
        }
        return null
    }

    fun getItemId(dayList: ArrayList<Data_IntDate>, date: Date): Int? {
        for (item in dayList) {
            if (date == item.date) {
                return item.id
            }
        }
        return null
    }
    fun getItemId(itemList: ArrayList<Data_IntString>, itemName: String?): Int? {
        if (itemName == null) {
            return null
        }
        for (item in itemList) {
            if (itemName == item.title) {
                return item.id
            }
        }
        throw(IllegalStateException("No item ID was found, some part of the DB is probably missing or incorrect (${itemName})."))
    }

    fun getEmptyId(currentIds: ArrayList<Int>): Int {
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
        return getEmptyId(currentIds)
    }
    @JvmName("getPossibleIdDate")
    fun getPossibleId(originalList: ArrayList<Data_IntDate>): Int {
        val currentIds = arrayListOf<Int>()
        originalList.forEach { currentIds.add(it.id!!) }
        return getEmptyId(currentIds)
    }

    //================================================================================================================
    //Deep copy creation
    //================================================================================================================

    fun getDataIntDateDeepCopy(origItem: Data_IntDate): Data_IntDate {
        return Data_IntDate(
            id = origItem.id,
            date = Date(origItem.date?.year, origItem.date?.month, origItem.date?.day) )
    }

    fun getDataIntArrayDeepCopy(origItem: Data_IntArray): Data_IntArray {
        return Data_IntArray(
            specialId = origItem.specialId,
            scheduleId = (origItem.scheduleId.clone() as ArrayList<Int>) )
    }

    fun getDataIntIntIntArrayArrayDeepCopy(origItem: Data_IntIntIntArrayArray): Data_IntIntIntArrayArray {
        return Data_IntIntIntArrayArray(
            scheduleId = origItem.scheduleId,
            pairNum = origItem.pairNum,
            subPairs = (origItem.subPairs.clone() as ArrayList<Int>),
            subGroups = (origItem.subGroups.clone() as ArrayList<Int>),
            specialId = origItem.specialId)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getItemArrayDeepCopy(origArr: ArrayList<T>): ArrayList<T> {
        val newArr: ArrayList<T> = arrayListOf()
        origArr.forEach { item ->
            when(item!!::class.java) {
                Data_IntDate::class.java -> newArr.add(getDataIntDateDeepCopy(item as Data_IntDate) as T)
                Data_IntArray::class.java -> newArr.add(getDataIntArrayDeepCopy(item as Data_IntArray) as T)
                Data_IntIntIntArrayArray::class.java -> newArr.add(getDataIntIntIntArrayArrayDeepCopy(item as Data_IntIntIntArrayArray) as T)
                Data_IntString::class.java -> newArr.add((item as Data_IntString).copy() as T)
                AddPairItem::class.java -> newArr.add((item as AddPairItem).copy() as T)
                else -> throw(Exception("Attempt to duplicate an unexpected class."))
            }
        }
        return newArr
    }

    fun getFlatScheduleDetailedDeepCopy(origSchedule: FlatScheduleDetailed): FlatScheduleDetailed {
        return FlatScheduleDetailed(
            scheduleDay = getItemArrayDeepCopy(origSchedule.scheduleDay),
            scheduleGroup = getItemArrayDeepCopy(origSchedule.scheduleGroup),
            scheduleLesson = getItemArrayDeepCopy(origSchedule.scheduleLesson),
            cabinetLesson = getItemArrayDeepCopy(origSchedule.cabinetLesson),
            teacherLesson = getItemArrayDeepCopy(origSchedule.teacherLesson)
        )
    }

    fun getFlatScheduleBaseDeepCopy(origSchedule: FlatScheduleBase): FlatScheduleBase {
        return FlatScheduleBase(
            nameList = getItemArrayDeepCopy(origSchedule.nameList),
            scheduleName = getItemArrayDeepCopy(origSchedule.scheduleName),
            scheduleDay = getItemArrayDeepCopy(origSchedule.scheduleDay),
            scheduleGroup = getItemArrayDeepCopy(origSchedule.scheduleGroup),
            scheduleLesson = getItemArrayDeepCopy(origSchedule.scheduleLesson),
            cabinetLesson = getItemArrayDeepCopy(origSchedule.cabinetLesson),
            teacherLesson = getItemArrayDeepCopy(origSchedule.teacherLesson)
        )
    }

    //================================================================================================================
    //FlatScheduleDetailed-related stuff
    //================================================================================================================

    fun getScheduleIdByGroupAndDate(flatSchedule: FlatScheduleDetailed, currentDateId: Int, currentGroupId: Int): Int? {
        var scheduleId: Int? = null
        val firstScheduleArray = getById(currentDateId, flatSchedule.scheduleDay)
        val secondScheduleArray = getById(currentGroupId, flatSchedule.scheduleGroup)

        if (firstScheduleArray == null || secondScheduleArray == null) {
            return scheduleId
        }

        for (item in firstScheduleArray.scheduleId) {
            if (secondScheduleArray.scheduleId.contains(item)) {
                scheduleId = item
                break
            }
        }

        return scheduleId
    }

    fun moveDataFromScheduleToArray(schedule: FlatScheduleDetailed, parameters: FlatScheduleParameters, scheduleId: Int, detArray: ArrayList<ScheduleDetailed>) {
        for (item in schedule.cabinetLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].cabinet1 =
                            getById(item.specialId!!, parameters.cabinetList)!!.title!!
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].cabinet2 =
                            getById(item.specialId!!, parameters.cabinetList)!!.title!!
                    }
                }
                if (item.subGroups.contains(3)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].cabinet3 =
                            getById(item.specialId!!, parameters.cabinetList)!!.title!!
                    }
                }
            }
        }
        for (item in schedule.scheduleLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline1 =
                            getById(item.specialId!!, parameters.lessonList)!!.title!!
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline2 =
                            getById(item.specialId!!, parameters.lessonList)!!.title!!
                    }
                }
                if (item.subGroups.contains(3)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline3 =
                            getById(item.specialId!!, parameters.lessonList)!!.title!!
                    }
                }
            }
        }
        for (item in schedule.teacherLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher1 =
                            getById(item.specialId!!, parameters.teacherList)!!.title!!
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher2 =
                            getById(item.specialId!!, parameters.teacherList)!!.title!!
                    }
                }
                if (item.subGroups.contains(3)) {
                    for (subPair in item.subPairs) {
                        detArray[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher3 =
                            getById(item.specialId!!, parameters.teacherList)!!.title!!
                    }
                }
            }
        }
    }

    fun removeScheduleItemById(flatSchedule: FlatScheduleDetailed, scheduleId: Int, pairNum: Int? = null, canRemoveScheduleId: Boolean = true) {
        val arrayToRemove: ArrayList<Data_IntIntIntArrayArray> = arrayListOf()
        flatSchedule.scheduleLesson.forEach {
            if (it.scheduleId == scheduleId && (it.pairNum == pairNum || pairNum == null)) {
                arrayToRemove.add(it)
            }
        }
        arrayToRemove.forEach {
            flatSchedule.scheduleLesson.remove(it)
        }

        arrayToRemove.clear()
        flatSchedule.cabinetLesson.forEach {
            if (it.scheduleId == scheduleId && (it.pairNum == pairNum || pairNum == null)) {
                arrayToRemove.add(it)
            }
        }
        arrayToRemove.forEach {
            flatSchedule.cabinetLesson.remove(it)
        }

        arrayToRemove.clear()
        flatSchedule.teacherLesson.forEach {
            if (it.scheduleId == scheduleId && (it.pairNum == pairNum || pairNum == null)) {
                arrayToRemove.add(it)
            }
        }
        arrayToRemove.forEach {
            flatSchedule.teacherLesson.remove(it)
        }


        if (!canRemoveScheduleId) { return }
        for (i: Int in 0 until flatSchedule.scheduleLesson.size) {
            if (flatSchedule.scheduleLesson[i].scheduleId == scheduleId) {
                return
            }
        }
        for (i: Int in 0 until flatSchedule.cabinetLesson.size) {
            if (flatSchedule.cabinetLesson[i].scheduleId == scheduleId) {
                return
            }
        }
        for (i: Int in 0 until flatSchedule.teacherLesson.size) {
            if (flatSchedule.teacherLesson[i].scheduleId == scheduleId) {
                return
            }
        }

        val secondArrayToRemove: ArrayList<Data_IntArray> = arrayListOf()
        flatSchedule.scheduleDay.forEach {
            it.scheduleId.remove(scheduleId)
            if (it.scheduleId.isEmpty()) {
                secondArrayToRemove.add(it)
            }
        }
        secondArrayToRemove.forEach {
            flatSchedule.scheduleDay.remove(it)
        }

        secondArrayToRemove.clear()
        flatSchedule.scheduleGroup.forEach {
            it.scheduleId.remove(scheduleId)
            if (it.scheduleId.isEmpty()) {
                secondArrayToRemove.add(it)
            }
        }
        secondArrayToRemove.forEach {
            flatSchedule.scheduleGroup.remove(it)
        }
    }

    fun addPairToFlatSchedule(flatSchedule: FlatScheduleDetailed, scheduleParameters: FlatScheduleParameters, scheduleId: Int, pair: Pair<ScheduleDetailed, ScheduleDetailed>) {
        val subPair1 = pair.first
        val subPair2 = pair.second


        if ((subPair1.lessonNum!!-1)/2 != (subPair2.lessonNum!!-1)/2) {
            throw(Exception("Provided subpairs are from different pairs! (${subPair1.lessonNum!!}, ${subPair2.lessonNum!!})"))
        }

        val lessonArray: ArrayList<Data_IntIntIntArrayArray> = arrayListOf()
        if (subPair1.discipline1!!.length > 1) {
            lessonArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(1), getItemId(scheduleParameters.lessonList, subPair1.discipline1)))
        }
        if (subPair1.discipline2!!.length > 1) {
            lessonArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(2), getItemId(scheduleParameters.lessonList, subPair1.discipline2)))
        }
        if (subPair1.discipline3!!.length > 1) {
            lessonArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(3), getItemId(scheduleParameters.lessonList, subPair1.discipline3)))
        }
        if (subPair2.discipline1!!.length > 1) {
            lessonArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(1), getItemId(scheduleParameters.lessonList, subPair2.discipline1)))
        }
        if (subPair2.discipline2!!.length > 1) {
            lessonArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(2), getItemId(scheduleParameters.lessonList, subPair2.discipline2)))
        }
        if (subPair2.discipline3!!.length > 1) {
            lessonArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(3), getItemId(scheduleParameters.lessonList, subPair2.discipline3)))
        }
        unifyScheduleArray(lessonArray)

        val teacherArray: ArrayList<Data_IntIntIntArrayArray> = arrayListOf()
        if (subPair1.teacher1!!.length > 1) {
            teacherArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(1), getItemId(scheduleParameters.teacherList, subPair1.teacher1)))
        }
        if (subPair1.teacher2!!.length > 1) {
            teacherArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(2), getItemId(scheduleParameters.teacherList, subPair1.teacher2)))
        }
        if (subPair1.teacher3!!.length > 1) {
            teacherArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(3), getItemId(scheduleParameters.teacherList, subPair1.teacher3)))
        }
        if (subPair2.teacher1!!.length > 1) {
            teacherArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(1), getItemId(scheduleParameters.teacherList, subPair2.teacher1)))
        }
        if (subPair2.teacher2!!.length > 1) {
            teacherArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(2), getItemId(scheduleParameters.teacherList, subPair2.teacher2)))
        }
        if (subPair2.teacher3!!.length > 1) {
            teacherArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(3), getItemId(scheduleParameters.teacherList, subPair2.teacher3)))
        }
        unifyScheduleArray(teacherArray)

        val cabinetArray: ArrayList<Data_IntIntIntArrayArray> = arrayListOf()
        if (subPair1.cabinet1!!.length > 1) {
            cabinetArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(1), getItemId(scheduleParameters.cabinetList, subPair1.cabinet1)))
        }
        if (subPair1.cabinet2!!.length > 1) {
            cabinetArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(2), getItemId(scheduleParameters.cabinetList, subPair1.cabinet2)))
        }
        if (subPair1.cabinet3!!.length > 1) {
            cabinetArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair1.lessonNum!!-1)/2+1, subPairs = arrayListOf(1), subGroups = arrayListOf(3), getItemId(scheduleParameters.cabinetList, subPair1.cabinet3)))
        }
        if (subPair2.cabinet1!!.length > 1) {
            cabinetArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(1), getItemId(scheduleParameters.cabinetList, subPair2.cabinet1)))
        }
        if (subPair2.cabinet2!!.length > 1) {
            cabinetArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(2), getItemId(scheduleParameters.cabinetList, subPair2.cabinet2)))
        }
        if (subPair2.cabinet3!!.length > 1) {
            cabinetArray.add(Data_IntIntIntArrayArray(scheduleId, (subPair2.lessonNum!!-1)/2+1, subPairs = arrayListOf(2), subGroups = arrayListOf(3), getItemId(scheduleParameters.cabinetList, subPair2.cabinet3)))
        }
        unifyScheduleArray(cabinetArray)

        lessonArray.forEach { flatSchedule.scheduleLesson.add(it) }
        teacherArray.forEach { flatSchedule.teacherLesson.add(it) }
        cabinetArray.forEach { flatSchedule.cabinetLesson.add(it) }
    }

    fun changeSingleScheduleDay(dayList: ArrayList<Data_IntDate>, baseSchedule: FlatScheduleDetailed, newSchedule: FlatScheduleDetailed, day: Date): FlatScheduleDetailed {
        val dateId = getItemId(dayList, day)
        Log.d("ADMIN_SCHEDULE_SINGLEDAY_CHANGE_CHECK", "dayList: $dayList")
        Log.d("ADMIN_SCHEDULE_SINGLEDAY_CHANGE_CHECK", "day: $day")
        if (dateId == null) {
            Log.d("ADMIN_SCHEDULE_SINGLEDAY_CHANGE_CHECK", "Killing the application, because there is no date Id.")
            throw(Exception("No initialized date for chosen item, some part of the DB is probably missing or incorrect (${day})."))
        }

        val returnSchedule = getFlatScheduleDetailedDeepCopy(baseSchedule)

        val dateIdArray = getById(dateId, newSchedule.scheduleDay)
        if (dateIdArray != null) {
            dateIdArray.scheduleId!!.forEach { scheduleId ->
                newSchedule.scheduleGroup.forEach { newGroup ->
                    if (newGroup.scheduleId.contains(scheduleId)) {
                        returnSchedule.scheduleGroup.forEach { baseGroup ->
                            if (baseGroup.specialId == newGroup.specialId) {
                                if (!baseGroup.scheduleId.contains(scheduleId)) {
                                    baseGroup.scheduleId.add(scheduleId)
                                }
                            }
                        }
                    }
                }
            }
        }

        val returnSchedule_dateIdArray = getById(dateId, returnSchedule.scheduleDay)
        if (returnSchedule_dateIdArray != null) {
            val arrayToRemove: ArrayList<Int> = arrayListOf()
            returnSchedule_dateIdArray.scheduleId.forEach {
                if (!arrayToRemove.contains(it)) {
                    arrayToRemove.add(it)
                }
            }
            arrayToRemove.forEach {
                removeScheduleItemById(returnSchedule, it, canRemoveScheduleId = (dateIdArray == null))
            }
            if (dateIdArray != null) {
                returnSchedule_dateIdArray.scheduleId = dateIdArray.scheduleId.clone() as ArrayList<Int>
            }
        } else if (dateIdArray != null) {
            returnSchedule.scheduleDay.add(Data_IntArray(dateId, dateIdArray.scheduleId.clone() as ArrayList<Int>))
        }

        if (dateIdArray != null) {
            dateIdArray.scheduleId!!.forEach { scheduleId ->
                removeScheduleItemById(returnSchedule, scheduleId, canRemoveScheduleId = false)
                newSchedule.scheduleLesson.forEach {
                    if (it.scheduleId == scheduleId) {
                        returnSchedule.scheduleLesson.add(getDataIntIntIntArrayArrayDeepCopy(it))
                    }
                }
                newSchedule.teacherLesson.forEach {
                    if (it.scheduleId == scheduleId) {
                        returnSchedule.teacherLesson.add(getDataIntIntIntArrayArrayDeepCopy(it))
                    }
                }
                newSchedule.cabinetLesson.forEach {
                    if (it.scheduleId == scheduleId) {
                        returnSchedule.cabinetLesson.add(getDataIntIntIntArrayArrayDeepCopy(it))
                    }
                }
            }
        }

        return returnSchedule
    }

    //================================================================================================================
    //FlatScheduleBase-related stuff
    //================================================================================================================

    fun getScheduleIdByGroupDateAndBaseScheduleId(flatSchedule: FlatScheduleBase, currentDateId: Int, currentGroupId: Int, baseScheduleId: Int): Int? {
        var scheduleId: Int? = null
        val firstScheduleArray = getById(currentDateId, flatSchedule.scheduleDay)
        val secondScheduleArray = getById(currentGroupId, flatSchedule.scheduleGroup)
        val thirdScheduleArray = getById(baseScheduleId, flatSchedule.scheduleName)

        if (firstScheduleArray == null || secondScheduleArray == null || thirdScheduleArray == null) {
            return scheduleId
        }

        for (item in firstScheduleArray.scheduleId) {
            if (secondScheduleArray.scheduleId.contains(item) && thirdScheduleArray.scheduleId.contains(item)) {
                scheduleId = item
                break
            }
        }

        return scheduleId
    }

    fun moveDataFromScheduleToArray(schedule: FlatScheduleBase, parameters: FlatScheduleParameters, scheduleId: Int, detArray: ArrayList<ScheduleDetailed>) {
        moveDataFromScheduleToArray(FlatScheduleDetailed(
            scheduleDay = schedule.scheduleDay,
            scheduleGroup = schedule.scheduleGroup,
            scheduleLesson = schedule.scheduleLesson,
            teacherLesson = schedule.teacherLesson,
            cabinetLesson = schedule.cabinetLesson
        ), parameters, scheduleId, detArray)
    }

    fun removeScheduleItemById(flatSchedule: FlatScheduleBase, scheduleId: Int, pairNum: Int? = null, canRemoveScheduleId: Boolean = true) {
        removeScheduleItemById(FlatScheduleDetailed(
            scheduleDay = flatSchedule.scheduleDay,
            scheduleGroup = flatSchedule.scheduleGroup,
            scheduleLesson = flatSchedule.scheduleLesson,
            teacherLesson = flatSchedule.teacherLesson,
            cabinetLesson = flatSchedule.cabinetLesson
        ), scheduleId, pairNum, canRemoveScheduleId)

        if (!canRemoveScheduleId) { return }

        flatSchedule.scheduleDay.forEach {
            if (it.scheduleId.contains(scheduleId)) {
                return
            }
        }
        flatSchedule.scheduleGroup.forEach {
            if (it.scheduleId.contains(scheduleId)) {
                return
            }
        }

        for (i in 0 until flatSchedule.scheduleName.count()) {
            if (flatSchedule.scheduleName[i].scheduleId.contains(scheduleId)) {
                flatSchedule.scheduleName[i].scheduleId.remove(scheduleId)
                if (flatSchedule.scheduleName[i].scheduleId.isEmpty()) {
                    //flatSchedule.nameList.remove(getById(flatSchedule.scheduleName[i].specialId!!, flatSchedule.nameList))
                    flatSchedule.scheduleName.removeAt(i)
                }
                break
            }
        }
    }

    fun addPairToFlatSchedule(flatSchedule: FlatScheduleBase, scheduleParameters: FlatScheduleParameters, scheduleId: Int, pair: Pair<ScheduleDetailed, ScheduleDetailed>) {
        addPairToFlatSchedule(FlatScheduleDetailed(
            scheduleDay = flatSchedule.scheduleDay,
            scheduleGroup = flatSchedule.scheduleGroup,
            scheduleLesson = flatSchedule.scheduleLesson,
            teacherLesson = flatSchedule.teacherLesson,
            cabinetLesson = flatSchedule.cabinetLesson
        ), scheduleParameters, scheduleId, pair)
    }

    fun changeSingleScheduleDay(dateId: Int, baseSchedule: FlatScheduleBase, newSchedule: FlatScheduleBase, nameId: Int): FlatScheduleBase {
        val returnSchedule = getFlatScheduleBaseDeepCopy(baseSchedule)

        val dateIdArray = getById(dateId, newSchedule.scheduleDay)
        val nameIdArray = getById(nameId, newSchedule.scheduleName)
        if (dateIdArray != null && nameIdArray != null) {
            nameIdArray.scheduleId.forEach { scheduleId ->
                if (dateIdArray.scheduleId.contains(scheduleId)) {
                    newSchedule.scheduleGroup.forEach { newGroup ->
                        if (newGroup.scheduleId.contains(scheduleId)) {
                            returnSchedule.scheduleGroup.forEach { baseGroup ->
                                if (baseGroup.specialId == newGroup.specialId) {
                                    if (!baseGroup.scheduleId.contains(scheduleId)) {
                                        baseGroup.scheduleId.add(scheduleId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        var returnSchedule_dateIdArray = getById(dateId, returnSchedule.scheduleDay)
        var returnSchedule_nameIdArray = getById(nameId, returnSchedule.scheduleName)
        if (returnSchedule_nameIdArray != null) {
            val arrayToRemove: ArrayList<Int> = arrayListOf()
            returnSchedule_nameIdArray.scheduleId.forEach {
                if (returnSchedule_dateIdArray == null || returnSchedule_dateIdArray?.scheduleId!!.contains(it)) {
                    if (!arrayToRemove.contains(it)) {
                        arrayToRemove.add(it)
                    }
                }
            }
            arrayToRemove.forEach {
                removeScheduleItemById(returnSchedule, it, canRemoveScheduleId = (dateIdArray == null || nameIdArray == null))
            }
            if (dateIdArray != null && nameIdArray != null) {
                nameIdArray.scheduleId.forEach {
                    if (dateIdArray.scheduleId.contains(it)) {
                        if (!returnSchedule_dateIdArray!!.scheduleId.contains(it)) {
                            returnSchedule_dateIdArray!!.scheduleId.add(it)
                        }
                        if (returnSchedule_nameIdArray == null) {
                            returnSchedule_nameIdArray = Data_IntArray(nameId)
                            returnSchedule.scheduleName.add(returnSchedule_nameIdArray!!)
                        }
                        if (!returnSchedule_nameIdArray!!.scheduleId.contains(it)) {
                            returnSchedule_nameIdArray!!.scheduleId.add(it)
                        }
                    }
                }
            }
        } else if (dateIdArray != null && nameIdArray != null) {
            nameIdArray.scheduleId.forEach {
                if (dateIdArray.scheduleId.contains(it)) {
                    if (returnSchedule_dateIdArray == null) {
                        returnSchedule_dateIdArray = Data_IntArray(dateId)
                        returnSchedule.scheduleDay.add(returnSchedule_dateIdArray!!)
                    }
                    if (!returnSchedule_dateIdArray!!.scheduleId.contains(it)) {
                        returnSchedule_dateIdArray!!.scheduleId.add(it)
                    }
                    if (returnSchedule_nameIdArray == null) {
                        returnSchedule_nameIdArray = Data_IntArray(nameId)
                        returnSchedule.scheduleName.add(returnSchedule_nameIdArray!!)
                    }
                    if (!returnSchedule_nameIdArray!!.scheduleId.contains(it)) {
                        returnSchedule_nameIdArray!!.scheduleId.add(it)
                    }
                }
            }
        }


        if (nameIdArray != null) {
            nameIdArray.scheduleId!!.forEach { scheduleId ->
                if (dateIdArray == null || dateIdArray?.scheduleId!!.contains(scheduleId)) {
                    removeScheduleItemById(returnSchedule, scheduleId, canRemoveScheduleId = false)
                    newSchedule.scheduleLesson.forEach {
                        if (it.scheduleId == scheduleId) {
                            returnSchedule.scheduleLesson.add(getDataIntIntIntArrayArrayDeepCopy(it))
                        }
                    }
                    newSchedule.teacherLesson.forEach {
                        if (it.scheduleId == scheduleId) {
                            returnSchedule.teacherLesson.add(getDataIntIntIntArrayArrayDeepCopy(it))
                        }
                    }
                    newSchedule.cabinetLesson.forEach {
                        if (it.scheduleId == scheduleId) {
                            returnSchedule.cabinetLesson.add(getDataIntIntIntArrayArrayDeepCopy(it))
                        }
                    }
                }
            }
        }

        return returnSchedule
    }

    fun applyBaseScheduleByNameAndDate(flatSchedule: FlatScheduleDetailed, dateId: Int, baseSchedule: FlatScheduleBase, nameId: Int, dayId: Int) {
        val base_dayIdArray = getById(dayId, baseSchedule.scheduleDay)
        val base_nameIdArray = getById(nameId, baseSchedule.scheduleName)

        if (base_dayIdArray == null || base_nameIdArray == null) {
            Log.d("ADMIN_BASE_SCHEDULE_APPLICATION", "No IDs to apply for that day and base schedule name.")
            return
        }

        val idsToApply: ArrayList<Int> = arrayListOf()
        base_nameIdArray.scheduleId.forEach {
            if (base_dayIdArray.scheduleId.contains(it)) {
                idsToApply.add(it)
            }
        }
        if (idsToApply.isEmpty()) {
            Log.d("ADMIN_BASE_SCHEDULE_APPLICATION", "No IDs to apply for that day and base schedule name.")
            return
        }

        var dateIdArray = getById(dateId, flatSchedule.scheduleDay)
        if (dateIdArray == null) {
            dateIdArray = Data_IntArray(dateId, arrayListOf())
            flatSchedule.scheduleDay.add(dateIdArray)
        }

        idsToApply.forEach { scheduleId ->
            var base_groupIdArray: Data_IntArray? = null
            baseSchedule.scheduleGroup.forEach {
                if (it.scheduleId.contains(scheduleId)) {
                    base_groupIdArray = it
                }
            }
            if (base_groupIdArray == null) {
                throw(Exception("Found a schedule ID with no group attached to it! Something is definitely wrong with the DB."))
            }

            var groupIdArray = getById(base_groupIdArray!!.specialId!!, flatSchedule.scheduleGroup)
            if (groupIdArray == null) {
                groupIdArray = Data_IntArray(base_groupIdArray!!.specialId!!, arrayListOf())
                flatSchedule.scheduleGroup.add(groupIdArray)
            }

            var sameIdInFlatSchedule: Int? = null
            dateIdArray.scheduleId.forEach {
                if (groupIdArray.scheduleId.contains(it)) {
                    sameIdInFlatSchedule = it
                }
            }

            if (sameIdInFlatSchedule == null) {
                val totalIdsList: ArrayList<Int> = arrayListOf()
                groupIdArray.scheduleId.forEach {
                    if (!totalIdsList.contains(it)) {
                        totalIdsList.add(it)
                    }
                }
                dateIdArray.scheduleId.forEach {
                    if (!totalIdsList.contains(it)) {
                        totalIdsList.add(it)
                    }
                }
                sameIdInFlatSchedule = getEmptyId(totalIdsList)

                groupIdArray.scheduleId.add(sameIdInFlatSchedule!!)
                dateIdArray.scheduleId.add(sameIdInFlatSchedule!!)
            } else {
                removeScheduleItemById(flatSchedule, sameIdInFlatSchedule!!, canRemoveScheduleId = false)
            }

            baseSchedule.scheduleLesson.forEach {
                if (it.scheduleId == scheduleId) {
                    val duplicateItem = getDataIntIntIntArrayArrayDeepCopy(it)
                    duplicateItem.scheduleId = sameIdInFlatSchedule
                    flatSchedule.scheduleLesson.add(duplicateItem)
                }
            }
            baseSchedule.cabinetLesson.forEach {
                if (it.scheduleId == scheduleId) {
                    val duplicateItem = getDataIntIntIntArrayArrayDeepCopy(it)
                    duplicateItem.scheduleId = sameIdInFlatSchedule
                    flatSchedule.cabinetLesson.add(duplicateItem)
                }
            }
            baseSchedule.teacherLesson.forEach {
                if (it.scheduleId == scheduleId) {
                    val duplicateItem = getDataIntIntIntArrayArrayDeepCopy(it)
                    duplicateItem.scheduleId = sameIdInFlatSchedule
                    flatSchedule.teacherLesson.add(duplicateItem)
                }
            }

            if (groupIdArray.scheduleId.isEmpty()) {
                flatSchedule.scheduleGroup.remove(groupIdArray)
            }
        }

        if (dateIdArray.scheduleId.isEmpty()) {
            flatSchedule.scheduleDay.remove(dateIdArray)
        }
    }

    //================================================================================================================
    //Conversions
    //================================================================================================================

    private fun convertScheduleDetailedToPairOfAddPairItem(subPair: ScheduleDetailed, items: Pair<AddPairItem, AddPairItem>) {
        val subGroup1Empty = (subPair.teacher1 == "-" && subPair.cabinet1 == "-" && subPair.discipline1 == "-")
        if (subPair.teacher1 == "-") subPair.teacher1 = ""; if (subPair.cabinet1 == "-") subPair.cabinet1 = ""; if (subPair.discipline1 == "-") subPair.discipline1 = ""

        val subGroup2Empty = (subPair.teacher2 == "-" && subPair.cabinet2 == "-" && subPair.discipline2 == "-")
        if (subPair.teacher2 == "-") subPair.teacher2 = ""; if (subPair.cabinet2 == "-") subPair.cabinet2 = ""; if (subPair.discipline2 == "-") subPair.discipline2 = ""

        val subGroup3Empty = (subPair.teacher3 == "-" && subPair.cabinet3 == "-" && subPair.discipline3 == "-")
        if (subPair.teacher3 == "-") subPair.teacher3 = ""; if (subPair.cabinet3 == "-") subPair.cabinet3 = ""; if (subPair.discipline3 == "-") subPair.discipline3 = ""

        items.first.teacher = subPair.teacher1!!
        items.first.pairName = subPair.discipline1!!
        items.first.cabinet = subPair.cabinet1!!

        if (!subGroup1Empty || !subGroup2Empty || !subGroup3Empty) {
            if (!subGroup1Empty && subGroup2Empty && subGroup3Empty) {
                items.second.visibility = true
                items.second.subGroup = "1"
            } else if (subGroup1Empty && !subGroup2Empty && subGroup3Empty) {
                items.second.visibility = true
                items.second.subGroup = "2"
                items.first.teacher = subPair.teacher2!!
                items.first.pairName = subPair.discipline2!!
                items.first.cabinet = subPair.cabinet2!!
            } else if (subGroup1Empty && subGroup2Empty && !subGroup3Empty) {
                items.second.visibility = true
                items.second.subGroup = "3"
                items.first.teacher = subPair.teacher3!!
                items.first.pairName = subPair.discipline3!!
                items.first.cabinet = subPair.cabinet3!!
            } else if ( (!subGroup2Empty && (subPair.teacher1 != subPair.teacher2 || subPair.cabinet1 != subPair.cabinet2 || subPair.discipline1 != subPair.discipline2)) ||
                (!subGroup3Empty && (subPair.teacher1 != subPair.teacher3 || subPair.cabinet1 != subPair.cabinet3 || subPair.discipline1 != subPair.discipline3))
            ) {
                items.second.visibility = true
                items.second.teacherSecond = subPair.teacher2!!
                items.second.cabinetSecond = subPair.cabinet2!!
                items.second.teacherThird = subPair.teacher3!!
                items.second.cabinetThird = subPair.cabinet3!!
            }
        }

        if (items.second.visibility || !subGroup1Empty) {
            items.first.visibility = true
        }
    }
    fun convertPairToArrayOfAddPairItem(pair: Pair<ScheduleDetailed, ScheduleDetailed>): ArrayList<AddPairItem> {
        val subPair1 = pair.first
        val subPair2 = pair.second
        val items = ArrayList(APP_ADMIN_EDIT_PAIR_ARRAY)

        if (!checkIfScheduleDetailedEquals(subPair1, subPair2)) {
            convertScheduleDetailedToPairOfAddPairItem(subPair1, Pair(items[0], items[1]))
            convertScheduleDetailedToPairOfAddPairItem(subPair2, Pair(items[2], items[3]))
        } else {
            convertScheduleDetailedToPairOfAddPairItem(subPair1, Pair(items[0], items[1]))
        }

        /*Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "")
        Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "Are they the same? ${checkIfScheduleDetailedEquals(subPair1, subPair2)}")
        Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "visibility1 = ${item[1].visibility}")
        Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "visibility2 = ${item[2].visibility}")
        Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "visibility3 = ${item[3].visibility}")*/

        return items
    }

    private fun convertPairOfAddPairItemToScheduleDetailed(item: Pair<AddPairItem, AddPairItem>, subPair: ScheduleDetailed) {
        if (item.second.visibility) {
            if (item.second.subGroup.isNotEmpty()) {
                when(item.second.subGroup) {
                    "1" -> {
                        subPair.cabinet1 = item.first.cabinet
                        subPair.teacher1 = item.first.teacher
                        subPair.discipline1 = item.first.pairName
                    }
                    "2" -> {
                        subPair.cabinet2 = item.first.cabinet
                        subPair.teacher2 = item.first.teacher
                        subPair.discipline2 = item.first.pairName
                    }
                    "3" -> {
                        subPair.cabinet3 = item.first.cabinet
                        subPair.teacher3 = item.first.teacher
                        subPair.discipline3 = item.first.pairName
                    }
                    else -> throw(IllegalStateException("Weird subgroup number was found: ${item.second.subGroup}."))
                }
            } else {
                if (item.first.cabinet.isNotEmpty() || item.first.teacher.isNotEmpty()) {
                    subPair.cabinet1 = item.first.cabinet
                    subPair.teacher1 = item.first.teacher
                    subPair.discipline1 = item.first.pairName
                }
                if (item.second.cabinetSecond.isNotEmpty() || item.second.teacherSecond.isNotEmpty()) {
                    subPair.cabinet2 = item.second.cabinetSecond
                    subPair.teacher2 = item.second.teacherSecond
                    subPair.discipline2 = item.first.pairName
                }
                if (item.second.cabinetThird.isNotEmpty() || item.second.teacherThird.isNotEmpty()) {
                    subPair.cabinet3 = item.second.cabinetThird
                    subPair.teacher3 = item.second.teacherThird
                    subPair.discipline3 = item.first.pairName
                }
            }
        } else {
            subPair.cabinet1 = item.first.cabinet
            subPair.teacher1 = item.first.teacher
            subPair.discipline1 = item.first.pairName
            subPair.cabinet2 = item.first.cabinet
            subPair.teacher2 = item.first.teacher
            subPair.discipline2 = item.first.pairName
        }
    }
    fun convertArrayOfAddPairItemToPair(addPairItem: ArrayList<AddPairItem>, pair: Pair<ScheduleDetailed, ScheduleDetailed>) {
        val subPair1 = pair.first
        val subPair2 = pair.second

        convertPairOfAddPairItemToScheduleDetailed(Pair(addPairItem[0], addPairItem[1]), subPair1)
        if (addPairItem[2].visibility) {
            convertPairOfAddPairItemToScheduleDetailed(Pair(addPairItem[2], addPairItem[3]), subPair2)
        } else {
            convertPairOfAddPairItemToScheduleDetailed(Pair(addPairItem[0], addPairItem[1]), subPair2)
        }
    }

    private fun unifyScheduleArray(scheduleItemArray: ArrayList<Data_IntIntIntArrayArray>) {
        val arrayToRemove: ArrayList<Data_IntIntIntArrayArray> = arrayListOf()

        var currentIndex = 0
        while (currentIndex < scheduleItemArray.size) {
            for (i: Int in 0 until scheduleItemArray.size) {
                if (scheduleItemArray[i].specialId == scheduleItemArray[currentIndex].specialId && currentIndex != i) {

                    /*Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "")
                    Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Current-subp = ${scheduleItemArray[currentIndex].subPairs}, i-subp = ${scheduleItemArray[i].subPairs}")
                    Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Are they the same? ${scheduleItemArray[currentIndex].subPairs == scheduleItemArray[i].subPairs}")*/
                    if (checkIfItemArraysAreEqual(scheduleItemArray[currentIndex].subPairs, scheduleItemArray[i].subPairs)) {
                        scheduleItemArray[i].subGroups.forEach {
                            if (!scheduleItemArray[currentIndex].subGroups.contains(it)) {
                                scheduleItemArray[currentIndex].subGroups.add(it)
                            }
                        }
                    }

                    /*Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "")
                    Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Current-subg = ${scheduleItemArray[currentIndex].subGroups}, i-subg = ${scheduleItemArray[i].subGroups}")
                    Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Are they the same? ${scheduleItemArray[currentIndex].subGroups == scheduleItemArray[i].subGroups}")*/
                    if (checkIfItemArraysAreEqual(scheduleItemArray[currentIndex].subGroups, scheduleItemArray[i].subGroups)) {
                        scheduleItemArray[i].subPairs.forEach {
                            if (!scheduleItemArray[currentIndex].subPairs.contains(it)) {
                                scheduleItemArray[currentIndex].subPairs.add(it)
                            }
                        }
                    }

                    if (checkIfItemArraysAreEqual(scheduleItemArray[currentIndex].subPairs, scheduleItemArray[i].subPairs) ||
                        checkIfItemArraysAreEqual(scheduleItemArray[currentIndex].subGroups, scheduleItemArray[i].subGroups))
                    {
                        arrayToRemove.add(scheduleItemArray[i])
                    }
                }
            }
            arrayToRemove.forEach {
                if (scheduleItemArray.indexOf(it) < currentIndex) {
                    currentIndex--
                }
                scheduleItemArray.remove(it)
            }
            arrayToRemove.clear()
            currentIndex++
        }
    }

    //================================================================================================================
    //Comparison-related stuff
    //================================================================================================================

    fun checkIfAddPairItemsAreEqual(item1: AddPairItem, item2: AddPairItem): Boolean {
        return (item1.pairName == item2.pairName &&
                item1.teacher == item2.teacher &&
                item1.cabinet == item2.cabinet &&
                item1.teacherSecond == item2.teacherSecond &&
                item1.cabinetSecond == item2.cabinetSecond &&
                item1.teacherThird == item2.teacherThird &&
                item1.cabinetThird == item2.cabinetThird &&
                item1.subGroup == item2.subGroup &&
                item1.type == item2.type &&
                item1.visibility == item2.visibility)
    }

    fun checkIfScheduleDetailedEquals(item1: ScheduleDetailed, item2: ScheduleDetailed): Boolean {
        return (item1.discipline1 == item2.discipline1 &&
                item1.teacher1 == item2.teacher1 &&
                item1.cabinet1 == item2.cabinet1 &&
                item1.discipline2 == item2.discipline2 &&
                item1.teacher2 == item2.teacher2 &&
                item1.cabinet2 == item2.cabinet2 &&
                item1.discipline3 == item2.discipline3 &&
                item1.teacher3 == item2.teacher3 &&
                item1.cabinet3 == item2.cabinet3)
    }

    fun <T> checkIfItemArraysAreEqual(itemArray1: ArrayList<T>, itemArray2: ArrayList<T>): Boolean {
        if (itemArray1.size != itemArray2.size) {return false}
        for (i: Int in 0 until itemArray1.size) {
            if (!itemArray1.contains(itemArray2[i]) || !itemArray2.contains(itemArray1[i])) {
                return false
            }
        }
        return true
    }

    fun checkIfFlatScheduleDetailedEquals(flatSchedule1: FlatScheduleDetailed, flatSchedule2: FlatScheduleDetailed): Boolean {
        /*Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "The FlatScheduleDetailed-comparison function was called.")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar schedule-lesson = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleLesson, flatSchedule2.scheduleLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar schedule-teacher = ${checkIfItemArraysAreEqual(flatSchedule1.teacherLesson, flatSchedule2.teacherLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar schedule-cabinet = ${checkIfItemArraysAreEqual(flatSchedule1.cabinetLesson, flatSchedule2.cabinetLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar day-schedule = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleDay, flatSchedule2.scheduleDay)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar group-schedule = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleGroup, flatSchedule2.scheduleGroup)}")*/
        return (checkIfItemArraysAreEqual(flatSchedule1.scheduleLesson, flatSchedule2.scheduleLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.teacherLesson, flatSchedule2.teacherLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.cabinetLesson, flatSchedule2.cabinetLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleDay, flatSchedule2.scheduleDay) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleGroup, flatSchedule2.scheduleGroup))
    }

    fun checkIfFlatScheduleBaseEquals(flatSchedule1: FlatScheduleBase, flatSchedule2: FlatScheduleBase, shouldCheckNameList: Boolean): Boolean {
        /*Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "The FlatScheduleBase-comparison function was called.")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Should check the name list = $shouldCheckNameList")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar name-list = ${checkIfItemArraysAreEqual(flatSchedule1.nameList, flatSchedule2.nameList)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar schedule-lesson = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleLesson, flatSchedule2.scheduleLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar schedule-teacher = ${checkIfItemArraysAreEqual(flatSchedule1.teacherLesson, flatSchedule2.teacherLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar schedule-cabinet = ${checkIfItemArraysAreEqual(flatSchedule1.cabinetLesson, flatSchedule2.cabinetLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar day-schedule = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleDay, flatSchedule2.scheduleDay)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "Similar group-schedule = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleGroup, flatSchedule2.scheduleGroup)}")*/
        return ((!shouldCheckNameList || checkIfItemArraysAreEqual(flatSchedule1.nameList, flatSchedule2.nameList)) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleName, flatSchedule2.scheduleName) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleLesson, flatSchedule2.scheduleLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.teacherLesson, flatSchedule2.teacherLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.cabinetLesson, flatSchedule2.cabinetLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleDay, flatSchedule2.scheduleDay) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleGroup, flatSchedule2.scheduleGroup))
    }
}