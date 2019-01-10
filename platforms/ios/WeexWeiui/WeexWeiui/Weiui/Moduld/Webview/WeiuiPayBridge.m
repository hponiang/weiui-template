//
//  WeiuiPayBridge.m
//  WeexWeiui
//
//  Created by 高一 on 2019/1/6.
//

#import "WeiuiPayBridge.h"
#import "WeiuiPayModule.h"

@interface WeiuiPayBridge ()

@property (nonatomic, strong) WeiuiPayModule *pay;

@end

@implementation WeiuiPayBridge

- (void)initialize
{
    if (self.pay == nil) {
        self.pay = [[WeiuiPayModule alloc] init];
    }
}

- (void)weixin:(NSDictionary *)payData callback:(WXModuleKeepAliveCallback)callback
{
    [self.pay weixin:payData callback:callback];
}

- (void)alipay:(NSString*)payData callback:(WXModuleKeepAliveCallback)callback
{
    [self.pay alipay:payData callback:callback];
}

- (void)union_weixin:(NSString*)payData
{
    [self.pay union_weixin:payData];
}

- (void)union_alipay:(NSString*)payData
{
    [self.pay union_alipay:payData];
}

@end
