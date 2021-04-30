package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//   Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    fun setReturnError(value: Boolean){
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result.Error("Test Exception")
        }
        reminders?.let{
            return Result.Success(ArrayList(it))
        }
        return Result.Error("Reminders not found",100)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("Test Exception")
        }
        reminders?.forEach {reminderDTO ->
            if(reminderDTO.id == id)   {
                return Result.Success(reminderDTO)
            }
        }
        return Result.Error("Reminder not found", 102)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}