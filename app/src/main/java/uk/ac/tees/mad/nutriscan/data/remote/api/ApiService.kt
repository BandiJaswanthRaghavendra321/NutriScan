package uk.ac.tees.mad.nutriscan.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import uk.ac.tees.mad.nutriscan.data.remote.model.ProductRes

interface ApiService {
    @GET("product/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): ProductRes
}