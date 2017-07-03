# tinker-manager

[![license](https://img.shields.io/hexpm/l/plug.svg)](https://raw.githubusercontent.com/baidao/tinker-manager/master/LICENSE)

[ ![Download](https://api.bintray.com/packages/typ0520/maven/com.dx168.patchsdk%3Apatchsdk/images/download.svg) ](https://bintray.com/typ0520/maven/com.dx168.patchsdk%3Apatchsdk/_latestVersion)

关于Tinker请移步: https://github.com/Tencent/tinker

[服务器端部署文档](https://github.com/baidao/tinker-manager/tree/master/patchserver).
[客户端接入文档](https://github.com/baidao/tinker-manager/tree/master/patchsdk).

#License
Apache License 2.0

## CHANGELIST
server

```
v1.0.5 增加对debug工具的支持，在发布前可以先使用提供的debug工具调试，然后在全量发布
v1.0.6 补全包名时不在检查重复;解决ajax提交表单登录超时报系统异常的问题
v1.0.7 修复windows部署时路径的问题
v1.0.8 修复若干bug，如果使用老版本最好更新
v1.1.0 增加子账号管理功能，增加对App全量更新的支持
```

patchsdk

```
v1.0.7 修复patchsdk空指针错误
v1.0.8 增加对debug工具的支持
v1.0.9 优化对debug工具的支持
v1.1.0 修复bug增加容错处理
v1.1.4 1) 修改上报时机从patch success改到load success
	   2) targetSdkVersion>=23获取deviceId时READ_PHONE_STATE权限容错处理
v1.2.0 增加App全量更新支持
```

debugtool

```
v1.0.0 基础功能
v1.0.1 优化流程，增加自动更新的功能
```

[war包下载](https://pan.baidu.com/s/1minrdgO#list/path=%2Ftinker-manager).
[sdk下载](https://bintray.com/typ0520/maven/com.dx168.patchsdk%3Apatchsdk/_latestVersion).
[debug工具下载](http://fir.im/tpks).

QQ群: 338491549

## 后台效果
![补丁详情](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/1.jpg "Title")
![上传补丁](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/2.jpg "Title")
![发布补丁](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/3.jpg "Title")
![应用列表](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/4.jpg "Title")
![应用管理](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/5.jpg "Title")
![测试人员管理](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/6.jpg "Title")
![机型黑名单管理](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/7.jpg "Title")
![App全量更新管理](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/8.jpg "Title")
![子账号管理](https://raw.githubusercontent.com/baidao/tinker-manager/master/assets/9.jpg "Title")



