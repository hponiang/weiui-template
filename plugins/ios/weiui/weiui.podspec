

Pod::Spec.new do |s|

 

  s.name         = "weiui"
  s.version      = "0.0.1"
  s.summary      = "weiui plugin."
  s.description  = <<-DESC
                    weiui plugin.
                   DESC

  s.homepage     = "http://weiui.cc"
  s.license      = "MIT"
  s.author             = { "veryitman" => "aipaw@live.cn" }
  s.source =  { :path => '.' }
  s.source_files  = "weiui", "**/**/*.{h,m,mm,c}"
  s.exclude_files = "Source/Exclude"
  s.resources = ['weiui/Source/*.*', 
                  'weiui/Utility/CCNScan/*.png',
                  'weiui/Utility/MJRefresh/MJRefresh.bundle']
  s.platform     = :ios, "8.0"
  s.requires_arc = true  
  
  s.dependency 'WeexSDK'
  s.dependency 'WeexPluginLoader', '~> 0.0.1.9.1'
  
end
