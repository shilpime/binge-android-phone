package com.tatasky.binge.ui.features.player.interceptor;

import com.tatasky.binge.ui.features.player.listeners.DeviceRegistrationListener;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This is used for Intercepting of Player Response Headers.
 */
final public class HttpLoggingInterceptor implements Interceptor {
    private DeviceRegistrationListener deviceRegistrationListener =null;

    public HttpLoggingInterceptor(@NotNull DeviceRegistrationListener deviceRegistrationListener) {
        this.deviceRegistrationListener = deviceRegistrationListener;
    }
    private static final String TAG = HttpLoggingInterceptor.class.getSimpleName();
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        Headers headers = response.headers();
        @NotNull
        String deviceId = headers.get("x-irdeto-drmdeviceid");
        @NotNull
        String rrmError = headers.get("x-irdeto-error-code");
        if(deviceRegistrationListener!=null) {
            if (deviceId != null) {
                deviceRegistrationListener.onDeviceIdReceived(deviceId);
            }
            if (rrmError != null) {
                deviceRegistrationListener.onRRMError(rrmError);
            }
        }
        return response;
    }
}
