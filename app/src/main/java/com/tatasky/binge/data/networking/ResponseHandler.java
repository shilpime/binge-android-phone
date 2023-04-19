package com.tatasky.binge.data.networking;//package com.tatasky.binge.data.networking;
//
//import com.tatasky.binge.R;
//import com.tatasky.binge.data.networking.models.response.BaseResponse;
//import com.tatasky.binge.ui.base.MyApp;
//import org.json.JSONObject;
//import retrofit2.Call;
//import retrofit2.Response;
//
//import java.io.IOException;
//
//import static com.tatasky.binge.utils.AppConstantsKt.*;
//import static com.tatasky.binge.utils.LoggerKt.e;
//
//public class ResponseHandler<T extends BaseResponse> implements retrofit2.Callback<T> {
//
//    private ApiCallback<BaseResponse> mApiCallback;
//
//    public ResponseHandler(ApiCallback<BaseResponse> apiCallback) {
//        mApiCallback = apiCallback;
//    }
//
//    @Override
//    public void onResponse(Call<T> call, Response<T> response) {
//        if (response.isSuccessful() && mApiCallback != null/* && response.body().getErrorCode() != null*/) {
//            switch (response.code()) {
//                case RESPONSE_CODE_SUCCESS: {
//                    if (response.body() instanceof BaseResponse) {
//                        BaseResponse model = (BaseResponse) response.body();
//                        int code = model.getCode();
//                        if (code == 0) {
//                            mApiCallback.onSuccessFullyCallback(model);
//                        } else {
//                            try {
//                                assert response.errorBody() != null;
//                                JSONObject jObjError = new JSONObject(response.errorBody().string());
//                                trackErrorEvent(response.code(), jObjError.getString("message"),
//                                        response.raw().request().url().toString());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            mApiCallback.onErrorCallback(model);
//                        }
//                    }
//                    break;
//                }
//            }
//        } else {
//            switch (response.code()) {
//                case RESPONSE_CODE_NOT_FOUND:
//                case RESPONSE_CODE_DEACTIVATED:
//                case RESPONSE_CODE_LARGE_FILE:
//                case RESPONSE_CODE_BAD_GATEWAY:
//                case RESPONSE_CODE_SERVICE_TEMPORARY_UNAVAILABLE:
//                    handleServerUnreachableError(response);
//                    break;
//                case RESPONSE_CODE_UNAUTHORIZED:
//                    // handleError(response);
////                    AppToast.getInstance().showToast(TSApplication.getContext(), R.string.error_401);
//                    //Utils.logout(TSApplication.getContext());
//                    break;
//                default:
//                    handleError(response);
//            }
//        }
//    }
//
//    private void handleServerUnreachableError(Response<T> response) {
//        BaseResponse baseResponseModal = new BaseResponse();
//        try {
//            baseResponseModal.setCode(response.code());
////            baseResponseModal.setMessage(MyApp.Companion.getContext().getString(R.string.error_server_unreachable));
////            baseResponseModal.setRequestedUrl(response.raw().request().url().toString());
//            mApiCallback.onErrorCallback(baseResponseModal);
//            trackErrorEvent(response.code(), response.raw().message(), response.raw().request().url().toString());
//        } catch (Exception e) {
////            mApiCallback.onNetworkErrorCallback(MyApp.Companion.getContext().getString(R.string.error_server_unreachable));
//            e("network error parse error", e.toString());
//        }
//
//    }
//
//    private void trackErrorEvent(int code, String message, String url) {
////        JSONObject jsonObject = new JSONObject();
////        try {
////            jsonObject.put(MixPanelConstant.PARAMETER.ERROR_CODE, code);
////            jsonObject.put(MixPanelConstant.PARAMETER.ERROR_MESSAGE, message);
////            jsonObject.put(MixPanelConstant.PARAMETER.ERROR_LOCATION, MixPanelConstant.ERROR_LOCATION.OTT_REST_APT);
////            jsonObject.put(MixPanelConstant.PARAMETER.API_URL, url);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////        Mixpanel.getInstance(TSApplication.getContext()).trackEvent(MixPanelConstant.EVENT.ERROR, jsonObject);
//    }
//
//    private void handleError(Response<T> response) {
//        BaseResponse baseResponseModal = new BaseResponse();
//        try {
//            baseResponseModal.setCode(response.code());
//            baseResponseModal.setMessage(response.raw().message());
////            baseResponseModal.setRequestedUrl(response.raw().request().url().toString());
//            mApiCallback.onErrorCallback(baseResponseModal);
//            trackErrorEvent(response.code(), response.raw().message(), response.raw().request().url().toString());
//        } catch (Exception e) {
////            mApiCallback.onNetworkErrorCallback(MyApp.Companion.getContext().getString(R.string.error_server_unreachable));
//            e("network error parse error", e.toString());
//        }
//    }
//
//    @Override
//    public void onFailure(Call<T> call, Throwable throwable) {
//        if (call.isCanceled()) {
//            return;
//        }
//        String errorMessage = throwable.toString();
//        e("ERROR", errorMessage);
//        trackErrorEvent(-1, errorMessage, call.request().url().toString());
//        if (throwable instanceof IOException) {
//            mApiCallback.onNetworkErrorCallback(MyApp.Companion.getContext().getString(R.string.error_server_unreachable));
//        } else {
//            BaseResponse baseResponseModal = new BaseResponse();
//            baseResponseModal.setCode(RESPONSE_CODE_SERVER_ERROR);
//            baseResponseModal.setMessage(MyApp.Companion.getContext().getString(R.string.error_server_unreachable));
//            mApiCallback.onErrorCallback(baseResponseModal);
//        }
//    }
//
//}
