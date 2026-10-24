package com.example.gamequest.di

import com.example.gamequest.BuildConfig

/** Build-time data source selection. Local never contacts the web API. */
enum class CampusDataMode { LOCAL, REMOTE;
    companion object {
        fun current(): CampusDataMode = when (BuildConfig.CAMPUS_DATA_MODE.lowercase()) {
            "local" -> LOCAL
            "remote" -> REMOTE
            else -> error("Unsupported CAMPUS_DATA_MODE")
        }
    }
}
