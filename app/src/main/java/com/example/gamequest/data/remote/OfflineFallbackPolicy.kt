package com.example.gamequest.data.remote

import java.io.IOException

/** HTTP responses are server decisions, including 5xx; only transport failures allow local access. */
internal fun Throwable.permiteFallbackOffline(): Boolean = this is IOException && this !is ApiException
