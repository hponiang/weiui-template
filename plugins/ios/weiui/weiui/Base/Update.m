//
//  Update.m
//  WeexWeiui
//
//  Created by 高一 on 2018/9/27.
//

#import "Update.h"
#import "Config.h"
#import "SSZipArchive.h"

@implementation Update

//解压文件
+(BOOL) zipToDist:(NSString *)zipFile zipUnDir:(NSString *)zipUnDir
{
    if (![SSZipArchive unzipFileAtPath:zipFile toDestination:zipUnDir]) {
        return NO;
    }
    //
    if (![self weiuiToDist]) {
        return NO;
    }
    //
    NSFileManager *fm = [NSFileManager defaultManager];
    NSString *dist = [Config getPath:@"dist"];
    NSString *zipUnPath;
    NSString *distPath;
    NSString *temp;
    NSError *error;
    NSDirectoryEnumerator *dirEnum = [fm enumeratorAtPath:zipUnDir];
    while ((temp = [dirEnum nextObject]) != nil) {
        zipUnPath = [NSString stringWithFormat:@"%@/%@", zipUnDir, temp];
        distPath = [NSString stringWithFormat:@"%@/%@", dist, temp];
        if ([Config isFileExists:distPath]) {
            [fm removeItemAtPath:distPath error:nil];
        }
        if ([Config isFile:zipUnPath]) {
            [fm copyItemAtPath:zipUnPath toPath:distPath error:&error];
        }else if ([Config isDir:zipUnPath]) {
            [fm createDirectoryAtPath:distPath withIntermediateDirectories:YES attributes:nil error:&error];
        }
    }
    if (error == nil) {
        return YES;
    }else{
        return NO;
    }
}

//将weiui下的文件复制到dist
+ (BOOL) weiuiToDist
{
    NSString *lockFile = [Config getPath:[[NSString alloc] initWithFormat:@"dist/%ld.lock", [Config getLocalVersion]]];
    if ([Config isFile:lockFile]) {
        return YES;
    }
    NSFileManager *fm = [NSFileManager defaultManager];
    NSString *weiui = [NSString stringWithFormat:@"%@/bundlejs/weiui", [NSBundle mainBundle].bundlePath];
    NSString *dist = [Config getPath:@"dist"];
    NSString *weiuiPath;
    NSString *distPath;
    NSString *temp;
    NSError *error;
    NSDirectoryEnumerator *dirEnum = [fm enumeratorAtPath:weiui];
    while ((temp = [dirEnum nextObject]) != nil) {
        weiuiPath = [NSString stringWithFormat:@"%@/%@", weiui, temp];
        distPath = [NSString stringWithFormat:@"%@/%@", dist, temp];
        if ([Config isFileExists:distPath]) {
            [fm removeItemAtPath:distPath error:nil];
        }
        if ([Config isFile:weiuiPath]) {
            [fm copyItemAtPath:weiuiPath toPath:distPath error:&error];
        }else if ([Config isDir:weiuiPath]) {
            [fm createDirectoryAtPath:distPath withIntermediateDirectories:YES attributes:nil error:&error];
        }
    }
    if (error == nil) {
        [fm createFileAtPath:lockFile contents:[[Config getyyyMMddHHmmss] dataUsingEncoding:NSUTF8StringEncoding] attributes:nil];
        return YES;
    }else{
        return NO;
    }
}

@end
