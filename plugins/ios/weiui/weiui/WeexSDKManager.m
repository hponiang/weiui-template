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

+ (WeexSDKManager *)sharedIntstance
{
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
    [WXAppConfiguration setAppName:@"WEIUI"];
    [WXAppConfiguration setAppGroup:@"WEIUI"];
    
    [WXSDKEngine initSDKEnvironment];
    
    //Handler
    [WXSDKEngine registerHandler:[WXImgLoaderDefaultImpl new] withProtocol:@protocol(WXImgLoaderProtocol)];

    //Module
    [WXSDKEngine registerModule:@"weiui" withClass:NSClassFromString(@"WeiuiModule")];
    [WXSDKEngine registerModule:@"navigator" withClass:NSClassFromString(@"WeiuiNavigatorModule")];
    [WXSDKEngine registerModule:@"navigationBar" withClass:NSClassFromString(@"WeiuiNavigationBarModule")];

    //Component
    [WXSDKEngine registerComponent:@"banner" withClass:NSClassFromString(@"WeiuiBannerComponent")];
    [WXSDKEngine registerComponent:@"button" withClass:NSClassFromString(@"WeiuiButtonComponent")];
    [WXSDKEngine registerComponent:@"grid" withClass:NSClassFromString(@"WeiuiGridComponent")];
    [WXSDKEngine registerComponent:@"icon" withClass:NSClassFromString(@"WeiuiIconComponent")];
    [WXSDKEngine registerComponent:@"marquee" withClass:NSClassFromString(@"WeiuiMarqueeComponent")];
    [WXSDKEngine registerComponent:@"navbar" withClass:NSClassFromString(@"WeiuiNavbarComponent")];
    [WXSDKEngine registerComponent:@"navbar-item" withClass:NSClassFromString(@"WeiuiNavbarItemComponent")];
    [WXSDKEngine registerComponent:@"ripple" withClass:NSClassFromString(@"WeiuiRippleComponent")];
    [WXSDKEngine registerComponent:@"scroll-text" withClass:NSClassFromString(@"WeiuiScrollTextComponent")];
    [WXSDKEngine registerComponent:@"scroll-view" withClass:NSClassFromString(@"WeiuiRecylerComponent")];
    [WXSDKEngine registerComponent:@"side-panel" withClass:NSClassFromString(@"WeiuiSidePanelComponent")];
    [WXSDKEngine registerComponent:@"side-panel-menu" withClass:NSClassFromString(@"WeiuiSidePanelItemComponent")];
    [WXSDKEngine registerComponent:@"tabbar" withClass:NSClassFromString(@"WeiuiTabbarComponent")];
    [WXSDKEngine registerComponent:@"tabbar-page" withClass:NSClassFromString(@"WeiuiTabbarPageComponent")];
    [WXSDKEngine registerComponent:@"web-view" withClass:NSClassFromString(@"WeiuiWebviewComponent")];
    
#ifdef DEBUG
    [WXLog setLogLevel:WXLogLevelLog];
#endif
}


@end
