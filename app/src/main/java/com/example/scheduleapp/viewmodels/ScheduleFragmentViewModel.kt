package com.example.scheduleapp.viewmodels


import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.data.*
import com.example.scheduleapp.data.Date
import com.example.scheduleapp.models.FirebaseRepository
import com.example.scheduleapp.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class ScheduleFragmentViewModel @Inject constructor(
    private val rImplementation: FirebaseRepository, private val sPreferences: SharedPreferences
) : ViewModel() {

    /*
    fun getGroup(): String? {
        if (rImplementation.getCurrentUser() == null) {
            return null
        }
        val groupName = sPreferences.getString(
            Constants.APP_PREFERENCES_GROUP + "_" + rImplementation.getCurrentUser()!!.email.toString(),
            null
        )
        return groupName
    }
    */

    fun getDayId(dayList: ArrayList<Data_IntDate>, date: Date): Int? {
        for (item in dayList) {
            if (date == item.date) {
                return item.id
            }
        }
        return null
    }

    fun getGroupId(groupList: ArrayList<Data_IntString>, groupName: String?): Int? {
        if (groupName == null) {
            return null
        }
        for (item in groupList) {
            if (groupName == item.title) {
                return item.id
            }
        }
        return null
    }

    fun getScheduleByGroupAndDayDetailed(
        groupId: Int, dayId: Int, schedule: FlatScheduleDetailed, parameters: FlatScheduleParameters
    ): ArrayList<Schedule>? {
        val resArray = arrayListOf<Schedule>()

        val result: ArrayList<ScheduleDetailed> = arrayListOf()
        for (i in 1..14) {
            result.add(ScheduleDetailed(lessonNum = i, "-", "-", "-", "-", "-", "-", "-", "-", "-"))
        }

        var scheduleId: Int? = null

        val firstScheduleArray = getById(dayId, schedule.scheduleDay)
        val secondScheduleArray = getById(groupId, schedule.scheduleGroup)

        if (firstScheduleArray == null || secondScheduleArray == null) {
            Log.d("TAG", "getScheduleByGroupAndDay: One of the schedule arrays is missing!")
            return resArray
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
            return resArray
        }

        for (item in schedule.cabinetLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].cabinet1 =
                            getById(item.specialId!!, parameters.cabinetList)!!.title!!
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].cabinet2 =
                            getById(item.specialId!!, parameters.cabinetList)!!.title!!
                    }
                }
                if (item.subGroups.contains(3)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].cabinet3 =
                            getById(item.specialId!!, parameters.cabinetList)!!.title!!
                    }
                }
            }
        }
        for (item in schedule.scheduleLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline1 =
                            getById(item.specialId!!, parameters.lessonList)!!.title!!
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline2 =
                            getById(item.specialId!!, parameters.lessonList)!!.title!!
                    }
                }
                if (item.subGroups.contains(3)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline3 =
                            getById(item.specialId!!, parameters.lessonList)!!.title!!
                    }
                }
            }
        }
        for (item in schedule.teacherLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher1 =
                            getById(item.specialId!!, parameters.teacherList)!!.title!!
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher2 =
                            getById(item.specialId!!, parameters.teacherList)!!.title!!
                    }
                }
                if (item.subGroups.contains(3)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher3 =
                            getById(item.specialId!!, parameters.teacherList)!!.title!!
                    }
                }
            }
        }

        for (i in 0 until 14) {
            val scheduleObject = checkForEquality(result[i])
            resArray.add(
                scheduleObject
            )
        }
        return resArray
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


    private fun getById(id: Int, array: ArrayList<Data_IntString>): Data_IntString? {
        for (item in array) {
            if (item.id == id) {
                return item
            }
        }
        return null
    }

    private fun getById(id: Int, array: ArrayList<Data_IntArray>): Data_IntArray? {
        for (item in array) {
            if (item.specialId == id) {
                return item
            }
        }
        return null
    }

    fun getPossibleId(originalList: ArrayList<Data_IntString>): Int {
        val currentList = Utils.getDataIntStringArrayDeepCopy(originalList)
        currentList.sortBy { it.id!! }

        var new_id = 0
        for(e: Data_IntString in currentList){
            if(new_id == e.id!!){
                new_id += 1
            }
        }
        return new_id
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
}
