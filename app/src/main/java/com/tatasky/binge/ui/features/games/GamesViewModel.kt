package com.tatasky.binge.ui.features.games

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.ToggleFavouriteRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class GamesViewModel @Inject constructor(
    val sharedPref: PrefsRepo,
    private val useCase: CommonUseCase
) : BaseViewModel() {

    private var _addFavGame = MutableLiveData<SingleEvent<Boolean>>()
    fun getAddFavGame(): LiveData<SingleEvent<Boolean>> = _addFavGame

    private var _gameFavResponse = MutableLiveData<SingleEvent<IsFavouriteResponse>>()
    fun getGameFavResponse() : LiveData<SingleEvent<IsFavouriteResponse>> = _gameFavResponse

    @SuppressLint("CheckResult")
    fun addGameToFav(item: ContentItem) {
        useCase.addFavGame(
            sharedPref.getProfileId() ?: "",
            sharedPref.getOriginalSubscriberId(),
            item.id,
            item.contentType
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<GameFavResponse>(){
                override fun onSuccessResponse(t: GameFavResponse) {
                    _addFavGame.postValue(SingleEvent(t.data?.status ?: false))
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

            })

    }


    @SuppressLint("CheckResult")
    fun fetchGamesLastWatchedFavourite(id: String, contentType: String) {
        var uniqueId = sharedPref.getAnonymousId() ?: ""
        val toggleFavouriteRequest = ToggleFavouriteRequest(
            profileId = sharedPref.getProfileId() ?: "",
            subscriberId = sharedPref.getOriginalSubscriberId(),
            contentId = id,
            contentType = contentType,
            uniqueId = uniqueId,
            isLoggedIn = sharedPref.getLoginStatus()
        )

        useCase.fetchLastWatch(toggleFavouriteRequest,sharedPref.getSubscribedPack())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<IsFavouriteResponse>(false) {
                override fun onSuccessResponse(t: IsFavouriteResponse) {
                    _gameFavResponse.postValue(SingleEvent(t))
                }
                override fun onError(error: ErrorModel?) {
                    _gameFavResponse.postValue(SingleEvent(IsFavouriteResponse()))
                }
            })
    }


}