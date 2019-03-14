//
//  WeiuiNavigatorModule.m
//  Pods
//
//  Created by 高一 on 2019/3/13.
//

#import "WeiuiNavigatorModule.h"
#import "WeiuiNewPageManager.h"

@implementation WeiuiNavigatorModule

@synthesize weexInstance;

WX_EXPORT_METHOD(@selector(push:callback:))
WX_EXPORT_METHOD(@selector(pop:callback:))

- (void)push:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    if (![params isKindOfClass:[NSDictionary class]]) {
        return;
    }
    NSMutableDictionary *info = [params mutableCopy];
    info[@"pageTitle"] = info[@"pageTitle"] ? [WXConvert NSString:info[@"pageTitle"]] : info[@"url"];
    [WeiuiNewPageManager sharedIntstance].weexInstance = weexInstance;
    [[WeiuiNewPageManager sharedIntstance] openPage:info callback:callback];
}

- (void)pop:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    if (![params isKindOfClass:[NSDictionary class]]) {
        return;
    }
    [[WeiuiNewPageManager sharedIntstance] setPageStatusListener:nil callback:callback];
    [[WeiuiNewPageManager sharedIntstance] closePage:nil];
}

@end
