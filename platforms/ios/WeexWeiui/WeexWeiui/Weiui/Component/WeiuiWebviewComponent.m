//
//  WeiuiWebviewComponent.m
//  WeexTestDemo
//
//  Created by apple on 2018/6/5.
//  Copyright © 2018年 TomQin. All rights reserved.
//

#import "WeiuiWebviewComponent.h"
#import "DeviceUtil.h"
#import "YHWebViewProgress.h"
#import "YHWebViewProgressView.h"

@interface WeiuiWebView : UIWebView

@end

@implementation WeiuiWebView


@end

@interface WeiuiWebviewComponent() <UIWebViewDelegate>

@property (nonatomic, strong) NSString *content;
@property (nonatomic, strong) NSString *url;
@property (nonatomic, strong) NSString *title;
@property (nonatomic, assign) BOOL isShowProgress;
@property (nonatomic, assign) BOOL isScrollEnabled;
@property (nonatomic, assign) BOOL isHeightChanged;
@property (strong, nonatomic) YHWebViewProgress *progressProxy;

@end

@implementation WeiuiWebviewComponent

WX_EXPORT_METHOD(@selector(setContent:))
WX_EXPORT_METHOD(@selector(setUrl:))
WX_EXPORT_METHOD(@selector(setProgressbarVisibility:))
WX_EXPORT_METHOD(@selector(setScrollEnabled:))
WX_EXPORT_METHOD(@selector(canGoBack:))
WX_EXPORT_METHOD(@selector(goBack:))
WX_EXPORT_METHOD(@selector(canGoForward:))
WX_EXPORT_METHOD(@selector(goForward:))

- (instancetype)initWithRef:(NSString *)ref type:(NSString *)type styles:(NSDictionary *)styles attributes:(NSDictionary *)attributes events:(NSArray *)events weexInstance:(WXSDKInstance *)weexInstance
{
    self = [super initWithRef:ref type:type styles:styles attributes:attributes events:events weexInstance:weexInstance];
    if (self) {
        _url = @"";
        _content = @"";
        _title = @"";
        _isShowProgress = YES;
        _isScrollEnabled = YES;
        _isHeightChanged = [events containsObject:@"heightChanged"];
        
        for (NSString *key in styles.allKeys) {
            [self dataKey:key value:styles[key] isUpdate:NO];
        }
        for (NSString *key in attributes.allKeys) {
            [self dataKey:key value:attributes[key] isUpdate:NO];
        }
    }
    
    return self;
}

- (UIView*)loadView
{
    return [[WeiuiWebView alloc] init];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    WeiuiWebView *webView = (WeiuiWebView*)self.view;
    
    YHWebViewProgressView *progressView = [[YHWebViewProgressView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.bounds), 2)];
    progressView.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleBottomMargin;
    self.progressProxy = [[YHWebViewProgress alloc] init];
    self.progressProxy.progressView = progressView;
    self.progressProxy.progressView.hidden = !_isShowProgress;
    [self.view addSubview:progressView];
    
    ((UIScrollView *)[webView.subviews objectAtIndex:0]).scrollEnabled = _isScrollEnabled;
    
    webView.delegate = self;
    
    if (_url.length > 0) {
        if (![_url hasPrefix:@"http://"] && ![_url hasPrefix:@"https://"]) {
            _url = [NSString stringWithFormat:@"http://%@", _url];
        }
        NSURL *url = [NSURL URLWithString:_url];
        NSURLRequest *request =[NSURLRequest requestWithURL:url];
        [webView loadRequest:request];
    }
    
    if (_content.length > 0) {
        [webView loadHTMLString:_content baseURL:nil];
    }
    
    [self fireEvent:@"ready" params:nil];
    
    if (_isHeightChanged) {
        [webView.scrollView addObserver:self forKeyPath:@"contentSize" options:NSKeyValueObservingOptionNew context:nil];
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    WeiuiWebView *webView = (WeiuiWebView*)self.view;
    if ([keyPath isEqualToString:@"contentSize"]) {
        CGFloat webViewHeight = webView.scrollView.contentSize.height;
        [self fireEvent:@"heightChanged" params:@{@"height":@(750 * 1.0 / [UIScreen mainScreen].bounds.size.width * webViewHeight)}];
    }
}

-(void)viewDidUnload {
    if (_isHeightChanged) {
        WeiuiWebView *webView = (WeiuiWebView*)self.view;
        [webView.scrollView removeObserver:self forKeyPath:@"contentSize" context:nil];
    }
    [super viewDidUnload];
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
            [self setContent:_content];
        }
    } else if ([key isEqualToString:@"url"]) {
        _url = [WXConvert NSString:value];
        if (isUpdate) {
            [self setUrl:_url];
        }
    } else if ([key isEqualToString:@"progressbarVisibility"]) {
        _isShowProgress = [WXConvert BOOL:value];
        if (isUpdate) {
            [self setProgressbarVisibility:_isShowProgress];
        }
    } else if ([key isEqualToString:@"scrollEnabled"]) {
        _isScrollEnabled = [WXConvert BOOL:value];
        if (isUpdate) {
            [self setScrollEnabled:_isScrollEnabled];
        }
    }
}

#pragma mark delegate
//是否允许加载网页，也可获取js要打开的url，通过截取此url可与js交互
- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType
{
    return YES;
}

//开始加载网页
- (void)webViewDidStartLoad:(UIWebView *)webView
{
    if (_isShowProgress) {
        [self.progressProxy startProgress];
    }
    [self fireEvent:@"stateChanged" params:@{@"status":@"start", @"title":@"", @"errCode":@"", @"errMsg":@"", @"errUrl":@""}];
}

//网页加载完成
- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    if (_isShowProgress) {
        [self.progressProxy completeProgress];
    }
    NSString *title = [webView stringByEvaluatingJavaScriptFromString:@"document.title"];
    if (![title isEqualToString:_title]) {
        _title = title;
        [self fireEvent:@"stateChanged" params:@{@"status":@"title", @"title":_title, @"errCode":@"", @"errMsg":@"", @"errUrl":@""}];
    }

    [self fireEvent:@"stateChanged" params:@{@"status":@"success", @"title":@"", @"errCode":@"", @"errMsg":@"", @"errUrl":@""}];
}

//网页加载错误
- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    if (_isShowProgress) {
        [self.progressProxy completeProgress];
    }
    if (error) {
        NSString *code = [NSString stringWithFormat:@"%ld", error.code];
        NSString *msg = [NSString stringWithFormat:@"%@", error.description];

        [self fireEvent:@"stateChanged" params:@{@"status":@"error", @"title":@"", @"errCode":code, @"errMsg":msg, @"errUrl":_url}];
    }
}

#pragma mark set
//设置浏览器内容
- (void)setContent:(NSString*)content
{
    WeiuiWebView *webView = (WeiuiWebView*)self.view;
    [webView loadHTMLString:content baseURL:nil];
}

//设置浏览器地址
- (void)setUrl:(NSString*)urlStr
{
    WeiuiWebView *webView = (WeiuiWebView*)self.view;

    if (![urlStr hasPrefix:@"http://"] && ![urlStr hasPrefix:@"https://"]) {
        urlStr = [NSString stringWithFormat:@"http://%@", urlStr];
    }
    _url = urlStr;
    NSURL *url = [NSURL URLWithString:urlStr];
    NSURLRequest *request =[NSURLRequest requestWithURL:url];
    [webView loadRequest:request];
}

//是否显示进度条
- (void)setProgressbarVisibility:(BOOL)var
{
    _isShowProgress = var;
    if (_isShowProgress == NO) {
        [self.progressProxy completeProgress];
    }
    self.progressProxy.progressView.hidden = !_isShowProgress;
}

//设置是否允许滚动
- (void)setScrollEnabled:(BOOL)var
{
    _isScrollEnabled = var;
    WeiuiWebView *webView = (WeiuiWebView*)self.view;
    ((UIScrollView *)[webView.subviews objectAtIndex:0]).scrollEnabled = var;
}

//是否可以后退
- (void)canGoBack:(WXModuleKeepAliveCallback)callback
{
    WeiuiWebView *webView = (WeiuiWebView*)self.view;
    callback(@(webView.canGoBack), NO);
}

//后退并返回是否后退成功
- (void)goBack:(WXModuleKeepAliveCallback)callback
{
    WeiuiWebView *webView = (WeiuiWebView*)self.view;

    if (webView.canGoBack) {
        [webView goBack];
    }
    callback(@(webView.canGoBack), NO);
}

//是否可以前进
- (void)canGoForward:(WXModuleKeepAliveCallback)callback
{
    WeiuiWebView *webView = (WeiuiWebView*)self.view;
    callback(@(webView.canGoForward), NO);
}

//前进并返回是否前进成功
- (void)goForward:(WXModuleKeepAliveCallback)callback
{
    WeiuiWebView *webView = (WeiuiWebView*)self.view;
    if (webView.canGoForward) {
        [webView goForward];
    }
    callback(@(webView.canGoForward), NO);
}


@end
