package com.ashok.holidayplanner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ashok.holidayplanner.data.local.AppDatabase
import com.ashok.holidayplanner.data.local.dao.LeavePlanDao
import com.ashok.holidayplanner.data.local.entities.LeavePlan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class LeavePlanDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var leavePlanDao: LeavePlanDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        leavePlanDao = db.leavePlanDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetLeavePlan() = runBlocking {
        val plan = LeavePlan(
            userId = "user_123",
            title = "Winter Holiday",
            startDate = LocalDate.of(2026, 12, 20),
            endDate = LocalDate.of(2026, 12, 31),
            note = "Cabin in the woods"
        )
        val id = leavePlanDao.insertLeavePlan(plan)
        val loaded = leavePlanDao.getLeavePlanById(id, "user_123")

        assertNotNull(loaded)
        assertEquals("Winter Holiday", loaded?.title)
        assertEquals(LocalDate.of(2026, 12, 20), loaded?.startDate)
        assertEquals(LocalDate.of(2026, 12, 31), loaded?.endDate)

        val userLeaves = leavePlanDao.getLeavePlans("user_123").first()
        assertEquals(1, userLeaves.size)

        // User data isolation check: another user sees empty list
        val otherUserLeaves = leavePlanDao.getLeavePlans("other_user").first()
        assertEquals(0, otherUserLeaves.size)
    }

    @Test
    fun updateAndDeleteLeavePlan() = runBlocking {
        val plan = LeavePlan(
            userId = "user_123",
            title = "Spring Break",
            startDate = LocalDate.of(2026, 3, 10),
            endDate = LocalDate.of(2026, 3, 15)
        )
        val id = leavePlanDao.insertLeavePlan(plan)

        val updated = plan.copy(id = id, title = "Updated Spring Break")
        leavePlanDao.updateLeavePlan(updated)

        val loaded = leavePlanDao.getLeavePlanById(id, "user_123")
        assertEquals("Updated Spring Break", loaded?.title)

        leavePlanDao.deleteLeavePlan(updated)
        val afterDelete = leavePlanDao.getLeavePlanById(id, "user_123")
        assertNull(afterDelete)
    }
}
