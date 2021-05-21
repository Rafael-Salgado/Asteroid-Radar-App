package com.udacity.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.api.getStartAndEndDate
import com.udacity.asteroidradar.api.getYesterdayDate
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class RefreshDataWorker(private val context: Context, params:WorkerParameters): CoroutineWorker(context, params) {

    companion object{
        const val WORK_NAME = "RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(context)
        val daysList = getStartAndEndDate()
        val key = BuildConfig.api_key
        val asteroidsRepository = AsteroidsRepository(database,daysList)
        withContext(Dispatchers.IO){
            database.asteroidDao.deleteYesterdayAsteroids(getYesterdayDate())
        }
        return try {
            asteroidsRepository.refreshAsteroids(daysList[0],key)
            Result.success()
        }catch (e: Exception){
            Result.failure()
        }
    }
}