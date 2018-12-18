//
//  WeiuiShareManager.m
//  WeexTestDemo
//
//  Created by apple on 2018/6/7.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import "WeiuiShareManager.h"
#import "DeviceUtil.h"
#import "SDWebImageManager.h"
#import "UIButton+WebCache.h"

@implementation WeiuiShareManager

+ (WeiuiShareManager *)sharedIntstance {
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (void)shareText:(NSString*)text
{
    if (text.length == 0) {
        return;
    }
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:@[text] applicationActivities:nil];
    activityVC.completionWithItemsHandler = ^(NSString *activityType,BOOL completed,NSArray *returnedItems,NSError *activityError)
    {
        NSLog(@"%@", activityType);
        if (completed) {
            NSLog(@"分享成功");
        } else {
            NSLog(@"分享失败");
        }
    };
    
    [[DeviceUtil getTopviewControler] presentViewController:activityVC animated:YES completion:nil];
}

- (void)shareImage:(NSString*)imgUrl
{
    if (imgUrl.length == 0) {
        return;
    }
    //获取图片
//    NSString * imageUrl = [imgUrl stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSString * imageUrl = [imgUrl stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet characterSetWithCharactersInString:@"`#%^{}\"[]|\\<> "].invertedSet];

    UIImage *newImage = [[SDImageCache sharedImageCache] imageFromDiskCacheForKey:imageUrl];//用地址去本地找图片
    if (newImage != nil) {//如果本地有
        [self imageShare:newImage];
    } else {//如果本地没有
        //下载图片
        [SDWebImageDownloader.sharedDownloader downloadImageWithURL:[NSURL URLWithString:imageUrl] options:SDWebImageDownloaderLowPriority progress:nil completed:^(UIImage * _Nullable image, NSData * _Nullable data, NSError * _Nullable error, BOOL finished) {
            if (image) {
                [self imageShare:image];
            }
        }];
    }
}

- (void)imageShare:(UIImage*)image
{
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:@[image] applicationActivities:nil];
    activityVC.completionWithItemsHandler = ^(NSString *activityType,BOOL completed,NSArray *returnedItems,NSError *activityError)
    {
        NSLog(@"%@", activityType);
        if (completed) {
            NSLog(@"分享成功");
        } else {
            NSLog(@"分享失败");
        }
    };
    
    [[DeviceUtil getTopviewControler] presentViewController:activityVC animated:YES completion:nil];
}

@end
