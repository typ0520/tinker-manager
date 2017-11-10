0需要启动三个独立的服务1、后台管理服务 2、对客户端提供api的服务 3、需要另外部署一个用于下载补丁文件的静态服务

1、下载部署所需要的文件(war包、配置文件、建库sql文件)  [war包下载](https://pan.baidu.com/s/1minrdgO#list/path=%2Ftinker-manager).

2、在mysql里面建一个数据库,建表sql在patchserver-manager/import.sql中

3、把hotfix-apis.properties和hotfix-console.properties两个配置文件放到/opt/config(*如果是windows部署，放置在tomcat对应的盘符下，假如tomcat在d://tomcat  配置文件放在d://opt/config下})目录下，并且修改里面对应的配置(数据源配置、访问路径配置、补丁存放目录)

4、把hotfix-apis.war hotfix-console.war放到tomcat下面的webapps目录下

等服务启动完毕就可以在浏览器上访问http://localhost:8080/hotfix-console
