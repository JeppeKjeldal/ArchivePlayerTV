package com.archiveplayer.tv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.archiveplayer.tv.data.network.RetrofitClient

class ArchivePlayerApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(RetrofitClient.okHttpClient)
            .build()
    }
}