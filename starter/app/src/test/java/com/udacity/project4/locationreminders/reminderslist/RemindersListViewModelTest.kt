package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
class RemindersListViewModelTest{

    //provide testing to the RemindersListViewModel and its live data objects
    
    private lateinit var remindersListViewModel: RemindersListViewModel
    
    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var remindersList: MutableList<ReminderDTO>


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Execute pending coroutines actions.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupReminderListViewModel(){
        val reminderDataItem1 = ReminderDTO("Title1","Description1","Location1",20.0,20.0)
        val reminderDataItem2 = ReminderDTO("Title2","Description2","Location2",30.0,30.0)
        val reminderDataItem3 = ReminderDTO("Title3","Description3","Location3",40.0,40.0)
        remindersList = mutableListOf<ReminderDTO>()
        remindersList.add(reminderDataItem1)
        remindersList.add(reminderDataItem2)
        remindersList.add(reminderDataItem3)
        reminderDataSource = FakeDataSource(remindersList)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),reminderDataSource)

    }

    @Test
    fun loadReminders_showLoading(){
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Load reminders in the view model.
        remindersListViewModel.loadReminders()

        //then progress bar indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

        //show no data value should be false because reminderList is not empty at this point
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))

    }


    @Test
    fun loadReminders_equalsListInReminderDataSource(){

        val reminderDataItemList = mutableListOf<ReminderDataItem>()
        reminderDataItemList.addAll((remindersList as List<ReminderDTO>).map {reminder ->
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id
            )
        })
        // Load reminders in the view model.
        remindersListViewModel.loadReminders()

        //then reminders list in view model should be the list we pass from reminder data source
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), `is`(reminderDataItemList))

        //show no data value should be false because reminderList is not empty
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun loadReminders_showError(){
        // setting return error value to true explicitly for testing show Error
        reminderDataSource.setReturnError(true)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),reminderDataSource)

        remindersListViewModel.loadReminders()

        // since the reminder list is null, snack bar should display the error value as shown
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(equalTo("Test Exception")))
    }

    @After
    fun tearDown(){
        stopKoin()
    }

}
