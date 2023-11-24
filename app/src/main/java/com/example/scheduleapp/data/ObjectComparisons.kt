package com.example.scheduleapp.data

import android.util.Log

fun Any.actuallyEquals(comparisonItem: ScheduleDetailed): Boolean {
    if (this::class.java != comparisonItem::class.java) { return false }
    return (//(this as ScheduleDetailed).lessonNum == comparisonItem.lessonNum &&
            (this as ScheduleDetailed).discipline1 == comparisonItem.discipline1 &&
                    teacher1 == comparisonItem.teacher1 &&
                    cabinet1 == comparisonItem.cabinet1 &&
                    discipline2 == comparisonItem.discipline2 &&
                    teacher2 == comparisonItem.teacher2 &&
                    cabinet2 == comparisonItem.cabinet2 &&
                    discipline3 == comparisonItem.discipline3 &&
                    teacher3 == comparisonItem.teacher3 &&
                    cabinet3 == comparisonItem.cabinet3)
}

fun Any.actuallyEquals(comparisonItem: AddPairItem): Boolean {
    if (this::class.java != comparisonItem::class.java) { return false }
    return (//(this as AddPairItem).visibility == comparisonItem.visibility &&
            (this as AddPairItem).pairName == comparisonItem.pairName &&
                    teacher == comparisonItem.teacher &&
                    teacherSecond == comparisonItem.teacherSecond &&
                    teacherThird == comparisonItem.teacherThird &&
                    cabinet == comparisonItem.cabinet &&
                    cabinetSecond == comparisonItem.cabinetSecond &&
                    cabinetThird == comparisonItem.cabinetThird &&
                    subGroup == comparisonItem.subGroup)
}

fun Any.actuallyEquals(comparisonItem: Data_IntIntIntArrayArray): Boolean {
    if (this::class.java != comparisonItem::class.java) { return false }
    return ((this as Data_IntIntIntArrayArray).subPairs == comparisonItem.subPairs &&
            subGroups == comparisonItem.subGroups &&
            pairNum == comparisonItem.pairNum &&
            scheduleId == comparisonItem.scheduleId &&
            specialId == comparisonItem.specialId)
}

fun Any.actuallyEquals(comparisonItem: Data_IntArray): Boolean {
    if (this::class.java != comparisonItem::class.java) { return false }
    return ((this as Data_IntArray).scheduleId == comparisonItem.scheduleId &&
            specialId == comparisonItem.specialId)
}

fun Any.actuallyEquals(comparisonItem: Any?): Boolean {
    Log.d("ADMIN_OBJECT_COMPARISON_DEBUGGER", "")
    Log.d("ADMIN_OBJECT_COMPARISON_DEBUGGER", "Comparison function was called.")
    if (comparisonItem == null) {return false}
    if (comparisonItem::class.java == ScheduleDetailed::class.java) { return actuallyEquals(comparisonItem as ScheduleDetailed) }
    if (comparisonItem::class.java == AddPairItem::class.java) { return actuallyEquals(comparisonItem as AddPairItem) }
    if (comparisonItem::class.java == Data_IntIntIntArrayArray::class.java) { return actuallyEquals(comparisonItem as Data_IntIntIntArrayArray) }
    if (comparisonItem::class.java == Data_IntArray::class.java) { return actuallyEquals(comparisonItem as Data_IntArray) }
    Log.d("ADMIN_OBJECT_COMPARISON_DEBUGGER", "Warning! Actually-equals function returned result of the default equals function.")
    return (equals(comparisonItem))
}