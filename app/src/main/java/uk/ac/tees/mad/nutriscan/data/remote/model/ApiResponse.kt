package uk.ac.tees.mad.nutriscan.data.remote.model

sealed class ApiResponse<out T> {
    object Loading : ApiResponse<Nothing>()
    object Initial : ApiResponse<Nothing>()
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
}
