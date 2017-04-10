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
```

patchsdk
```
v1.0.7 修复patchsdk空指针错误
v1.0.8 增加对debug工具的支持
v1.0.9 优化对debug工具的支持
v1.1.0 修复bug增加容错处理
v1.1.4 1) 修改上报时机从patch success改到load success
	   2) targetSdkVersion>=23获取deviceId时READ_PHONE_STATE权限容错处理
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
![补丁详情](http://img1.ph.126.net/pofzmHW665Kku85cT_nsTA==/6631721975120842573.png "Title")
![上传补丁](http://img1.ph.126.net/GJmvB7Oc4xQFTIOhXdBq8Q==/6631515266934546508.png "Title")
![发布补丁](http://img0.ph.126.net/oMbFJ-ENd5mAQNiwTK8lhQ==/6631885802350009215.png "Title")
![应用列表](http://img1.ph.126.net/mcuYkRGWx6prlwJEUl0SaQ==/6631488878655488887.png "Title")
![应用管理](http://img2.ph.126.net/txeHTxXyrAlPkAY-Grmvzg==/6631617521515927688.png "Title")
![测试人员管理](http://img2.ph.126.net/k8CFyzgbCfvugGsfrdt10g==/6631505371329905060.png "Title")
![机型黑名单管理](http://img2.ph.126.net/BzWzuW2X0eABYr5i-Dus1w==/6631974862791848663.png "Title")



