package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidTestRepository: ReminderDataSource {

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

        fun addReminders(vararg local: ReminderDTO) {
            locals?.addAll(local)
        }
}