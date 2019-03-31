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
#import "UINavigationController+FDFullscreenPopGesture.h"

@interface ViewController ()

@property (nonatomic, assign) BOOL ready;
@property (nonatomic, strong) UIActivityIndicatorView *activityIndicatorView;

@end

@implementation ViewController

WXMainViewController *homeController;

- (void) viewDidLoad {
    [super viewDidLoad];
    
    [self setFd_prefersNavigationBarHidden:YES];
    [self.view setBackgroundColor:[UIColor whiteColor]];
    
    self.activityIndicatorView = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(0, 0, 30, 30)];
    self.activityIndicatorView.center = self.view.center;
    [self.activityIndicatorView setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleGray];
    [self.view addSubview:self.activityIndicatorView];
    [self.activityIndicatorView setHidden:NO];
    [self.activityIndicatorView startAnimating];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)([Cloud welcome:nil] * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        NSString *bundleUrl = [Config getHome];
        [WeexSDKManager sharedIntstance].weexUrl = bundleUrl;
        [[WeexSDKManager sharedIntstance] setup];
        
        [self.activityIndicatorView setHidden:YES];
        [self.activityIndicatorView stopAnimating];
        
        self.ready = YES;
        homeController = [[WXMainViewController alloc] init];
        homeController.url = bundleUrl;
        homeController.pageName = [Config getHomeParams:@"pageName" defaultVal:@"firstPage"];
        homeController.pageTitle = [Config getHomeParams:@"pageTitle" defaultVal:@""];
        homeController.pageType = [Config getHomeParams:@"pageType" defaultVal:@"weex"];
        homeController.safeAreaBottom = [Config getHomeParams:@"safeAreaBottom" defaultVal:@""];
        homeController.params = [Config getHomeParams:@"params" defaultVal:@"{}"];
        homeController.cache = [[Config getHomeParams:@"cache" defaultVal:@"0"] intValue];
        homeController.loading = [[Config getHomeParams:@"loading" defaultVal:@"true"] isEqualToString:@"true"] ? YES : NO;
        homeController.isFirstPage = YES;
        homeController.isDisSwipeBack = YES;
        homeController.statusBarType = [Config getHomeParams:@"statusBarType" defaultVal:@"normal"];
        homeController.statusBarColor = [Config getHomeParams:@"statusBarColor" defaultVal:@"#3EB4FF"];
        homeController.statusBarAlpha = [[Config getHomeParams:@"statusBarAlpha" defaultVal:@"0"] intValue];
        homeController.statusBarStyleCustom = [Config getHomeParams:@"statusBarStyle" defaultVal:@""];
        homeController.backgroundColor = [Config getHomeParams:@"backgroundColor" defaultVal:@"#ffffff"];
        homeController.statusBlock = ^(NSString *status) {
            if ([status isEqualToString:@"create"]) {
                [Cloud appData];
            }
        };
        [[UIApplication sharedApplication] delegate].window.rootViewController =  [[WXRootViewController alloc] initWithRootViewController:homeController];
        [[WeiuiNewPageManager sharedIntstance] setPageData:homeController.pageName vc:homeController];
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
