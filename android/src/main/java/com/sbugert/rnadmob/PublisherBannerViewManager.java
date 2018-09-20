package com.sbugert.rnadmob;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;

import java.util.Map;

class PublisherBannerView extends ReactViewGroup implements AppEventListener {
    private String testDeviceID = null;

    public PublisherBannerView(@NonNull Context context) {
        super(context);
        attachNewAdView();
    }

    @Override
    public void onAppEvent(String name, String info) {
        String message = String.format("Received app event (%s, %s)", name, info);
        WritableMap event = Arguments.createMap();
        event.putString("name", name);
        event.putString("info", info);
        sendEvent(PublisherBannerViewManager.EVENT_ADMOB_EVENT_RECEIVED, event);
    }

    protected void attachNewAdView() {
        final PublisherAdView adView = new PublisherAdView(getContext());
        adView.setAppEventListener(this);
        // destroy old AdView if present
        PublisherAdView oldAdView = (PublisherAdView) getChildAt(0);
        removeAllViews();
        if (oldAdView != null) {
            oldAdView.destroy();
        }
        addView(adView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        attachEvents();
    }

    protected void attachEvents() {
        final PublisherAdView adView = (PublisherAdView) getChildAt(0);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                int width = adView.getAdSize().getWidthInPixels(getContext());
                int height = adView.getAdSize().getHeightInPixels(getContext());
                int left = adView.getLeft();
                int top = adView.getTop();
                adView.measure(width, height);
                adView.layout(left, top, left + width, top + height);
                sendOnSizeChangeEvent(adView);

                sendEvent(PublisherBannerViewManager.EVENT_RECEIVE_AD, null);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                String errorMessage = "Unknown error";
                switch (errorCode) {
                    case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                        errorMessage = "Internal error, an invalid response was received from the ad server.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                        errorMessage = "Invalid ad request, possibly an incorrect ad unit ID was given.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                        errorMessage = "The ad request was unsuccessful due to network connectivity.";
                        break;
                    case PublisherAdRequest.ERROR_CODE_NO_FILL:
                        errorMessage = "The ad request was successful, but no ad was returned due to lack of ad inventory.";
                        break;
                }
                WritableMap event = Arguments.createMap();
                WritableMap error = Arguments.createMap();
                error.putString("message", errorMessage);
                event.putMap("error", error);
                sendEvent(PublisherBannerViewManager.EVENT_ERROR, event);
            }

            @Override
            public void onAdOpened() {
                sendEvent(PublisherBannerViewManager.EVENT_WILL_PRESENT, null);
            }

            @Override
            public void onAdClosed() {
                sendEvent(PublisherBannerViewManager.EVENT_WILL_DISMISS, null);
            }

            @Override
            public void onAdLeftApplication() {
                sendEvent(PublisherBannerViewManager.EVENT_WILL_LEAVE_APP, null);
            }
        });
    }

    public void setBannerSize(final String sizeString) {
        AdSize adSize = getAdSizeFromString(sizeString);
        AdSize[] adSizes = new AdSize[1];
        adSizes[0] = adSize;

        // store old ad unit ID (even if not yet present and thus null)
        PublisherAdView oldAdView = (PublisherAdView) getChildAt(0);
        String adUnitId = oldAdView.getAdUnitId();

        attachNewAdView();
        PublisherAdView newAdView = (PublisherAdView) getChildAt(0);
        newAdView.setAdSizes(adSizes);
        newAdView.setAdUnitId(adUnitId);

        // send measurements to js to style the AdView in react
        sendOnSizeChangeEvent(newAdView);

        loadAd(newAdView);
    }

    public void setAdUnitID(final String adUnitID) {
        if (adUnitID != null) {
            // store old banner size (even if not yet present and thus null)
            PublisherAdView oldAdView = (PublisherAdView) getChildAt(0);
            AdSize[] adSizes = oldAdView.getAdSizes();

            if (adSizes != null && adSizes.length > 0) {
                attachNewAdView();
                PublisherAdView newAdView = (PublisherAdView) getChildAt(0);
                newAdView.setAdUnitId(adUnitID);
                newAdView.setAdSizes(adSizes);
                loadAd(newAdView);
            }
        }
    }

    public void setPropTestDeviceID(final String testDeviceID) {
        this.testDeviceID = testDeviceID;
    }

    private void loadAd(final PublisherAdView adView) {
        if (adView.getAdSizes() != null && adView.getAdUnitId() != null) {
            PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();
            if (testDeviceID != null) {
                if (testDeviceID.equals("EMULATOR")) {
                    adRequestBuilder = adRequestBuilder.addTestDevice(PublisherAdRequest.DEVICE_ID_EMULATOR);
                } else {
                    adRequestBuilder = adRequestBuilder.addTestDevice(testDeviceID);
                }
            }
            PublisherAdRequest adRequest = adRequestBuilder.build();
            adView.loadAd(adRequest);
        }
    }

    private void sendOnSizeChangeEvent(final PublisherAdView adView) {
        int width;
        int height;
        ReactContext reactContext = (ReactContext) getContext();
        WritableMap event = Arguments.createMap();
        AdSize adSize = adView.getAdSize();
        if (adSize == AdSize.SMART_BANNER) {
            width = (int) PixelUtil.toDIPFromPixel(adSize.getWidthInPixels(reactContext));
            height = (int) PixelUtil.toDIPFromPixel(adSize.getHeightInPixels(reactContext));
        } else {
            width = adSize.getWidth();
            height = adSize.getHeight();
        }
        event.putDouble("width", width);
        event.putDouble("height", height);
        sendEvent(PublisherBannerViewManager.EVENT_SIZE_CHANGE, event);
    }

    private void sendEvent(String name, @Nullable WritableMap event) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                name,
                event);
    }

    private AdSize getAdSizeFromString(String adSize) {
        switch (adSize) {
            case "banner":
                return AdSize.BANNER;
            case "largeBanner":
                return AdSize.LARGE_BANNER;
            case "mediumRectangle":
                return AdSize.MEDIUM_RECTANGLE;
            case "fullBanner":
                return AdSize.FULL_BANNER;
            case "leaderBoard":
                return AdSize.LEADERBOARD;
            case "smartBannerPortrait":
                return AdSize.SMART_BANNER;
            case "smartBannerLandscape":
                return AdSize.SMART_BANNER;
            case "smartBanner":
                return AdSize.SMART_BANNER;
            default:
                return AdSize.BANNER;
        }
    }
}

public class PublisherBannerViewManager extends SimpleViewManager<PublisherBannerView> {

    public static final String REACT_CLASS = "RNDFPBannerView";

    public static final String EVENT_SIZE_CHANGE = "onSizeChange";
    public static final String EVENT_RECEIVE_AD = "onAdViewDidReceiveAd";
    public static final String EVENT_ERROR = "onDidFailToReceiveAdWithError";
    public static final String EVENT_WILL_PRESENT = "onAdViewWillPresentScreen";
    public static final String EVENT_WILL_DISMISS = "onAdViewWillDismissScreen";
    public static final String EVENT_DID_DISMISS = "onAdViewDidDismissScreen";
    public static final String EVENT_WILL_LEAVE_APP = "onAdViewWillLeaveApplication";
    public static final String EVENT_ADMOB_EVENT_RECEIVED = "onAdmobDispatchAppEvent";

    public static final String PROP_BANNER_SIZE = "bannerSize";
    public static final String PROP_AD_UNIT_ID = "adUnitID";
    public static final String PROP_TEST_DEVICE_ID = "testDeviceID";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected PublisherBannerView createViewInstance(ThemedReactContext themedReactContext) {
        return new PublisherBannerView(themedReactContext);
    }

    @Override
    public Map getExportedCustomBubblingEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        String[] events = {
                EVENT_SIZE_CHANGE,
                EVENT_RECEIVE_AD,
                EVENT_ERROR,
                EVENT_WILL_PRESENT,
                EVENT_WILL_DISMISS,
                EVENT_DID_DISMISS,
                EVENT_WILL_LEAVE_APP,
                EVENT_ADMOB_EVENT_RECEIVED
        };
        for (int i = 0; i < events.length; i++) {
            builder.put(
                    events[i],
                    MapBuilder.of(
                            "phasedRegistrationNames",
                            MapBuilder.of("bubbled", events[i])));
        }
        return builder.build();
    }

    @ReactProp(name = PROP_BANNER_SIZE)
    public void setBannerSize(PublisherBannerView view, final String sizeString) {
        view.setBannerSize(sizeString);
    }

    @ReactProp(name = PROP_AD_UNIT_ID)
    public void setAdUnitID(PublisherBannerView view, final String adUnitID) {
        view.setAdUnitID(adUnitID);
    }

    @ReactProp(name = PROP_TEST_DEVICE_ID)
    public void setPropTestDeviceID(PublisherBannerView view, final String testDeviceID) {
        view.setPropTestDeviceID(testDeviceID);
    }
}
