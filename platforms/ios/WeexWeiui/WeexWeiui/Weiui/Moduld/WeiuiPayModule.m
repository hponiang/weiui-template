
#import "WeiuiPayModule.h"
#import <AlipaySDK/AlipaySDK.h>
#import "WXApi.h"
#import "UMSPPPayUnifyPayPlugin.h"

static WXModuleKeepAliveCallback alipayCallback;
static WXModuleKeepAliveCallback weixinCallback;

@implementation WeiuiPayModule

WX_EXPORT_METHOD(@selector(weixin:callback:))
WX_EXPORT_METHOD(@selector(alipay:callback:))
WX_EXPORT_METHOD(@selector(union_weixin:))
WX_EXPORT_METHOD(@selector(union_alipay:))

+ (void)alipayHandleOpenURL:(NSURL *) url
{
    [[AlipaySDK defaultService] processOrderWithPaymentResult:url standbyCallback:^(NSDictionary *resultDic) {
        [WeiuiPayModule onAlipayResp:resultDic];
    }];
}

+ (BOOL)weixinHandleOpenURL:(NSURL *) url
{
    return [WXApi handleOpenURL:url delegate:(id<WXApiDelegate>)[[WeiuiPayModule alloc] init]];
}

//支付宝结果回调
+ (void)onAlipayResp:(NSDictionary *)result
{
    NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
    if (result != nil) {
        [data setObject:result[@"resultStatus"] forKey:@"status"];
        [data setObject:result[@"result"] forKey:@"result"];
        if ([data[@"status"] isEqualToString:@"9000"] && !result[@"memo"]) {
            [data setObject:@"支付成功" forKey:@"memo"];
        }else{
            [data setObject:result[@"memo"] forKey:@"memo"];
        }
    }
    if (alipayCallback != nil) {
        alipayCallback(data, NO);
        alipayCallback = nil;
    }
}

// 微信支付结果回调
- (void)onResp:(BaseResp *)resp
{
    if (weixinCallback != nil) {
        NSString *msg;
        if (resp.errCode == 0) {
            msg = @"支付成功";
        }else if (resp.errCode == -1) {
            msg = @"可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等";
        }else if (resp.errCode == -2) {
            msg = @"用户取消";
        }else{
            return;
        }
        weixinCallback(@{@"status":@(resp.errCode), @"msg":msg}, NO);
        weixinCallback = nil;
    }
}

/*******************************************************************************************/
/*******************************************************************************************/
/*******************************************************************************************/

//官方微信支付
- (void)weixin:(NSDictionary *)payData callback:(WXModuleKeepAliveCallback)callback
{
    weixinCallback = callback;
    [WXApi registerApp:payData[@"appid"]];
    PayReq *req  = [[PayReq alloc] init];
    req.partnerId = payData[@"partnerid"];
    req.prepayId = payData[@"prepayid"];
    req.package = payData[@"package"];
    req.nonceStr = payData[@"noncestr"];
    req.timeStamp = [payData[@"timestamp"] intValue];
    req.sign = payData[@"sign"];
    if (![WXApi sendReq:req]) {
        if (weixinCallback != nil) {
            weixinCallback(@{@"status":@(-999), @"msg":@"启动微信支付失败"}, NO);
            weixinCallback = nil;
        }
    }
}

//官方支付宝支付
- (void)alipay:(NSString*)payData callback:(WXModuleKeepAliveCallback)callback
{
    alipayCallback = callback;
    [[AlipaySDK defaultService] payOrder:payData fromScheme:@"weiuiApp_xxxxxxxx" callback:^(NSDictionary *resultDic) {
        [WeiuiPayModule onAlipayResp:resultDic];
    }];
}

//银联微信支付（无回调功能）
- (void)union_weixin:(NSString*)payData
{
    [UMSPPPayUnifyPayPlugin payWithPayChannel:CHANNEL_WEIXIN payData:payData callbackBlock:^(NSString *resultCode, NSString *resultInfo) {
        
    }];
}

//银联支付宝支付（无回调功能）
- (void)union_alipay:(NSString*)payData
{
    [UMSPPPayUnifyPayPlugin payWithPayChannel:CHANNEL_ALIPAY payData:payData callbackBlock:^(NSString *resultCode, NSString *resultInfo) {
        
    }];
}

@end
