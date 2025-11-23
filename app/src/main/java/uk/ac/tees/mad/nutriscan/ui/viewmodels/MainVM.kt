package uk.ac.tees.mad.nutriscan.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import uk.ac.tees.mad.nutriscan.data.remote.api.ApiService

@HiltViewModel
class MainVM @Inject constructor(
    val apiService: ApiService,
): ViewModel() {
    init {
        fetchApiData()
    }
    fun fetchApiData() {
        viewModelScope.launch {
            val res = apiService.getProduct("7622201766054")
            Log.d("Response of API", res.toString())
        }
    }
}