#bboss booter
基于bboss booter容器，内置jetty容器和tomcat容器
部署运行时可以在config.properties中可以配置应用访问上下文和端口
web.contextPath=gencode
#web.host=0.0.0.0
web.port=80
web.workTempDir=c:/workspace/bbossgroups/bbootdemo/temp
web.docBase=c:/workspace/bbossgroups/bbootdemo/WebRoot


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
  <version>6.3.5</version>
</dependency>

gradle坐标

compile 'com.bbossgroups.boot:bboot-starter-jetty:6.3.5'

## 工程中导入bboot-starter-tomcat
maven坐标
''''
<dependency>
  <groupId>com.bbossgroups.boot</groupId>
  <artifactId>bboot-starter-tomcat</artifactId>
  <version>6.3.5</version>
</dependency>
''''

gradle坐标

compile 'com.bbossgroups.boot:bboot-starter-tomcat:6.3.5'



