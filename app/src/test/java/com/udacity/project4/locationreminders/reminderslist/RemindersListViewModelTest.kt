package com.udacity.project4.locationreminders.reminderslist

import MainCoroutineRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersFakeDataSource: FakeDataSource
    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

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
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersFakeDataSource)
    }
    @After
    fun clear(){
        stopKoin()
    }

    @Test
    fun loadReminders_ReminderListIsUpdated() {
        // When loading reminders
        remindersListViewModel.loadReminders()

        // Then list is updated
        val value = remindersListViewModel.remindersList.getOrAwaitValue ()

        assertThat(value, not(CoreMatchers.nullValue()))
    }

    @Test
    fun loadReminders_error() {
        remindersFakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Tasks not found"))
    }

    @Test
    fun loadReminders_showNoData() {
        remindersFakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadingReminders_check_loading(){
        // Pause dispatcher so we can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Save reminder in the view model.
        remindersListViewModel.loadReminders()

        // Then assert that the progress indicator is shown.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is hidden.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadingReminders_shouldReturnError(){
        remindersFakeDataSource.setReturnError(true)
        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showError.getOrAwaitValue(), `is`(false))

        mainCoroutineRule.resumeDispatcher()

        assertThat(remindersListViewModel.showError.getOrAwaitValue(), `is`(true))
    }
}