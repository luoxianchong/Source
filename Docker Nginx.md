### Docker Nginx

从中央仓库查询nginx镜像

``` docker search nginx ```



获取Nginx镜像

`docker pull nginx` 



启动nginx容器实例

``` docker run --name  nginx4Ten  -d nginx```

--name: 表示给运行的容器取个别名 比如：nginx4Ten  

-d ：表示后台运行  



查看nginx容器实例的进程信息

``` docker top nginx  或者  ps -ef | grep nginx ```



进入nginx容器内

``` docker exec -it nginx /bin/bash   或者 docker attach --sig-proxy=false mynginx ```

注意：docker attach 命令退出时会kill 容器进程，所以退出时要选择Ctrl+P+Q退出容器。 



杀死nginx容器实例

 *docker kill -s KILL 4a3ae8c39144*



停止nginx实例

docker stop nginx



启动nginx容器实例

docker start nginx



重启nginx容器实例

docker restart nginx



指定外部的配置文件

语法：docker run --name nginx -p 80:80 -v /develop/nginx/nginx.conf:/etc/nginx/nginx.conf -v /develop:/develop -d nginx 
说明： 
-v: 表示挂载一个本机目录或文件到容器里。 
-v /develop/nginx/nginx.conf:/etc/nginx/nginx.conf：将/develop/nginx/nginx.conf配置文件挂载到容器中/etc/nginx/nginx.conf这个配置文件中。 

-v /develop:/develop：将/develop这个目录挂载到容器里的/develop这个目录里。

