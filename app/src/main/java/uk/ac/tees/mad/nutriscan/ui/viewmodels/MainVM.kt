package uk.ac.tees.mad.nutriscan.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.nutriscan.data.local.ProductLocal
import uk.ac.tees.mad.nutriscan.data.remote.api.ApiService
import uk.ac.tees.mad.nutriscan.data.remote.api.EdamamApi
import uk.ac.tees.mad.nutriscan.data.remote.model.ApiResponse
import uk.ac.tees.mad.nutriscan.data.remote.model.Product
import uk.ac.tees.mad.nutriscan.data.remote.model.RecipeSearchResponse
import uk.ac.tees.mad.nutriscan.data.repository.NutriRepository


@HiltViewModel
class MainVM @Inject constructor(
    val apiService: ApiService,
    val edamamApi: EdamamApi,
    val repo : NutriRepository
) : ViewModel() {

    private val _product = MutableStateFlow<ApiResponse<Product>>(ApiResponse.Initial)
    val product: StateFlow<ApiResponse<Product>> = _product

    private val _recipe = MutableStateFlow<ApiResponse<RecipeSearchResponse>>(ApiResponse.Initial)
    val recipe: StateFlow<ApiResponse<RecipeSearchResponse>> = _recipe

    val productRoom: StateFlow<List<ProductLocal>> = repo.getProductFromRoom()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )



    fun fetchRecipe() {
        _recipe.value = ApiResponse.Loading
        viewModelScope.launch {
            try {
                val res = edamamApi.searchRecipes(query = "healthy")
                _recipe.value = ApiResponse.Success(res)
                Log.d("Response of API", res.toString())
            } catch (e: Exception) {
                _recipe.value = ApiResponse.Error(e.message ?: "Unknown error")
                Log.e("Response of API", e.message, e)
            }
        }
    }


    fun fetchApiData(barcode: String) {
        viewModelScope.launch {
            _product.value = ApiResponse.Loading

            try {
                repo.getProductAndSave(barcode,onSuccess = {
                    _product.value = ApiResponse.Success(it)
                },
                    onFailure = {
                        _product.value = ApiResponse.Error(it.message ?: "Unknown error")
                    })
            } catch (e: Exception) {
                _product.value = ApiResponse.Error(e.message ?: "Unknown error")
                Log.e("Response of API", e.message, e)
            }
        }
    }

}