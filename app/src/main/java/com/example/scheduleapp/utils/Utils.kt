package com.example.scheduleapp.utils

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

    //================================================================================================================
    //Deep copy creation
    //================================================================================================================

    fun getArrayOfDataIntStringDeepCopy(origArr: ArrayList<Data_IntString>): ArrayList<Data_IntString> {
        val newArr = arrayListOf<Data_IntString>()
        origArr.forEach { newArr.add(
            //Data_IntString(it.id, it.title)
            it.copy()
        ) }
        return newArr
    }

    fun getArrayOfDataIntArrayDeepCopy(origArr: ArrayList<Data_IntArray>): ArrayList<Data_IntArray> {
        val newArr = arrayListOf<Data_IntArray>()
        origArr.forEach { newArr.add(
            //Data_IntArray(it.specialId, (it.scheduleId.clone() as ArrayList<Int>) )
            it.copy()
        ) }
        return newArr
    }

    fun getArrayOfDataIntIntIntArrayArrayDeepCopy(origArr: ArrayList<Data_IntIntIntArrayArray>): ArrayList<Data_IntIntIntArrayArray> {
        val newArr = arrayListOf<Data_IntIntIntArrayArray>()
        origArr.forEach { newArr.add(
            /*Data_IntIntIntArrayArray(
            scheduleId = it.scheduleId,
            pairNum = it.pairNum,
            subPairs = (it.subPairs.clone() as ArrayList<Int>),
            subGroups = (it.subGroups.clone() as ArrayList<Int>),
            specialId = it.specialId)*/
            it.copy()
        ) }
        return newArr
    }

    fun getArrayOfAddPairItemDeepCopy(origArr: ArrayList<AddPairItem>): ArrayList<AddPairItem> {
        val newArr = arrayListOf<AddPairItem>()
        origArr.forEach { newArr.add(
            /*AddPairItem(
            pairName = it.pairName,
            teacher = it.teacher,
            teacherSecond = it.teacherSecond,
            teacherThird = it.teacherThird,
            cabinet = it.cabinet,
            cabinetSecond = it.cabinetSecond,
            cabinetThird = it.cabinetThird,
            subGroup = it.subGroup,
            type = it.type,
            id = it.id,
            visibility = it.visibility)*/
            it.copy()
        ) }
        return newArr
    }

    fun getFlatScheduleDetailedDeepCopy(origSchedule: FlatScheduleDetailed): FlatScheduleDetailed {
        val newSchedule = FlatScheduleDetailed(
            scheduleDay = getArrayOfDataIntArrayDeepCopy(origSchedule.scheduleDay),
            scheduleGroup = getArrayOfDataIntArrayDeepCopy(origSchedule.scheduleGroup),
            scheduleLesson = getArrayOfDataIntIntIntArrayArrayDeepCopy(origSchedule.scheduleLesson),
            cabinetLesson = getArrayOfDataIntIntIntArrayArrayDeepCopy(origSchedule.cabinetLesson),
            teacherLesson = getArrayOfDataIntIntIntArrayArrayDeepCopy(origSchedule.teacherLesson))
        return newSchedule
    }

    //================================================================================================================
    //FlatScheduleDetailed-related stuff
    //================================================================================================================

    fun getScheduleIdByGroupAndDate(flatSchedule: FlatScheduleDetailed, currentDateId: Int, currentGroupId: Int): Int? {
        var scheduleId: Int? = null
        val firstScheduleArray = getById(currentDateId, flatSchedule.scheduleDay)
        val secondScheduleArray = getById(currentGroupId, flatSchedule.scheduleGroup)

        val smallerArray: Data_IntArray
        val biggerArray: Data_IntArray
        if (firstScheduleArray!!.scheduleId.size < secondScheduleArray!!.scheduleId.size) {
            smallerArray = firstScheduleArray
            biggerArray = secondScheduleArray
        } else {
            smallerArray = secondScheduleArray
            biggerArray = firstScheduleArray
        }
        for (item in smallerArray.scheduleId) {
            if (biggerArray.scheduleId.contains(item)) {
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
        if (dateId == null) {
            throw(Exception("No initialized date for chosen item, some part of the DB is probably missing or incorrect (${day})."))
        }

        val returnSchedule = getFlatScheduleDetailedDeepCopy(baseSchedule)

        val dateIdArray = getById(dateId, newSchedule.scheduleDay)!!
        dateIdArray.scheduleId.forEach { scheduleId->
            newSchedule.scheduleGroup.forEach { newGroup->
                if (newGroup.scheduleId.contains(scheduleId)) {
                    returnSchedule.scheduleGroup.forEach { baseGroup->
                        if (baseGroup.specialId == newGroup.specialId) {
                            if (!baseGroup.scheduleId.contains(scheduleId)) {
                                baseGroup.scheduleId.add(scheduleId)
                            }
                        }
                    }
                }
            }
        }

        val returnSchedule_date = getById(dateId, returnSchedule.scheduleDay)
        if (returnSchedule_date != null) {
            val arrayToRemove: ArrayList<Int> = arrayListOf()
            returnSchedule_date.scheduleId.forEach {
                if (!arrayToRemove.contains(it)) {
                    arrayToRemove.add(it)
                }
            }
            arrayToRemove.forEach {
                removeScheduleItemById(returnSchedule, it, canRemoveScheduleId = false)
            }
            returnSchedule_date.scheduleId = dateIdArray.scheduleId.clone() as ArrayList<Int>
        } else {
            returnSchedule.scheduleDay.add(Data_IntArray(dateId, dateIdArray.scheduleId.clone() as ArrayList<Int>))
        }

        dateIdArray.scheduleId.forEach { scheduleId->
            removeScheduleItemById(returnSchedule, scheduleId, canRemoveScheduleId = false)
            newSchedule.scheduleLesson.forEach {
                if (it.scheduleId == scheduleId) {
                    returnSchedule.scheduleLesson.add(it.copy())
                }
            }
            newSchedule.teacherLesson.forEach {
                if (it.scheduleId == scheduleId) {
                    returnSchedule.teacherLesson.add(it.copy())
                }
            }
            newSchedule.cabinetLesson.forEach {
                if (it.scheduleId == scheduleId) {
                    returnSchedule.cabinetLesson.add(it.copy())
                }
            }
        }

        return returnSchedule
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

        if (!subPair1.actuallyEquals(subPair2)) {
            convertScheduleDetailedToPairOfAddPairItem(subPair1, Pair(items[0], items[1]))
            convertScheduleDetailedToPairOfAddPairItem(subPair2, Pair(items[2], items[3]))
        } else {
            convertScheduleDetailedToPairOfAddPairItem(subPair1, Pair(items[0], items[1]))
        }

        //Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "")
        //Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "Are they the same? ${checkIfScheduleDetailedEquals(subPair1, subPair2)}")
        //Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "visibility1 = ${item[1].visibility}")
        //Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "visibility2 = ${item[2].visibility}")
        //Log.d("ADMIN_PAIR_TO_ADDPAIR_CONVERSION_CHECKER", "visibility3 = ${item[3].visibility}")

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

                    //Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "")
                    //Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Current-subp = ${scheduleItemArray[currentIndex].subPairs}, i-subp = ${scheduleItemArray[i].subPairs}")
                    //Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Are they the same? ${scheduleItemArray[currentIndex].subPairs == scheduleItemArray[i].subPairs}")

                    if (scheduleItemArray[currentIndex].subPairs == scheduleItemArray[i].subPairs) {
                        scheduleItemArray[i].subGroups.forEach {
                            if (!scheduleItemArray[currentIndex].subGroups.contains(it)) {
                                scheduleItemArray[currentIndex].subGroups.add(it)
                            }
                        }
                    }

                    //Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "")
                    //Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Current-subg = ${scheduleItemArray[currentIndex].subGroups}, i-subg = ${scheduleItemArray[i].subGroups}")
                    //Log.d("ADMIN_SCHEDULE_UNIFIER_CHECKER", "Are they the same? ${scheduleItemArray[currentIndex].subGroups == scheduleItemArray[i].subGroups}")

                    if (scheduleItemArray[currentIndex].subGroups == scheduleItemArray[i].subGroups) {
                        scheduleItemArray[i].subPairs.forEach {
                            if (!scheduleItemArray[currentIndex].subPairs.contains(it)) {
                                scheduleItemArray[currentIndex].subPairs.add(it)
                            }
                        }
                    }

                    if (scheduleItemArray[currentIndex].subPairs == scheduleItemArray[i].subPairs ||
                        scheduleItemArray[currentIndex].subGroups == scheduleItemArray[i].subGroups)
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
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "The function was called.")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "schedule-lesson same = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleLesson, flatSchedule2.scheduleLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "schedule-teacher same = ${checkIfItemArraysAreEqual(flatSchedule1.teacherLesson, flatSchedule2.teacherLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "schedule-cabinet same = ${checkIfItemArraysAreEqual(flatSchedule1.cabinetLesson, flatSchedule2.cabinetLesson)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "day-schedule same = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleDay, flatSchedule2.scheduleDay)}")
        Log.d("ADMIN_FLATSCHEDULE_COMPARISON_CHECKER", "group-schedule same = ${checkIfItemArraysAreEqual(flatSchedule1.scheduleGroup, flatSchedule2.scheduleGroup)}")
        return (checkIfItemArraysAreEqual(flatSchedule1.scheduleLesson, flatSchedule2.scheduleLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.teacherLesson, flatSchedule2.teacherLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.cabinetLesson, flatSchedule2.cabinetLesson) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleDay, flatSchedule2.scheduleDay) &&
                checkIfItemArraysAreEqual(flatSchedule1.scheduleGroup, flatSchedule2.scheduleGroup))
    }
}