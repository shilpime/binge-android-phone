package com.ttn.ttnplayer.player;

import static com.google.android.exoplayer2.util.Util.toByteArray;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DummyExoMediaDrm;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DRMSessionManager {
    private static final String USER_AGENT = "ttnplayer";
//    private static final String WIDEVINE_LICENSE_BASE_URI = "https://widevine-proxy.drm.technology/proxy";
    private String WIDEVINE_DRM = "widevine";
    private static final String SECURITY_LEVEL = "securityLevel";

    public DefaultDrmSessionManager buildDrmSessionManager(String kid, String token, String drmProxyUrl){

        DefaultDrmSessionManager defaultDrmSessionManager = new DefaultDrmSessionManager.Builder().build(new MediaDrmCallback() {
            @Override
            public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request) {
                try {
                    String url = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
                    return executePost(url, null, null);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new byte[0];
            }

            @Override
            public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request){

                Map<String, String> postParameters = new HashMap<>();
                postParameters.put("kid", kid);
                postParameters.put("token", token);
                try {
                    return executePost(request.getData(), postParameters, drmProxyUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new byte[0];
            }
        });
        defaultDrmSessionManager.setMode(DefaultDrmSessionManager.MODE_PLAYBACK, null);
        return defaultDrmSessionManager;
    }

    private byte[] executePost(byte[] data, Map<String, String> requestProperties, String drmProxyUrl) throws IOException {
        Log.v("RNdrm executePost", "called");
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(drmProxyUrl).openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(data != null);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);

            JSONObject json = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray();
                int bitmask = 0x000000FF;
                for (byte aData : data) {
                    int val = (int) aData;
                    jsonArray.put(bitmask & val);
                }

                json.put("token", requestProperties.get("token"));
                json.put("drm_info", jsonArray);
                json.put("kid", requestProperties.get("kid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                data = json.toString().getBytes(StandardCharsets.UTF_8);
            } else {
                data = json.toString().getBytes();
            }

            if (data != null) {
                OutputStream out = urlConnection.getOutputStream();
                try {
                    out.write(data);
                } finally {
                    out.close();
                }
            }

            int responseCode = urlConnection.getResponseCode();
            if (responseCode < 400) {
                // Read and return the response body.
                InputStream inputStream = urlConnection.getInputStream();
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte scratch[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(scratch)) != -1) {
                        byteArrayOutputStream.write(scratch, 0, bytesRead);
                    }
                    Log.v("RNdrm: execute done ", Arrays.toString(byteArrayOutputStream.toByteArray()));
                    return byteArrayOutputStream.toByteArray();
                } finally {
                    inputStream.close();
                }
            } else {
                throw new IOException();
            }

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }



    protected byte[] executePost(String url, byte[] data, Map<String, String> requestProperties) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(data != null);
            urlConnection.setDoInput(true);
            if (requestProperties != null) {
                for (java.util.Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                    urlConnection.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
                }
            }
            // Write the request body, if there is one.
            if (data != null) {
                OutputStream out = urlConnection.getOutputStream();
                try {
                    out.write(data);
                } finally {
                    out.close();
                }
            }
            // Read and return the response body.
            InputStream inputStream = urlConnection.getInputStream();
            try {
                return toByteArray(inputStream);
            } finally {
                Util.closeQuietly(inputStream);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public DefaultDrmSessionManager<ExoMediaCrypto> buildSessionManager(HttpMediaDrmCallback drmCallback, String securityLevel) {
        return
                new DefaultDrmSessionManager.Builder()
                        .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID,
                                uuid -> {
                                    try {
                                        FrameworkMediaDrm mediaDrm = FrameworkMediaDrm.newInstance(uuid);
                                        // Force L1 or L3.
                                        mediaDrm.setPropertyString(SECURITY_LEVEL, securityLevel);
                                        return mediaDrm;
                                    } catch (UnsupportedDrmException e) {
                                        return new DummyExoMediaDrm();
                                    }
                                })
                        .setMultiSession(true)
                        .build(drmCallback);
    }

    public DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(UUID drmSchemeUuid,
            String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession, FrameworkMediaDrm mediaDrm)
            throws UnsupportedDrmException {
        HttpDataSource.Factory licenseDataSourceFactory = buildHttpDataSourceFactory();
        HttpMediaDrmCallback drmCallback =
                new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        return new DefaultDrmSessionManager<>(drmSchemeUuid, mediaDrm, drmCallback, null, multiSession);
    }

    public MediaSource buildMediaSource(Context context, DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager, String url) {
        Uri uri = Uri.parse(url);
        DataSource.Factory dataSourceFactory = buildDataSourceFactory(context);
        if (url.endsWith(".mpd")) {
            return new DashMediaSource.Factory(dataSourceFactory).setDrmSessionManager(drmSessionManager)
                    .setManifestParser(new FilteringManifestParser<>(new DashManifestParser(), null))
                    .createMediaSource(uri);
        }

        throw new IllegalStateException("media type not supported!");
    }

    /**
     * Returns a {@link DataSource.Factory}.
     */
    private DataSource.Factory buildDataSourceFactory(Context context) {

        return new DefaultDataSourceFactory(context, buildHttpDataSourceFactory());
    }


    /**
     * Returns a {@link HttpDataSource.Factory}.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(USER_AGENT);
    }
}
