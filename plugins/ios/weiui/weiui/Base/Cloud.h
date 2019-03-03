//
//  Cloud.h
//  WeexWeiui
//
//  Created by 高一 on 2018/9/27.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Cloud : NSObject

+ (NSInteger) welcome:(nullable UIView *) view;
+ (void) welcomeClose;
+ (void) appData;
+ (void) saveWelcomeImage:(NSString*)url wait:(NSInteger)wait;
+ (void) checkUpdateLists:(NSMutableArray*)lists number:(NSInteger)number isReboot:(BOOL)isReboot;
+ (void) reboot;
+ (void) clearUpdate;

@end

NS_ASSUME_NONNULL_END
