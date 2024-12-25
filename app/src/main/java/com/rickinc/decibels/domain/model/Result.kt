package com.rickinc.decibels.domain.model

import com.rickinc.decibels.domain.exception.ErrorHolder

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: ErrorHolder) : Result<Nothing>()

    fun fold(
        onSuccess: (value: R) -> Unit,
        onFailure: (error: ErrorHolder) -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(this.data)
            is Error -> onFailure(this.error)
        }
    }

    fun onSuccess(action: (value: R) -> Unit) {
        if (this is Success) action(this.data)
    }

    fun onError(onFailure: (error: ErrorHolder) -> Unit = { }) {
        if (this is Error) onFailure(this.error)
    }

    val Result<*>.isSuccess
        get() = this is Success && data != null

    fun <T> Result<T>.successOr(fallback: T): T {
        return (this as? Success<T>)?.data ?: fallback
    }
}
