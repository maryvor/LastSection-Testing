package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidTestRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: KoinTest {

//     test the navigation of the fragments.
//     test the displayed data on the UI.
//     add testing for the error messages.

    private val repository by inject<FakeAndroidTestRepository>()
    private lateinit var appContext : Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    val module = module {
        viewModel {
            RemindersListViewModel(
                appContext,
                get() as FakeAndroidTestRepository
            )
        }
        single{ FakeAndroidTestRepository() }
    }

        @Before
        fun init(){
            stopKoin()//stop the original app koin
            appContext = getApplicationContext()
            //declare a new koin module
            startKoin {
                modules(listOf(module))
            }
            //clear the data to start fresh
            runBlocking {
                repository.deleteAllReminders()
            }

        }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

            @Test
            fun addReminder_showInTheRecyclerView() = runBlockingTest{
                repository.saveReminder(ReminderDTO("title",
                    "description",
                    "location",
                    0.0,
                    0.0
                ))
                val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
                dataBindingIdlingResource.monitorFragment(fragment)

                onView(withText("title")).check(matches(isDisplayed()))
            }



            @Test
            fun reminderListEmpty_displayedInUi() = runBlockingTest {
                val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
                dataBindingIdlingResource.monitorFragment(fragment)

                onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
            }

            @Test
            fun clickAddReminderButton_navigateToSaveReminderFragment() = runBlocking {
                val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
                dataBindingIdlingResource.monitorFragment(scenario)
                val navController = mock(NavController::class.java)

                scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

                onView(withId(R.id.addReminderFAB))
                    .perform(click())

                verify(navController).navigate(
                    ReminderListFragmentDirections.toSaveReminder())
            }

            @Test
            fun repositoryReturnsError_displayNoRemindersIsUI() = runBlockingTest{
                repository.setReturnError(true)
                repository.saveReminder(ReminderDTO("title",
                    "description",
                    "location",
                    0.0,
                    0.0
                ))
                val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
                dataBindingIdlingResource.monitorFragment(fragment)

                onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

            }
    }
