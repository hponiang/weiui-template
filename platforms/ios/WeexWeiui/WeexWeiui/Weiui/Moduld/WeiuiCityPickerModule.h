//
//  WeiuiCityPickerModule.h
//  WeexTestDemo
//
//  Created by apple on 2018/6/8.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "WeexSDK.h"

@interface WeiuiCityPickerModule : NSObject <WXModuleProtocol>

- (void)select:(NSDictionary*)params callback:(WXModuleKeepAliveCallback)callback;

@end
