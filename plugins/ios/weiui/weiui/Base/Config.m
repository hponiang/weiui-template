//
//  Config.m
//  WeexWeiui
//
//  Created by 高一 on 2018/9/27.
//

#import "Config.h"
#import <CommonCrypto/CommonDigest.h>

@implementation Config

static BOOL configDataIsDist;
static NSMutableDictionary *configData;

//读取配置
+ (NSMutableDictionary *) get
{
    NSString *dist = [self getPath:@"dist"];
    //判断创建文件夹
    if (![[NSFileManager defaultManager] fileExistsAtPath:dist]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:dist withIntermediateDirectories:YES attributes:nil error:nil];
    }
    //读取json
    if (configData == nil) {
        NSString *lockFile = [self getPath:[[NSString alloc] initWithFormat:@"dist/%ld.lock", [self getLocalVersion]]];
        NSString *jsonFile = [self getPath:@"dist/config.json"];
        if ([self isFile:lockFile] && [self isFile:jsonFile]) {
            configDataIsDist = YES;
        }else{
            jsonFile = [[ NSBundle mainBundle ] pathForResource : @"bundlejs/weiui/config" ofType : @"json" ];
        }
        NSData *fileData = [[ NSData alloc ] initWithContentsOfFile :jsonFile];
        NSDictionary *jsonObject = [NSJSONSerialization JSONObjectWithData:fileData options:kNilOptions error:nil];
        configData = [NSMutableDictionary dictionaryWithDictionary:jsonObject];
    }
    return configData;
}

//清除配置
+ (void) clear
{
    configDataIsDist = NO;
    configData = nil;
}

//获取配置值
+ (NSString *) getString:(NSString*)key defaultVal:(NSString *)defaultVal
{
    NSMutableDictionary *json = [self get];
    if (json == nil) {
        return defaultVal;
    }
    NSString *str = [NSString stringWithFormat:@"%@", json[key]];
    if (str == nil) {
        return defaultVal;
    }
    if ([str isEqual:[NSNull null]] || [str isEqualToString:@"(null)"]) {
        return defaultVal;
    }
    if (!str.length) {
        return defaultVal;
    }
    return str;
}

//获取配置值
+ (NSMutableDictionary *) getObject:(NSString*)key
{
    NSMutableDictionary *json = [self get];
    if (json == nil) {
        return nil;
    }
    return [json objectForKey:key];
}

//获取主页地址
+ (NSString *) getHome
{
    NSString *homePage = [self getString:@"homePage" defaultVal:@""];
    if (homePage.length == 0) {
        if (configDataIsDist) {
            NSString *indexFile = [self getPath:@"dist/index.js"];
            if ([self isFile:indexFile]) {
                homePage = [NSString stringWithFormat:@"file://%@", indexFile];
            }
        }
    }
    if (homePage.length == 0) {
        homePage = [NSString stringWithFormat:@"file://%@/bundlejs/weiui/index.js", [NSBundle mainBundle].bundlePath];
    }
    return homePage;
}

//获取主页配置值
+ (NSString *) getHomeParams:(NSString*)key defaultVal:(NSString *)defaultVal
{
    NSDictionary *params = [self getObject:@"homePageParams"];
    if (params == nil) {
        return defaultVal;
    }
    NSString *str = [NSString stringWithFormat:@"%@", params[key]];
    if (str == nil) {
        return defaultVal;
    }
    if ([str isEqual:[NSNull null]] || [str isEqualToString:@"(null)"]) {
        return defaultVal;
    }
    if (!str.length) {
        return defaultVal;
    }
    return str;
}

//判断是否
+ (BOOL) isConfigDataIsDist
{
    return configDataIsDist;
}

//******************************************************************************************
//******************************************************************************************
//******************************************************************************************


//获取路径
+ (NSString *) getPath:(NSString*)name
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    return [[NSString alloc] initWithFormat:@"%@/%@/%@", [paths objectAtIndex:0], [[NSBundle mainBundle]bundleIdentifier], name];
}

//获取版本号
+ (NSInteger) getLocalVersion
{
    NSString *version = [[[NSBundle mainBundle] infoDictionary]  objectForKey:@"CFBundleVersion"];
    NSArray *list = [version componentsSeparatedByString:@"."];
    if (list.count > 0) {
        return [list.lastObject integerValue];
    } else {
        return [version integerValue];
    }
}

//获取版本名称
+ (NSString*) getLocalVersionName
{
    return (NSString*)[[[NSBundle mainBundle] infoDictionary]  objectForKey:@"CFBundleShortVersionString"];
}

//文件是否存在
+ (BOOL) isFileExists:(NSString*)path
{
    if ([[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:nil]) {
        return YES;
    }
    return NO;
}

//判断是否文件（不存在返回NO）
+ (BOOL) isFile:(NSString*)path
{
    BOOL isDir;
    if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
        return NO;
    }
    if (isDir) {
        return NO;
    }else{
        return YES;
    }
}

//判断是否文件夹（不存在返回NO）
+ (BOOL) isDir:(NSString*)path
{
    BOOL isDir;
    if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
        return NO;
    }
    if (isDir) {
        return YES;
    }else{
        return NO;
    }
}

//获取系统当前时间
+ (NSString *) getyyyMMddHHmmss
{
    NSDate * date = [NSDate date];
    NSTimeInterval sec = [date timeIntervalSinceNow];
    NSDate * currentDate = [[NSDate alloc] initWithTimeIntervalSinceNow:sec];
    NSDateFormatter * df = [[NSDateFormatter alloc] init ];
    [df setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    return [df stringFromDate:currentDate];
}

//MD5加密32位大写
+ (NSString *) MD5ForLower32Bate:(NSString *)str
{
    const char* input = [str UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(input, (CC_LONG)strlen(input), result);
    NSMutableString *digest = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for (NSInteger i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [digest appendFormat:@"%02X", result[i]];
    }
    return digest;
}

//获取中间字符串
+ (NSString *) getMiddle:(NSString *)string start:(nullable NSString *)startString to:(nullable NSString *)endString {
    NSString *text = string;
    if (text.length) {
        if (startString != nil && startString.length && [text containsString:startString]) {
            NSRange startRange = [text rangeOfString:startString];
            NSRange range = NSMakeRange(startRange.location + startRange.length, text.length - startRange.location - startRange.length);
            text = [text substringWithRange:range];
        }
        if (endString != nil && endString.length && [text containsString:endString]) {
            NSRange endRange = [text rangeOfString:endString];
            NSRange range = NSMakeRange(0, endRange.location);
            text = [text substringWithRange:range];
        }
    }
    return text;
}

@end
