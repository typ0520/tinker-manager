需要启动三个独立的服务1、后台管理服务(tm-manager) 2、对客户端提供api的服务网 3、需要另外部署一个用于下载补丁文件的静态服务

1、在mysql里面建一个数据库,建表sql在tm-server/import.sql中

2、在把war/1.0.2目录下的hotfix-apis.properties和hotfix-console.properties两个配置文件放到/opt/config目录下，并且修改里面对应的配置

3、在把war/1.0.2目录下的hotfix-apis.war hotfix-console.war放到tomcat下面的webapps目录下

等服务启动完毕就可以在浏览器上访问http://localhost:8080/tm-manager
