package com.udacity.project4

import android.app.Application
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.PreferenceMatchers.withTitle
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun saveReminder_noTitle_displaySnackbar() {
        runBlocking {
            // Start up Tasks screen.
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            //click on add reminder FAB
            onView(withId(R.id.addReminderFAB)).perform(click())

            // replace text and description to the given text and description
            onView(withId(R.id.reminderDescription)).perform(replaceText("Description 1"))

            onView(withId(R.id.selectLocation)).perform(setTextInTextView("Location 1"))

            onView(withId(R.id.saveReminder)).perform(click())

            onView(withText("Please enter title")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        }
    }

    @Test
    fun saveReminder_noLocation_displaySnackbar() {
        runBlocking {
            // Start up Tasks screen.
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            //click on add reminder FAB
            onView(withId(R.id.addReminderFAB)).perform(click())

            onView(withId(R.id.reminderTitle)).perform(replaceText("Title 1"))
            // replace text and description to the given text and description
            onView(withId(R.id.reminderDescription)).perform(replaceText("Description 1"))

            onView(withId(R.id.saveReminder)).perform(click())

            onView(withText("Please select location")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        }
    }

    @Test
    fun addReminder_saveReminder() {
        runBlocking {

            // Start up Tasks screen.
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            //click on add reminder FAB
            onView(withId(R.id.addReminderFAB)).perform(click())

            // replace text and description to the given text and description
            onView(withId(R.id.reminderTitle)).perform(replaceText("Title 1"))
            onView(withId(R.id.reminderDescription)).perform(replaceText("Description 1"))

            onView(withId(R.id.selectLocation)).perform(setTextInTextView("Location 1"))

            onView(withId(R.id.saveReminder)).perform(click())

            onView(withText("Title 1")).check(matches(isDisplayed()))
            onView(withText("Description 1")).check(matches(isDisplayed()))
            onView(withText("Location 1")).check(matches(isDisplayed()))
        }
    }
}


 // from stack overflow
fun setTextInTextView(value: String): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return CoreMatchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(TextView::class.java))
        }

        override fun perform(uiController: UiController, view: View) {
            (view as TextView).text = value
        }

        override fun getDescription(): String {
            return "replace text"
        }
    }
}





