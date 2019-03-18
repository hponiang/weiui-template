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
WX_EXPORT_METHOD(@selector(setTitle:callback:noopParam:))

- (void)setTitle:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setTitle:params callback:callback];
}

- (void)setTitle:(id)params callback:(WXModuleKeepAliveCallback)callback noopParam:(id)noopParam
{
    [[WeiuiNewPageManager sharedIntstance] setTitle:params callback:callback];
}

WX_EXPORT_METHOD(@selector(setLeftItem:callback:))
WX_EXPORT_METHOD(@selector(setLeftItem:callback:noopParam:))

- (void)setLeftItem:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setLeftItems:params callback:callback];
}

- (void)setLeftItem:(id)params callback:(WXModuleKeepAliveCallback)callback noopParam:(id)noopParam
{
    [[WeiuiNewPageManager sharedIntstance] setLeftItems:params callback:callback];
}

WX_EXPORT_METHOD(@selector(setRightItem:callback:))
WX_EXPORT_METHOD(@selector(setRightItem:callback:noopParam:))

- (void)setRightItem:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    [[WeiuiNewPageManager sharedIntstance] setRightItems:params callback:callback];
}

- (void)setRightItem:(id)params callback:(WXModuleKeepAliveCallback)callback noopParam:(id)noopParam
{
    [[WeiuiNewPageManager sharedIntstance] setRightItems:params callback:callback];
}

WX_EXPORT_METHOD(@selector(show))
WX_EXPORT_METHOD(@selector(hide))

- (void)show
{
    [[WeiuiNewPageManager sharedIntstance] showNavigation];
}

- (void)hide
{
    [[WeiuiNewPageManager sharedIntstance] hideNavigation];
}

@end
