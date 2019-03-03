/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#import "WXImgLoaderDefaultImpl.h"
#import "UIImageView+WebCache.h"
#import "DeviceUtil.h"

#define MIN_IMAGE_WIDTH 36
#define MIN_IMAGE_HEIGHT 36

#if OS_OBJECT_USE_OBJC
#undef  WXDispatchQueueRelease
#undef  WXDispatchQueueSetterSementics
#define WXDispatchQueueRelease(q)
#define WXDispatchQueueSetterSementics strong
#else
#undef  WXDispatchQueueRelease
#undef  WXDispatchQueueSetterSementics
#define WXDispatchQueueRelease(q) (dispatch_release(q))
#define WXDispatchQueueSetterSementics assign
#endif

@interface WXImgLoaderDefaultImpl()

@property (WXDispatchQueueSetterSementics, nonatomic) dispatch_queue_t ioQueue;

@end

@implementation WXImgLoaderDefaultImpl

#pragma mark -
#pragma mark WXImgLoaderProtocol

- (id<WXImageOperationProtocol>)downloadImageWithURL:(NSString *)url imageFrame:(CGRect)imageFrame userInfo:(NSDictionary *)userInfo completed:(void(^)(UIImage *image,  NSError *error, BOOL finished))completedBlock
{
    url = [DeviceUtil rewriteUrl:url];
    
    return (id<WXImageOperationProtocol>)[SDWebImageDownloader.sharedDownloader downloadImageWithURL:[NSURL URLWithString:url] options:SDWebImageDownloaderLowPriority progress:nil completed:^(UIImage * _Nullable image, NSData * _Nullable data, NSError * _Nullable error, BOOL finished) {
        if (completedBlock) {
            
            if (!image) {
                image = [UIImage imageWithContentsOfFile:url];
                if (!image) {
                    //缓存
                    NSString *newUrl = [url stringByReplacingOccurrencesOfString:@"file://" withString:@""];
                    image = [[SDImageCache sharedImageCache] imageFromDiskCacheForKey:newUrl];
                    if (image) {
                        error = nil;
                    }
                }
            }
            
            completedBlock(image, error, finished);
        }
    }];
}
//
//- (void)setImageViewWithURL:(UIImageView *)imageView url:(NSURL *)url placeholderImage:(UIImage *)placeholder options:(NSDictionary *)options progress:(void (^)(NSInteger, NSInteger))progressBlock completed:(void (^)(UIImage *, NSError *, WXImageLoaderCacheType, NSURL *))completedBlock
//{
//    SDWebImageOptions sdWebimageOption = SDWebImageRetryFailed;
//    if (options && options[@"sdWebimageOption"]) {
//        [options[@"sdWebimageOption"] intValue];
//    }
//
//    [imageView sd_setImageWithURL:url placeholderImage:placeholder options:sdWebimageOption progress:^(NSInteger receivedSize, NSInteger expectedSize) {
//        if (progressBlock) {
//            progressBlock(receivedSize, expectedSize);
//        }
//    } completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
//        if (completedBlock) {
//            completedBlock(image, error, (WXImageLoaderCacheType)cacheType, imageURL);
//        }
//    }];
//}

@end
