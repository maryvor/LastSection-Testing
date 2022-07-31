package com.udacity.project4.locationreminders.savereminder

import MainCoroutineRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    // provide testing to the SaveReminderView and its live data objects

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersFakeDataSource: FakeDataSource
    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        remindersFakeDataSource = FakeDataSource()
        val reminder1 = ReminderDTO("first","first","first",0.1,0.0)
        val reminder2 = ReminderDTO("second","second","second",0.2,0.0)
        val reminder3 = ReminderDTO("third","third","third",0.3,0.0)
        remindersFakeDataSource.addTasks(reminder1, reminder2, reminder3)
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersFakeDataSource)
    }

    @After
    fun stop(){
        stopKoin()
    }

    @Test
    fun saveReminder_ShowToast() {
      //  val reminder4 = ReminderDTO("fourth","fourth","fourth",0.4,0.0)
        saveReminderViewModel.saveReminder(ReminderDataItem("fourth","fourth","fourth",0.4,0.0))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun validateEnteredData_showSnackBar() {
        val itemWithoutLocation = ReminderDataItem("fourth","fourth","",0.4,0.0)
        saveReminderViewModel.validateEnteredData(itemWithoutLocation)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))

        val itemWithoutTitle = ReminderDataItem(
            "","fourth","fourth",0.4,0.0)
        saveReminderViewModel.validateEnteredData(itemWithoutTitle)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))

    }

    @Test
    fun onClear_nullValuesFields(){
        saveReminderViewModel.onClear()
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

   @Test
   fun validateAndSaveReminder_fails(){
       val itemWithoutLocation = ReminderDataItem("fourth","fourth","",0.4,0.0)
       saveReminderViewModel.validateAndSaveReminder(itemWithoutLocation)
       assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))

   }

    @Test
    fun validateAndSaveReminder_completes(){
        saveReminderViewModel.validateAndSaveReminder(ReminderDataItem("fourth","fourth","fourth",0.4,0.0))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun savingReminders_check_loading(){
        // Pause dispatcher so we can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Save reminder in the view model.
        saveReminderViewModel.saveReminder(ReminderDataItem("fourth","fourth","fourth",0.4,0.0))

        // Then assert that the progress indicator is shown.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is hidden.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    @Test
    fun savingReminders_shouldReturnError() {
        remindersFakeDataSource.setReturnError(true)
        // Pause dispatcher so we can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Save reminder in the view model.
        saveReminderViewModel.saveReminder(ReminderDataItem("fourth", "fourth", "fourth", 0.4, 0.0))

        // Then assert that the progress indicator is shown.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is hidden.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}