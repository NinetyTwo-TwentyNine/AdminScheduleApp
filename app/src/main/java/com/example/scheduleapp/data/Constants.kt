package com.example.scheduleapp.data

import com.example.scheduleapp.utils.Utils.getItemArrayDeepCopy

object Constants {
    const val APP_MIN_PASSWORD_LENGTH = 8
    const val APP_PREFERENCES = "APP_PREFERENCES"
    const val APP_PREFERENCES_STAY = "APP_PREFERENCES_STAY_BOOL"
    const val APP_PREFERENCES_AUTOUPDATE = "APP_PREFERENCES_AUTOUPDATE_BOOL"
    const val APP_BD_PATHS_BASE = "FlatSchedule"
    const val APP_BD_PATHS_BASE_PARAMETERS = "${APP_BD_PATHS_BASE}/BaseParameters"
    const val APP_BD_PATHS_SCHEDULE_BASE = "${APP_BD_PATHS_BASE}/BaseSchedules"
    const val APP_BD_PATHS_SCHEDULE_CURRENT = "${APP_BD_PATHS_BASE}/CurrentSchedules"
    const val APP_BD_PATHS_SCHEDULE_BASE_NAME_LIST = "${APP_BD_PATHS_SCHEDULE_BASE}/nameList"
    const val APP_BD_PATHS_GROUP_LIST = "${APP_BD_PATHS_BASE_PARAMETERS}/groupList"
    const val APP_BD_PATHS_TEACHER_LIST = "${APP_BD_PATHS_BASE_PARAMETERS}/teacherList"
    const val APP_BD_PATHS_CABINET_LIST = "${APP_BD_PATHS_BASE_PARAMETERS}/cabinetList"
    const val APP_BD_PATHS_DISCIPLINE_LIST = "${APP_BD_PATHS_BASE_PARAMETERS}/lessonList"
    const val APP_BD_PATHS_DATE_LIST = "${APP_BD_PATHS_BASE_PARAMETERS}/dayList"
    const val APP_ADMIN_TABLE_EDIT_OPTIONS_OFF = "Edit mode off"
    const val APP_ADMIN_TABLE_EDIT_OPTIONS_ON = "Edit mode on"
    const val APP_ADMIN_TABLE_EDIT_OPTIONS_DELETE = "Delete"
    const val APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_CLEAR = "Clear"
    const val APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_TWEAK = "Edit"
    const val APP_ADMIN_PARAMETERS_DISCIPLINE_NAME = "Discipline"
    const val APP_ADMIN_PARAMETERS_TEACHER_NAME = "Teacher"
    const val APP_ADMIN_PARAMETERS_GROUP_NAME = "Group"
    const val APP_ADMIN_PARAMETERS_CABINET_NAME = "Cabinet"
    const val APP_WARNING_WEAK_CONNECTION = "Looks like there are some problems with connection..."
    const val APP_ADMIN_WARNING_MISSING_DAY = "Warning! No ID was found for this date!\nDo you want to update the day list?"
    const val APP_ADMIN_WARNING_SAVE_CHANGES = "Are sure you want to save these changes?"
    const val APP_ADMIN_WARNING_RESET_CHANGES = "Are sure you want to reset these changes?"
    const val APP_ADMIN_WARNING_ID_DELETION = "Some of the IDs were deleted."
    const val APP_ADMIN_WARNING_SHOULD_UPLOAD = "The schedule should be uploaded first before it can be edited."
    const val APP_ADMIN_BASE_SCHEDULE_EDIT_MODE = 0
    const val APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE = 1

    val APP_CALENDER_DAY_OF_WEEK = listOf("вс", "пн", "вт", "ср", "чт", "пт", "сб")
    val APP_ADMIN_PARAMETERS_LIST = listOf(APP_ADMIN_PARAMETERS_DISCIPLINE_NAME, APP_ADMIN_PARAMETERS_TEACHER_NAME, APP_ADMIN_PARAMETERS_GROUP_NAME, APP_ADMIN_PARAMETERS_CABINET_NAME)
    val APP_ADMIN_EDIT_PAIR_ARRAY = listOf(
        AddPairItem(id = 0, visibility = true),
        AddPairItem(type = 1, id = 1),
        AddPairItem(id = 2),
        AddPairItem(type = 1, id = 3)
    )
    get() {
        return getItemArrayDeepCopy(ArrayList(field))
    }
}
