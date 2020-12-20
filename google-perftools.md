# google-perftools

google-perftools是一个堆外内存分析工具，大致原理是：内存分配时使用的函数是google的malloc,从此记录分配日志。



##### 1、安装gcc

```
sudo yum -y install gcc make
sudo yum -y install gcc gcc-c++
```

##### 安装libunwind

```
wget http://download.savannah.gnu.org/releases/libunwind/libunwind-0.99.tar.gz

tar -xzvf libunwind-0.99.tar.gz

cd libunwind-0.99
```

##### 默认libunwind安装到/usr/local/lib,使用prefix命令指定安装目录

```bash
./configure  --prefix=/usr/local/libunwind/
make
make install
```

##### 安装google-perfile

```bash
wget https://github.com/gperftools/gperftools/releases/download/gperftools-2.5/gperftools-2.5.tar.gz

tar -xzvf gperftools-2.5.tar.gz

cd gperftools-2.5

./configure --prefix=/usr/local/gperftool/

make

make install
```

##### 使配置生效

```bash
vi /etc/ld.so.conf.d/usr_local_lib.conf  
```

##### 编辑，添加以下内容，需要sudo权限

```bash
/usr/local/libunwind/lib
:wq! #保存退出
sudo /sbin/ldconfig  #执行此命令，使libunwind生效。 需要sudo权限
```

在应用程序启动前加入：

```shell
export LD_PRELOAD=/usr/local/gperftool/lib/libtcmalloc.so
export HEAPPROFILE=/usr/local/gperftool/heap/hp   #配置生成堆的路径和以hp为前缀的文件
export CPUPROFILE=/usr/local/gperftool/prof.out  #配置cpu profile



具体可参看官方帮助 https://github.com/gperftools/gperftools/wiki
```

##### 分析结果

```
gperftools-home-dir/bin/pprof --text /usr/java/jdk1.7.0_03/bin/java  hp.0005.heap
```

```cpp
Using local file /usr/local/java/jdk1.7.0_55/bin/java.
Using local file test.0132.heap.
Total: 237.3 MB
   129.2  54.5%  54.5%    129.2  54.5% deflateInit2_
    57.5  24.2%  78.7%     57.5  24.2% os::malloc
    48.0  20.2%  98.9%     48.0  20.2% init
     0.9   0.4%  99.3%      0.9   0.4% ObjectSynchronizer::inflate
     0.8   0.3%  99.6%      0.8   0.3% updatewindow
     0.4   0.2%  99.8%      0.4   0.2% readCEN
     0.3   0.1%  99.9%      0.3   0.1% inflateInit2_
     0.1   0.0%  99.9%      0.1   0.0% _dl_allocate_tls
     0.0   0.0% 100.0%    129.3  54.5% Java_java_util_zip_Deflater_init
     0.0   0.0% 100.0%      0.0   0.0% _dl_new_object
     0.0   0.0% 100.0%      1.2   0.5% JavaThread::JavaThread@94a810
     0.0   0.0% 100.0%      0.0   0.0% SharedHeap::SharedHeap
     0.0   0.0% 100.0%      0.0   0.0% vm_init_globals
     0.0   0.0% 100.0%      0.4   0.2% ZIP_Put_In_Cache0
     0.0   0.0% 100.0%      0.0   0.0% strdup
     0.0   0.0% 100.0%      0.0   0.0% read_alias_file
     0.0   0.0% 100.0%      0.0   0.0% _nl_intern_locale_data
     0.0   0.0% 100.0%      0.3   0.1% Java_java_util_zip_Inflater_init
     0.0   0.0% 100.0%     13.4   5.6% init_globals
```