package com.livtech.common.core.models

import com.livtech.common.core.network.ApiClient
import com.livtech.common.core.network.ErrorMessageParser
import com.livtech.common.core.network.ErrorMessageParserImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

open class BaseRepo(
    val scope: CoroutineScope,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val errorParser: ErrorMessageParser = ErrorMessageParserImpl()
) {

    suspend fun <T> makeApiCall(req: Deferred<Response<T>>): Resource<T?> {
        return try {
            val call = suspend { req.await() }
            call.invoke().let {
                if (it.isSuccessful) {
                    if (it.body() == null) {
                        Resource.Failure(errorParser.onApiCallFailure(it))
                    } else {
                        Resource.Success(it.body())
                    }
                } else {
                    Resource.Failure(errorParser.onApiCallFailure(it))
                }
            }
        } catch (t: Throwable) {
            Resource.Failure(errorParser.onNetworkFailure(t))
        }
    }

    fun <T : Any> apiService(classType: Class<T>): T {
        return ApiClient.retrofit().create(classType)
    }
}