package com.plcoding.bookpedia.core.data

import com.plcoding.bookpedia.core.domain.DataError
import com.plcoding.bookpedia.core.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T,DataError.Remote> {
    val response = try {
        execute()
    } catch (e: SocketTimeoutException){
        return Result.Error(DataError.Remote.REQUEST_TIMEOUT)
    }catch (e: UnresolvedAddressException){
        return Result.Error(DataError.Remote.NO_INTERNET)
    }catch (e: Exception){
        coroutineContext.ensureActive()
        return Result.Error(DataError.Remote.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): com.plcoding.bookpedia.core.domain.Result<T, DataError.Remote> {
    return when(response.status.value){
        in 200..209 -> {
            try {
                com.plcoding.bookpedia.core.domain.Result.Success(response.body<T>())
            } catch(e: NoTransformationFoundException){
                com.plcoding.bookpedia.core.domain.Result.Error(DataError.Remote.SERIALIZATION)
            }
        }
        408 -> com.plcoding.bookpedia.core.domain.Result.Error(DataError.Remote.REQUEST_TIMEOUT)
        429 -> com.plcoding.bookpedia.core.domain.Result.Error(DataError.Remote.TOO_MANY_REQUEST)
        in 500.. 599 -> com.plcoding.bookpedia.core.domain.Result.Error(DataError.Remote.SERVER)
        else -> com.plcoding.bookpedia.core.domain.Result.Error(DataError.Remote.UNKNOWN)
    }
}