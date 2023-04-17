package com.ttn.ttnplayer.player;

import android.util.Log;

import com.ttn.ttnplayer.listeners.TtnPlayerListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * This is used for Intercepting of Player Response Headers.
 */
public class HttpLoggingInterceptor implements Interceptor {
    private TtnPlayerListener ttnPlayerListener;

    public HttpLoggingInterceptor(TtnPlayerListener ttnPlayerListener) {
        this.ttnPlayerListener = ttnPlayerListener;
    }

    private static final String TAG = HttpLoggingInterceptor.class.getSimpleName();

    /*@Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        Headers headers = response.headers();
        if (ttnPlayerListener != null) {
            ttnPlayerListener.onPlayerResponse(response);
        }
        return response;
    }*/

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        Headers headers = response.headers();
        @NotNull
        String deviceId = headers.get("x-irdeto-drmdeviceid");
        @NotNull
        String rrmError = headers.get("x-irdeto-error-code");
        /*if(deviceRegistrationListener!=null) {
            if (deviceId != null) {
                deviceRegistrationListener.onDeviceIdReceived(deviceId);
            }
            if (rrmError != null) {
                deviceRegistrationListener.onRRMError(rrmError);
            }
        }*/
        if (ttnPlayerListener != null) {
            ttnPlayerListener.onPlayerResponse(response);
        }
        return response;
    }
}