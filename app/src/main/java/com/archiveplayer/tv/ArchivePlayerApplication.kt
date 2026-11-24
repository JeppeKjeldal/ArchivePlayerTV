package com.archiveplayer.tv

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.archiveplayer.tv.data.local.AppDatabase
import com.archiveplayer.tv.data.repository.MediaRepository
import com.archiveplayer.tv.data.network.RetrofitClient

class ArchivePlayerApplication : Application(), ImageLoaderFactory {

    lateinit var mediaRepository: MediaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("AppInit", "ArchivePlayerApplication onCreate called")

        val database = AppDatabase.getDatabase(this)
        mediaRepository = MediaRepository(
            mediaItemDao = database.mediaItemDao(),
            watchProgressDao = database.watchProgressDao(),
            userProfileDao = database.userProfileDao()
        )
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(RetrofitClient.okHttpClient)
            .build()
    }
}