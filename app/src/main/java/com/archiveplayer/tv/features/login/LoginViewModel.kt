package com.archiveplayer.tv.features.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archiveplayer.tv.ArchivePlayerApplication
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    fun saveProfileName(profileName: String) {
        Log.d("LoginViewModel", "Attempting to save profile: $profileName")
        viewModelScope.launch {
            try {
                app.mediaRepository.saveProfileName(profileName)
                Log.d("LoginViewModel", "SUCCESS: Profile saved to DB")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "ERROR saving profile: ${e.message}", e)
            }
        }
    }
}