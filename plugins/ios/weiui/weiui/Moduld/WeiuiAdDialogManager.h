//
//  WeiuiAdDialogManager.h
//  WeexTestDemo
//
//  Created by apple on 2018/6/5.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "WeexSDK.h"

@interface WeiuiAdDialogManager : NSObject

@property (nonatomic, strong) NSMutableDictionary *adDialogDic;
@property (nonatomic, strong) NSMutableArray *adDialogList;

+ (WeiuiAdDialogManager *)sharedIntstance;

- (void)adDialog:(id)param callback:(WXModuleKeepAliveCallback)callback;

- (void)adDialogClose:(NSString*)dialogName;

@end
