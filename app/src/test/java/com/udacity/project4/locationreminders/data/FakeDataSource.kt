package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking
import java.lang.Error
import java.util.LinkedHashMap
//это замена репозитория
//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private val locals: MutableList<ReminderDTO>? = mutableListOf()

    private var shouldReturnError = false

    fun setReturnError(flag: Boolean){
        shouldReturnError = flag
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (!shouldReturnError) locals?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Tasks not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        locals?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!shouldReturnError) locals?.let { return Result.Success(locals.first { it.id == id })}
        return Result.Error("Task not found")
    }

    override suspend fun deleteAllReminders() {
        locals?.clear()
    }

    fun addTasks(vararg local: ReminderDTO) {
            locals?.addAll(local)
    }


}