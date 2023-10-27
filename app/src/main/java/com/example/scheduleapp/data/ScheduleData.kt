package com.example.scheduleapp.data

data class Date (
    var year : Int?   = null,
    var month : Int?   = null,
    var day : Int?   = null
)

data class Schedule (
    var lessonNum       : Int?    = null,
    var discipline : String? = null,
    var cabinet    : String?    = null,
    var teacher    : String? = null
)

data class ScheduleDetailed (
    var lessonNum  : Int?    = null,

    var discipline1 : String? = null,
    var cabinet1    : String?    = null,
    var teacher1    : String? = null,

    var discipline2 : String? = null,
    var cabinet2    : String?    = null,
    var teacher2    : String? = null,
)