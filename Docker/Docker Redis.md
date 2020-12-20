### Docker Redis

搜索redis
``` docker search redis ```

下载最新的redis 
``` docker pull redis:latest ```


下载redis配置文件

``` wget https://raw.githubusercontent.com/antirez/redis/4.0/redis.conf -O conf/redis.conf ```

 创建并运行一个名为 myredis 的容器

```java
docker run -p 6379:6379 -v $PWD/data:/data -v  $PWD/conf/redis.conf:/etc/redis/redis.conf 
--privileged=true  --name myredis  -d redis redis-server /etc/redis/redis.conf

# 命令分解
docker run \
-p 6379:6379 \ # 端口映射 宿主机:容器
-v $PWD/data:/data:rw \ # 映射数据目录 rw 为读写
-v $PWD/conf/redis.conf:/etc/redis/redis.conf:ro \ # 挂载配置文件 ro 为readonly
--privileged=true \ # 给与一些权限
--name myredis \ # 给容器起个名字
--restart=always \ #docker重启，容器自动重启
-d redis redis-server /etc/redis/redis.conf # deamon 运行 服务使用指定的配置文件
```

查看活跃的容器

> docker ps

如果没有 myredis 说明启动失败 查看错误日志

> docker logs myredis

查看 myredis 的 ip 挂载 端口映射等信息

> docker inspect myredis

查看 myredis 的端口映射

> docker port myredis



IP 查看

docker inspect e60da5191243|grep -i add





```jsx
docker run -d -name redis -p 6379:6379 -v /data/redis/data:/data -v /data/redis.conf:/etc/redis/redis.conf:ro --privileged=true --restart=always redis:6.0.8 redis-server /etc/redis/redis.conf
```

### 