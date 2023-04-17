package com.tatasky.binge.data.networking.models

import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.COMMON_ERROR_MSG
import com.tatasky.binge.utils.COMMON_ERROR_TITLE
import com.tatasky.binge.utils.RESPONSE_CODE_SUCCESS

data class ErrorModel(val code:Int=-1, var message:String?= COMMON_ERROR_MSG, var title:String= COMMON_ERROR_TITLE,
                      var statusCode:Int= CODE_SUCCESS,
                      var isConcurrency : Boolean = false)