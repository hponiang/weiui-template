# coding: utf-8

Pod::Spec.new do |s|

  s.name         = "WeexSDK"

  s.version      = "0.20.0"

  s.summary      = "WeexSDK Source."

  s.description  = <<-DESC
                   A framework for building Mobile cross-platform UI
                   DESC

  s.homepage     = "https://github.com/alibaba/weex"
  s.license = {
    :type => 'Copyright',
    :text => <<-LICENSE
           Alibaba-INC copyright
    LICENSE
  }
  s.authors      = { 
                    "cxfeng1"      => "cxfeng1@gmail.com",
                    "boboning"     => "ningli928@163.com",
                    "yangshengtao" => "yangshengtao1314@163.com",
                    "kfeagle"      => "sunjjbobo@163.com",
                    "acton393"     => "zhangxing610321@gmail.com",
                    "wqyfavor"     => "wqyfavor88@gmail.com",
                    "doumafang "   => "doumafang@gmail.com"
                   }
  s.platform     = :ios
  s.ios.deployment_target = '8.0'

  # use for public
  # s.source =  { 
  #  :git => 'https://github.com/apache/incubator-weex.git',
  #  :tag => #{s.version}
  # }

  # use for playground
  s.source =  { :path => '.' }

  s.source_files = 'ios/sdk/WeexSDK/Sources/**/*.{h,m,mm,c,cpp,cc}',
                    'weex_core/Source/base/**/*.{h,hpp,m,mm,c,cpp,cc}',
                    'weex_core/Source/core/**/*.{h,hpp,m,mm,c,cpp,cc}',
                    'weex_core/Source/wson/**/*.{h,hpp,m,mm,c,cpp,cc}',
                    'weex_core/Source/third_party/**/*.{h,hpp,m,mm,c,cpp,cc}',
                    'weex_core/Source/include/**/*.{h,hpp,m,mm,c,cpp,cc}'
  s.exclude_files = 'weex_core/Source/**/*android.{h,hpp,m,mm,c,cpp,cc}'

  # 0.21.0 版本开始不再需要 native-bundle-main.js
  s.resources = 'pre-build/*.js','ios/sdk/WeexSDK/Resources/wx_load_error@3x.png'

  s.user_target_xcconfig  = { 'FRAMEWORK_SEARCH_PATHS' => "'$(PODS_ROOT)/WeexSDK'" }
  s.requires_arc = true
  s.prefix_header_file = 'ios/sdk/WeexSDK/Sources/Supporting Files/WeexSDK-Prefix.pch'

  s.xcconfig = { "OTHER_LINK_FLAG" => '$(inherited) -ObjC' }
  s.pod_target_xcconfig = { 'USER_HEADER_SEARCH_PATHS' => '${PODS_ROOT}/WeexSDK/weex_core/Source/ ${PROJECT_DIR}/../../../../plugins/ios/WeexSDK/weex_core/Source',
    'GCC_PREPROCESSOR_DEFINITIONS' => 'OS_IOS=1' }

  s.frameworks = 'CoreMedia','MediaPlayer','AVFoundation','AVKit','JavaScriptCore','GLKit','OpenGLES','CoreText','QuartzCore','CoreGraphics'
  
  s.libraries = 'c++'

end
