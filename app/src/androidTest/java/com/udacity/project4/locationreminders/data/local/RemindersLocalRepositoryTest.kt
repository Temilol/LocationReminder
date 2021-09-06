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
    private lateinit var remindersLocalRepository: RemindersLocalRepository
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

        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminderAndGetReminders() = runBlocking {
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminders()

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.count(), `is`(1))

        assertThat(result.data.first().title, `is`(reminder.title))
        assertThat(result.data.first().description, `is`(reminder.description))
        assertThat(result.data.first().location, `is`(reminder.location))
        assertThat(result.data.first().latitude, `is`(reminder.latitude))
        assertThat(result.data.first().longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminderDisplayNotFoundError() = runBlocking {
        val result = remindersLocalRepository.getReminder(reminder.id)
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    companion object {
        val reminder = ReminderDTO("Title1", "Description1", "Location1", 10.01,1.1111)
    }
}