package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        stopKoin()
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 10.01,1.1111)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 20.02,2.2222)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 30.03,3.3333)
        val reminders = mutableListOf(reminder1, reminder2, reminder3)
        fakeDataSource = FakeDataSource(reminders)

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun cleanUp() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun saveReminder() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun validateAndSaveReminder_success() {
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun validateAndSaveReminder_error_null_title() {
        val mockReminder = ReminderDataItem(null, "Description1", "Location1", 10.01,1.1111)
        saveReminderViewModel.validateAndSaveReminder(mockReminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateAndSaveReminder_error_null_location() {
        val mockReminder = ReminderDataItem("Title1", "Description1", null, 10.01,1.1111)
        saveReminderViewModel.validateAndSaveReminder(mockReminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun validateAndSaveReminder_error_null_latitude_or_longitude() {
        var mockReminder = ReminderDataItem("Title1", "Description1", "Location1", null,1.1111)
        saveReminderViewModel.validateAndSaveReminder(mockReminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_selected_location))


        mockReminder = ReminderDataItem("Title1", "Description1", "Location1", 10.01,null)
        saveReminderViewModel.validateAndSaveReminder(mockReminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_selected_location))
    }

    companion object {
        val reminder = ReminderDataItem("Title1", "Description1", "Location1", 10.01,1.1111)
    }
}