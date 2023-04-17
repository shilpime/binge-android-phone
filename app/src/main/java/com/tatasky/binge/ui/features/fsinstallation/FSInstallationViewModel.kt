package com.tatasky.binge.ui.features.fsinstallation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.ContactPoint
import com.tatasky.binge.data.networking.models.requests.LocationAddress
import com.tatasky.binge.data.networking.models.requests.SlotsRequest
import com.tatasky.binge.data.networking.models.requests.WorkOrderRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.CODE_SUCCESS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FSInstallationViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo
) : BaseViewModel() {

    var installationReq = false
    var flag = false
    private val firestickWOResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    private val addressResponse = MutableLiveData<SingleEvent<AddressResponse>>()
    private val slotResponse = MutableLiveData<SingleEvent<SlotsResponse>>()
    private val _campaignResponse = MutableLiveData<SingleEvent<FtvCampaignResponse>>()
    fun getCampaignResponse(): LiveData<SingleEvent<FtvCampaignResponse>> = _campaignResponse

    fun isWorkOrderProcessed(): LiveData<SingleEvent<BaseResponse>> = firestickWOResponse
    fun getAddress(): LiveData<SingleEvent<AddressResponse>> = addressResponse
    fun getSlots(): LiveData<SingleEvent<SlotsResponse>> = slotResponse
    fun callFirestickWorkOrder(startSlot: String?=null,endSlot: String?=null, diy:Boolean=false) {
        setProgressing(true)
        useCase.executeWorkOrder(sharedPrefs.getBaId(), WorkOrderRequest(slotEndTime = endSlot,
            slotStartTime = startSlot,
            subscriberId = sharedPrefs.getOriginalSubscriberId(),isDIY=diy))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    if (t.code == CODE_SUCCESS)
                        firestickWOResponse.postValue(SingleEvent(t))
                    else
                        setError(ErrorModel(t.code, t.message))
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }
            })
    }

    fun fetchAddress() {
        setProgressing(true)
        useCase.fetchAddress()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<AddressResponse>() {
                override fun onSuccessResponse(t: AddressResponse) {
                    setProgressing(false)
                    if (t.code == CODE_SUCCESS)

                        addressResponse.postValue(SingleEvent(t))
                    else
                        setError(ErrorModel(t.code, t.message))
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }
            })
    }

    fun fetchSlots(addressResponse: AddressResponse, selectedDate: String) {
        setProgressing(true)
        if (true == addressResponse.data?.ocsFlag?.equals("N", true)) {
            if (!addressResponse.data?.slotSuggestions.isNullOrEmpty())
                slotResponse.postValue(SingleEvent(SlotsResponse().apply { data = SlotsData(addressResponse.data?.slotSuggestions ?: emptyList()) }))
            else
                setError(ErrorModel(message = "No slots found"))
            setProgressing(false)
        } else {
            val slotRequest = SlotsRequest(ContactPoint(sharedPrefs.getOriginalSubscriberId(), sharedPrefs.getOriginalSubscriberId()),
                LocationAddress(city = addressResponse.data?.city, formattedAddress = addressResponse.data?.pincode, pincode = addressResponse.data?.pincode, state = addressResponse.data?.state),
                SlotSuggestion(selectedDate, selectedDate),
                teamId = addressResponse.data?.serviceRegionId ?: ""
            )
            useCase.fetchSlots(slotRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<SlotsResponse>() {
                    override fun onSuccessResponse(t: SlotsResponse) {
                        setProgressing(false)
                        if (t.code == CODE_SUCCESS)
                            slotResponse.postValue(SingleEvent(t))
                        else
                            setError(ErrorModel(t.code, t.message))
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(error)
                    }
                })
        }
    }

    fun confirmSlot(taskId:String, addressResponse: AddressResponse, startSlot: String, endSlot:String, diy:Boolean=false) {
        setProgressing(true)
        val slotRequest = SlotsRequest(ContactPoint(sharedPrefs.getOriginalSubscriberId(), sharedPrefs.getOriginalSubscriberId()),
            LocationAddress(city = addressResponse.data?.city, formattedAddress = addressResponse.data?.pincode, pincode = addressResponse.data?.pincode, state = addressResponse.data?.state),
            SlotSuggestion(startSlot, endSlot),
            teamId = addressResponse.data?.serviceRegionId ?: "",
            taskId = taskId
        )
        useCase.confirmSlots(slotRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    if (t.code == CODE_SUCCESS)
                        callFirestickWorkOrder(startSlot, endSlot, diy)
                    else
                        setError(ErrorModel(t.code, t.message))
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }
            })
    }

    fun fetchCampaignResponse() {
        setProgressing(true)
        val d = useCase.fetchCampaign(sharedPrefs.getBaId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<FtvCampaignResponse>() {
                override fun onSuccessResponse(t: FtvCampaignResponse) {
                    setProgressing(false)
                    if (t.code == CODE_SUCCESS) {
                        _campaignResponse.postValue(SingleEvent(t))
                    } else {
                        _campaignResponse.postValue(SingleEvent(FtvCampaignResponse()))
                    //setError(ErrorModel(t.code, t.message))
                    }

                }
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    _campaignResponse.postValue(SingleEvent(FtvCampaignResponse()))
                //setError(error)
                }
            })
    }

}