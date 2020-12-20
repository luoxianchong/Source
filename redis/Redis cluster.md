#  Redis cluster

```
redis-6 需要先升级gcc 到9版本
yum -y install centos-release-scl
yum -y install devtoolset-9-gcc devtoolset-9-gcc-c++ devtoolset-9-binutils

//临时将此时的gcc版本改为9
scl enable devtoolset-9 bash
//或永久改变
echo "source /opt/rh/devtoolset-9/enable" >>/etc/profile

# cd redis-6.0.8/
# make
# make install PREFIX=/home/Qing/redis/redis-6.0.8
```



```
./redis-6.0.8/src/redis-cli --cluster create 192.168.139.128:7003 192.168.139.128:7004 192.168.139.130:7001 192.168.139.130:7002 192.168.139.129:7005 192.168.139.129:7006 --cluster-replicas 1
```

