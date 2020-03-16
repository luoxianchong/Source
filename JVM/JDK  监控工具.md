# JDK  监控工具

### jps

```shell
-l 输出主类全类名

-m 虚拟机主类main() 函数

-v jvm参数

jps -lvm

27805 /data/erp/erp-service/service1/bin/.launch.jar -start -Xms1024m -Xmx3072m -Xmn768m -Xss256k -XX:+UseParNewGC -XX:MaxPermSize=384m -XX:+HeapDumpOnOutOfMemoryError -XX:ErrorFile=./hs_err_%p.log -Duser.locale=CHINA -Dfile.encoding=utf-8 -Dmain.class=com.hupun.erp.main.ServiceMaintain -Dlib.paths=/data/erp/erp-service/service1/conf:/data/erp/erp-service/service1/etc:/data/erp/erp-service/service1/lib:/data/erp/erp-service/service1/docs/lib:
```



### jmap

```-heap  显示堆栈信息```

jdk7：

```shell
[weihu@iz8vbhpdwx5fe45jx8frb2z ~]$ jmap -heap 27805
Attaching to process ID 27805, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 22.1-b02

using parallel threads in the new generation.
using thread-local object allocation.
Mark Sweep Compact GC

Heap Configuration:
   MinHeapFreeRatio = 40
   MaxHeapFreeRatio = 70
   MaxHeapSize      = 3221225472 (3072.0MB)
   NewSize          = 805306368 (768.0MB)
   MaxNewSize       = 805306368 (768.0MB)
   OldSize          = 5439488 (5.1875MB)
   NewRatio         = 2
   SurvivorRatio    = 8
   PermSize         = 21757952 (20.75MB)
   MaxPermSize      = 402653184 (384.0MB)

Heap Usage:
New Generation (Eden + 1 Survivor Space):
   capacity = 724828160 (691.25MB)
   used     = 563631544 (537.5209274291992MB)
   free     = 161196616 (153.72907257080078MB)
   77.76071282881725% used
Eden Space:
   capacity = 644349952 (614.5MB)
   used     = 550701608 (525.1899795532227MB)
   free     = 93648344 (89.31002044677734MB)
   85.4662293821355% used
From Space:
   capacity = 80478208 (76.75MB)
   used     = 12929936 (12.330947875976562MB)
   free     = 67548272 (64.41905212402344MB)
   16.0663815973636% used
To Space:
   capacity = 80478208 (76.75MB)
   used     = 0 (0.0MB)
   free     = 80478208 (76.75MB)
   0.0% used
tenured generation:
   capacity = 623661056 (594.76953125MB)
   used     = 461703456 (440.3147277832031MB)
   free     = 161957600 (154.45480346679688MB)
   74.03115066399144% used
Perm Generation:
   capacity = 108986368 (103.9375MB)
   used     = 108949648 (103.90248107910156MB)
   free     = 36720 (0.0350189208984375MB)
   99.96630771290589% used
```

jdk8：

```shell
[weihu@iZ8vbburlqaim59fkykw8lZ ~]$ jmap -heap 18489
Attaching to process ID 18489, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 25.171-b11

using thread-local object allocation.
Parallel GC with 4 thread(s)

Heap Configuration:
   MinHeapFreeRatio         = 0
   MaxHeapFreeRatio         = 100
   MaxHeapSize              = 1073741824 (1024.0MB)
   NewSize                  = 133693440 (127.5MB)
   MaxNewSize               = 134217728 (128.0MB)
   OldSize                  = 524288 (0.5MB)
   NewRatio                 = 2
   SurvivorRatio            = 8
   MetaspaceSize            = 21807104 (20.796875MB)
   CompressedClassSpaceSize = 1073741824 (1024.0MB)
   MaxMetaspaceSize         = 17592186044415 MB
   G1HeapRegionSize         = 0 (0.0MB)

Heap Usage:
PS Young Generation
Eden Space:
   capacity = 122159104 (116.5MB)
   used     = 9793184 (9.339508056640625MB)
   free     = 112365920 (107.16049194335938MB)
   8.016745112996245% used
From Space:
   capacity = 5767168 (5.5MB)
   used     = 0 (0.0MB)
   free     = 5767168 (5.5MB)
   0.0% used
To Space:
   capacity = 5767168 (5.5MB)
   used     = 0 (0.0MB)
   free     = 5767168 (5.5MB)
   0.0% used
PS Old Generation
   capacity = 145752064 (139.0MB)
   used     = 83948120 (80.05916595458984MB)
   free     = 61803944 (58.940834045410156MB)
   57.5965222694891% used
```



-dump  生成快照  例如：jmap -dump:live,format=b,file=heap.out <pid>



export JAVA_OPTS="-Xms4096m -Xmx4096m -XX:PermSize=256m -XX:MaxPermSize=512m -Dfile.encoding=UTF-8 -XX:+PrintGCDetails -XX:+UseBiasedLocking -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly"

