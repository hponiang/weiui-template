//
//  WeiuiPictureSelectorModule.h
//  WeexTestDemo
//
//  Created by apple on 2018/6/8.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "WeexSDK.h"

@interface WeiuiPictureSelectorModule : NSObject <WXModuleProtocol>

- (void)create:(NSDictionary*)params callback:(WXModuleKeepAliveCallback)callback;
- (void)compressImage:(NSDictionary*)params callback:(WXModuleKeepAliveCallback)callback;
- (void)picturePreview:(NSInteger)index paths:(NSArray*)paths callback:(WXModuleKeepAliveCallback)callback;
- (void)videoPreview:(NSString*)path;
- (void)deleteCache;

@end
