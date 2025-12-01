package uk.ac.tees.mad.nutriscan.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import uk.ac.tees.mad.nutriscan.data.remote.model.ProductRes
import uk.ac.tees.mad.nutriscan.data.remote.model.RecipeSearchResponse

interface ApiService {
    @GET("product/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): ProductRes
}

object EdamamConfig {
    const val APP_ID = "3285eed6"
    const val APP_KEY = "f16897ac00149fa0bf92c6a5d82b05ca"
}

interface EdamamApi {
    @GET("recipes/v2")
    suspend fun searchRecipes(
        @Query("type") type: String = "public",
        @Query("q") query: String,
        @Query("app_id") appId: String = EdamamConfig.APP_ID,
        @Query("app_key") appKey: String = EdamamConfig.APP_KEY,
        @Query("health") health: String? = null,
        @Query("diet") diet: String? = null,
        @Query("from") from: Int = 0,
        @Query("to") to: Int = 5
    ): RecipeSearchResponse
}
