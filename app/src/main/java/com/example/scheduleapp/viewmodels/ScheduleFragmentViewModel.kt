package com.example.scheduleapp.viewmodels


import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.data.*
import com.example.scheduleapp.data.Date
import com.example.scheduleapp.models.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ScheduleFragmentViewModel @Inject constructor(
    private val rImplementation: FirebaseRepository, private val sPreferences: SharedPreferences
) : ViewModel() {

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
        groupId: Int, dayId: Int, schedule: FlatScheduleDetailed
    ): ArrayList<Schedule>? {
        val resArray = arrayListOf<Schedule>()

        val result: ArrayList<ScheduleDetailed> = arrayListOf()
        for (i in 1..14) {
            result.add(ScheduleDetailed(lessonNum = i, "-", "-", "-", "-", "-", "-"))
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
                            getById(item.specialId!!, schedule.cabinetList)!!.title
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].cabinet2 =
                            getById(item.specialId!!, schedule.cabinetList)!!.title
                    }
                }
            }
        }
        for (item in schedule.scheduleLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline1 =
                            getById(item.specialId!!, schedule.lessonList)!!.title
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].discipline2 =
                            getById(item.specialId!!, schedule.lessonList)!!.title
                    }
                }
            }
        }
        for (item in schedule.teacherLesson) {
            if (item.scheduleId == scheduleId) {
                if (item.subGroups.contains(1)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher1 =
                            getById(item.specialId!!, schedule.teacherList)!!.title
                    }
                }
                if (item.subGroups.contains(2)) {
                    for (subPair in item.subPairs) {
                        result[(item.pairNum!! - 1) * 2 + (subPair - 1)].teacher2 =
                            getById(item.specialId!!, schedule.teacherList)!!.title
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

        if (scheduleDetailed.discipline1 == scheduleDetailed.discipline2) {
            scheduleObject.discipline = scheduleDetailed.discipline1
        } else {
            scheduleObject.discipline =
                scheduleDetailed.discipline1 + System.getProperty("line.separator") + scheduleDetailed.discipline2
        }

        if (scheduleDetailed.cabinet1 == scheduleDetailed.cabinet2) {
            scheduleObject.cabinet = scheduleDetailed.cabinet1
        } else {
            scheduleObject.cabinet =
                scheduleDetailed.cabinet1 + System.getProperty("line.separator") + scheduleDetailed.cabinet2
        }

        if (scheduleDetailed.teacher1 == scheduleDetailed.teacher2) {
            scheduleObject.teacher = scheduleDetailed.teacher1
        } else {
            scheduleObject.teacher =
                scheduleDetailed.teacher1 + System.getProperty("line.separator") + scheduleDetailed.teacher2
        }

        return scheduleObject
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

    fun getPossibleId(currentList: ArrayList<Data_IntString>): Int {
        val idArray: MutableList<Int> = arrayListOf()
        var maxId = 0
        var currentId: Int? = null
        currentList.forEach {
            idArray.add(it.id!!)
            if (it.id!! > maxId) {
                maxId = it.id!!
            }
        }
        for (i in 0..maxId) {
            if (!idArray.contains(i)) {
                currentId = i
                break
            }
        }
        if (currentId == null) {
            currentId = maxId + 1
        }
        return currentId
    }
}
