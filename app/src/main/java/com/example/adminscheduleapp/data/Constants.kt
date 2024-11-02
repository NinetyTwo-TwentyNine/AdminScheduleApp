package com.example.adminscheduleapp.data

import com.example.adminscheduleapp.utils.Utils.getItemArrayDeepCopy

object Constants {
    const val APP_MIN_PASSWORD_LENGTH = 8

    const val APP_PREFERENCES = "APP_PREFERENCES"
    const val APP_PREFERENCES_STAY = "APP_PREFERENCES_STAY_BOOL"
    const val APP_PREFERENCES_AUTOUPDATE = "APP_PREFERENCES_AUTOUPDATE_BOOL"
    const val APP_PREFERENCES_PUSHES = "APP_PREFERENCES_PUSHES_BOOL"

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

    const val APP_ADMIN_TABLE_EDIT_OPTIONS_OFF = "Выключить изменение названия"
    const val APP_ADMIN_TABLE_EDIT_OPTIONS_ON = "Включить изменение названия"
    const val APP_ADMIN_TABLE_EDIT_OPTIONS_DELETE = "Удалить"

    const val APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_CLEAR = "Очистить"
    const val APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_TWEAK = "Изменить"

    const val APP_ADMIN_PARAMETERS_DISCIPLINE_NAME = "Дисциплина"
    const val APP_ADMIN_PARAMETERS_TEACHER_NAME = "Преподаватель"
    const val APP_ADMIN_PARAMETERS_GROUP_NAME = "Группа"
    const val APP_ADMIN_PARAMETERS_CABINET_NAME = "Кабинет"

    const val APP_ADMIN_CHOOSE_BASE_SCHEDULE_TEXT = "Выбрать базовое расписание"

    const val APP_TOAST_WEAK_CONNECTION = "Looks like there are some problems with connection..."
    const val APP_TOAST_NOT_SIGNED_IN = "You aren't signed in yet."
    const val APP_TOAST_LOGIN_FAILED = "Failed to log in"
    const val APP_TOAST_LOGIN_SUCCESS = "Logged in successfully"
    const val APP_TOAST_SIGNUP_FAILED = "Failed to sign up"
    const val APP_TOAST_SIGNUP_SUCCESS = "Registered successfully"
    const val APP_TOAST_RESET_SEND_FAILED = "Failed to send the reset message"
    const val APP_TOAST_RESET_SEND_SUCCESS = "Reset message sent successfully"
    const val APP_TOAST_PASSWORD_TOO_SHORT = "Your password should be at least $APP_MIN_PASSWORD_LENGTH characters long."
    const val APP_TOAST_PASSWORDS_DONT_MATCH = "Your passwords don't match. Please confirm your password."
    const val APP_TOAST_DATA_DOWNLOAD_FAILED = "Failed to download the data"
    const val APP_TOAST_SCHEDULE_DOWNLOAD_FAILED = "Failed to download the schedules"

    const val APP_ADMIN_TOAST_TITLE_CAN_NOT_BE_SAVED = "Can not save such a title."
    const val APP_ADMIN_TOAST_ITEM_CAN_NOT_BE_DELETED = "Can not delete this item."
    const val APP_ADMIN_TOAST_SHOULD_UPLOAD_SCHEDULE = "The schedule should be uploaded first, before it can be edited."
    const val APP_ADMIN_TOAST_DATA_UPLOAD_FAILED = "Failed to upload the data"
    const val APP_ADMIN_TOAST_DATA_UPLOAD_SUCCESS = "Succeeded in uploading the data"
    const val APP_ADMIN_TOAST_DAY_LIST_UPLOAD_FAILED = "Failed to upload the day list"
    const val APP_ADMIN_TOAST_DAY_LIST_UPLOAD_SUCCESS = "Succeeded in updating the day list"

    const val APP_ADMIN_WARNING_MISSING_DAY = "Внимание! Для данной даты не было найдено ID!\nВозможно, стоит обновить список дней."
    const val APP_ADMIN_WARNING_SAVE_CHANGES = "Вы уверены, что хотите сохранить эти изменения?"
    const val APP_ADMIN_WARNING_RESET_CHANGES = "Вы уверены, что хотите отменить эти изменения?"
    const val APP_ADMIN_WARNING_ID_DELETION = "Некоторые ID были удалены."
    const val APP_ADMIN_WARNING_APPLY_BASE_SCHEDULE = "Вы уверены, что хотите применить это расписание?\nЛюбые совпадающие элементы расписания будут перезаписаны."

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
