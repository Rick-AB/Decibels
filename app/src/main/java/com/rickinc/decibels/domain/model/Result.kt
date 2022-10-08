package com.rickinc.decibels.domain.model

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorMessage: String) : Result<Nothing>()

    fun fold(
        onSuccess: (value: R) -> Unit,
        onFailure: (error: Error) -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(this.data)
            is Error -> onFailure(this)
        }
    }

    fun onSuccess(action: (value: R) -> Unit) {
        if (this is Success) action(this.data)
    }

    fun onError(onFailure: (error: Error) -> Unit = { }) {
        if (this is Error) onFailure(this)
    }

    val Result<*>.succeeded
        get() = this is Success && data != null

    fun <T> Result<T>.successOr(fallback: T): T {
        return (this as? Success<T>)?.data ?: fallback
    }
}
