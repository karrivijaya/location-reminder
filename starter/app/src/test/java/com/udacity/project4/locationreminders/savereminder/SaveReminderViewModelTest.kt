package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.KoinAppDeclaration
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
class SaveReminderViewModelTest: KoinComponent {

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var remindersList: MutableList<ReminderDTO>

    //provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Execute pending coroutines actions.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupSaveReminderViewModel(){
        val reminderDataItem1 = ReminderDTO("Title1","Description1","Location1",20.0,20.0)
        val reminderDataItem2 = ReminderDTO("Title2","Description2","Location2",30.0,30.0)
        val reminderDataItem3 = ReminderDTO("Title3","Description3","Location3",40.0,40.0)
        remindersList = mutableListOf<ReminderDTO>()
        remindersList.add(reminderDataItem1)
        remindersList.add(reminderDataItem2)
        remindersList.add(reminderDataItem3)
        reminderDataSource = FakeDataSource(remindersList)
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),reminderDataSource)
    }

    @Test
    fun onClear_allValues_null(){
        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun validateEnteredData_titleEmpty_showSnackBar(){
        // passing nulls for all values
        val reminderDataItem = ReminderDataItem(null,"desc","Esparina",2.0,15.0)

        //validating entered data
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // snackbar should show up to ask user to enter location
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(equalTo(R.string.err_enter_title)))

    }

    @Test
    fun validateEnteredData_locationEmpty_showSnackBar(){
        // passing nulls for all values
        val reminderDataItem = ReminderDataItem("title","desc",null,2.0,15.0)

        //validating entered data
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // snackbar should show up to ask user to enter location
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(equalTo(R.string.err_select_location)))

    }

    @Test
    fun saveReminder_showLoading(){
        val reminderDataItem = ReminderDataItem("title","desc","Airport",2.0,15.0)

        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Load reminders in the view model.
        saveReminderViewModel.saveReminder(reminderDataItem)

        //then progress bar indicator is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun saveReminder_showToast_afterSaving(){
        val reminderDataItem = ReminderDataItem("title","desc","Marina Country club",3.0,15.0)
        // Load reminders in the view model.
        saveReminderViewModel.saveReminder(reminderDataItem)
        //then progress bar indicator is shown
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(equalTo("Reminder Saved !")))
    }

    @Test
    fun saveReminder_navigationCommandBack_afterSaving()= mainCoroutineRule.runBlockingTest{
        val reminderDataItem = ReminderDataItem("title","desc","Marina Country club",3.0,15.0)
        // Load reminders in the view model.
        saveReminderViewModel.saveReminder(reminderDataItem)
        //then progress bar indicator is shown
        assertEquals(saveReminderViewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @After
    fun tearDown(){
        stopKoin()
    }

}