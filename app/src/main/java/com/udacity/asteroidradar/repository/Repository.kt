package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.NeoApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.asDomainDatabaseAsteroid
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainAsteroid
import com.udacity.asteroidradar.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception


class AsteroidsRepository(private val database: AsteroidsDatabase,private val dateArrayList: ArrayList<String>) {

    val asteroids: LiveData<List<Asteroid>> =
            Transformations.map(database.asteroidDao.getAsteroids()) {
                it.asDomainAsteroid()
            }

    fun getAsteroidList(filter: MainViewModel.AsteroidsFilter): LiveData<List<Asteroid>>{
       return when(filter){
            MainViewModel.AsteroidsFilter.TODAY->Transformations.map(
                    database.asteroidDao.getTodayAsteroids(dateArrayList[0])){
                    it.asDomainAsteroid()
           }
           MainViewModel.AsteroidsFilter.WEEK->Transformations.map(
                   database.asteroidDao.getWeeklyAsteroids(dateArrayList[0],dateArrayList[1])){
               it.asDomainAsteroid()
           }
            else->Transformations.map(database.asteroidDao.getAsteroids()) {
                it.asDomainAsteroid()
            }
        }
    }

    suspend fun refreshAsteroids(todayDate: String, apiKey: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = NeoApi.retrofitService.getAsteroids(todayDate, apiKey)
                val asteroidsList = parseAsteroidsJsonResult(JSONObject(response))
                database.asteroidDao.insertAll(*asteroidsList.asDomainDatabaseAsteroid())
            } catch (e: Exception) {
                Log.e("ASTEROID_API", e.toString())
            }

        }
    }

}