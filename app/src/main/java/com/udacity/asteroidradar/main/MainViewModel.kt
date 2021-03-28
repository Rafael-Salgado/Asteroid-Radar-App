package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.api.NeoApi
import com.udacity.asteroidradar.api.NeoWsApi
import com.udacity.asteroidradar.api.getStartAndEndDate
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.asDomainAsteroid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(application: Application) : ViewModel() {

    enum class AsteroidsFilter { WEEK, TODAY, SAVED }

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid?>()
    val navigateToSelectedAsteroid: LiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    private val _todayImgUrl = MutableLiveData<String>()
    val todayImgUrl: LiveData<String>
        get() = _todayImgUrl

    private val database = getDatabase(application)
    private val daysList = getStartAndEndDate()
    private val asteroidsRepository = AsteroidsRepository(database,daysList)

    private val _asteroidsList = MutableLiveData<List<Asteroid>>()
    val asteroidsList: LiveData<List<Asteroid>>
        get() = _asteroidsList

    private var asteroidsListLiveData: LiveData<List<Asteroid>>
    private val asteroidsListObserver = Observer<List<Asteroid>>{
        _asteroidsList.value = it
    }

    init {
        val key = application.getString(R.string.api_key)
        asteroidsListLiveData = asteroidsRepository.getAsteroidList(AsteroidsFilter.SAVED)
        asteroidsListLiveData.observeForever(asteroidsListObserver)
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids(daysList[0], key)
            getNasaTodayImage(key)
        }
    }

    override fun onCleared() {
        super.onCleared()
        asteroidsListLiveData.removeObserver(asteroidsListObserver)
    }


    private suspend fun getNasaTodayImage(apiKey: String) {
        val result = JSONObject(NeoApi.retrofitService.getTodayImage(apiKey))
        _todayImgUrl.value = result.getString("url")
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