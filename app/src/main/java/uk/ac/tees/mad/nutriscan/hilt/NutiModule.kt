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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NutiModule {

    private const val BASE_URL = "https://world.openfoodfacts.net/api/v2/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun providesAuthentication() : FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun providesFirestore() : FirebaseFirestore = Firebase.firestore

}