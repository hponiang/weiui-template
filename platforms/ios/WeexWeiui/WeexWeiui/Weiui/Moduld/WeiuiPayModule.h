
#import <Foundation/Foundation.h>
#import "WeexSDK.h"

@interface WeiuiPayModule : NSObject <WXModuleProtocol>

+ (void)alipayHandleOpenURL:(NSURL *) url;
+ (BOOL)weixinHandleOpenURL:(NSURL *) url;

@end
