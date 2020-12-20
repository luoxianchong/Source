# Flink

Apache Flink 是一款开源大数据计算引擎，它同时支持了**批处理**和**流处理.** 用于对无界和有界数据流进行有状态计算。









Flink 搭建

三种模式：Flink支持local模式、集群模式（standalone集群或者Yarn集群）、云端部署



$ wget  [**https://mirror.bit.edu.cn/apache/flink/flink-1.10.0/flink-1.10.0-bin-scala_2.11.tgz**](https://mirror.bit.edu.cn/apache/flink/flink-1.10.0/flink-1.10.0-bin-scala_2.11.tgz) 

$ sudo tar -zxvf  **flink-1.10.0-bin-scala_2.11.tgz**

$ vim /etc/profile

```
#添加环境变量
export FLNK_HOME=/root/soft/flink-1.9.2
export PATH=$FLINK_HOME/bin:$PATH
```

local 模式

./start-cluster.sh

```
#日志 $flink-home$/log
tail log/flink-*-standalonesession-*.log
```

standalone集群

修改 $flink-home$/config/flink-conf.yml与 $flink-home$/config/slaves 文件

```yaml
flink-conf.yml

#指定 jobmanager ip 地址
jobmanager.rpc.address : k8s1.ing.org

#指定 资源槽个数
taskmanager.numberOfTaskSlots : 4
```



```xml
slaves 文件

k8s2.ing.org
k8s3.ing.org

```



flink application example

1、启动监听

$ nc -lk 8888 

2、命令行提交job

```bash
$ ./bin/flink run examples/streaming/SocketWindowWordCount.jar --port 8888

#查看输入结果
$tail -f log/flink-*-taskexecutor-*.out
```



3、图形界面提交

<img src=".\image\flink\flink-submit-job.png" alt="flink-submit-job" style="zoom:80%;" />



