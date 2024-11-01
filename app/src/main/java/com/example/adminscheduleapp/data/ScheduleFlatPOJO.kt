package com.example.adminscheduleapp.data


data class FlatSchedule(
    var BaseParameters : FlatScheduleParameters = FlatScheduleParameters(),
    var BaseSchedules : FlatScheduleBase = FlatScheduleBase(),
    var CurrentSchedules : FlatScheduleDetailed = FlatScheduleDetailed()
)

data class FlatScheduleDetailed (
    var scheduleDay   : ArrayList<Data_IntArray>   = arrayListOf(),
    var scheduleGroup : ArrayList<Data_IntArray> = arrayListOf(),
    var scheduleLesson : ArrayList<Data_IntIntIntArrayArray> = arrayListOf(),
    var cabinetLesson  : ArrayList<Data_IntIntIntArrayArray>  = arrayListOf(),
    var teacherLesson  : ArrayList<Data_IntIntIntArrayArray>  = arrayListOf()
)

data class FlatScheduleBase (
    var nameList    : ArrayList<Data_IntString>    = arrayListOf(),
    var scheduleName   : ArrayList<Data_IntArray>   = arrayListOf(),
    var scheduleDay   : ArrayList<Data_IntArray>   = arrayListOf(),
    var scheduleGroup : ArrayList<Data_IntArray> = arrayListOf(),
    var scheduleLesson : ArrayList<Data_IntIntIntArrayArray> = arrayListOf(),
    var cabinetLesson  : ArrayList<Data_IntIntIntArrayArray>  = arrayListOf(),
    var teacherLesson  : ArrayList<Data_IntIntIntArrayArray>  = arrayListOf()
)

data class FlatScheduleParameters (
    var cabinetList    : ArrayList<Data_IntString>    = arrayListOf(),
    var groupList      : ArrayList<Data_IntString>      = arrayListOf(),
    var lessonList     : ArrayList<Data_IntString>     = arrayListOf(),
    var teacherList    : ArrayList<Data_IntString>    = arrayListOf(),
    var dayList        : ArrayList<Data_IntDate>        = arrayListOf()
)

data class Data_IntString (
    var id    : Int?    = null,
    var title : String? = null
)

data class Data_IntDate (
    var id   : Int?  = null,
    var date : Date? = Date()
)

data class Data_IntArray (
    var specialId  : Int?           = null,
    var scheduleId : ArrayList<Int> = arrayListOf()
)

data class Data_IntIntIntArrayArray (
    var scheduleId : Int? = null,
    var pairNum  : Int? = null,
    var subPairs  : ArrayList<Int> = arrayListOf(),
    var subGroups  : ArrayList<Int> = arrayListOf(),
    var specialId  : Int? = null
)