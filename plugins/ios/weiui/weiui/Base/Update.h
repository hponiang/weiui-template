//
//  Update.h
//  WeexWeiui
//
//  Created by 高一 on 2018/9/27.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Update : NSObject

+(BOOL) zipToDist:(NSString *)zipFile zipUnDir:(NSString *)zipUnDir;
+(BOOL) weiuiToDist;

@end

NS_ASSUME_NONNULL_END
