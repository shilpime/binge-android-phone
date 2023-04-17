package com.tatasky.binge.learnactions

import android.annotation.SuppressLint
import android.content.Context
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.database.model.LAContentDBModel
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.utils.CLICK_LEARN_ACTION
import com.tatasky.binge.utils.RENTAL
import com.tatasky.binge.utils.e
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton

@Singleton
class LearnActionHelper(val mContext: Context,
                        val database: AppDatabase,
                        val sharedPrefs: PrefsRepo,
                        val commonUseCase: CommonUseCase
)  {
    fun hitFavoriteLearnAction(
        contentType: String,
        id: String,
        taShowType: String,
        provider: String,
        contractName : String,
        partnerSubscriptionType : String?,
        refUsecase: String
    ) {
        val data = taShowType.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if(data.isNotEmpty()
            && !RENTAL.equals(contractName, ignoreCase = true)
        ) {
            val showType = if (data[0] == "CatchupEPG") "EPG" else "VOD"
            val contentType2 = data[1]
            val disposal = commonUseCase.callFavouriteLearnAction(
                contentType2, id, showType, provider, refUsecase
            )
            disposal
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                    override fun onError(error: ErrorModel?) {
                    }
                    override fun onSuccessResponse(t: BaseResponse) {
                        when (t.code) {

                        }
                    }
                })
        }
    }

    @SuppressLint("CheckResult")
    fun trackOnceIn24hrLearnAction(
        partnerSubscriptionType: String?,
        contentType: String,
        id: String,
        taShowType: String,
        provider: String,
        contractName: String,
        isLoggedIn: Boolean,
        type: String,
        refUsecase: String
    ) {
        if(checkLAWatchTriggered(id)) return
        val data = taShowType.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var laModel : LAContentDBModel? = null
        val disposal : Single<BaseResponse>
        if( RENTAL.equals(contractName, ignoreCase = true)
            || data.isEmpty()) {
            disposal = commonUseCase.callWatchViewAction(
                contentType, id,
                sharedPrefs.getOriginalSubscriberId(),
                sharedPrefs.getProfileId() ?: ""
            )
            laModel = LAContentDBModel(
                contentType = contentType,
                timestamp = System.currentTimeMillis(),
                contentId = id,
                learnActionName = "VR"
            )
            e(
                "LearnActionHelper",
                "inside viewaction count laModel : $laModel"
            )
        }
        else{
            val showType = if (data[0] == "CatchupEPG") "EPG" else "VOD"
            val contentType2 = data[1]
            disposal = commonUseCase.callLearnAction(
                contentType2, id, showType, provider,isLoggedIn,type, refUsecase
            )
            laModel = LAContentDBModel(
                contentType = contentType2,
                timestamp = System.currentTimeMillis(),
                contentId = id,
                learnActionName = "LA"
            )
            e(
                "LearnActionHelper",
                "inside learnaction laModel : $laModel row"
            )
        }

        database.laDao.insertLAContent(laModel)

        disposal
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onError(error: ErrorModel?) {
                }
                override fun onSuccessResponse(t: BaseResponse) {

                }
            })

    }

    private fun checkLAWatchTriggered(contentId: String): Boolean {
        val laModel = database.laDao.getLAContents(contentId)
        if(laModel.isNotEmpty() && laModel[0].timestamp != null) {
            val diff = (System.currentTimeMillis() - laModel[0].timestamp!!) / (60 * 60 * 24 * 1000)
            e(
                "LearnActionHelper",
                "inside isAlreadyNotExistContent " +
                        "System.currentTimeMillis() :${System.currentTimeMillis()}, " +
                        "laModel : $laModel diff : $diff"
            )
            if (diff > 24) {
                database.laDao.deleteLAContent(contentId)
                return false
            }
            else
                return true
        }
        return false
    }
}