//
//  WeexSDKManager.m
//  WeexDemo
//
//  Created by yangshengtao on 16/11/14.
//  Copyright © 2016年 taobao. All rights reserved.
//

#import "WeexSDKManager.h"
#import "WeexSDK.h"
#import "WXMainViewController.h"
#import "WXImgLoaderDefaultImpl.h"

@implementation WeexSDKManager

+ (WeexSDKManager *)sharedIntstance {
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (instancetype)init
{
    if (self = [super init]) {
        self.cacheData = [NSMutableDictionary dictionaryWithCapacity:5];
    }
    
    return self;
}

- (void)setup
{
    [self initWeexSDK];
}

- (void)initWeexSDK
{
    [WXAppConfiguration setAppGroup:@"AliApp"];
    [WXAppConfiguration setAppName:@"WeexTestDemo"];
    [WXAppConfiguration setAppVersion:@"1.0.0"];
//    [WXAppConfiguration setExternalUserAgent:@"ExternalUA"];
    
    [WXSDKEngine initSDKEnvironment];
    
    //Handler
    [WXSDKEngine registerHandler:[WXImgLoaderDefaultImpl new] withProtocol:@protocol(WXImgLoaderProtocol)];

    //Module
    [WXSDKEngine registerModule:@"weiui" withClass:NSClassFromString(@"WeiuiModule")];
    [WXSDKEngine registerModule:@"weiui_citypicker" withClass:NSClassFromString(@"WeiuiCityPickerModule")];
    [WXSDKEngine registerModule:@"weiui_picture" withClass:NSClassFromString(@"WeiuiPictureSelectorModule")];
    [WXSDKEngine registerModule:@"weiui_umeng" withClass:NSClassFromString(@"WeiuiUmengModule")];
    [WXSDKEngine registerModule:@"weiui_rongim" withClass:NSClassFromString(@"WeiuiRongcloudModule")];
    [WXSDKEngine registerModule:@"weiui_pay" withClass:NSClassFromString(@"WeiuiPayModule")];

    //Component
    [WXSDKEngine registerComponent:@"weiui_button"
                         withClass:NSClassFromString(@"WeiuiButtonComponent")];
    [WXSDKEngine registerComponent:@"weiui_navbar_item"
                         withClass:NSClassFromString(@"WeiuiNavbarItemComponent")];
    [WXSDKEngine registerComponent:@"weiui_navbar"
                         withClass:NSClassFromString(@"WeiuiNavbarComponent")];
    [WXSDKEngine registerComponent:@"weiui_banner"
                         withClass:NSClassFromString(@"WeiuiBannerComponent")];
    [WXSDKEngine registerComponent:@"weiui_icon"
                         withClass:NSClassFromString(@"WeiuiIconComponent")];
    [WXSDKEngine registerComponent:@"weiui_marquee"
                         withClass:NSClassFromString(@"WeiuiMarqueeComponent")];
    [WXSDKEngine registerComponent:@"weiui_scroll_text"
                         withClass:NSClassFromString(@"WeiuiScrollTextComponent")];
    [WXSDKEngine registerComponent:@"weiui_webview"
                         withClass:NSClassFromString(@"WeiuiWebviewComponent")];
    [WXSDKEngine registerComponent:@"weiui_tabbar"
                         withClass:NSClassFromString(@"WeiuiTabbarComponent")];
    [WXSDKEngine registerComponent:@"weiui_tabbar_page"
                         withClass:NSClassFromString(@"WeiuiTabbarPageComponent")];
    [WXSDKEngine registerComponent:@"weiui_side_panel"
                         withClass:NSClassFromString(@"WeiuiSidePanelComponent")];
    [WXSDKEngine registerComponent:@"weiui_side_panel_menu"
                         withClass:NSClassFromString(@"WeiuiSidePanelItemComponent")];
    [WXSDKEngine registerComponent:@"weiui_grid"
                         withClass:NSClassFromString(@"WeiuiGridComponent")];
    [WXSDKEngine registerComponent:@"weiui_recyler"
                         withClass:NSClassFromString(@"WeiuiRecylerComponent")];
    [WXSDKEngine registerComponent:@"weiui_list"
                         withClass:NSClassFromString(@"WeiuiRecylerComponent")];
    [WXSDKEngine registerComponent:@"weiui_ripple"
                         withClass:NSClassFromString(@"WeiuiRippleComponent")];
    [WXSDKEngine registerComponent:@"ripple"
                         withClass:NSClassFromString(@"WeiuiRippleComponent")];
    
#ifdef DEBUG
    [WXLog setLogLevel:WXLogLevelLog];
#endif
}


@end
