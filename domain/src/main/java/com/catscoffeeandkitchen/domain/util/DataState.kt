package com.catscoffeeandkitchen.domain.util

sealed class DataState<T> {
    data class NotSent<T>(val data: T? = null) : DataState<T>()
    data class Loading<T>(val data: T? = null) : DataState<T>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error<T>(val e: Throwable) : DataState<T>()
}