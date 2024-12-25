package com.rickinc.decibels.data.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.rickinc.decibels.data.datasource.network.dto.HttpErrorDto
import com.rickinc.decibels.domain.exception.ErrorHolder
import com.rickinc.decibels.domain.model.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

object RequestHandler {
    suspend inline fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        crossinline apiCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                Result.Success(apiCall())
            } catch (exception: Exception) {
                if (exception is HttpException && exception.code().toString().startsWith("5")) {
                    Result.Error(ErrorHolder.InternalServerError("Oops...Something went wrong on our end.\nWe are working to fix it"))
                } else {
                    val error = asNetworkException(exception)
                    Result.Error(error)
                }
            }
        }
    }
}

fun asNetworkException(ex: Throwable): ErrorHolder {
    return when (ex) {
        is IOException -> ErrorHolder.NetworkConnection("No Internet Connection")
        is HttpException -> extractHttpExceptions(ex)
        else -> ErrorHolder.Unknown("Something went wrong...")
    }
}

private fun extractHttpExceptions(ex: HttpException): ErrorHolder {
    val body = ex.response()?.errorBody()
    val gson = GsonBuilder().create()
    val responseBody = gson.fromJson(body.toString(), JsonObject::class.java)
    val errorDto = gson.fromJson(responseBody, HttpErrorDto::class.java)
    val errorDetail = errorDto.detail
    return when (errorDetail.errorCode) {
        ErrorCodes.BAD_REQUEST ->
            ErrorHolder.BadRequest(errorDetail.errorMessage)

        ErrorCodes.INTERNAL_SERVER ->
            ErrorHolder.InternalServerError(errorDetail.errorMessage)

        ErrorCodes.UNAUTHORIZED ->
            ErrorHolder.UnAuthorized(errorDetail.errorMessage)

        ErrorCodes.NOT_FOUND ->
            ErrorHolder.ResourceNotFound(errorDetail.errorMessage)

        else ->
            ErrorHolder.Unknown(errorDetail.errorMessage)

    }
}