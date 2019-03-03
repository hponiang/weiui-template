//
//  WeiuiRippleComponent.m
//  WeexTestDemo
//
//  Created by apple on 2018/7/2.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import "WeiuiRippleComponent.h"

@implementation WeiuiRippleComponent


- (instancetype)initWithRef:(NSString *)ref type:(NSString *)type styles:(NSDictionary *)styles attributes:(NSDictionary *)attributes events:(NSArray *)events weexInstance:(WXSDKInstance *)weexInstance
{
    self = [super initWithRef:ref type:type styles:styles attributes:attributes events:events weexInstance:weexInstance];
    if (self) {
    }
    
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // 单击的 Recognizer
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(itemClick)];
    tap.numberOfTapsRequired = 1;
    [self.view addGestureRecognizer:tap];
    
    [self fireEvent:@"ready" params:nil];
}

- (void)itemClick
{
    [self fireEvent:@"itemClick" params:nil];
}

- (void)insertSubview:(WXComponent *)subcomponent atIndex:(NSInteger)index
{
    [super insertSubview:subcomponent atIndex:index];
}

@end
