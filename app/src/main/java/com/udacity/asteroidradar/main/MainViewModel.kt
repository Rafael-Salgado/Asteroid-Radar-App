package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.api.NeoApi
import com.udacity.asteroidradar.api.getPictureOfDay
import com.udacity.asteroidradar.api.getStartAndEndDate
import com.udacity.asteroidradar.api.getYesterdayDate
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainViewModel(application: Application) : ViewModel() {

    enum class AsteroidsFilter { WEEK, TODAY, SAVED }

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid?>()
    val navigateToSelectedAsteroid: LiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    private val _todayImg = MutableLiveData<PictureOfDay>()
    val todayImg: LiveData<PictureOfDay>
        get() = _todayImg

    private val database = getDatabase(application)
    private val daysList = getStartAndEndDate()
    private val asteroidsRepository = AsteroidsRepository(database, daysList)

    private val _asteroidsList = MutableLiveData<List<Asteroid>>()
    val asteroidsList: LiveData<List<Asteroid>>
        get() = _asteroidsList

    private val _asteroidsListStatus = MutableLiveData<Boolean>()
    val asteroidsListStatus: LiveData<Boolean>
        get() = _asteroidsListStatus

    private var asteroidsListLiveData: LiveData<List<Asteroid>>
    private val asteroidsListObserver = Observer<List<Asteroid>> {
        _asteroidsList.value = it
    }

    init {
        _asteroidsListStatus.value = true
        val key = BuildConfig.api_key
        asteroidsListLiveData = asteroidsRepository.getAsteroidList(AsteroidsFilter.SAVED)
        asteroidsListLiveData.observeForever(asteroidsListObserver)
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids(daysList[0], key)
            getNasaTodayImage(key)
            _asteroidsListStatus.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        asteroidsListLiveData.removeObserver(asteroidsListObserver)
    }


    private suspend fun getNasaTodayImage(apiKey: String) {
        try {
            val result = JSONObject(NeoApi.retrofitService.getTodayImage(apiKey))
            _todayImg.value = getPictureOfDay(result)
        } catch (e: Exception) {
            Log.e("ASTEROID_API", e.toString())
        }

    }


    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsCompleted() {
        _navigateToSelectedAsteroid.value = null
    }

    fun filterDatabase(filter: AsteroidsFilter) {
        asteroidsListLiveData = asteroidsRepository.getAsteroidList(filter)
        asteroidsListLiveData.observeForever(asteroidsListObserver)
    }

}