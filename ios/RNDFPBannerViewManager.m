#import "RNDFPBannerViewManager.h"
#import "RNDFPBannerView.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <React/RCTEventDispatcher.h>
#else
#import "RCTBridge.h"
#import "RCTUIManager.h"
#import "RCTEventDispatcher.h"
#endif

@implementation RNDFPBannerViewManager

RCT_EXPORT_MODULE();

- (UIView *)view
{
  return [[RNDFPBannerView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(bannerSize, NSString *)
RCT_EXPORT_VIEW_PROPERTY(adUnitID, NSString *)
RCT_EXPORT_VIEW_PROPERTY(testDeviceID, NSString *)

RCT_EXPORT_VIEW_PROPERTY(onAdmobDispatchAppEvent, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidReceiveAd, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillLeaveApplication, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillPresentScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidFailToReceiveAdWithError, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onSizeChange, RCTBubblingEventBlock)

@end
