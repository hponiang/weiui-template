//
//  AppDelegate.m
//  WeexWeiui
//
//  Created by 高一 on 2018/8/15.
//

#import "AppDelegate.h"
#import "WeexSDKManager.h"
#import "WeiuiRongcloudManager.h"
#import "MNAssistiveBtn.h"
#import "ViewController.h"
#import "WeiuiStorageManager.h"
#import "WeiuiNewPageManager.h"
#import "WeiuiUmengManager.h"
#import "scanViewController.h"
#import "DeviceUtil.h"
#import <SocketRocket/SRWebSocket.h>
#import "Config.h"
#import "Cloud.h"
#import "WeiuiPayModule.h"

@interface AppDelegate ()<SRWebSocketDelegate>

@property (nonatomic, strong) SRWebSocket *webSocket;
@property (nonatomic, assign) BOOL isSocketConnect;

@end

@implementation AppDelegate

ViewController *mController;
MNAssistiveBtn *debugBtn;
NSString *socketHost;
NSString *socketPort;
NSTimeInterval reconnectionNumber;
NSDictionary *mLaunchOptions;

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    mLaunchOptions = launchOptions;
    
    #if DEBUG
    mController = [[ViewController alloc]init];
    UINavigationController *navi = [[UINavigationController alloc]initWithRootViewController:mController];
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.window.rootViewController = navi;
    [self.window makeKeyAndVisible];
    [self initDebug:0];
    #endif
    
    [Cloud welcome:self.window];
    
    [self initRongcloud];
    [self initUmeng];
    
    return YES;
}

- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString*, id> *)options
{
    if ([url.host isEqualToString:@"safepay"]) {
        // 支付宝处理支付结果
        [WeiuiPayModule alipayHandleOpenURL:url];
    }else if ([url.host isEqualToString:@"pay"]) {
        // 微信支付处理支付结果
        return [WeiuiPayModule weixinHandleOpenURL:url];
    }
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

//初始化融云
- (void)initRongcloud {
    NSMutableDictionary *rongim = [[Config getObject:@"rongim"] objectForKey:@"ios"];
    NSString *enabled = [NSString stringWithFormat:@"%@", rongim[@"enabled"]];
    //
    if ([enabled containsString:@"1"] || [enabled containsString:@"true"]) {
        NSString *appKey = [NSString stringWithFormat:@"%@", rongim[@"appKey"]];
        NSString *appSecret = [NSString stringWithFormat:@"%@", rongim[@"appSecret"]];
        [WeexSDKManager sharedIntstance].rongKey = appKey;
        [WeexSDKManager sharedIntstance].rongSec = appSecret;
        [[WeiuiRongcloudManager sharedIntstance] init:appKey appSecret:appSecret];
    }
}

//初始化友盟
- (void)initUmeng {
    NSMutableDictionary *umeng = [[Config getObject:@"umeng"] objectForKey:@"ios"];
    NSString *enabled = [NSString stringWithFormat:@"%@", umeng[@"enabled"]];
    //
    if ([enabled containsString:@"1"] || [enabled containsString:@"true"]) {
        NSString *appKey = [NSString stringWithFormat:@"%@", umeng[@"appKey"]];
        NSString *appSecret = [NSString stringWithFormat:@"%@", umeng[@"appSecret"]];
        NSString *channel = [NSString stringWithFormat:@"%@", umeng[@"channel"]];
        [[WeiuiUmengManager sharedIntstance] init:appKey secret:appSecret channel:channel launchOptions:mLaunchOptions];
    }
}

//初始化DEBUG
-(void) initDebug:(NSInteger) number {
    if (number > 100) {
        [self setDebugBtn:self.isSocketConnect];
        [self setSocketData];
        [self setSocketConnect:@"initialize"];
        return;
    }
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if ([mController isReady]) {
            [self initDebug:999];
        }else{
            [self initDebug:number+1];
        }
    });
}

//添加悬浮按钮
-(void) setDebugBtn:(BOOL)isSuccess {
    if (debugBtn) {
        [debugBtn setBackgroundUIImage: [UIImage imageNamed:isSuccess ? @"debugButtonSuccess" : @"debugButtonConnect"]];
        return;
    }
    CGFloat touchW = 48;
    CGFloat touchH = 48;
    CGFloat touchX = [[UIScreen mainScreen] bounds].size.width - touchW;
    CGFloat touchY = ([[UIScreen mainScreen] bounds].size.height - touchH) / 2;
    CGRect frame = CGRectMake(touchX, touchY, touchW, touchH);
    debugBtn = [MNAssistiveBtn mn_touchWithType:MNAssistiveTypeNone
                                          Frame:frame
                                          title:@"DEV"
                                     titleColor:[UIColor whiteColor]
                                      titleFont:[UIFont systemFontOfSize:12]
                                backgroundColor:nil
                                backgroundImage:[UIImage imageNamed:isSuccess ? @"debugButtonSuccess" : @"debugButtonConnect"]];
    [self.window addSubview:debugBtn];
    [debugBtn addTarget:self action:@selector(clickDebugBtn) forControlEvents:UIControlEventTouchUpInside];
}

//点击悬浮按钮
- (void) clickDebugBtn {
    UIAlertController *alertController = [UIAlertController
                                          alertControllerWithTitle:@"开发工具菜单"
                                          message:nil
                                          preferredStyle:UIAlertControllerStyleActionSheet];
    [alertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
    [alertController addAction:[UIAlertAction actionWithTitle:self.isSocketConnect ? @"WiFi真机同步 [已连接]" : @"WiFi真机同步" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self wifiSetting];
    }]];
    [alertController addAction:[UIAlertAction actionWithTitle:@"扫一扫" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self openScan];
    }]];
    [alertController addAction:[UIAlertAction actionWithTitle:@"刷新" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self refresh];
    }]];
    [alertController addAction:[UIAlertAction actionWithTitle:@"重启APP" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self rebootConfirm];
    }]];
    if ([Config isConfigDataIsDist]) {
        [alertController addAction:[UIAlertAction actionWithTitle:@"清除热更新数据" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            [Cloud clearUpdate];
        }]];
    }
    
    [self.window.rootViewController presentViewController:alertController animated:TRUE completion:nil];
}

//WiFi真机同步配置
- (void) wifiSetting {
    UIAlertController * alertController = [UIAlertController
                                           alertControllerWithTitle: @"WiFi真机同步配置"
                                           message: @"配置成功后，可实现真机同步实时预览"
                                           preferredStyle:UIAlertControllerStyleAlert];
    [alertController addTextFieldWithConfigurationHandler:^(UITextField *textField) {
        textField.placeholder = @"请输入IP地址";
        textField.text = socketHost;
        textField.textColor = [UIColor blueColor];
        textField.clearButtonMode = UITextFieldViewModeWhileEditing;
        textField.keyboardType = UIKeyboardTypeDecimalPad;
    }];
    [alertController addTextFieldWithConfigurationHandler:^(UITextField *textField) {
        textField.placeholder = @"请输入端口号";
        textField.text = socketPort;
        textField.textColor = [UIColor blueColor];
        textField.clearButtonMode = UITextFieldViewModeWhileEditing;
        textField.keyboardType = UIKeyboardTypeNumberPad;
    }];
    [alertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil]];
    [alertController addAction:[UIAlertAction actionWithTitle:@"连接" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        NSArray * textfields = alertController.textFields;
        UITextField * hostField = textfields[0];
        UITextField * portFiled = textfields[1];
        socketHost = hostField.text;
        socketPort = portFiled.text;
        [self setSocketConnect:@"initialize"];
    }]];

    UIWindow *alertWindow = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    alertWindow.rootViewController = [[UIViewController alloc] init];
    alertWindow.windowLevel = UIWindowLevelAlert + 1;
    [alertWindow makeKeyAndVisible];
    [alertWindow.rootViewController presentViewController:alertController animated:YES completion:nil];
}

//打开扫一扫
- (void) openScan {
    scanViewController *scan = [[scanViewController alloc]init];
    scan.desc = nil;
    scan.successClose = YES;
    scan.scanerBlock = ^(NSDictionary *dic) {
        if ([dic[@"status"] isEqualToString:@"success"]) {
            NSString *text = dic[@"url"];
            NSString *url = text, *host = @"", *port = @"";
            if ([url hasPrefix:@"http"]) {
                if ([text containsString:@"?socket="]) {
                    url = [Config getMiddle:text start:nil to:@"?socket="];
                    host = [Config getMiddle:text start:@"?socket=" to:@":"];
                    port = [Config getMiddle:text start:[NSString stringWithFormat:@"?socket=%@:", host] to:@"&"];
                }
                
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [WeiuiNewPageManager sharedIntstance].weexInstance = [[WXSDKManager bridgeMgr] topInstance];
                    [[WeiuiNewPageManager sharedIntstance] openPage:@{@"url": url} callback:^(NSDictionary *result, BOOL keepAlive) {
                        if ([result[@"status"] isEqualToString:@"create"]) {
                            if (host.length && port.length) {
                                socketHost = host;
                                socketPort = port;
                                [self setSocketConnect:@"back"];
                            }
                        }
                    }];
                });
            }
        }
    };
    [[[DeviceUtil getTopviewControler] navigationController] pushViewController:scan animated:YES];
}

//刷新当前页面
- (void) refresh {
    [[WeiuiNewPageManager sharedIntstance] reloadPage:nil];
}

//确认重启APP
- (void) rebootConfirm {
    UIAlertController * alertController = [UIAlertController
                                           alertControllerWithTitle: @"热重启APP"
                                           message: @"确认要关闭所有页面热重启APP吗？"
                                           preferredStyle:UIAlertControllerStyleAlert];
    [alertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil]];
    [alertController addAction:[UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [Cloud reboot];
        [Cloud appData];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)([Cloud welcome:self.window] * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [Cloud welcomeClose];
        });
        [self initRongcloud];
        [self initUmeng];
    }]];
    UIWindow *alertWindow = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    alertWindow.rootViewController = [[UIViewController alloc] init];
    alertWindow.windowLevel = UIWindowLevelAlert + 1;
    [alertWindow makeKeyAndVisible];
    [alertWindow.rootViewController presentViewController:alertController animated:YES completion:nil];
}

//获取socket地址及端口
- (void) setSocketData {
    socketHost = [Config getString:@"socketHost" defaultVal:@""];
    socketPort = [Config getString:@"socketPort" defaultVal:@""];
}

//开始请求连接
- (void) setSocketConnect: (NSString *) param {
    self.webSocket.delegate = nil;
    [self.webSocket close];
    
    if (self.isSocketConnect != NO) {
        self.isSocketConnect = NO;
        [self setDebugBtn:self.isSocketConnect];
    }
    
    if (!socketHost.length || !socketPort.length) {
        return;
    }
    
    NSString *wsUrl = [NSString stringWithFormat:@"ws://%@:%@?mode=%@", socketHost, socketPort, param];
    self.webSocket = [[SRWebSocket alloc] initWithURLRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:wsUrl]]];
    self.webSocket.delegate = self;
    [self.webSocket open];
}

//长链接已连接成功
- (void)webSocketDidOpen:(SRWebSocket *)webSocket{
    reconnectionNumber = 0;
    if (self.isSocketConnect != YES) {
        self.isSocketConnect = YES;
        [self setDebugBtn:self.isSocketConnect];
    }
}

//请求长链接失败 及其原因
- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error{
    self.webSocket = nil;
    if (self.isSocketConnect != NO) {
        self.isSocketConnect = NO;
        [self setDebugBtn:self.isSocketConnect];
    }
    //重连
    if (reconnectionNumber < 64) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(reconnectionNumber * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self setSocketConnect:@"reconnection"];
        });
        if (reconnectionNumber == 0) {
            reconnectionNumber = 2;
        }else{
            reconnectionNumber *= 2;
        }
    }
}

//长链接收到消息
- (void)webSocket:(SRWebSocket *)webSocket didReceiveMessage:(id)message{
    NSString *msg = (NSString *)message;
    if ([msg hasPrefix:@"HOMEPAGE:"]) {
        [[[DeviceUtil getTopviewControler] navigationController] popToRootViewControllerAnimated:NO];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [mController loadUrl:[msg substringFromIndex:9]];
        });
    }else if ([msg hasPrefix:@"HOMEPAGEBACK:"]) {
        [mController loadUrl:[msg substringFromIndex:13]];
    }else if ([msg isEqualToString:@"RELOADPAGE"]) {
        [self refresh];
    }
}

//长链接断开 及其原因
- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean{
    self.webSocket.delegate = nil;
    self.webSocket = nil;
    if (self.isSocketConnect != NO) {
        self.isSocketConnect = NO;
        [self setDebugBtn:self.isSocketConnect];
    }
}

@end
