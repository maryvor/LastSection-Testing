package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() = database.close()

    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        // GIVEN - A new task saved in the database.
        val reminder= ReminderDTO("first","first","first",0.1,0.0, "first")
        localDataSource.saveReminder(reminder)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)
        // THEN - Same task is returned.
        result as Result.Success
        assertThat(result.data.latitude, `is`(0.1))
        assertThat(result.data.title, `is`("first"))
        assertThat(result.data.description, `is`("first"))
        assertThat(result.data.longitude, `is`(0.0))
        assertThat(result.data.id, `is` ("first"))
        assertThat(result.data.location, `is`("first"))

    }

    @Test
    fun addAndDeleteReminders_retrieveReminders() = runBlocking {
        val reminder1 = ReminderDTO("first","first","first",0.1,0.0)
        val reminder2 = ReminderDTO("second","second","second",0.2,0.0)
        val reminder3 = ReminderDTO("third","third","third",0.3,0.0)
        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder2)
        localDataSource.saveReminder(reminder3)

        val result = localDataSource.getReminders() as Result.Success

        assertThat(result.data.size, `is`(3))

        localDataSource.deleteAllReminders()

        val resultDelete = localDataSource.getReminders() as Result.Success

        assertThat(resultDelete.data, `is`(emptyList()))
    }

    @Test
    fun retrieveNonexistentReminder_returnError() = runBlocking {
        val result = localDataSource.getReminder("id") as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))

    }


}