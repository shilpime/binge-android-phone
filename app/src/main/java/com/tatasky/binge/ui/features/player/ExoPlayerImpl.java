package com.tatasky.binge.ui.features.player;//package com.tatasky.binge.ui.features.player;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.view.KeyEvent;
//
//import androidx.annotation.Dimension;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.google.android.exoplayer2.C;
//import com.google.android.exoplayer2.DefaultLoadControl;
//import com.google.android.exoplayer2.DefaultRenderersFactory;
//import com.google.android.exoplayer2.ExoPlayerFactory;
//import com.google.android.exoplayer2.Format;
//import com.google.android.exoplayer2.LoadControl;
//import com.google.android.exoplayer2.PlaybackPreparer;
//import com.google.android.exoplayer2.Player;
//import com.google.android.exoplayer2.SimpleExoPlayer;
//import com.google.android.exoplayer2.audio.AudioAttributes;
//import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
//import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
//import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
//import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
//import com.google.android.exoplayer2.drm.UnsupportedDrmException;
//import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
//import com.google.android.exoplayer2.offline.FilteringManifestParser;
//import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
//import com.google.android.exoplayer2.source.MediaSource;
//import com.google.android.exoplayer2.source.TrackGroup;
//import com.google.android.exoplayer2.source.TrackGroupArray;
//import com.google.android.exoplayer2.source.dash.DashMediaSource;
//import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
//import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
//import com.google.android.exoplayer2.source.hls.HlsMediaSource;
//import com.google.android.exoplayer2.text.CaptionStyleCompat;
//import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
//import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
//import com.google.android.exoplayer2.trackselection.TrackSelection;
//import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
//import com.google.android.exoplayer2.ui.PlayerView;
//import com.google.android.exoplayer2.upstream.DefaultAllocator;
//import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
//import com.google.android.exoplayer2.upstream.HttpDataSource;
//import com.google.android.exoplayer2.util.Util;
//import com.tatasky.binge.R;
//import com.tatasky.binge.data.networking.models.response.ConfigResponse;
//import com.tatasky.binge.data.prefs.SharedPrefs;
//import com.tatasky.binge.ui.features.player.interceptor.HttpLoggingInterceptor;
//import com.tatasky.binge.ui.features.player.listeners.DeviceRegistrationListener;
//import com.tatasky.binge.ui.features.player.listeners.DrmManagerListener;
//import com.tatasky.binge.ui.features.player.listeners.MediaSourceListener;
//import com.tatasky.binge.ui.features.player.listeners.PlayerAnalyticsListener;
//import com.tatasky.binge.ui.features.player.listeners.PlayerEventListener;
//import com.tatasky.binge.ui.features.player.listeners.PlayerListener;
//import com.tatasky.binge.ui.features.player.model.AudioLanguage;
//import com.tatasky.binge.ui.features.player.model.Bitrate;
//import com.tatasky.binge.ui.features.player.model.BitrateData;
//import com.tatasky.binge.ui.features.player.model.Hd;
//import com.tatasky.binge.ui.features.player.model.Sd;
//import com.tatasky.binge.ui.features.player.model.VideoQuality;
//import com.tatasky.binge.utils.AppConstantsKt;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.Call;
//import okhttp3.OkHttpClient;
//import okhttp3.Protocol;
//
//import static com.tatasky.binge.utils.LoggerKt.d;
//import static com.tatasky.binge.utils.LoggerKt.e;
//
//
//public class ExoPlayerImpl  {
//
//    public static final int PLAYER_INCREMENT_MS = 10000;
//    public static final int PLAYER_CONTROLLER_SHOW_TIMEOUT_MS = 3000;
//    private static final String TAG = "ExoPlayerImpl";
//    private static final String BITRATE_SUFFIX = "p";
//    private OkHttpClient mOkHttpClient;
//    private final Handler mDrmHandler;
//    private HttpDataSource.Factory factory;
//    private DefaultBandwidthMeter defaultBandwidthMeter;
//    private static final long PLAYER_DEFAULT_TIMEOUT = 30;
//    // Saved instance state keys.
//    private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
//    private static final String KEY_WINDOW = "window";
//    private static final String KEY_POSITION = "position";
//    private static final String KEY_AUTO_PLAY = "auto_play";
//    private String defaultLanguage;
//    private final String userAgent;
//    private SimpleExoPlayer player;
//    private FrameworkMediaDrm mediaDrm;
//    private MediaSource mediaSource;
//    private DefaultTrackSelector trackSelector;
//    private DefaultTrackSelector.Parameters trackSelectorParameters;
//    //    private DeviceRegistrationListener deviceRegistrationListener;
//    private boolean startAutoPlay = true;
//    private int startWindow = C.INDEX_UNSET;
//    private long startPosition;
//    private String[] header_key_requests;
//    private VideoQuality videoQuality;
//
//    public ExoPlayerImpl(@NotNull Context context) {
//        userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
//        HandlerThread mHandlerThread = new HandlerThread("exo-player-logging");
//        mHandlerThread.start();
//        mDrmHandler = new Handler(mHandlerThread.getLooper());
//    }
//
//    protected SimpleExoPlayer initializePlayer(@NotNull Activity activity,
//                                               @NotNull PlayerModel playerViewModel,
//                                               @NotNull PlayerView playerView,
//                                               Fragment fragment) {
//        this.header_key_requests = playerViewModel.getKeyRequestHeaders().toArray(new String[0]);
//        if (player == null) {
//            Uri data = Uri.parse(playerViewModel.getPlaybackUrl());
//            Uri[] uris;
//            if (data != null) {
//                uris = new Uri[]{data};
//            } else {
//                return null;
//            }
//            if (Util.maybeRequestReadExternalStoragePermission(activity, uris)) {
//                return null;
//            }
//            //deviceRegistrationListener = (DeviceRegistrationListener) activity;
//            DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = getDefaultDrmSessionManager(fragment, playerViewModel);
//            TrackSelection.Factory trackSelectionFactory = getAdaptiveTrackSelectionFactory(activity);
//            DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(activity, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
//            trackSelector = new DefaultTrackSelector(trackSelectionFactory);
//            if (trackSelectorParameters == null)
//                trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
//            trackSelector.setParameters(trackSelectorParameters);
//            player = ExoPlayerFactory.newSimpleInstance(activity, defaultRenderersFactory, trackSelector, drmSessionManager);
//            player.addListener(new PlayerEventListener((PlayerListener.EventListener) fragment));
//            player.addAnalyticsListener(new PlayerAnalyticsListener((PlayerListener.AnalyticsEventListener) fragment));
//
//            playerView.setPlayer(player);
//            playerView.setPlaybackPreparer((PlaybackPreparer) fragment);
//            MediaSource[] mediaSources = new MediaSource[uris.length];
//            for (int i = 0; i < uris.length; i++) {
//                mediaSources[i] = buildMediaSource(activity, uris[i], null);
//            }
//            mediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
//
//            Typeface typeFace = Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.medium_font));
//            CaptionStyleCompat captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.TRANSPARENT,
//                    Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_OUTLINE, Color.BLACK, typeFace);
//
//            playerView.getSubtitleView().setStyle(captionStyleCompat);
//
//            playerView.getSubtitleView().setFixedTextSize(Dimension.SP, 16);
//            playerView.getSubtitleView().setApplyEmbeddedStyles(false);
//
//        }
//        setupPrepare(playerViewModel);
//        return player;
//    }
//
//    private void setupPrepare(@NotNull PlayerModel playerViewModel) {
//        player.setPlayWhenReady(startAutoPlay);
//        if (playerViewModel.getResumeTime() > 0 && startWindow == C.INDEX_UNSET) {
//            startWindow = 0;
//            startPosition = playerViewModel.getResumeTime();
//        }
//        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
//        if (haveStartPosition) {
//            player.seekTo(startWindow, startPosition);
//        }
//        player.prepare(mediaSource, !haveStartPosition, false);
//        AudioAttributes audioAttributes = player.getAudioAttributes();
//        TrackGroupArray currentTrackGroups = player.getCurrentTrackGroups();
//        TrackSelectionArray currentTrackSelections = player.getCurrentTrackSelections();
//        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
//
//        e("ExoPlayerImpl", "setupPrepare mappedTrackInfo : " + mappedTrackInfo);
//    }
//
//    protected void reInitialize(@NotNull PlayerModel playerViewModel, long scrubberMovePosition) {
//        if (player != null) {
//            startWindow = C.INDEX_UNSET;
//            if (scrubberMovePosition > 0) {
//                playerViewModel.setResumeTime(scrubberMovePosition);
//            } else {
//                playerViewModel.setResumeTime(player.getCurrentPosition());
//            }
//            setupPrepare(playerViewModel);
//        }
//    }
//
//    protected void seekTo(int keycode) {
//        d("seekTo", "seekTo " + keycode);
//        if (player != null) {
//            d("seekTo", "seekTo");
//            if (keycode == KeyEvent.KEYCODE_DPAD_LEFT)
//                rewind();
//            if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT)
//                fastForward();
//        }
//    }
//
//    private void rewind() {
//        player.seekTo(Math.max(player.getCurrentPosition() - PLAYER_INCREMENT_MS, 0));
//    }
//
//    private void fastForward() {
//        long durationMs = player.getDuration();
//        long seekPositionMs = player.getCurrentPosition() + PLAYER_INCREMENT_MS;
//        if (durationMs != C.TIME_UNSET) {
//            seekPositionMs = Math.min(seekPositionMs, durationMs);
//        }
//        player.seekTo(seekPositionMs);
//    }
//
//
//    /**
//     * @return provide player is playing or not
//     */
//    protected boolean isPlaying() {
//        return player != null && player.getPlaybackState() != Player.STATE_ENDED && player.getPlaybackState() != Player.STATE_IDLE && player.getPlayWhenReady();
//    }
//
//    /**
//     * @return This will provide the trackSelector instance.
//     */
//    @NotNull
//    protected DefaultTrackSelector getTrackSelector() {
//        return trackSelector;
//    }
//
//    /**
//     * @return This will provide the player instance.
//     */
//    @NotNull
//    protected SimpleExoPlayer getPlayer() {
//        return player;
//    }
//
//
//    /**
//     * @param uuid
//     * @param licenseUrl
//     * @param keyRequestPropertiesArray
//     * @param multiSession
//     * @return DefaultDrmSessionManager which is used by Exo-player to create the player instance
//     */
//    @NonNull
//    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession) throws UnsupportedDrmException {
//        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, buildDataSourceFactory());
//        if (keyRequestPropertiesArray != null) {
//            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
//                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
//                        keyRequestPropertiesArray[i + 1]);
//            }
//        }
//        releaseMediaDrm();
//        mediaDrm = FrameworkMediaDrm.newInstance(uuid);
//        return new DefaultDrmSessionManager<>(uuid, mediaDrm, drmCallback, null, multiSession);
//    }
//
//    /**
//     * @return This will return AdaptiveTrackSelectionFactory which is used for creating DefaultTrackSelector
//     */
//    @NonNull
//    private TrackSelection.Factory getAdaptiveTrackSelectionFactory(Activity activity) {
//        return new AdaptiveTrackSelection.Factory(getBandwidthMeter(activity));
//    }
//
//    /**
//     * @param playerViewModel
//     * @return This Method is used to build the DRM Session Manager.
//     */
//    private DefaultDrmSessionManager<FrameworkMediaCrypto> getDefaultDrmSessionManager(@NotNull Fragment fragment, @NotNull PlayerModel playerViewModel) {
//        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
//        if (playerViewModel.isEncrypted()) {
//            String drmLicenseUrl = playerViewModel.getDrmLicenseUrl();
//            String[] keyRequestPropertiesArray = null;
//            try {
//                UUID drmSchemeUuid = Util.getDrmUuid(playerViewModel.getDrmType());
//                if (drmSchemeUuid != null) {
//                    drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, drmLicenseUrl, keyRequestPropertiesArray, false);
//                    drmSessionManager.addListener(mDrmHandler, new DrmManagerListener((PlayerListener.DefaultDrmSessionEventListener) fragment));
//                }
//            } catch (UnsupportedDrmException e) {
//
//            }
//        }
//        return drmSessionManager;
//    }
//
//
//    /**
//     * @return OkHttpDataSourceFactory which is used in DRM callbacks and used to create MediaSourceFactory
//     */
//    @NotNull
//    private HttpDataSource.Factory buildDataSourceFactory() {
//        if (factory != null)
//            return factory;
//        factory = new OkHttpDataSourceFactory(getHttpFactory(), userAgent);
//        if (header_key_requests != null) {
//            for (int i = 0; i < header_key_requests.length - 1; i += 2) {
//                factory.setDefaultRequestProperty(header_key_requests[i], header_key_requests[i + 1]);
//            }
//        }
//        return factory;
//    }
//
//    /**
//     * @return mOkHttpClient which is mainly used in buildDataSourceFactory for Intercepting Response of every request initiated by Exo player
//     */
//    public Call.Factory getHttpFactory() {
//        if (mOkHttpClient == null) {
//            OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                    .writeTimeout(PLAYER_DEFAULT_TIMEOUT, TimeUnit.SECONDS)
//                    .readTimeout(PLAYER_DEFAULT_TIMEOUT, TimeUnit.SECONDS)
//                    .connectTimeout(PLAYER_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//            builder.protocols(Arrays.asList(Protocol.HTTP_1_1));
//            builder.addInterceptor(new HttpLoggingInterceptor(new DeviceRegistrationListener() {
//                @Override
//                public void onDeviceIdReceived(String deviceId) {
////                    if(deviceRegistrationListener!=null)
////                        deviceRegistrationListener.onDeviceIdReceived(deviceId);
//                    d(TAG, "DeviceRegistrationListener: deviceId " + deviceId);
//                }
//
//                @Override
//                public void onRRMError(String rrmError) {
////                    if(deviceRegistrationListener!=null && rrmError!=null)
////                        deviceRegistrationListener.onRRMError(rrmError);
//                    d(TAG, "DeviceRegistrationListener: rrmError " + rrmError);
//                }
//            }));
//            mOkHttpClient = builder.build();
//        }
//        return mOkHttpClient;
//    }
//
//    /**
//     * @return DefaultBandwidthMeter which is used in creation of TrackSelection and DataSource in following methods getAdaptiveTrackSelectionFactory and buildDataSourceFactory
//     */
//    public DefaultBandwidthMeter getBandwidthMeter(Activity activity) {
//        if (defaultBandwidthMeter != null)
//            return defaultBandwidthMeter;
//        DefaultBandwidthMeter.Builder builder = new DefaultBandwidthMeter.Builder(activity);
//
////        DefaultBandwidthMeter.Builder builder = new DefaultBandwidthMeter.Builder();
////        builder.setEventListener(mDrmHandler,new BandwidthMeterListener());
//        defaultBandwidthMeter = builder.build();
//        return defaultBandwidthMeter;
//    }
//
//
//    /**
//     * @param uri
//     * @param overrideExtension
//     * @return MediaSource which is used in prepare of Exo-player
//     */
//    @SuppressWarnings("unchecked")
//    private MediaSource buildMediaSource(Context context, Uri uri, @Nullable String overrideExtension) {
//        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
//        switch (type) {
//            case C.TYPE_DASH:
//                DashMediaSource mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(buildDataSourceFactory()),
//                        buildDataSourceFactory())
//                        .setManifestParser(
//                                new FilteringManifestParser<>(
//                                        new DashManifestParser(), null))
//                        .createMediaSource(uri);
//                mediaSource.addEventListener(mDrmHandler, new MediaSourceListener());
//                return mediaSource;
//            case C.TYPE_HLS: {
//                /*DataSource.Factory dataSourceFactory =
//                        new DefaultHttpDataSourceFactory(userAgent);*/
//
////              Create a HLS media source pointing to a playlist uri.
//                DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
//                // Produces DataSource instances through which media data is loaded.
//                DefaultDataSourceFactory mDataSourceFactory = new DefaultDataSourceFactory(
//                        context,
//                        userAgent, bandwidthMeter
//                );
//                HlsMediaSource hlsMediaSource =
//                        new HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
//                hlsMediaSource.addEventListener(mDrmHandler, new MediaSourceListener());
//                return hlsMediaSource;
//            }
//            default: {
//                throw new IllegalStateException("Unsupported type: " + type);
//            }
//        }
//    }
//
//    /**
//     * This is used to Release the player and update the depending variables.
//     */
//    protected void releasePlayer() {
//        if (player != null) {
//            updateTrackSelectorParameters();
//            updateStartPosition();
//            player.release();
//            player = null;
//            mediaSource = null;
//            trackSelector = null;
//        }
//        releaseMediaDrm();
//    }
//
//    protected void pausePlayer() {
//        if (player != null) {
//            player.setPlayWhenReady(false);
//            player.getPlaybackState();
//        }
//    }
//
//    protected void startPlayer() {
//        if (player != null) {
//            player.setPlayWhenReady(true);
//            player.getPlaybackState();
//        }
//    }
//
//    /**
//     * This is used for release the DRM used after player release
//     */
//    private void releaseMediaDrm() {
//        if (mediaDrm != null) {
//            mediaDrm.release();
//            mediaDrm = null;
//        }
//    }
//
//    /**
//     * This is used for updating the trackSelectorParameters which is used in player release
//     */
//    protected void updateTrackSelectorParameters() {
//        if (trackSelector != null) {
//            trackSelectorParameters = trackSelector.getParameters();
//        }
//    }
//
//    /**
//     * This is used to update the player position used when the application save instance is called
//     */
//    protected void updateStartPosition() {
//        if (player != null) {
//            startAutoPlay = player.getPlayWhenReady();
//            startWindow = player.getCurrentWindowIndex();
//            startPosition = Math.max(0, player.getContentPosition());
//        }
//    }
//
//    /**
//     * This will clear the or reset the player to initial position used in onSaveInstance
//     */
//    protected void clearStartPosition() {
//        startAutoPlay = true;
//        startWindow = C.INDEX_UNSET;
//        startPosition = C.TIME_UNSET;
//    }
//
//    /**
//     * @param savedInstanceState This is for handling the saveInstance of player.
//     */
//    protected void onSaveInstance(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
//            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
//            startWindow = savedInstanceState.getInt(KEY_WINDOW);
//            startPosition = savedInstanceState.getLong(KEY_POSITION);
//        } else {
//            trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
////            trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().setPreferredAudioLanguage("hin")
////                    .build();
//            clearStartPosition();
//        }
//
//    }
//
//    /**
//     * @param outState This is used to save the instance of the player.
//     */
//    protected void onActivitySaveInstance(Bundle outState) {
//        updateTrackSelectorParameters();
//        updateStartPosition();
//        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
//        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
//        outState.putInt(KEY_WINDOW, startWindow);
//        outState.putLong(KEY_POSITION, startPosition);
//    }
//
//    public LoadControl getLoadControl() {
//        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
//        builder.setBufferDurationsMs(15000, 500000, 2500, 15000);
//        builder.setPrioritizeTimeOverSizeThresholds(true);
//        DefaultLoadControl.Builder builder1 = builder.setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE));
//        return builder1.createDefaultLoadControl();
//    }
//
//    protected VideoQuality getAvailableVideoQuality() {
//        return videoQuality;
//    }
//
//    /*private void initVideoQuality(Activity activity, PlayerModel playerViewModel){
//        boolean isHd = playerViewModel.isHd();
//        ConfigResponse configModel = new SharedPrefs(activity).getConfigResponse();
//        BitrateData bitrate = configModel.getData().getConfig().getBitrate();
//        videoQuality = new VideoQuality();
//        ArrayList<Bitrate> bitrates = new ArrayList<>();
//        Hd hd = bitrate != null ? bitrate.getVod().getHd() : null;
//        Sd sd = bitrate.getVod().getSd();
//        if (isHd && hd != null){
//            bitrates = getBitrateArrayList(hd.getHigh(),hd.getMedium(),hd.getLow());
//        }else if(sd != null){
//            bitrates = getBitrateArrayList(sd.getHigh(),sd.getMedium(),sd.getLow());
//        }
//        videoQuality.setBitrateArrayList(bitrates);
//        videoQuality.setSelectedQualityIndex(0);
//    }*/
//
//    /*@NonNull
//    private ArrayList<Bitrate> getBitrateArrayList(long high, long medium, long low) {
//        ArrayList<Bitrate> bitrates = new ArrayList<>();
//        bitrates.add(new Bitrate("Auto",0, -1,-1));
//        bitrates.add(new Bitrate("High (1080p)",high, -1,-1));
//        bitrates.add(new Bitrate("Medium (480p)",medium, -1,-1));
//        bitrates.add(new Bitrate("Low (144p)",low, -1,-1));
//        return bitrates;
//    }*/
//
//
//    protected AudioLanguage getAvailableLanguage() {
//        return null;
//    }
//
//    public void initVideo(@org.jetbrains.annotations.Nullable TrackGroupArray trackGroups, @org.jetbrains.annotations.Nullable TrackSelectionArray trackSelections) {
//        if(videoQuality != null) return;
//
//        ArrayList<Bitrate> bitrates = new ArrayList<>();
//        bitrates.add(new Bitrate(AppConstantsKt.QUALITY_AUTO,0, -1, -1));
//        if(trackGroups != null)
//            for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
//                TrackGroup group = trackGroups.get(groupIndex);
//                int prevBitrate = 0;
//                for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
//                    Format format = group.getFormat(trackIndex);
//                    e("ExoPlayerImpl", "trackIndex:"+trackIndex+", format : " + format.toString());
//                    if("video/avc".equalsIgnoreCase(format.sampleMimeType)) {
//                        if (prevBitrate != format.height) {
//                            prevBitrate = format.height;
//                            String quality = " (" + (format.height) + BITRATE_SUFFIX + ")";
//                            bitrates.add(new Bitrate(quality, format.bitrate / 1000, trackIndex, groupIndex));
//                        }
//                    }
//                }
//            }
//        videoQuality = new VideoQuality();
//        videoQuality.setBitrateArrayList(bitrates);
//        videoQuality.setSelectedQualityIndex(0);
//    }
//
//    public long getBufferedPosition(){
//        if (player != null)  return player.getBufferedPosition();
//        return 0L;
//    }
//}
