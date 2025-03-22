package com.plcoding.bookpedia.core.domain

sealed interface DataError : Error{
    enum class Remote : DataError{
        REQUEST_TIMEOUT,
        TOO_MANY_REQUEST,
        NO_INTERNET,
        SERVER,
        SERIALIZATION,
        UNKNOWN
    }

    enum class local: DataError{
        DISK_FULL,
        UNKNOWN
    }
}