package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

// Executes each task synchronously using Architecture Components.
@get:Rule
var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb(){
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetReminderById() = runBlockingTest {

        //Given - Insert a reminder
        val reminderDTO = ReminderDTO("title","desc","location", 3.0,100.0)

        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminderDTO.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDTO.id))
        assertThat(loaded.title, `is`(reminderDTO.title))
        assertThat(loaded.description, `is`(reminderDTO.description))
        assertThat(loaded.location, `is`(reminderDTO.location))
        assertThat(loaded.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun saveReminders_and_getReminders() = runBlockingTest {
        //Given - Insert  reminders
        val reminder1 = ReminderDTO("title1","desc1","location1", 3.0,100.0)
        val reminder2 = ReminderDTO("title2","desc2","location2", 4.0,90.0)

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Get the reminder list from the database
        val reminderList = database.reminderDao().getReminders()

        //THEN - the reminder list size should be equal to number of reminders inserted
        assertThat(reminderList.size, `is`(2))
        // data inserted should be equal to retrieved data
        assertThat(reminderList.first().title, `is`(reminder1.title))
        assertThat(reminderList.first().description, `is`(reminder1.description))
        assertThat(reminderList.first().location, `is`(reminder1.location))
        assertThat(reminderList.first().latitude, `is`(reminder1.latitude))
        assertThat(reminderList.first().longitude, `is`(reminder1.longitude))

        assertThat(reminderList.last().title, `is`(reminder2.title))
        assertThat(reminderList.last().description, `is`(reminder2.description))
        assertThat(reminderList.last().location, `is`(reminder2.location))
        assertThat(reminderList.last().latitude, `is`(reminder2.latitude))
        assertThat(reminderList.last().longitude, `is`(reminder2.longitude))
    }

    @Test
    fun deleteAllReminders_getReminders_zero() = runBlockingTest {
        //Given - Insert  reminders
        val reminder1 = ReminderDTO("title1","desc1","location1", 3.0,100.0)
        val reminder2 = ReminderDTO("title2","desc2","location2", 4.0,90.0)

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Get the reminder list from the database
        var reminderList = database.reminderDao().getReminders()

        //THEN - the reminder list size should be equal to number of reminders inserted
        assertThat(reminderList.size, `is`(2))

        // WHEN - delete the reminders from database
        database.reminderDao().deleteAllReminders()
        reminderList = database.reminderDao().getReminders()

        assertThat(reminderList.size, `is`(0))
    }
}