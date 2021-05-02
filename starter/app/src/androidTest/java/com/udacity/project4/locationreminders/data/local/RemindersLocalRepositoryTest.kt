package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest{
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        repository = RemindersLocalRepository(database.reminderDao(),
                                    Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_getReminder() = runBlocking {

        val newReminder = ReminderDTO("title","description",
                                        "location", 20.0,100.0)

        repository.saveReminder(newReminder)

        val result = repository.getReminder(newReminder.id)

        assertThat(result.succeeded, CoreMatchers.`is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.location, `is`("location"))
        assertThat(result.data.latitude, `is`(20.0))
        assertThat(result.data.longitude, `is`(100.0))
    }

    @Test
    fun deleteAllReminders_getReminders_ResultZero() = runBlocking {

        val reminder1 = ReminderDTO("title1","description1",
                "location1", 20.0,100.0)
        val reminder2 = ReminderDTO("title2","description2",
                "location2", 30.0,130.0)

        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        repository.deleteAllReminders()

        val result = (repository.getReminders() as Result.Success).data

        assertThat(result, `is`(emptyList()))

    }
}