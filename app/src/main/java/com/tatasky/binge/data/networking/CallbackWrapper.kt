package com.tatasky.binge.data.networking

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.utils.*
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

abstract class CallbackWrapper<T : BaseResponse>(val isPrimary: Boolean = true) : SingleObserver<T> {

    override fun onSuccess(t: T) {
        if (t.status == CODE_SUCCESS || t.status == RESPONSE_CODE_SUCCESS) {
            onSuccessResponse(t)
        } else {
            onError(ErrorModel(t.code, t.message ?: COMMON_ERROR_MSG, statusCode = t.status))
        }
    }

    override fun onSubscribe(d: Disposable) {
    }


    abstract fun onSuccessResponse(t: T)
    abstract fun onError(error: ErrorModel?)


    override fun onError(e: Throwable) {
        e("CallbackWrapper","$e")
        when (e) {
            is HttpException -> {
                when (e.response()?.code() ?: 0) {
//                    RESPONSE_CODE_UNAUTHORIZED ->
//                        if (isPrimary)
//                            onError(ErrorModel(e.response()?.code() ?: 0, UNAUTHORISED_MESSAGE))
                    RESPONSE_CODE_NOT_FOUND ->
                        onError(ErrorModel(message = UNREACHABLE_ERROR_MSG,
                            statusCode = e.response()?.code() ?: -1))

                    RESPONSE_CODE_DEACTIVATED,
                    RESPONSE_CODE_500,
                    RESPONSE_CODE_BAD_GATEWAY,
                    RESPONSE_CODE_SERVICE_TEMPORARY_UNAVAILABLE ->
                        if (isPrimary) {
                            try {
                                val response = Gson().fromJson<BaseResponse>(e.response()?.errorBody()?.string(), BaseResponse::class.java)

                                e("CallbackWrapper","response : $response")
                                onError(ErrorModel(statusCode = e.response()?.code() ?: -1,
                                    message = response.message?: COMMON_ERROR_MSG,
                                    title = response.title ?: COMMON_ERROR_TITLE))
                            }catch (exception:Exception) {
                                onError(ErrorModel(statusCode = e.response()?.code() ?: -1))
                            }
                        }
                        else{
                            onError(ErrorModel(statusCode = e.response()?.code() ?: -1))
                        }
                    else ->
                        onError(ErrorModel(statusCode = e.response()?.code() ?: -1,
                            message = e.message?: COMMON_ERROR_MSG))
                }
            }
            is SocketTimeoutException-> {
                if (isPrimary)
                    onError(ErrorModel(statusCode = RESPONSE_CODE_TIMEOUT,message = TIMEOUT_ERROR_MSG))
            }
            is IOException -> {
                if (isPrimary)
                    onError(ErrorModel(statusCode = RESPONSE_CODE_NETWORK_ERROR))
            }
            is JsonSyntaxException ->{
                onError(ErrorModel(-1, e.message, statusCode = RESPONSE_CODE_NO_DATA))
            }
            else -> onError(ErrorModel(-1, e.message))
        }
    }

    private fun getErrorMessage(responseBody: ResponseBody?): String? {
        try {
            val jsonObject = JSONObject(responseBody!!.string())
            return jsonObject.getString("message")
        } catch (e: Exception) {
            return e.message
        }

    }
}