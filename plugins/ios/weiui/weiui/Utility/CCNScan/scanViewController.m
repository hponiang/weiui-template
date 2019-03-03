//
//  scanViewController.m
//  CCNScan
//
//  Created by zcc on 16/4/14.
//  Copyright © 2016年 CCN. All rights reserved.
//

#import "scanViewController.h"
#import "resultViewController.h"
#import <AVFoundation/AVFoundation.h>
#import "LBXScanView.h"

#define mainWidth [UIScreen mainScreen].bounds.size.width
#define mainHeight [UIScreen mainScreen].bounds.size.height

@interface scanViewController ()<UINavigationControllerDelegate,UIImagePickerControllerDelegate,AVCaptureMetadataOutputObjectsDelegate>{
    UIImagePickerController *imagePicker;
}

@property ( strong , nonatomic ) AVCaptureDevice * device;
@property ( strong , nonatomic ) AVCaptureDeviceInput * input;
@property ( strong , nonatomic ) AVCaptureMetadataOutput * output;
@property ( strong , nonatomic ) AVCaptureSession * session;
@property ( strong , nonatomic ) AVCaptureVideoPreviewLayer * previewLayer;

@property ( strong , nonatomic ) LBXScanView *scanView;
@property ( strong , nonatomic ) LBXScanViewStyle *scanStyle;

@property (nonatomic,assign)BOOL isScanSuccess;

@end

@implementation scanViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.extendedLayoutIncludesOpaqueBars = NO;
    self.modalPresentationCapturesStatusBarAppearance = NO;
    
    UIBarButtonItem *navRightButton = [[UIBarButtonItem alloc]initWithTitle:@"相册" style:UIBarButtonItemStylePlain target:self action:@selector(choicePhoto)];
    self.navigationItem.rightBarButtonItem = navRightButton;
    self.navigationItem.title = @"二维码/条码";
    
    //扫描框
    [self loadScanerView];
    
    //开始扫描
    [self startScan];
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear: animated];
    
    [self.navigationController setNavigationBarHidden:NO animated:YES];

    if (_session != nil) {
        [self.session startRunning];
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if (self.scanView) {
        [self.scanView startScanAnimation];
    }
}

- (void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear: animated];
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];

    [self.session stopRunning];
    [self.scanView stopScanAnimation];
}

- (void)loadScanerView
{
    self.scanStyle = [[LBXScanViewStyle alloc] init];
    self.scanStyle.animationImage = [UIImage imageNamed:@"qrcode_Scan_weixin_Line"];
    self.scanStyle.colorAngle = [UIColor greenColor];
    
    self.scanView = [[LBXScanView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height) style:self.scanStyle];
    [self.view addSubview:self.scanView];
    NSLog(@"%f", self.view.frame.origin.y);
    if (self.desc.length > 0) {
        UILabel *lab = [[UILabel alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height - 200, self.view.frame.size.width, 50)];
        lab.textColor = [UIColor whiteColor];
        lab.textAlignment = NSTextAlignmentCenter;
        lab.font = [UIFont systemFontOfSize:16.0f];
        lab.text = self.desc;
        [self.view addSubview:lab];
    }
}

- (void)startScan
{
    [self.session addInput:self.input];
    [self.session addOutput:self.output];
    [self.session setSessionPreset:AVCaptureSessionPresetHigh];   //高质量采集
    //扫码类型，需要先将输出流添加到捕捉会话后再进行设置
    [self.output setMetadataObjectTypes:@[AVMetadataObjectTypeQRCode,//二维码
                                          //以下为条形码，如果项目只需要扫描二维码，下面都不要写
                                          AVMetadataObjectTypeEAN13Code,
                                          AVMetadataObjectTypeEAN8Code,
                                          AVMetadataObjectTypeUPCECode,
                                          AVMetadataObjectTypeCode39Code,
                                          AVMetadataObjectTypeCode39Mod43Code,
                                          AVMetadataObjectTypeCode93Code,
                                          AVMetadataObjectTypeCode128Code,
                                          AVMetadataObjectTypePDF417Code]];
    //设置输出流delegate,在主线程刷新UI
    [self.output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
    //预览层
    [self.view.layer insertSublayer:self.previewLayer atIndex:0];
    self.previewLayer.frame = self.view.frame;

    //设置扫描范围 output.rectOfInterest
    
    [self.session startRunning];
}


//扫码回调
- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection {
    if (!_isScanSuccess){
        NSString *content = @"";
        AVMetadataMachineReadableCodeObject *metadataObject = metadataObjects.firstObject;
        content = metadataObject.stringValue;
        
        if (![content isEqualToString:@""]) {
            //震动
            [self playBeep];
            _isScanSuccess = YES;

            NSDictionary *dic = @{@"status":@"success", @"url":content, @"source":@"photo"};
            self.scanerBlock(dic);
            
            if (self.successClose) {
                [self.navigationController popViewControllerAnimated:YES];
            }
//            resultViewController *result = [[resultViewController alloc]init];
//            result.content = content;
//            [self.navigationController pushViewController:result animated:NO];
        }else{
            NSLog(@"没内容");
            NSDictionary *dic = @{@"status":@"error", @"url":@"", @"source":@"photo"};
            self.scanerBlock(dic);
        }
    }
}

#pragma mark - 从相册识别二维码
- (void)choicePhoto{
    imagePicker = [[UIImagePickerController alloc]init];
    imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    imagePicker.delegate = self;
    [self presentViewController:imagePicker animated:YES completion:nil];
}

//音效震动
#define SOUNDID  1109  //1012 -iphone   1152 ipad  1109 ipad

- (void)playBeep
{
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    
    AudioServicesPlaySystemSound(SOUNDID);

}

#pragma mark - ImagePickerDelegate
-(void)imagePickerController:(UIImagePickerController*)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    NSString *content = @"" ;
    //取出选中的图片
    UIImage *pickImage = info[UIImagePickerControllerOriginalImage];
    NSData *imageData = UIImagePNGRepresentation(pickImage);
    CIImage *ciImage = [CIImage imageWithData:imageData];
    
    //创建探测器
    CIDetector *detector = [CIDetector detectorOfType:CIDetectorTypeQRCode context:nil options:@{CIDetectorAccuracy: CIDetectorAccuracyLow}];
    NSArray *feature = [detector featuresInImage:ciImage];
    
    //取出探测到的数据
    for (CIQRCodeFeature *result in feature) {
        content = result.messageString;
    }
    __weak typeof(self) weakSelf = self;
    //选中图片后先返回扫描页面，然后跳转到新页面进行展示
    [picker dismissViewControllerAnimated:NO completion:^{
      
        if (![content isEqualToString:@""]) {
            //震动
            [weakSelf playBeep];
            
            NSDictionary *dic = @{@"status":@"success", @"url":content, @"source":@"camera"};
            self.scanerBlock(dic);
            if (self.successClose) {
                [self.navigationController popViewControllerAnimated:YES];
            }
//            resultViewController *result = [[resultViewController alloc]init];
//            result.content = content;
//            [weakSelf.navigationController pushViewController:result animated:NO];
        }else{
            NSLog(@"没扫到东西");
            NSDictionary *dic = @{@"status":@"success", @"url":@"", @"source":@"camera"};
            self.scanerBlock(dic);
        }
    }];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    [picker dismissViewControllerAnimated:YES completion:nil];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (AVCaptureDevice *)device
{
    if (_device == nil) {
        _device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    }
    return _device;
}

- (AVCaptureDeviceInput *)input
{
    if (_input == nil) {
        _input = [AVCaptureDeviceInput deviceInputWithDevice:self.device error:nil];
    }
    return _input;
}

- (AVCaptureSession *)session
{
    if (_session == nil) {
        _session = [[AVCaptureSession alloc] init];
    }
    return _session;
}

- (AVCaptureVideoPreviewLayer *)previewLayer
{
    if (_previewLayer == nil) {
        _previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.session];
    }
    return _previewLayer;
}

- (AVCaptureMetadataOutput *)output
{
    if (_output == nil) {
        _output = [[AVCaptureMetadataOutput alloc] init];
    }
    return _output;
}



@end
