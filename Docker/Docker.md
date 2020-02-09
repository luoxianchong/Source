# Docker

### Docker修改已停止运行容器配置文件

基于Docker的Nginx服务中，笔者修改了Nginx的配置文件，结果重启容器时导致Nginx起不来，又不能使用 `docker exec ...`的方式进入容器修改配置文件。

#### 解决步骤

> 查看容器报错原因
>
> [root@pro nginx]# docker logs nginx
> nginx: [emerg] unknown directive "gizp" in /etc/nginx/nginx.conf:29
>
> //在此目录找到nginx容器的配置文件  /var/lib/docker 为docker目录
>
> [root@pro nginx]# cd /var/lib/docker/overlay2/ 
> [root@pro overlay2]# find ./ -name nginx.conf
> ./7baeb968df6b073708cce37a182cf54fd033023a5eda6bb6d1077438d950ce6e/diff/etc/nginx/nginx.conf
>
> //将文件修改正确
>
> [root@pro overlay2]# vim ./7baeb968df6b073708cce37a182cf54fd033023a5eda6bb6d1077438d950ce6e/diff/etc/nginx/nginx.conf
>
> //重启容器
>
> [root@pro overlay2]# docker restart nginx
> nginx



