//
//  WXMainViewController.m
//  WeexTestDemo
//
//  Created by apple on 2018/5/31.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import "WXMainViewController.h"
#import "WeexSDK.h"
#import "WeexSDKManager.h"
#import "weiuiNewPageManager.h"
#import "UINavigationController+FDFullscreenPopGesture.h"
#import "CustomWeexSDKManager.h"

#define kCacheUrl @"cache_url"
#define kCacheTime @"cache_time"

#define kLifeCycle @"lifecycle"//生命周期

@interface WXMainViewController ()<UIWebViewDelegate, UIGestureRecognizerDelegate>

@property (nonatomic, strong) WXSDKInstance *instance;
@property (nonatomic, strong) UIView *weexView;
@property (nonatomic, assign) CGFloat weexHeight;
@property (nonatomic, strong) NSMutableArray *listenerList;
@property (nonatomic, strong) NSURL *URL;
@property (nonatomic, strong) UIWebView *webView;
@property (nonatomic, strong) UIActivityIndicatorView *activityIndicatorView;
@property (nonatomic, strong) UIView *statusBar;
@property (nonatomic, assign) NSString *notificationStatus;
@property (nonatomic, assign) NSString *liftCycleLastStatus;
@property (nonatomic, assign) NSString *liftCycleLastStatusChild;
@property (nonatomic, assign) BOOL didWillEnter;
//@property (nonatomic, strong) UIScrollView *mainScrollView;

@end

@implementation WXMainViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    [self setFd_prefersNavigationBarHidden:YES];
    if (_isDisSwipeBack) {
        [self setFd_interactivePopDisabled:YES];
    }
    
    [self.view setClipsToBounds:YES];
    
    _weexHeight = self.view.frame.size.height - CGRectGetMaxY(self.navigationController.navigationBar.frame);
    _cache = 0;
    
    _showNavigationBar = YES;
    //_statusBarColor = @"#3EB4FF";
    _statusBarAlpha = 0;
    
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    [center addObserver:self selector:@selector(keyboardDidShow:) name:UIKeyboardDidShowNotification object:nil];
    [center addObserver:self selector:@selector(keyboardDidHide:) name:UIKeyboardWillHideNotification object:nil];
    _keyBoardlsVisible = NO;
    
    [self.navigationController setNavigationBarHidden:_showNavigationBar];
    
    if (_backgroundColor) {
        self.view.backgroundColor = [WXConvert UIColor:_backgroundColor];
    }
    
    [self setupNaviBar];
    
    [self setupUI];
    
    if ([_pageType isEqualToString:@"web"]) {
        [self loadWebPage];
    } else {
        [self loadWeexPage];
    }
    
    [self setupActivityView];
    
    [self updateStatus:@"create"];
    
    [CustomWeexSDKManager setKeyBoardlsVisible:_keyBoardlsVisible];
    [CustomWeexSDKManager setSoftInputMode:_softInputMode];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self updateStatus:@"start"];
    
    if ([_statusBarType isEqualToString:@"fullscreen"]) {
        [UIApplication sharedApplication].statusBarHidden = YES;//状态栏隐藏
    } else {
        [UIApplication sharedApplication].statusBarHidden = NO;
    }
    
    //状态栏样式
    if ([_statusBarStyleCustom isEqualToString:@"1"]) {
        [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    }else{
        [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleDefault];
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self updateInstanceState:WeexInstanceAppear];
    
    [self updateStatus:@"resume"];
    [self liftCycleEvent:LifeCycleResume];
    
    [CustomWeexSDKManager setSoftInputMode:_softInputMode];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    //状态栏样式
    if ([_statusBarStyleCustom isEqualToString:@"1"]) {
        [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    }else{
        [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleDefault];
    }
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [self updateInstanceState:WeexInstanceDisappear];
    
    [self updateStatus:@"pause"];
    [self liftCycleEvent:LifeCyclePause];
    
    [self updateStatus:@"stop"];
}

//TODO get height
- (void)viewDidLayoutSubviews
{
    _weexHeight = self.view.frame.size.height;
    UIEdgeInsets safeArea = UIEdgeInsetsZero;

    if (@available(iOS 11.0, *)) {
        safeArea = self.view.safeAreaInsets;
    }else if (@available(iOS 9.0, *)) {
        safeArea.top = 20;
    }
    
    //自定义状态栏
    if ([_statusBarType isEqualToString:@""] || [_statusBarType isEqualToString:@"fullscreen"] || [_statusBarType isEqualToString:@"immersion"]) {
        _statusBar.hidden = YES;
        if ([_pageType isEqualToString:@"web"]) {
            _webView.frame = CGRectMake(safeArea.left, 0, self.view.frame.size.width-safeArea.left-safeArea.right, _weexHeight-safeArea.bottom);
        }else{
            _instance.frame = CGRectMake(safeArea.left, 0, self.view.frame.size.width-safeArea.left-safeArea.right, _weexHeight-safeArea.bottom);
        }
    } else {
        CGFloat top = 0;
        if (!_isChildSubview) {
            top = safeArea.top;
            _statusBar.hidden = NO;
            _statusBar.frame = CGRectMake(0, 0, self.view.frame.size.width, safeArea.top);
        }

        if ([_pageType isEqualToString:@"web"]) {
            _webView.frame = CGRectMake(safeArea.left, top, self.view.frame.size.width - safeArea.left-safeArea.right, _weexHeight - top -safeArea.bottom);
        }else{
            _instance.frame = CGRectMake(safeArea.left, top, self.view.frame.size.width - safeArea.left-safeArea.right, _weexHeight - top -safeArea.bottom);
        }
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc
{
    NSLog(@"gggggggg::dealloc");
    [self updateStatus:@"destroy"];
    
    [_instance destroyInstance];
#ifdef DEBUG
    [_instance forceGarbageCollection];
#endif
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)didMoveToParentViewController:(UIViewController *)parent
{
    [super didMoveToParentViewController:parent];
    
    if (parent == nil) {
        [[WeiuiNewPageManager sharedIntstance] removePageData:self.pageName];
    }
}


//键盘弹出触发该方法
- (void)keyboardDidShow:(NSNotification *)notification
{
    _keyBoardlsVisible = YES;
    [CustomWeexSDKManager setKeyBoardlsVisible:_keyBoardlsVisible];
}

// 键盘隐藏触发该方法
- (void)keyboardDidHide:(NSNotification *)notification
{
    _keyBoardlsVisible = NO;
    [CustomWeexSDKManager setKeyBoardlsVisible:_keyBoardlsVisible];
}

#pragma mark 生命周期
- (void)liftCycleEvent:(LifeCycleType)type
{
    //页面生命周期:生命周期
    NSString *status = @"";
    switch (type) {
        case LifeCycleReady:
            status = @"ready";
            break;
        case LifeCycleResume:
            status = @"resume";
            break;
        case LifeCyclePause:
            status = @"pause";
            break;
        default:
            return;
    }
    if ([status isEqualToString:_liftCycleLastStatus]) {
        return;
    }
    _liftCycleLastStatus = status;
    
    for (UIViewController * childViewController in self.childViewControllers) {
        if ([childViewController isKindOfClass:[WXMainViewController class]]) {
            WXMainViewController *vc = (WXMainViewController*) childViewController;
            if ([status isEqualToString:@"pause"]) {
                if ([vc.liftCycleLastStatus isEqualToString:@"resume"]) {
                    [vc liftCycleEvent:type];
                    vc.liftCycleLastStatusChild = status;
                }
            }else if ([status isEqualToString:@"resume"]) {
                if ([vc.liftCycleLastStatusChild isEqualToString:@"pause"]) {
                    [vc liftCycleEvent:type];
                    vc.liftCycleLastStatusChild = status;
                }
            }
        }
    }

    [[WXSDKManager bridgeMgr] fireEvent:_instance.instanceId ref:WX_SDK_ROOT_REF type:kLifeCycle params:@{@"status":status} domChanges:nil];
}

#pragma mark view
- (void)setupUI
{
    self.statusBar = [[UIView alloc] init];
    CGFloat alpha = ((255 - _statusBarAlpha)*1.0/255);
    _statusBar.backgroundColor = [[WXConvert UIColor:_statusBarColor?_statusBarColor : @"#3EB4FF"] colorWithAlphaComponent:alpha];
    [self.view addSubview:_statusBar];
    _statusBar.hidden = YES;

    //主页滚动图
    //    self.mainScrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    //    self.mainScrollView.showsVerticalScrollIndicator = NO;
    //    self.mainScrollView.contentSize = CGSizeMake(0, self.view.frame.size.height + 0.1);
    //    self.mainScrollView.scrollEnabled = NO;
    //    self.mainScrollView.contentOffset = CGPointMake(0, 0);
    //    [self.view addSubview:self.mainScrollView];
    //
    //    self.mainScrollView.mj_header = [MJRefreshHeader headerWithRefreshingBlock:^{
    //        if (self.refreshHeaderBlock) {
    //            self.refreshHeaderBlock();
    //        }
    //    }];
}

- (void)setupActivityView
{
    //加载图
    self.activityIndicatorView = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(0, 0, 30, 30)];
    self.activityIndicatorView.center = self.view.center;
    [self.activityIndicatorView setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleGray];
    [self.view addSubview:self.activityIndicatorView];
    
    [self startLoading];
}

- (void)setupNaviBar
{
//    UIScreenEdgePanGestureRecognizer *edgePanGestureRecognizer = [[UIScreenEdgePanGestureRecognizer alloc] initWithTarget:self action:@selector(edgePanGesture:)];
//    edgePanGestureRecognizer.delegate = self;
//    edgePanGestureRecognizer.edges = UIRectEdgeLeft;
//    [self.view addGestureRecognizer:edgePanGestureRecognizer];
//
//    NSArray *ver = [[UIDevice currentDevice].systemVersion componentsSeparatedByString:@"."];
//    if ([[ver objectAtIndex:0] intValue] >= 7) {
//        // iOS 7.0 or later
//        self.navigationController.navigationBar.barTintColor = [UIColor colorWithRed:0.27 green:0.71 blue:0.94 alpha:1];
//        self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
//        [self.navigationController.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
//        self.navigationController.navigationBar.translucent = NO;
//    }
    
    //backBarItem
    //    UIImage *backImg = [UIImage iconWithInfo:TBCityIconInfoMake([IconFontUtil iconFont:@"android-arrow-back"], 19, [UIColor whiteColor])];
    //
    //    [self.navigationController.navigationBar setBackIndicatorImage:backImg];
    //    [self.navigationController.navigationBar setBackIndicatorTransitionMaskImage:backImg];
    //
    //    UIBarButtonItem *backItem = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:self action:@selector(backButtonClicked)];
    //
    //    self.navigationItem.backBarButtonItem = backItem;
}

- (void)loadWebPage
{
    self.webView = [[UIWebView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    self.webView.delegate = self;
    NSURL *url = [NSURL URLWithString:_url];
    NSURLRequest *request =[NSURLRequest requestWithURL:url];
    [self.webView loadRequest:request];
    [self.view addSubview:self.webView];
}

- (void)loadWeexPage
{
    //缓存文件
    if (self.cache > 0) {
        BOOL isCache = NO;
        if ([WeexSDKManager sharedIntstance].cacheData[_url]) {
            //存在缓存文件，则判断是否过期
            NSDictionary *data = [WeexSDKManager sharedIntstance].cacheData[_url];
            NSInteger time = [data[kCacheTime] integerValue];
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:time];
            
            if ([date compare:[NSDate date]] == NSOrderedDescending) {
                NSString *cacheUrl = data[kCacheUrl];
                //使用缓存文件
                self.URL = [NSURL fileURLWithPath:cacheUrl];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self renderView];
                });
                isCache = NO;
            } else {
                self.URL = [NSURL URLWithString:_url];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self renderView];
                });
                //重新下载
                isCache = YES;
            }
        } else {
            //不存在缓存文件，则下载并缓存
            isCache = YES;
        }
        
        if (isCache) {
            __weak typeof(self) ws = self;
            NSString * urlStr = [_url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet characterSetWithCharactersInString:@"`#%^{}\"[]|\\<> "].invertedSet];
            NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:urlStr]];
            NSURLSession *session = [NSURLSession sharedSession];
            NSURLSessionDownloadTask *downloadTask = [session downloadTaskWithRequest:request completionHandler:^(NSURL * _Nullable location, NSURLResponse * _Nullable response, NSError * _Nullable error) {
                if (!error) {
                    NSFileManager *fileManager = [NSFileManager defaultManager];
                    NSString *filePath =  [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:kCachePath];
                    if (![fileManager fileExistsAtPath:filePath]) {
                        [fileManager createDirectoryAtPath:filePath withIntermediateDirectories:YES attributes:nil error:nil];
                    }
                    
                    NSString *fullPath =  [filePath stringByAppendingPathComponent:response.suggestedFilename];
                    
                    [fileManager moveItemAtURL:location toURL:[NSURL fileURLWithPath:fullPath] error:nil];
                    
                    NSInteger time = [[NSDate date] timeIntervalSince1970] + ws.cache * 1.0f / 1000;
                    NSDictionary *saveDic = @{kCacheUrl:fullPath, kCacheTime:@(time)};
                    [[WeexSDKManager sharedIntstance].cacheData setObject:saveDic forKey:ws.url];
                    
                    dispatch_async(dispatch_get_main_queue(), ^{
                        ws.URL = [NSURL fileURLWithPath:fullPath];
                        [ws renderView];
                    });
                }
            }];
            [downloadTask resume];
        }
    } else {
        self.URL = [NSURL URLWithString:_url];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self renderView];
        });
    }
}

- (void)renderView
{
    CGFloat width = self.view.frame.size.width;
    [_instance destroyInstance];
    _instance = [[WXSDKInstance alloc] init];
    
    if([WXPrerenderManager isTaskExist:self.url]){
        _instance = [WXPrerenderManager instanceFromUrl:self.url];
    }
    
    _instance.viewController = self;
    UIEdgeInsets safeArea = UIEdgeInsetsZero;
    
    if (@available(iOS 11.0, *)) {
        safeArea = self.view.safeAreaInsets;
    }else if (@available(iOS 9.0, *)) {
        safeArea.top = 20;
    }
    
    _instance.frame = CGRectMake(self.view.frame.size.width-width, safeArea.top, width, _weexHeight-safeArea.bottom);
    
    __weak typeof(self) weakSelf = self;
    _instance.onCreate = ^(UIView *view) {
        [weakSelf.weexView removeFromSuperview];
        weakSelf.weexView = view;
//        [weakSelf.mainScrollView addSubview:weakSelf.weexView];
        [weakSelf.view addSubview:weakSelf.weexView];
        UIAccessibilityPostNotification(UIAccessibilityScreenChangedNotification, weakSelf.weexView);
        
        [weakSelf updateStatus:@"viewCreated"];
    };
    
    _instance.onFailed = ^(NSError *error) {
        [weakSelf stopLoading];
        [weakSelf updateStatus:@"error"];
        
        if ([[error domain] isEqualToString:@"1"]) {
            dispatch_async(dispatch_get_main_queue(), ^{
                NSMutableString *errMsg=[NSMutableString new];
                [errMsg appendFormat:@"ErrorType:%@\n",[error domain]];
                [errMsg appendFormat:@"ErrorCode:%ld\n",(long)[error code]];
                [errMsg appendFormat:@"ErrorInfo:%@\n", [error userInfo]];
                NSLog(@"%@", errMsg);
            });
        }
    };
    
    _instance.renderFinish = ^(UIView *view) {
        WXLogDebug(@"%@", @"Render Finish...");
        [weakSelf updateInstanceState:WeexInstanceAppear];
        [weakSelf stopLoading];
        [weakSelf updateStatus:@"renderSuccess"];
        [weakSelf liftCycleEvent:LifeCycleReady];
        [weakSelf liftCycleEvent:LifeCycleResume];
    };
    
    _instance.updateFinish = ^(UIView *view) {
        WXLogDebug(@"%@", @"Update Finish...");
    };
    if (!self.url) {
        WXLogError(@"error: render url is nil");
        return;
    }
    if([WXPrerenderManager isTaskExist:self.url]){
        WX_MONITOR_INSTANCE_PERF_START(WXPTJSDownload, _instance);
        WX_MONITOR_INSTANCE_PERF_END(WXPTJSDownload, _instance);
        WX_MONITOR_INSTANCE_PERF_START(WXPTFirstScreenRender, _instance);
        WX_MONITOR_INSTANCE_PERF_START(WXPTAllRender, _instance);
        [WXPrerenderManager renderFromCache:self.url];
        return;
    }
    _instance.viewController = self;
    
    [_instance renderWithURL:_URL options:@{@"params":_params?_params:@""} data:nil];
    
    if (_didWillEnter == NO) {
        _didWillEnter = YES;
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appDidEnterBackground:) name:UIApplicationDidEnterBackgroundNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appWillEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
    }
    
    //    NSURL *URL = [self testURL: [self.url absoluteString]];
    //    NSString *randomURL = [NSString stringWithFormat:@"%@%@random=%d",URL.absoluteString,URL.query?@"&":@"?",arc4random()];
    //    [_instance renderWithURL:[NSURL URLWithString:randomURL] options:@{@"bundleUrl":URL.absoluteString} data:nil];
    
    //    [self.navigationController.navigationBar setTitleTextAttributes: [NSDictionary dictionaryWithObjectsAndKeys:
    //                                                                      [UIColor whiteColor], NSForegroundColorAttributeName, nil]];
    //
    //    if([_instance.pageName hasPrefix:@"http://dotwe.org"] || [_instance.pageName hasPrefix:@"https://dotwe.org"]) {
    //        self.navigationItem.title = @"Weex Online Example";
    //    } else {
    //        self.navigationItem.title = _instance.pageName;
    //    }
}

- (void)appDidEnterBackground:(NSNotification*)notification
{
    if ([self.liftCycleLastStatus isEqualToString:@"resume"]) {
        [self updateStatus:@"pause"];
        [self liftCycleEvent:LifeCyclePause];
        self.liftCycleLastStatusChild = @"pause";
    }
}

- (void)appWillEnterForeground:(NSNotification*)notification
{
    if ([self.liftCycleLastStatusChild isEqualToString:@"pause"]) {
        [self updateStatus:@"resume"];
        [self liftCycleEvent:LifeCycleResume];
        self.liftCycleLastStatusChild = @"resume";
    }
}

#pragma mark action
//- (void)edgePanGesture:(UIScreenEdgePanGestureRecognizer*)edgePanGestureRecognizer
//{
//    if (self.navigationController && [self.navigationController.viewControllers count] == 1) {
//        return;
//    }
//
//    if (!_isDisSwipeBack) {
//        [self.navigationController popViewControllerAnimated:YES];
//    }
//}

- (void)stopLoading
{
    [self.activityIndicatorView setHidden:YES];
    [self.activityIndicatorView stopAnimating];
}

- (void)startLoading
{
    [self.activityIndicatorView setHidden:NO];
    [self.activityIndicatorView startAnimating];
}

- (void)updateInstanceState:(WXState)state
{
    if (_instance && _instance.state != state) {
        _instance.state = state;
        
        if (state == WeexInstanceAppear) {
            [[WXSDKManager bridgeMgr] fireEvent:_instance.instanceId ref:WX_SDK_ROOT_REF type:@"viewappear" params:nil domChanges:nil];
        }
        else if (state == WeexInstanceDisappear) {
            [[WXSDKManager bridgeMgr] fireEvent:_instance.instanceId ref:WX_SDK_ROOT_REF type:@"viewdisappear" params:nil domChanges:nil];
        }
    }
}

- (void)updateStatus:(NSString*)status
{
    if (self.statusBlock) {
        self.statusBlock(status);
    }
    
    //通知监听
    if ([status isEqualToString:_notificationStatus]) {
        return;
    }
    _notificationStatus = status;
    for (NSString *key in self.listenerList) {
        [[NSNotificationCenter defaultCenter] postNotificationName:key object:@{@"status":status}];
    }
}

- (void)setHomeUrl:(NSString*)url
{
    self.url = url;
    self.URL = [NSURL URLWithString:_url];
}

- (void)addStatusListener:(NSString*)name
{
    if (!self.listenerList) {
        self.listenerList = [NSMutableArray arrayWithCapacity:5];
    }
    
    if (![self.listenerList containsObject:name]) {
        [self.listenerList addObject:name];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(listennerEvent:) name:name object:nil];
    }
}

- (void)clearStatusListener:(NSString*)name
{
    if ([self.listenerList containsObject:name]) {
        [self.listenerList removeObject:name];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:name object:nil];
    }
}

- (void)listennerEvent:(NSNotification*)notification
{
    id obj = notification.object;
    if (obj) {
        if (self.listenerBlock) {
            self.listenerBlock(obj);
        }
    }
}

- (void)postStatusListener:(NSString*)name data:(id)data
{
    if (name.length > 0) {
        if ([self.listenerList containsObject:name]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:name object:data];
        }
    } else {
        for (NSString *key in self.listenerList) {
            [[NSNotificationCenter defaultCenter] postNotificationName:key object:data];
        }
    }
}


#pragma mark - refresh
- (void)refreshPage
{
    if ([_pageType isEqualToString:@"web"]) {
        [self.webView reload];
    } else {
        [self renderView];
        [self updateStatus:@"restart"];
    }
}

#pragma mark - notification
- (void)notificationRefreshInstance:(NSNotification *)notification {
    [self refreshPage];
}

#pragma mark- UIGestureRecognizerDelegate
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
    if (self.navigationController && [self.navigationController.viewControllers count] == 1) {
        return NO;
    }
    return YES;
}

#pragma mark webDelegate
//是否允许加载网页，也可获取js要打开的url，通过截取此url可与js交互
- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType
{
    return YES;
}

//开始加载网页
- (void)webViewDidStartLoad:(UIWebView *)webView
{
    self.webBlock(@{@"status":@"statusChanged", @"webStatus":@"", @"errCode":@"", @"errMsg":@"", @"errUrl":@"", @"title":@""});
}

//网页加载完成
- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    [self stopLoading];
    
    NSString *title = [webView stringByEvaluatingJavaScriptFromString:@"document.title"];
    if (![self.title isEqualToString:title]) {
        self.title = title;
        self.webBlock(@{@"status":@"titleChanged", @"webStatus":@"", @"errCode":@"", @"errMsg":@"", @"errUrl":@"", @"title":title});
    }
}

//网页加载错误
- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    [self stopLoading];
    
    if (error) {
        NSString *code = [NSString stringWithFormat:@"%ld", error.code];
        NSString *msg = [NSString stringWithFormat:@"%@", error.description];
        self.webBlock(@{@"status":@"errorChanged", @"webStatus":@"", @"errCode":code, @"errMsg":msg, @"errUrl":_url, @"title":@""});
    }
}


@end
