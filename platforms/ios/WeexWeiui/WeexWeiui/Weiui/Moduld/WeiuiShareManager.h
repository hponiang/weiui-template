//
//  WeiuiShareManager.h
//  WeexTestDemo
//
//  Created by apple on 2018/6/7.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "WeexSDK.h"

@interface WeiuiShareManager : NSObject

+ (WeiuiShareManager *)sharedIntstance;

- (void)shareText:(NSString*)text;
- (void)shareImage:(NSString*)imgUrl;

@end
