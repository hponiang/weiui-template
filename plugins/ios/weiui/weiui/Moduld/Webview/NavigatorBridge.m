//
//  NavigatorBridge.m
//  Pods
//
//  Created by 高一 on 2019/3/16.
//

#import "NavigatorBridge.h"
#import "WeiuiNewPageManager.h"
#import "DeviceUtil.h"
#import "WXConvert.h"

@implementation NavigatorBridge

- (void)push:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    NSMutableDictionary *info = [[NSMutableDictionary alloc] init];
    if ([params isKindOfClass:[NSString class]]) {
        info[@"url"] = params;
    }else if (![params isKindOfClass:[NSDictionary class]]) {
        return;
    }
    info = [params mutableCopy];
    info[@"pageTitle"] = info[@"pageTitle"] ? [WXConvert NSString:info[@"pageTitle"]] : @" ";
    [WeiuiNewPageManager sharedIntstance].weexInstance = [[WXSDKManager bridgeMgr] topInstance];
    [[WeiuiNewPageManager sharedIntstance] openPage:info callback:callback];
}

- (void)pop:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    NSMutableDictionary *info = [[NSMutableDictionary alloc] init];
    NSString *name = @"";
    if ([params isKindOfClass:[NSString class]]) {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    } else if ([params isKindOfClass:[NSDictionary class]]) {
        info = [[NSMutableDictionary alloc] initWithDictionary:params];
        name = params[@"pageName"] ? [WXConvert NSString:params[@"pageName"]] : [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    info[@"pageName"] = name;
    if (callback) {
        info[@"listenerName"] = @"__navigatorPop";
        [[WeiuiNewPageManager sharedIntstance] setPageStatusListener:info callback:callback];
    }
    [[WeiuiNewPageManager sharedIntstance] closePage:info];
}

@end

