#bboss booter
基于bboss booter容器，内置jetty容器和tomcat容器
部署运行时可以在config.properties中可以配置应用访问上下文和端口
contextPath=demoproject 
port=8080

开发调试实时通过jvm参数设置docBase/contextPaht/port：
-DdocBase=E:/workspace/bbossgroups/bbootdemo/WebRoot 
-DcontextPath=demoproject 
-Dport=8080

# 构建部署
前提：安装和配置好最新的gradle版本，下载源码
## 利用gradle构建发布版本
gradle install

## 运行demo
下载demo
git clone -b master --depth 1 https://github.com/bbossgroups/bbootdemo.git

参考demo中的README.md中的说明运行demo 


