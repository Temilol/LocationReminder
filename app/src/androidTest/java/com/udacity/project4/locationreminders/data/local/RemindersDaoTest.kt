package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var remindersDao: RemindersDao
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
            ).allowMainThreadQueries()
            .build()

        remindersDao = database.reminderDao()
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun retrieve_reminders_success() = runBlockingTest {
        remindersDao.saveReminder(reminder)
        val result = remindersDao.getReminders()
        assertThat(result.count(), `is`(1))

        assertThat(result.first().title, `is`(reminder.title))
        assertThat(result.first().description, `is`(reminder.description))
        assertThat(result.first().location, `is`(reminder.location))
    }

    @Test
    fun delete_reminders_success() = runBlockingTest {
        remindersDao.saveReminder(reminder)
        remindersDao.deleteAllReminders()
        val result = remindersDao.getReminders()
        assertThat(result.count(), `is`(0))
    }

    @Test
    fun save_reminder_and_retrieve_by_id() = runBlockingTest {
        remindersDao.saveReminder(reminder)

        val result = remindersDao.getReminderById(reminder.id)

        assertThat(result as ReminderDTO, notNullValue())
        assertThat(result.id, `is`(reminder.id))
        assertThat(result.title, `is`(reminder.title))
        assertThat(result.description, `is`(reminder.description))
        assertThat(result.location, `is`(reminder.location))
        assertThat(result.latitude, `is`(reminder.latitude))
        assertThat(result.longitude, `is`(reminder.longitude))
    }

    companion object {
        val reminder = ReminderDTO("Title1", "Description1", "Location1", 10.01,1.1111)
    }
}