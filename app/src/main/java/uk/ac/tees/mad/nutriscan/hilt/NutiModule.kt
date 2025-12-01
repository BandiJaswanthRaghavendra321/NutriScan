package uk.ac.tees.mad.nutriscan.hilt

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.ac.tees.mad.nutriscan.data.remote.api.ApiService
import uk.ac.tees.mad.nutriscan.data.remote.api.EdamamApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NutiModule {

    private const val BASE_URL = "https://world.openfoodfacts.net/api/v2/"
    private const val RECIPE_SEARCH_URL = "https://api.edamam.com/api/"

    @Provides
    @Singleton
    @OpenFoodFactsRetrofit
    fun provideOpenFoodFactsRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(@OpenFoodFactsRetrofit retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    @EdamamRetrofit
    fun provideEdamamRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(RECIPE_SEARCH_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

    @Provides
    @Singleton
    fun provideEdamamApi(@EdamamRetrofit retrofit: Retrofit): EdamamApi =
        retrofit.create(EdamamApi::class.java)

    @Provides
    @Singleton
    fun providesAuthentication(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun providesFirestore(): FirebaseFirestore = Firebase.firestore
}
