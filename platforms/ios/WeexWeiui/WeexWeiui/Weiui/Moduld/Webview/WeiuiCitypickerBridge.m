//
//  WeiuiCitypickerBridge.m
//  WeexWeiui
//
//  Created by 高一 on 2019/1/6.
//

#import "WeiuiCitypickerBridge.h"
#import "WeiuiCityPickerModule.h"

@interface WeiuiCitypickerBridge ()

@property (nonatomic, strong) WeiuiCityPickerModule *city;

@end

@implementation WeiuiCitypickerBridge

- (void)initialize
{
    if (self.city == nil) {
        self.city = [[WeiuiCityPickerModule alloc] init];
    }
}

- (void)select:(NSDictionary*)params callback:(WXModuleKeepAliveCallback)callback
{
    [self.city select:params callback:callback];
}

@end
