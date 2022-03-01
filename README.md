#bboss booter
基于bboss booter容器，内置jetty容器和tomcat容器
部署运行时可以在config.properties中可以配置应用访问上下文和端口
contextPath=demoproject 
port=8080

开发调试实时通过jvm参数设置docBase/contextPaht/port：
-DdocBase=E:/workspace/bbossgroups/bbootdemo/WebRoot 
-DcontextPath=demoproject 
-Dport=8080

# 版本构建方法

gradle clean publishToMavenLocal

需要通过gradle构建发布版本,gradle安装配置参考文档：

https://esdoc.bbossgroups.com/#/bboss-build


## 运行demo
下载demo
git clone -b master --depth 1 https://github.com/bbossgroups/bbootdemo.git

参考demo中的README.md中的说明运行demo 

## 工程中导入bboot-starter-jetty
maven坐标

<dependency>
  <groupId>com.bbossgroups.boot</groupId>
  <artifactId>bboot-starter-jetty</artifactId>
  <version>5.0.3.8.0</version>
</dependency>

gradle坐标

compile 'com.bbossgroups.boot:bboot-starter-jetty:5.0.3.8.0'

## 工程中导入bboot-starter-tomcat
maven坐标
''''
<dependency>
  <groupId>com.bbossgroups.boot</groupId>
  <artifactId>bboot-starter-tomcat</artifactId>
  <version>5.0.3.8.0</version>
</dependency>
''''

gradle坐标

compile 'com.bbossgroups.boot:bboot-starter-tomcat:5.0.3.8.0'



