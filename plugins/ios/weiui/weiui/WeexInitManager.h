//
//  WeexInitManager.h
//  weiui
//
//  Created by 高一 on 2019/3/1.
//

#import <Foundation/Foundation.h>
#import <UserNotifications/UserNotifications.h>

static NSMutableArray *init_lists;


#ifndef __WEIUI_WEEX_PLUGIN_MACRO_H__
#define __WEIUI_WEEX_PLUGIN_MACRO_H__

#define  WEEX_PLUGIN_INIT(ClassA) __WEEX_PLUGIN_INIT(ClassA)

#define __WEEX_PLUGIN_INIT(ClassA) \
void __attribute__ ((constructor)) WEEX_PLUGIN_INIT##ClassA##func(){  \
[WeexInitManager addInitClass: ClassA.class]; \
}

#endif

NS_ASSUME_NONNULL_BEGIN

@interface WeexInitManager : NSObject

+ (void) addInitClass:(Class)cls;
+ (void) didFinishLaunchingWithOptions:(NSDictionary *)launchOptions;
+ (void) didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
+ (void) didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
+ (void) didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler;
+ (void) willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler API_AVAILABLE(ios(10.0));
+ (void) didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler API_AVAILABLE(ios(10.0));
+ (void) openURL:(NSURL *)url options:(NSDictionary<NSString*, id> *)options;

@end

NS_ASSUME_NONNULL_END