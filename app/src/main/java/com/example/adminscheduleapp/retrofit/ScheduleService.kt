package com.example.adminscheduleapp.retrofit

import com.example.adminscheduleapp.data.Data_IntString
import com.example.adminscheduleapp.data.FlatSchedule
import com.example.adminscheduleapp.data.FlatScheduleAnswer
import com.example.adminscheduleapp.data.FlatScheduleBase
import com.example.adminscheduleapp.data.FlatScheduleDetailed
import com.example.adminscheduleapp.data.FlatScheduleParameters
import com.example.adminscheduleapp.data.ScheduleDetailed
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ScheduleService {
    @GET("/schedule")
    suspend fun getEntireSchedule(): FlatSchedule

    @GET("schedule/parameters")
    suspend fun getScheduleParameters(): FlatScheduleParameters

    @GET("schedule/parameters/{id}")
    suspend fun getSpecificParameters(@Path("id") id: Int): List<Data_IntString>

    @GET("schedule/version")
    suspend fun getScheduleVersion(): Long

    @GET("schedule/current/{date}")
    suspend fun getScheduleCurrent(@Path("date") dateId: Int): FlatScheduleAnswer<FlatScheduleDetailed>

    @GET("schedule/base/{name}/{day}")
    suspend fun getScheduleBase(@Path("name") nameId: Int, @Path("day") dayNum: Int): FlatScheduleAnswer<FlatScheduleBase>

    @PUT("schedule/parameters/{id}/upload/")
    suspend fun uploadSpecificParameters(@Path("id") id: Int, @Body body: List<Data_IntString>): Boolean

    @PUT("schedule/current/stagepair/{group}/{date}/")
    suspend fun stageCurrentSchedulePair(@Path("group") groupId: Int, @Path("date") dateId: Int, @Body body: Pair<ScheduleDetailed, ScheduleDetailed>): FlatScheduleAnswer<FlatScheduleDetailed>

    @PUT("schedule/current/applybase/{name}/{day}/{date}/")
    suspend fun applyBaseScheduleToCurrent(@Path("name") nameId: Int, @Path("day") dayNum: Int, @Path("date") dateId: Int): FlatScheduleAnswer<FlatScheduleDetailed>

    @PUT("schedule/base/stagepair/{group}/{day}/{name}/")
    suspend fun stageBaseSchedulePair(@Path("group") groupId: Int, @Path("day") dayNum: Int, @Path("name") nameId: Int, @Body body: Pair<ScheduleDetailed, ScheduleDetailed>): FlatScheduleAnswer<FlatScheduleBase>

    @PUT("schedule/base/stageschedule/")
    suspend fun stageBaseScheduleList(@Body body: Data_IntString): FlatScheduleAnswer<FlatScheduleBase>

    @PUT("schedule/current/apply/{date}/{update}/")
    suspend fun applyStagedChangesToScheduleCurrent(@Path("date") dateId: Int, @Path("update") updateVersion: Int = 1): FlatScheduleAnswer<FlatScheduleDetailed>

    @PUT("schedule/base/apply/{day}/{name}/")
    suspend fun applyStagedChangesToScheduleBase(@Path("day") dayNum: Int, @Path("name") nameId: Int): FlatScheduleAnswer<FlatScheduleBase>

    @PUT("schedule/current/reset/{date}/")
    suspend fun resetStagedChangesToScheduleCurrent(@Path("date") dateId: Int): FlatScheduleAnswer<FlatScheduleDetailed>

    @PUT("schedule/base/reset/{day}/{name}/")
    suspend fun resetStagedChangesToScheduleBase(@Path("day") dayNum: Int, @Path("name") nameId: Int): FlatScheduleAnswer<FlatScheduleBase>
}