package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid

@Dao
interface AsteroidDao {
    @Query("select * from DatabaseAsteroids")
    fun getAsteroids(): LiveData<List<DatabaseAsteroids>>

    @Query("select * from DatabaseAsteroids where closeApproachDate = :todayDate")
    fun getTodayAsteroids(todayDate: String): LiveData<List<DatabaseAsteroids>>

    @Query("select * from DatabaseAsteroids where closeApproachDate between :startDate and :endDate")
    fun getWeeklyAsteroids(startDate: String, endDate: String): LiveData<List<DatabaseAsteroids>>

    @Query("delete from DatabaseAsteroids where closeApproachDate = :yesterdayDate")
    fun deleteYesterdayAsteroids(yesterdayDate: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroids)
}

@Database(entities = [DatabaseAsteroids::class], version = 1)
abstract class AsteroidsDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getDatabase(context: Context): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                    AsteroidsDatabase::class.java,"asteroids").build()
        }
        return INSTANCE
    }
}