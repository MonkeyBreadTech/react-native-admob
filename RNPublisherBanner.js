import { requireNativeComponent, View, ViewPropTypes } from "react-native";
import PropTypes from "prop-types";

import React, { Component } from "react";

class PublisherBanner extends Component {
  state = { style: {} };

  _handleSizeChange = ({
    nativeEvent
  }: {
    nativeEvent: { width: number, height: number }
  }) => {
    const { height, width } = nativeEvent;
    this.setState({ style: { width, height } });
  };

  _handleDidFailToReceiveAdWithError = ({
    nativeEvent
  }: {
    nativeEvent: { error: string }
  }) =>
    this.props.onDidFailToReceiveAdWithError &&
    this.props.onDidFailToReceiveAdWithError(nativeEvent.error);

  render() {
    return (
      <View style={this.props.style}>
        <RNDFPBannerView
          style={this.state.style}
          adUnitID={this.props.adUnitID}
          bannerSize={this.props.bannerSize}
          testDeviceID={this.props.testDeviceID}
          onSizeChange={this._handleSizeChange}
          onAdViewDidReceiveAd={this.props.onAdViewDidReceiveAd}
          onDidFailToReceiveAdWithError={
            this._handleDidFailToReceiveAdWithError
          }
          onAdViewWillPresentScreen={this.props.onAdViewWillPresentScreen}
          onAdViewWillDismissScreen={this.props.onAdViewWillDismissScreen}
          onAdViewDidDismissScreen={this.props.onAdViewDidDismissScreen}
          onAdViewWillLeaveApplication={this.props.onAdViewWillLeaveApplication}
          onAdmobDispatchAppEvent={this.props.onAdMobDispatchAppEvent}
        />
      </View>
    );
  }
}

PublisherBanner.simulatorId = "SIMULATOR";

PublisherBanner.propTypes = {
  /**
   * DFP iOS library banner size constants
   * (https://developers.google.com/admob/ios/banner)
   * banner (320x50, Standard Banner for Phones and Tablets)
   * largeBanner (320x100, Large Banner for Phones and Tablets)
   * mediumRectangle (300x250, IAB Medium Rectangle for Phones and Tablets)
   * fullBanner (468x60, IAB Full-Size Banner for Tablets)
   * leaderboard (728x90, IAB Leaderboard for Tablets)
   * smartBannerPortrait (Screen width x 32|50|90, Smart Banner for Phones and Tablets)
   * smartBannerLandscape (Screen width x 32|50|90, Smart Banner for Phones and Tablets)
   *
   * banner is default
   */
  adSize: PropTypes.oneOf([
    "banner",
    "largeBanner",
    "mediumRectangle",
    "fullBanner",
    "leaderboard",
    "smartBannerPortrait",
    "smartBannerLandscape"
  ]),
  adUnitID: PropTypes.string,
  testDeviceID: PropTypes.string,
  onAdViewDidReceiveAd: PropTypes.func,
  onDidFailToReceiveAdWithError: PropTypes.func,
  onAdViewWillPresentScreen: PropTypes.func,
  onAdViewWillDismissScreen: PropTypes.func,
  onAdViewDidDismissScreen: PropTypes.func,
  onAdViewWillLeaveApplication: PropTypes.func,
  onAdmobDispatchAppEvent: PropTypes.func,
  ...ViewPropTypes
};

const RNDFPBannerView = requireNativeComponent(
  "RNDFPBannerView",
  PublisherBanner
);

export default PublisherBanner;
