package com.tatasky.binge.ui.base.frameworks.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


abstract class BaseViewModel : ViewModel() {

    private var _errorMessage = MediatorLiveData<SingleEvent<ErrorModel>>()
    private var _errorOkClicked = MediatorLiveData<SingleEvent<Unit>>()
    private var _retryError = MediatorLiveData<SingleEvent<Throwable>>()
    private var _progressListener = MediatorLiveData<Boolean>()
    private var _bottomSheetProgressListener = MediatorLiveData<Boolean>()
    private var _forceLogout = MediatorLiveData<Boolean>()
    private var _forceDeviceStatusLogout = MediatorLiveData<SingleEvent<Boolean>>()
    private var _updateInPack = MediatorLiveData<SingleEvent<Boolean>>()
    var _isRetry =  MediatorLiveData<SingleEvent<Boolean>>()
    var retrySubject = PublishSubject.create<Any>()


    val errorMessage: LiveData<SingleEvent<ErrorModel>>
        get() = _errorMessage

    val retryError: MediatorLiveData<SingleEvent<Throwable>>
        get() = _retryError

    val progressListener: LiveData<Boolean>
        get() = _progressListener

    val bottomSheetProgressListener : LiveData<Boolean>
        get() = _bottomSheetProgressListener

    val forceLogout: LiveData<Boolean>
        get() = _forceLogout

    val forceDeviceStatusLogout: LiveData<SingleEvent<Boolean>>
        get() = _forceDeviceStatusLogout

    val errorOkClicked: LiveData<SingleEvent<Unit>>
        get() = _errorOkClicked

    val updateInPack : LiveData<SingleEvent<Boolean>>
        get() = _updateInPack

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    protected fun clearDisposables() {
        compositeDisposable.clear()
    }

    override fun onCleared() {
        clearDisposables()
    }

    protected fun setError(error: ErrorModel?) {
        error?.let {
            setProgressing(false)
            _errorMessage.postValue(SingleEvent(it))
        }
    }

    fun setErrorOkClicked() {
        _errorOkClicked.postValue(SingleEvent(Unit))
    }

    protected fun setRetryError(error: Throwable, isRetry: Boolean) {
        setProgressing(false)
        _isRetry.postValue(SingleEvent(isRetry))
        _retryError.postValue(SingleEvent(error))
    }

    public fun setProgressing(boolean: Boolean) {
        _progressListener.postValue(boolean)
    }

    public fun setBottomSheetProgressing(boolean: Boolean){
        _bottomSheetProgressListener.postValue(boolean)
    }

    fun forceLogoutUser(){
        _forceLogout.postValue(true)
    }

    protected fun forceDeviceStatusLogoutUser(){
        _forceDeviceStatusLogout.postValue(SingleEvent(true))
    }

    fun updateInpack() {
        _updateInPack.postValue(SingleEvent(true))
    }


}