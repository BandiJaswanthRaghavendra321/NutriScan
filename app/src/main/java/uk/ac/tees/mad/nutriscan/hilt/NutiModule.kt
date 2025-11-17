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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NutiModule {

    @Provides
    @Singleton
    fun providesAuthentication() : FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun providesFirestore() : FirebaseFirestore = Firebase.firestore

}