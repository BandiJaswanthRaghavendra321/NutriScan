package uk.ac.tees.mad.nutriscan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.ac.tees.mad.nutriscan.data.local.BiometricPrefs
import uk.ac.tees.mad.nutriscan.ui.theme.*
import uk.ac.tees.mad.nutriscan.ui.viewmodels.AuthenticationVM
import uk.ac.tees.mad.nutriscan.utils.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authVm: AuthenticationVM = hiltViewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val user = authVm.user.collectAsState().value
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(user?.name ?: "") }
    var editing by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(true) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        biometricEnabled = BiometricPrefs.isEnabled(context)
        locked = biometricEnabled
    }

    if (locked && biometricEnabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("App Locked", color = GreenDark)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (BiometricHelper.isBiometricAvailable(context)) {
                            BiometricHelper.showBiometricPrompt(
                                context,
                                onSuccess = { locked = false },
                                onError = { snackbarMessage = it }
                            )
                        } else snackbarMessage = "Biometric not available"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unlock", color = White)
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile", color = White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
                    actions = {
                        IconButton(onClick = { authVm.logout { onLogout() } }) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = White)
                        }
                    }
                )
            },
            containerColor = GreenLight
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(GreenLight)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = White, modifier = Modifier.size(50.dp))
                }

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    enabled = editing,
                    label = { Text("Name") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (editing) GreenDark else GrayMedium,
                            modifier = Modifier.clickable {
                                if (editing) {
//                                    val uid = authVm.authentication.uid
//                                    if (uid != null) {
//                                        authVm.firestore.collection("users")
//                                            .document(uid)
//                                            .update("name", name)
//                                        snackbarMessage = "Name updated!"
//                                    }
                                }
                                editing = !editing
                            }
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = user?.email ?: "",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Email") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val newValue = !biometricEnabled
                                if (newValue) {
                                    if (BiometricHelper.isBiometricAvailable(context)) {
                                        BiometricHelper.showBiometricPrompt(
                                            context,
                                            onSuccess = {
                                                biometricEnabled = true
                                                scope.launch { BiometricPrefs.setEnabled(context, true) }
                                            },
                                            onError = { snackbarMessage = it }
                                        )
                                    } else snackbarMessage = "Biometric not available"
                                } else {
                                    biometricEnabled = false
                                    BiometricPrefs.setEnabled(context, false)
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("App Lock (Biometric)", color = GreenDark, fontWeight = FontWeight.SemiBold)
                    Switch(checked = biometricEnabled, onCheckedChange = null)
                }
            }
        }
    }

    snackbarMessage?.let { msg ->
        Snackbar(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp)),
            containerColor = GreenPrimary,
            contentColor = White
        ) { Text(msg) }
        LaunchedEffect(msg) {
            delay(2000)
            snackbarMessage = null
        }
    }
}
