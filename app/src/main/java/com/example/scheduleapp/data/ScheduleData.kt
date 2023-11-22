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

    var discipline1 : String? = "-",
    var cabinet1    : String?    = "-",
    var teacher1    : String? = "-",

    var discipline2 : String? = "-",
    var cabinet2    : String?    = "-",
    var teacher2    : String? = "-",

    var discipline3 : String? = "-",
    var cabinet3    : String?    = "-",
    var teacher3    : String? = "-"
)