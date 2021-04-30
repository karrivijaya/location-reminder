package com.udacity.project4.locationreminders.data.local

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidTestRepository : IRemindersLocalRepository{

    var reminders: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    private val observableReminders = MutableLiveData<Result<List<ReminderDTO>>>()

    fun setReturnError(value:Boolean){
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(reminders.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("Test exception")
        }
        reminders[id]?.let{
            return Result.Success(it)
        }
        return Result.Error("could not find reminder")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()

    }
}