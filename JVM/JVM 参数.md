# JVM 参数

jvm 优化原则：

 heap    				-Xms 和-Xmm                                               full gc 之后old区的3-4倍

permant            -XX:PermSize 和-XX:MaxPermSize       full gc 后的1.2-1.5倍

young                -Xmn                                                               full gc  后old区的1-1.5倍

old                                                                                            full gc  后old区的2-3倍



### -XX:+AlwaysPreTouch

JAVA进程启动的时候，虽然我们可以为JVM指定合适的内存大小，但是这些内存操作系统并没有真正的分配给JVM,而是等JVM访问这些内存的时候,才真正分配,这样会造成以下问题:

1. 第1次YGC之前Eden区分配对象的速度较慢;
2. YGC的时候，Young区的对象要晋升到Old区的时候，这个时候需要操作系统真正分配内存，这样就会加大YGC的停顿时间;

| ~    | -XX:+AlwaysPreTouch | XX:-AlwaysPreTouch(default) |
| ---- | ------------------- | --------------------------- |
| 16G  | 36s                 | <1s                         |
| 8G   | 20s                 | <1s                         |



### -XX:+OmitStackTraceInFastThrow

日志打印时只有空指针，没有堆栈信息。是因为空指针打印的太多了,OmitStackTraceInFastThrow字面意思是省略异常栈信息从而快速抛出.

-XX:-OmitStackTraceInFastThrow  关闭则表示每一条空指针都打印堆栈

OmitStackTraceInFastThrow  jdk1.5之后才有，-server模式是默认开启的。



### 查看 jvm 参数

######  windows cmd  命令行

​			java -XX:+PrintFlagsFinal -version | FIND "TLAB"

​	linux  命令行

​		java -XX:+PrintFlagsFinal -version | grep TLAB



java -XX:+PrintFlagsInitial -version



查看使用java 启动的默认参数

​		java  -XX:+PrintCommandLineFlags  -version



###### JVM之对象分配：栈上分配 

```
//不使用逃逸分析
-server -Xmx15m -Xms15m -XX:－DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB -XX:+EliminateAllocations

//不使用标量替换
-server -Xmx15m -Xms15m -XX:＋DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB -XX:－EliminateAllocations
```



| 参数                        | 作用                                 | 备注                                                   |
| --------------------------- | ------------------------------------ | ------------------------------------------------------ |
| `-server`                   | 使用server模式                       | 只有在server模式下，才可以弃用逃逸分析                 |
| `-Xmx15m`                   | 设置最大堆空间为15m                  | 如果在堆上分配，必然触发大量GC                         |
| `-Xms15m`                   | 设初始对空间为15m                    |                                                        |
| `-XX:+DoEscapeAnalysis`     | 启用逃逸分析                         | 默认启用                                               |
| `-XX:-DoEscapeAnalysis`     | 关闭逃逸分析                         |                                                        |
| `-XX:+PrintGC`              | 打印ＧＣ日志                         |                                                        |
| -XX:-UseTLAB                | 关闭TLAB                             | TLAB(Thread Local Allocation Buffer)线程本地分配缓存区 |
| `-XX:+EliminateAllocations` | 启用标量替换，允许对象打散分配到栈上 | 默认启用                                               |
| `-XX:-EliminateAllocations` | 关闭标量替换                         |                                                        |





#### gc 日志参数



-verbose:gc 

-XX:+PrintGCDetails 

-Xloggc:/path/gc.log

-XX:+UseGCLogFileRotation  启用GC日志文件的自动转储 (Since Java)

-XX:NumberOfGCLogFiles=2  

```
GC日志文件的循环数目 (Since Java) 

Set the number of files to use when rotating logs, must be >= 1.

The rotated log files will use the following naming scheme, <filename>.0, <filename>.1, ..., <filename>.n-1.

设置滚动日志文件的个数，必须大于1

日志文件命名策略是，<filename>.0, <filename>.1, ..., <filename>.n-1，其中n是该参数的值
```

-XX:GCLogFileSize=1M  控制GC日志文件的大小 (Since Java)

-XX:+PrintGC包含-verbose:gc

-XX:+PrintGCDetails //包含-XX:+PrintGC

只要设置-XX:+PrintGCDetails 就会自动带上-verbose:gc和-XX:+PrintGC

-XX:+PrintGCDateStamps/-XX:+PrintGCTimeStamps 输出gc的触发时间

-XX:+PrintGCCause 





--Xloggc:/path/xxx/xxx-xx-gc-%t.log



/home/erp/erp-online/logs/gc



-XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=2 -XX:GCLogFileSize=64M -Xloggc
:/data/erp/erp-online1/logs/gc/online.log