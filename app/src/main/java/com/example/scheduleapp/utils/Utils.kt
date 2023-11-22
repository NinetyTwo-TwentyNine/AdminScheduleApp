package com.example.scheduleapp.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.example.scheduleapp.adapters.AddPairItem
import com.example.scheduleapp.data.*
import com.example.scheduleapp.data.Constants.APP_ADMIN_EDIT_PAIR_ARRAY

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

    fun getAddPairItemArrayDeepCopy(origArr: ArrayList<AddPairItem>): ArrayList<AddPairItem> {
        val newArr = arrayListOf<AddPairItem>()
        origArr.forEach { newArr.add(AddPairItem(it.pairName, it.teacher, it.teacherSecond, it.teacherThird, it.cabinet, it.cabinetSecond, it.cabinetThird, it.subGroup, it.type, it.id, it.visibility)) }
        return newArr
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

    fun removeScheduleItemById(flatSchedule: FlatScheduleDetailed, scheduleId: Int, pairNum: Int, canRemoveScheduleId: Boolean) {
        val arrayToRemove: ArrayList<Data_IntIntIntArrayArray> = arrayListOf()
        flatSchedule.scheduleLesson.forEach {
            if (it.scheduleId == scheduleId && it.pairNum == pairNum) {
                arrayToRemove.add(it)
            }
        }
        arrayToRemove.forEach {
            flatSchedule.scheduleLesson.remove(it)
        }

        arrayToRemove.clear()
        flatSchedule.cabinetLesson.forEach {
            if (it.scheduleId == scheduleId && it.pairNum == pairNum) {
                arrayToRemove.add(it)
            }
        }
        arrayToRemove.forEach {
            flatSchedule.cabinetLesson.remove(it)
        }

        arrayToRemove.clear()
        flatSchedule.teacherLesson.forEach {
            if (it.scheduleId == scheduleId && it.pairNum == pairNum) {
                arrayToRemove.add(it)
            }
        }
        arrayToRemove.forEach {
            flatSchedule.teacherLesson.remove(it)
        }


        if (!canRemoveScheduleId) { return }
        var shouldRemoveTheEntireSchedule = true
        for (i: Int in 0 until flatSchedule.scheduleLesson.size) {
            if (flatSchedule.scheduleLesson[i].scheduleId == scheduleId) {
                shouldRemoveTheEntireSchedule = false
                break
            }
        }
        for (i: Int in 0 until flatSchedule.cabinetLesson.size) {
            if (!shouldRemoveTheEntireSchedule) {break}
            if (flatSchedule.cabinetLesson[i].scheduleId == scheduleId) {
                shouldRemoveTheEntireSchedule = false
                break
            }
        }
        for (i: Int in 0 until flatSchedule.teacherLesson.size) {
            if (!shouldRemoveTheEntireSchedule) {break}
            if (flatSchedule.teacherLesson[i].scheduleId == scheduleId) {
                shouldRemoveTheEntireSchedule = false
                break
            }
        }


        if (!shouldRemoveTheEntireSchedule) { return }
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

    private fun checkIfAddPairItemsAreEqual(addPairItem1: AddPairItem, addPairItem2: AddPairItem): Boolean {
        //Log.d("ADMIN_ADDPAIR_COMPARISON_CHECKER", "")
        //Log.d("ADMIN_ADDPAIR_COMPARISON_CHECKER", "First AddPairItem:" + System.lineSeparator() + "(visibility = ${addPairItem1.visibility}, pairName = ${addPairItem1.pairName}, teacher = ${addPairItem1.teacher}, teacherSecond = ${addPairItem1.teacherSecond}, teacherThird = ${addPairItem1.teacherThird}, cabinet = ${addPairItem1.cabinet}, cabinetSecond = ${addPairItem1.cabinetSecond}, cabinetThird = ${addPairItem1.cabinetThird}, subGroup = ${addPairItem1.subGroup})")
        //Log.d("ADMIN_ADDPAIR_COMPARISON_CHECKER", "Second AddPairItem:" + System.lineSeparator() + "(visibility = ${addPairItem2.visibility}, pairName = ${addPairItem2.pairName}, teacher = ${addPairItem2.teacher}, teacherSecond = ${addPairItem2.teacherSecond}, teacherThird = ${addPairItem2.teacherThird}, cabinet = ${addPairItem2.cabinet}, cabinetSecond = ${addPairItem2.cabinetSecond}, cabinetThird = ${addPairItem2.cabinetThird}, subGroup = ${addPairItem2.subGroup})")

        return (//addPairItem1.visibility == addPairItem2.visibility &&
                addPairItem1.pairName == addPairItem2.pairName &&
                addPairItem1.teacher == addPairItem2.teacher &&
                addPairItem1.teacherSecond == addPairItem2.teacherSecond &&
                addPairItem1.teacherThird == addPairItem2.teacherThird &&
                addPairItem1.cabinet == addPairItem2.cabinet &&
                addPairItem1.cabinetSecond == addPairItem2.cabinetSecond &&
                addPairItem1.cabinetThird == addPairItem2.cabinetThird &&
                addPairItem1.subGroup == addPairItem2.subGroup)
    }
    fun checkIfAddPairItemArraysAreEqual(addPairArray1: ArrayList<AddPairItem>, addPairArray2: ArrayList<AddPairItem>): Boolean {
        for (i: Int in 0 until addPairArray1.size) {
            if (!checkIfAddPairItemsAreEqual(addPairArray1[i], addPairArray2[i])) {
                return false
            }
        }
        return true
    }
    private fun checkIfScheduleDetailedEquals(schedule1: ScheduleDetailed, schedule2: ScheduleDetailed): Boolean {
        return (//schedule1.lessonNum == schedule2.lessonNum &&
                schedule1.discipline1 == schedule2.discipline1 &&
                schedule1.teacher1 == schedule2.teacher1 &&
                schedule1.cabinet1 == schedule2.cabinet1 &&
                schedule1.discipline2 == schedule2.discipline2 &&
                schedule1.teacher2 == schedule2.teacher2 &&
                schedule1.cabinet2 == schedule2.cabinet2 &&
                schedule1.discipline3 == schedule2.discipline3 &&
                schedule1.teacher3 == schedule2.teacher3 &&
                schedule1.cabinet3 == schedule2.cabinet3)
    }
}