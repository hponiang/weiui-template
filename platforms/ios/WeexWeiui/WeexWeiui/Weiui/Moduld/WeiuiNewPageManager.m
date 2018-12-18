//
//  WeiuiNewPageManager.m
//  WeexTestDemo
//
//  Created by apple on 2018/6/6.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import "WeiuiNewPageManager.h"
#import "DeviceUtil.h"
//#import "IQKeyboardManager.h"
#import "WeexSDKManager.h"

@interface WeiuiNewPageManager ()

//@property (nonatomic, strong) NSDictionary *params;
@property (nonatomic, strong) NSMutableDictionary *pageData;
@property (nonatomic, strong) NSMutableDictionary *viewData;
@property (nonatomic, strong) NSMutableDictionary *callData;
//@property (nonatomic, copy) WXModuleKeepAliveCallback callback;

@end

@implementation WeiuiNewPageManager

+ (WeiuiNewPageManager *)sharedIntstance {
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (instancetype)init
{
    if (self = [super init]) {
        self.pageData = [NSMutableDictionary dictionaryWithCapacity:5];
        self.viewData = [NSMutableDictionary dictionaryWithCapacity:5];
        self.callData = [NSMutableDictionary dictionaryWithCapacity:5];
    }
    
    return self;
}

#pragma mark openPage
- (void)openPage:(NSDictionary*)params callback:(WXModuleKeepAliveCallback)callback
{
    NSString *url = params[@"url"] ? [WXConvert NSString:params[@"url"]] : @"";
    
    //返回随机数名称
    NSString *pageName = params[@"pageName"] ? [WXConvert NSString:params[@"pageName"]] : [NSString stringWithFormat:@"NewPage-%d", (arc4random() % 100) + 1000];
    
    NSString *pageType = params[@"pageType"] ? [WXConvert NSString:params[@"pageType"]] : @"weex";
    id data = params[@"params"];
    NSInteger cache = params[@"cache"] ? [WXConvert NSInteger:params[@"cache"]] : 0;
    BOOL loading = params[@"loading"] ? [WXConvert BOOL:params[@"loading"]] : YES;
    #warning ssss swipeBack默认改为yes
    BOOL swipeBack = params[@"swipeBack"] ? [WXConvert BOOL:params[@"swipeBack"]] : YES;
    NSString *statusBarType = params[@"statusBarType"] ? [WXConvert NSString:params[@"statusBarType"]] : @"normal";
    NSString *statusBarColor = params[@"statusBarColor"] ? [WXConvert NSString:params[@"statusBarColor"]] : @"#3EB4FF";
    NSInteger statusBarAlpha = params[@"statusBarAlpha"] ? [WXConvert NSInteger:params[@"statusBarAlpha"]] : 0;
    
    NSString *softInputMode = params[@"softInputMode"] ? [WXConvert NSString:params[@"softInputMode"]] : @"auto";
    BOOL translucent = params[@"translucent"] ? [WXConvert BOOL:params[@"translucent"]] : NO;
    
    NSString *backgroundColor = params[@"backgroundColor"] ? [WXConvert NSString:params[@"backgroundColor"]] : @"#f4f8f9";
    BOOL backPressedClose = params[@"backPressedClose"] ? [WXConvert BOOL:params[@"backPressedClose"]] : YES;
    
    //键盘处理
    //if ([softInputMode isEqualToString:@"pan"]) {
    //    [IQKeyboardManager sharedManager].enable = NO;
    //} else {
    //    [IQKeyboardManager sharedManager].enable = YES;
    //}

    url = [DeviceUtil rewriteUrl:url];
    NSLog(@"NewPage = %@", url);
    
    //跳转页面
    WXMainViewController *mainVC = [[WXMainViewController alloc] init];
    mainVC.pageType = pageType;
    mainVC.url = url;
    mainVC.cache = cache;
    mainVC.isDisSwipeBack = !swipeBack;
    mainVC.isDisItemBack = !backPressedClose;
    mainVC.loading = loading;
    mainVC.statusBarType = statusBarType;
    mainVC.statusBarColor = statusBarColor;
    mainVC.statusBarAlpha = statusBarAlpha;
    mainVC.params = data;
    mainVC.pageName = pageName;
    mainVC.backgroundColor = backgroundColor;
    
    mainVC.statusBlock = ^(NSString *status) {
        if (callback) {
            callback(@{@"pageName":pageName, @"status":status, @"webStatus":@"", @"errCode":@"", @"errMsg":@"", @"errUrl":@"", @"title":@""}, YES);
        }
    };
    mainVC.webBlock = ^(NSDictionary *dic) {
        if (callback) {
            NSMutableDictionary *result = [NSMutableDictionary dictionaryWithDictionary:dic];
            [result setObject:pageName forKey:@"pageName"];
            callback(result, YES);
        }
    };
    
    if (self.weexInstance.viewController.navigationController) {
        [self.weexInstance.viewController.navigationController pushViewController:mainVC animated:YES];
    } else if ([[DeviceUtil getTopviewControler] navigationController]) {
        [[[DeviceUtil getTopviewControler] navigationController] pushViewController:mainVC animated:YES];
    } else {
        [[UIApplication sharedApplication] delegate].window.rootViewController =  [[WXRootViewController alloc] initWithRootViewController:mainVC];
    }
    
    //存储页面数据
    [self setPageData:pageName vc:mainVC];
}

- (NSDictionary*)getPageInfo:(id)params
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    } else {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    if (name.length > 0) {
        NSDictionary *data = self.pageData[name];
        if (data) {
            return data;
        }
    }
    return nil;
}

- (void)reloadPage:(id)params
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    } else {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    WXMainViewController *vc = nil;
    if (name.length > 0) {
        id data = self.viewData[name];
        if (data && [data isKindOfClass:[UIViewController class]]) {
            vc = data;
        } else {
            vc = (WXMainViewController*)[DeviceUtil getTopviewControler];
        }
    }
    
    [(WXMainViewController*)vc refreshPage];
}

- (void)setSoftInputMode:(id)params modo:(NSString*)modo
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    } else {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    //if ([modo isEqualToString:@"pan"]) {
    //    [IQKeyboardManager sharedManager].enable = NO;
    //} else {
    //    [IQKeyboardManager sharedManager].enable = YES;
    //}
}
#warning ssss
- (void)setPageBackPressed:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    } else {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    //    if (callback) {
    //        callback(nil, YES);
    //    }
}

- (void)setOnRefreshListener:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    } else {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    WXMainViewController *vc = nil;
    id data = self.viewData[name];
    if (data && [data isKindOfClass:[WXMainViewController class]]) {
        vc = (WXMainViewController*)data;
    } else {
        vc = (WXMainViewController*)[DeviceUtil getTopviewControler];
    }
    
#warning ssss 下拉页面复杂，暂时禁用
    //vc.refreshHeaderBlock = ^{
    //    if (callback) {
    //        callback(name, YES);
    //    }
    //};
}

- (void)setRefreshing:(id)params refreshing:(BOOL)refreshing
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    } else {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    WXMainViewController *vc = nil;
    id data = self.viewData[name];
    if (data && [data isKindOfClass:[WXMainViewController class]]) {
        vc = (WXMainViewController*)data;
    } else {
        vc = (WXMainViewController*)[DeviceUtil getTopviewControler];
    }
    
    //    [vc changeRefresh:refreshing];
}


- (void)setPageStatusListener:(id)params callback:(WXModuleKeepAliveCallback)callback
{
    NSString *listener = @"";
    NSString *name = @"";
    if ([params isKindOfClass:[NSString class]]) {
        listener = params;
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    } else if ([params isKindOfClass:[NSDictionary class]]) {
        listener = [WXConvert NSString:params[@"listenerName"]];
        name = params[@"pageName"] ? [WXConvert NSString:params[@"pageName"]] : [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    WXMainViewController *vc = nil;
    id data = self.viewData[name];
    if (data && [data isKindOfClass:[WXMainViewController class]]) {
        vc = (WXMainViewController*)data;
    } else {
        vc = (WXMainViewController*)[DeviceUtil getTopviewControler];
    }
    
    [vc addStatusListener:listener];
    
    //通过监听名称存取相应的block
    [self.callData setObject:callback forKey:listener];
    
    vc.listenerBlock = ^(id obj) {

        if (obj && [obj isKindOfClass:[NSDictionary class]]) {
            NSString *listenerName = obj[@"listenerName"];
            WXModuleKeepAliveCallback callBack = self.callData[listenerName];

            if (callBack) {
                NSString *status = @"";
                id extra = nil;
                if ([obj isKindOfClass:[NSString class]]) {
                    status = obj;
                } else if ([obj isKindOfClass:[NSDictionary class]]) {
                    status = obj[@"status"];
                    extra = obj[@"extra"];
                }
                
                NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithDictionary:@{@"pageName":name, @"status":status, @"webStatus":@"", @"errCode":@"", @"errMsg":@"", @"errUrl":@"", @"title":@""}];
                if (extra) {
                    [dic setObject:extra forKey:@"extra"];
                }
                
                callBack(dic, YES);
            }
        }
    };
}

- (void)clearPageStatusListener:(id)params
{
    NSString *listener = @"";
    NSString *name = @"";
    if ([params isKindOfClass:[NSString class]]) {
        listener = params;
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    } else if ([params isKindOfClass:[NSDictionary class]]) {
        listener = params[@"listenerName"];
        name = params[@"pageName"] ? params[@"pageName"] : [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    
    WXMainViewController *vc = nil;
    id data = self.viewData[name];
    if (data && [data isKindOfClass:[WXMainViewController class]]) {
        vc = (WXMainViewController*)data;
    } else {
        vc = (WXMainViewController*)[DeviceUtil getTopviewControler];
    }
    
    [vc clearStatusListener:listener];
    
    if ([[self.callData allKeys] containsObject:listener]) {
        [self.callData removeObjectForKey:listener];
    }
}

- (void)onPageStatusListener:(id)params status:(NSString*)status
{
    NSString *status2 = @"";
    NSString *listener = @"";
    NSString *name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    id extra = nil;
    
    //第二个参数为空，则表示第一个参数是status
    if (status == nil) {
        if ([params isKindOfClass:[NSString class]]) {
            status2 = params;
        }
    } else {
        status2 = status;

        if ([params isKindOfClass:[NSString class]]) {
            listener = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            listener = [WXConvert NSString:params[@"listenerName"]];
            if (params[@"pageName"]) {
                name = [WXConvert NSString:params[@"pageName"]];
            }
            extra = params[@"extra"];
        }
    }
    
    WXMainViewController *vc = nil;
    id data = self.viewData[name];
    if (data && [data isKindOfClass:[WXMainViewController class]]) {
        vc = (WXMainViewController*)data;
    } else {
        vc = (WXMainViewController*)[DeviceUtil getTopviewControler];
    }
    
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithDictionary:@{@"status":status2, @"pageName":name, @"listenerName":listener}];
    
    if (extra) {
        [dic setObject:extra forKey:@"extra"];
    }
    
    [vc postStatusListener:listener data:dic];
}

- (void)getCacheSizePage:(WXModuleKeepAliveCallback)callback
{
    NSString *filePath =  [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:kCachePath];
    NSInteger  size = [[[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil] fileSize];
    
    if (callback) {
        callback(@{@"size":@(size)}, YES);
    }
}

- (void)clearCachePage
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *filePath =  [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:kCachePath];
    
    if ([fileManager fileExistsAtPath:filePath]) {
        [fileManager removeItemAtPath:filePath error:nil];
    }
}

- (void)closePage:(id)params
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    } else {
        name = [(WXMainViewController*)[DeviceUtil getTopviewControler] pageName];
    }
    if ([name isEqualToString:@""]) {
        return;
    }
    
    id data = self.viewData[name];
    if (data == nil) {
        return;
    }
    
    UIViewController *vc = nil;
    if (data && [data isKindOfClass:[UIViewController class]]) {
        vc = (UIViewController*)data;
    } else {
        vc = [DeviceUtil getTopviewControler];
    }
    if (vc == nil) {
        return;
    }
    
    if ([[self.pageData allKeys] containsObject:name]) {
        [self.pageData removeObjectForKey:name];
    }
    if ([[self.viewData allKeys] containsObject:name]) {
        [self.viewData removeObjectForKey:name];
    }
    
    NSMutableArray *array = [NSMutableArray arrayWithArray:self.weexInstance.viewController.navigationController.viewControllers];
    BOOL isDeviceUtil = NO;
    if (array.count == 0) {
        array = [NSMutableArray arrayWithArray:[[DeviceUtil getTopviewControler] navigationController].viewControllers];
        isDeviceUtil = YES;
    }
    for (int i = 0; i < array.count; i++) {
        if (array[i] == vc) {
            [self removePageData:[(WXMainViewController*)array[i] pageName]];
            if (i + 1 == array.count) {
                if (isDeviceUtil) {
                    [[[DeviceUtil getTopviewControler] navigationController] popViewControllerAnimated:YES];
                } else {
                    [self.weexInstance.viewController.navigationController popViewControllerAnimated:YES];
                }
            } else {
                [array removeObjectAtIndex:i];
                if (isDeviceUtil) {
                    [[DeviceUtil getTopviewControler] navigationController].viewControllers = array;
                } else {
                    self.weexInstance.viewController.navigationController.viewControllers = array;
                }
            }
            break;
        }
    }
}

- (void)closePageTo:(id)params
{
    NSString *name = @"";
    if (params) {
        if ([params isKindOfClass:[NSString class]]) {
            name = params;
        } else if ([params isKindOfClass:[NSDictionary class]]) {
            name = [WXConvert NSString:params[@"pageName"]];
        }
    }
    if ([name isEqualToString:@""]) {
        return;
    }
    
    id data = self.viewData[name];
    if (data == nil) {
        return;
    }
    
    UIViewController *vc = nil;
    if (data && [data isKindOfClass:[UIViewController class]]) {
        vc = (UIViewController*)data;
    }
    if (vc == nil) {
        return;
    }
    
    NSMutableArray *array = [NSMutableArray arrayWithArray:self.weexInstance.viewController.navigationController.viewControllers];
    BOOL isDeviceUtil = NO;
    if (array.count == 0) {
        array = [NSMutableArray arrayWithArray:[[DeviceUtil getTopviewControler] navigationController].viewControllers];
        isDeviceUtil = YES;
    }
    
    BOOL isClose = NO;
    BOOL isRemove = NO;
    for (int i = 0; i < array.count; i++) {
        if (isClose == YES) {
            if (i + 1 != array.count) {
                [self removePageData:[(WXMainViewController*)array[i] pageName]];
                [array removeObjectAtIndex:i];
                isRemove = YES;
            }
        }else{
            if (array[i] == vc) {
                isClose = YES;
            }
        }
    }
    
    if (isRemove) {
        if (isDeviceUtil) {
            [[DeviceUtil getTopviewControler] navigationController].viewControllers = array;
        } else {
            self.weexInstance.viewController.navigationController.viewControllers = array;
        }
    }
    
    if (isClose) {
       [self closePage:nil];
    }
}

- (void)openWeb:(NSString*)url
{
    NSURL *URL = [NSURL URLWithString:url];
    [[UIApplication sharedApplication] openURL:URL];
}

- (void)goDesktop
{
    [[UIApplication sharedApplication] performSelector:@selector(suspend)];
}

- (void)removePageData:(NSString*)pageName
{
    if ([[self.pageData allKeys] containsObject:pageName]) {
        [self.pageData removeObjectForKey:pageName];
    }
    if ([[self.viewData allKeys] containsObject:pageName]) {
        [self.viewData removeObjectForKey:pageName];
    }
}

- (void)setPageData:(NSString*)pageName vc:(WXMainViewController *)vc
{
    if (pageName.length > 0) {
        [self.viewData setObject:vc forKey:pageName];
        
        NSMutableDictionary *res = [NSMutableDictionary dictionaryWithDictionary:@{}];
        [res setObject:vc.url forKey:@"url"];
        [res setObject:vc.pageName forKey:@"pageName"];
        [res setObject:vc.pageType forKey:@"pageType"];
        [res setObject:vc.params ? vc.params: @{} forKey:@"params"];
        [res setObject:[NSString stringWithFormat:@"%ld", vc.cache] forKey:@"cache"];
        [res setObject:vc.loading ? @"true" : @"false" forKey:@"loading"];
        [res setObject:vc.statusBarType forKey:@"statusBarType"];
        [res setObject:vc.statusBarColor forKey:@"statusBarColor"];
        [res setObject:[NSString stringWithFormat:@"%ld", vc.statusBarAlpha] forKey:@"statusBarAlpha"];
        [res setObject:vc.backgroundColor forKey:@"backgroundColor"];
        [self.pageData setObject:res forKey:pageName];
    }
}

@end
