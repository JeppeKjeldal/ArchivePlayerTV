package com.archiveplayer.tv.features.library

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    val userProfile: Flow<UserProfileEntity?> = app.mediaRepository.getUserProfile()

    val allMediaItems: Flow<List<MediaItemEntity>> = app.mediaRepository.getAllMediaItems()

    val categories: Flow<List<String>> = app.mediaRepository.getAllCategories()
}