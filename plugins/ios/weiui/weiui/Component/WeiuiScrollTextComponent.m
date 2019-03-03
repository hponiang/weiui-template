//
//  WeiuiScrollTextComponent.m
//  WeexTestDemo
//
//  Created by apple on 2018/6/4.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import "WeiuiScrollTextComponent.h"
#import "SKAutoScrollLabel.h"
#import "DeviceUtil.h"

@interface WeiuiScrollTextComponent()

@property (nonatomic, strong) NSString *content;
@property (nonatomic, strong) NSString *ktext;
@property (nonatomic, strong) NSString *color;
@property (nonatomic, assign) NSInteger fontSize;
@property (nonatomic, assign) CGFloat kspeed;
@property (nonatomic, strong) NSString *kbackgroundColor;

@property (nonatomic, strong) SKAutoScrollLabel *skLab;
@property (nonatomic, assign) BOOL isItemClick;

@end

@implementation WeiuiScrollTextComponent

WX_EXPORT_METHOD(@selector(setText:))
WX_EXPORT_METHOD(@selector(addText:))
WX_EXPORT_METHOD(@selector(startScroll))
WX_EXPORT_METHOD(@selector(stopScroll))
WX_EXPORT_METHOD(@selector(isStarting))
WX_EXPORT_METHOD(@selector(setSpeed:))
WX_EXPORT_METHOD(@selector(setTextSize:))
WX_EXPORT_METHOD(@selector(setTextColor:))
WX_EXPORT_METHOD(@selector(setBackgroundColor:))

- (instancetype)initWithRef:(NSString *)ref type:(NSString *)type styles:(NSDictionary *)styles attributes:(NSDictionary *)attributes events:(NSArray *)events weexInstance:(WXSDKInstance *)weexInstance
{
    self = [super initWithRef:ref type:type styles:styles attributes:attributes events:events weexInstance:weexInstance];
    if (self) {
        
        _content = @"";
        _color = @"#000000";
        _kspeed = 2.0f;
        _kbackgroundColor = @"";
        _fontSize = FONT(24);

        for (NSString *key in styles.allKeys) {
            [self dataKey:key value:styles[key] isUpdate:NO];
        }
        for (NSString *key in attributes.allKeys) {
            [self dataKey:key value:attributes[key] isUpdate:NO];
        }
    }
    
    return self;
}

- (void)viewDidLoad
{
    _skLab = [[SKAutoScrollLabel alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    _skLab.text = _content;
    _skLab.textColor = [WXConvert UIColor:_color];
    _skLab.font = [UIFont systemFontOfSize:_fontSize];
    _skLab.scrollSpeed = _kspeed * 10;
    _skLab.direction = SK_AUTOSCROLL_DIRECTION_LEFT;
    _skLab.textAlignment = NSTextAlignmentLeft;
    [self.view addSubview:_skLab];
    
    if (_kbackgroundColor.length > 0) {
        _skLab.backgroundColor = [WXConvert UIColor:_kbackgroundColor];
    }
    
    UIButton *tapBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    tapBtn.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
    tapBtn.backgroundColor = [UIColor clearColor];
    [tapBtn addTarget:self action:@selector(itemPanClick) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:tapBtn];
    
    [self fireEvent:@"ready" params:nil];
}

- (void)updateStyles:(NSDictionary *)styles
{
    for (NSString *key in styles.allKeys) {
        [self dataKey:key value:styles[key] isUpdate:YES];
    }
}

- (void)updateAttributes:(NSDictionary *)attributes
{
    for (NSString *key in attributes.allKeys) {
        [self dataKey:key value:attributes[key] isUpdate:YES];
    }
}

- (void)addEvent:(NSString *)eventName
{
    if ([eventName isEqualToString:@"itemClick"]) {
        _isItemClick = YES;
    }
}

- (void)removeEvent:(NSString *)eventName
{
    if ([eventName isEqualToString:@"itemClick"]) {
        _isItemClick = NO;
    }
}

#pragma mark data
- (void)dataKey:(NSString*)key value:(id)value isUpdate:(BOOL)isUpdate
{
    key = [DeviceUtil convertToCamelCaseFromSnakeCase:key];
    if ([key isEqualToString:@"weiui"] && [value isKindOfClass:[NSDictionary class]]) {
        for (NSString *k in [value allKeys]) {
            [self dataKey:k value:value[k] isUpdate:isUpdate];
        }
    } else if ([key isEqualToString:@"content"]) {
        _content = [WXConvert NSString:value];
        if (isUpdate) {
            _skLab.text = _content;
        }
    } else if ([key isEqualToString:@"text"]) {
        _content = [WXConvert NSString:value];
        if (isUpdate) {
            _skLab.text = _content;
        }
    } else if ([key isEqualToString:@"speed"]) {
        _kspeed = [WXConvert CGFloat:value];
        if (isUpdate) {
            _skLab.scrollSpeed = _kspeed * 10;
        }
    } else if ([key isEqualToString:@"fontSize"]) {
        _fontSize = FONT( [WXConvert NSInteger:value]);
        if (isUpdate) {
            _skLab.font = [UIFont systemFontOfSize:_fontSize];
        }
    } else if ([key isEqualToString:@"color"]) {
        _color = [WXConvert NSString:value];
        if (isUpdate) {
            _skLab.textColor = [WXConvert UIColor:_color];
        }
    } else if ([key isEqualToString:@"backgroundColor"]) {
        _kbackgroundColor = [WXConvert NSString:value];
        if (isUpdate) {
            _skLab.backgroundColor = [WXConvert UIColor:_kbackgroundColor];
        }
    }
}

#pragma mark methods

- (void)setText:(id)value
{
    if (value) {
        _content = [WXConvert NSString:value];
        _skLab.text = _content;
    }
}

- (void)addText:(id)value
{
    if (value) {
        _content = [_content stringByAppendingString:[WXConvert NSString:value]];
        _skLab.text = _content;
    }
}

- (void)startScroll
{
    [_skLab startScroll];
}

- (void)stopScroll
{
    [_skLab stopScroll];
}

- (BOOL)isStarting
{
    return _skLab.scrolling;
}

- (void)setSpeed:(id)value
{
    if (value) {
        _kspeed = [WXConvert CGFloat:value];
        _skLab.scrollSpeed = _kspeed;
    }
}

- (void)setTextSize:(id)value
{
    if (value) {
        _fontSize = [WXConvert NSInteger:value];
        _skLab.font = [UIFont systemFontOfSize:_fontSize];
    }
}

- (void)setTextColor:(id)value
{
    if (value) {
        _color = [WXConvert NSString:value];
        _skLab.textColor = [WXConvert UIColor:_color];
    }
}

- (void)setBackgroundColor:(id)value
{
    if (value) {
        _kbackgroundColor = [WXConvert NSString:value];
        _skLab.backgroundColor = [WXConvert UIColor:_kbackgroundColor];
    }
}

#pragma mark action

- (void)itemPanClick
{
    if (_skLab.scrolling) {
        [self stopScroll];
    } else {
        [self startScroll];
    }
    
    if (_isItemClick) {
        [self fireEvent:@"itemClick" params:@{@"position":@(_skLab.scrolling)}];
    }
}

@end
