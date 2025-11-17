package uk.ac.tees.mad.nutriscan.ui.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.nutriscan.models.User

@HiltViewModel
class AuthenticationVM @Inject constructor(
    private val authentication : FirebaseAuth,
    private val firestore : FirebaseFirestore
) : ViewModel() {

    val loggedIn get() = authentication.uid != null
    val loading = MutableStateFlow(false)

    init{
        if (loggedIn){
            getUserData()
        }
    }

    val user = MutableStateFlow<User?>(null)

    fun getUserData(){
        viewModelScope.launch {
            firestore.collection("users").document(authentication.uid!!).get()
                .addOnSuccessListener {
                    val u = it.toObject(User::class.java)
                    user.value = u
                }
                .addOnFailureListener {
                    Log.e("AuthenticationVM", "getUserData: ${it.localizedMessage}", it)
                }
        }
    }

    fun login(context: Context, email: String, password: String, onSuccess : () -> Unit) {
        loading.value = true
            viewModelScope.launch {
                authentication.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        loading.value = false
                        onSuccess()
                    }
                    .addOnFailureListener {
                        loading.value = false
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }
    }

    fun signup(context: Context, name: String, email: String, password: String, onSuccess : () -> Unit) {
        loading.value = true
            viewModelScope.launch {
                authentication.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        loading.value = false
                        onSuccess()
                        addUserToFirestore(name, email, it.user!!.uid, onSuccess = {
                            onSuccess()
                        }, onFailure = {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                        })
                    }
                    .addOnFailureListener {
                        loading.value = false
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun addUserToFirestore(
        name: String,
        email: String,
        uid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = hashMapOf(
            "name" to name,
            "email" to email
        )
        firestore.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun logout(onSuccess : () -> Unit) {
        authentication.signOut()
        onSuccess()
    }

}