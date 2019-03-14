//
//  WeiuiNavigationBarModule.m
//  Pods
//
//  Created by 高一 on 2019/3/13.
//

#import "WeiuiNavigationBarModule.h"
#import "WeiuiNewPageManager.h"

@implementation WeiuiNavigationBarModule

@synthesize weexInstance;

WX_EXPORT_METHOD(@selector(setTitle:callback:))
WX_EXPORT_METHOD(@selector(setLeftItems:callback:))
WX_EXPORT_METHOD(@selector(setRightItems:callback:))

- (void)setTitle:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setTitle:params callback:callback];
}

- (void)setLeftItems:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setLeftItems:params callback:callback];
}

- (void)setRightItems:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setRightItems:params callback:callback];
}

@end
