//
//  ViewController.m
//  WeexWeiui
//
//  Created by 高一 on 2018/8/15.
//

#import "ViewController.h"

#import "WeexSDK.h"
#import "WeexSDKManager.h"
#import "WXMainViewController.h"
#import "WeiuiNewPageManager.h"
#import "Config.h"
#import "Cloud.h"

@interface ViewController ()

@property (nonatomic, assign) BOOL ready;

@end

@implementation ViewController

WXMainViewController *homeController;

- (void) viewDidLoad {
    [super viewDidLoad];
    
    NSString *bundleUrl = [Config getHome];
    
    [WeexSDKManager sharedIntstance].weexUrl = bundleUrl;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [[WeexSDKManager sharedIntstance] setup];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)([Cloud welcome:nil] * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            self.ready = YES;
            homeController = [[WXMainViewController alloc] init];
            homeController.url = bundleUrl;
            homeController.pageName = [Config getHomeParams:@"params" defaultVal:@"firstPage"];
            homeController.pageType = [Config getHomeParams:@"pageType" defaultVal:@"weex"];
            homeController.params = [Config getHomeParams:@"params" defaultVal:@"{}"];
            homeController.cache = [[Config getHomeParams:@"cache" defaultVal:@"0"] intValue];
            homeController.loading = [[Config getHomeParams:@"loading" defaultVal:@"true"] isEqualToString:@"true"] ? YES : NO;
            homeController.isDisSwipeBack = YES;
            homeController.statusBarType = [Config getHomeParams:@"statusBarType" defaultVal:@"normal"];
            homeController.statusBarColor = [Config getHomeParams:@"statusBarColor" defaultVal:@"#3EB4FF"];
            homeController.statusBarAlpha = [[Config getHomeParams:@"statusBarAlpha" defaultVal:@"0"] intValue];
            homeController.backgroundColor = [Config getHomeParams:@"backgroundColor" defaultVal:@"#f4f8f9"];
            homeController.statusBlock = ^(NSString *status) {
                if ([status isEqualToString:@"create"]) {
                    [Cloud appData];
                }
            };
            [[UIApplication sharedApplication] delegate].window.rootViewController =  [[WXRootViewController alloc] initWithRootViewController:homeController];
            [[WeiuiNewPageManager sharedIntstance] setPageData:homeController.pageName vc:homeController];
        });
    });
}

- (void) loadUrl:(NSString*) url {
    [WeexSDKManager sharedIntstance].weexUrl = url;
    [homeController setHomeUrl: url];
    [homeController refreshPage];
}

- (BOOL) isReady {
    return self.ready;
}

@end
