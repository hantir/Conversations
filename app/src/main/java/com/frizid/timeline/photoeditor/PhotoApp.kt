package com.frizid.timeline.photoeditor

import android.app.Application
import android.content.Context


class PhotoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        photoApp = this

    }

    val context: Context
        get() = photoApp!!.context

    companion object {
        var photoApp: PhotoApp? = null
            private set
        private val TAG = PhotoApp::class.java.simpleName
    }
}
