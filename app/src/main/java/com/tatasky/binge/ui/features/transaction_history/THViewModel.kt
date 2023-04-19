package com.tatasky.binge.ui.features.transaction_history

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.InvoiceDownloadRequest
import com.tatasky.binge.data.networking.models.requests.TransactionHistoryRequest
import com.tatasky.binge.data.networking.models.response.InvoiceDownloadResponse
import com.tatasky.binge.data.networking.models.response.TransactionHistoryResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import javax.inject.Inject


class THViewModel @Inject constructor(
    private val commonUseCase: CommonUseCase,
    val sharedPrefs: PrefsRepo
) : BaseViewModel() {

    private val transactionHistoryResponse =
        MutableLiveData<SingleEvent<TransactionHistoryResponse>>()
    private val downloadComplete =
        MutableLiveData<SingleEvent<ResponseBody>>()
    private val invoiceDownloadLiveData=MutableLiveData<SingleEvent<InvoiceDownloadResponse>>()



    var selectedId = ""
    fun getTransactionHistory(): LiveData<SingleEvent<TransactionHistoryResponse>> =
        transactionHistoryResponse

    fun getDownloadComplete(): LiveData<SingleEvent<ResponseBody>> =
        downloadComplete

    fun getInvoiceDownloadComplete(): LiveData<SingleEvent<InvoiceDownloadResponse>> =
            invoiceDownloadLiveData

    fun fetchTransactionHistory() {
        setProgressing(true)
        val subscriptionType = sharedPrefs.getSubscribedPack()?.subscriptionType
        val s = commonUseCase.getTransactionHistory(
            TransactionHistoryRequest(dsn = if(subscriptionType == subscriptionTypeAtv || subscriptionType == subscriptionTypeFtv) sharedPrefs.getDsn() else null , baId = sharedPrefs.getBaId(), limit = null, offset = null))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<TransactionHistoryResponse>() {
                override fun onSuccessResponse(t: TransactionHistoryResponse) {
                    setProgressing(false)
                    if(t.code == CODE_SUCCESS) {
                        transactionHistoryResponse.postValue(SingleEvent(t))
                    } else {
                        onError(ErrorModel(code = t.code, message = t.message))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

            })
    }

    fun fetchTransactionHistoryForNonDTHUser() {
        setProgressing(true)
        val subscriptionType = sharedPrefs.getSubscriptionType()
        val s = commonUseCase.getTransactionHistoryForNonDTHUser(sharedPrefs.getBaId())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<TransactionHistoryResponse>() {
                override fun onSuccessResponse(t: TransactionHistoryResponse) {
                    setProgressing(false)
                    if(t.code == CODE_SUCCESS) {
                        transactionHistoryResponse.postValue(SingleEvent(t))
                    } else {
                        onError(ErrorModel(code = t.code, message = t.message))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

            })
    }

    @SuppressLint("CheckResult")
    fun downloadPDF(url: String, id: String) {
        setProgressing(true)
        commonUseCase.downloadPDF(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error -> setRetryError(error, false) }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : SingleObserver<ResponseBody> {
                override fun onError(e: Throwable) {
                    e("LoginFragment", "inside errorMessage : " + errorMessage)
                    setProgressing(false)
                    setError(ErrorModel())
                }

                override fun onSuccess(t: ResponseBody) {

                    if (t == null) {
                        setError(ErrorModel())
                    } else {
                        // downloadFile(t,id)
                        selectedId = id
                        downloadComplete.postValue(SingleEvent(t))
                    }
                    setProgressing(false)
                }

                override fun onSubscribe(d: Disposable) {
                }

            })
    }

    fun getBase64Pdf(invoiceNo: String?) {
        setProgressing(true)

        val s=commonUseCase.fetchInvoiceDownloadLink(InvoiceDownloadRequest(invoiceNo),sharedPrefs.getBaId())
        .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { error -> setRetryError(error, false) }
                .subscribeWith(object : CallbackWrapper<InvoiceDownloadResponse>() {
                    override fun onSuccessResponse(t: InvoiceDownloadResponse) {
                        setProgressing(false)
                        if(t.code == CODE_SUCCESS) {
                            t.data?.let {

                                invoiceDownloadLiveData.postValue(SingleEvent(t))
                                /*val path=downloadPdfBase64(it.paymentInvoice)
                                Log.d("Path--",path.toString())*/

                            }
                            //transactionHistoryResponse.postValue(SingleEvent(t))
                        } else {
                            onError(ErrorModel(code = t.code, message = t.message))
                        }

                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(ErrorModel())
                    }

                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                })


    }

}