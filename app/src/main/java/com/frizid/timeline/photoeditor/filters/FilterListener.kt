package com.frizid.timeline.photoeditor.filters

import ja.burhanrashid52.photoeditor.PhotoFilter


interface FilterListener {
    fun onFilterSelected(photoFilter: PhotoFilter?)
}