//
//  NavigationBarBridge.m
//  Pods
//
//  Created by 高一 on 2019/3/16.
//

#import "NavigationBarBridge.h"
#import "WeiuiNewPageManager.h"

@implementation NavigationBarBridge

- (void)setTitle:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setTitle:params callback:callback];
}

- (void)setLeftItem:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setLeftItems:params callback:callback];
}

- (void)setRightItem:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setRightItems:params callback:callback];
}

- (void)show
{
    [[WeiuiNewPageManager sharedIntstance] showNavigation];
}

- (void)hide
{
    [[WeiuiNewPageManager sharedIntstance] hideNavigation];
}

@end
