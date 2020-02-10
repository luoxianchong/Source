## jcmd

jcmd -l

查看jvm进程信息





jvm 参数 -XX:NativeMemoryTracking=detail

jcmd <pid> VM.native_memory [summary | detail | baseline | summary.diff | detail.diff | shutdown] [scale= KB | MB | GB]

jcmd 15610 VM.native_memory summary scale=MB

```
$ jcmd 15610 VM.native_memory summary scale=MB
15610:

Native Memory Tracking:

Total:  reserved=5735MB,  committed=4105MB

- Java Heap (reserved=5512MB, committed=3903MB)
            (mmap: reserved=5512MB, committed=3903MB)
- Class (reserved=3MB, committed=3MB)
        (classes #17175)
        (malloc=3MB, #29131)
- Thread (reserved=84MB, committed=84MB)
         (thread #286)
         (stack: reserved=82MB, committed=82MB)
         (malloc=1MB, #1154)
         (arena=1MB, #572)
- Code (reserved=57MB, committed=43MB)
       (malloc=8MB, #10668)
       (mmap: reserved=49MB, committed=35MB)
- GC (reserved=27MB, committed=20MB)
     (malloc=8MB, #116)
     (mmap: reserved=19MB, committed=12MB)
- Compiler (reserved=1MB, committed=1MB)
           (malloc=1MB, #943)
- Internal (reserved=10MB, committed=10MB)
           (malloc=10MB, #11220)
- Symbol (reserved=17MB, committed=17MB)
         (malloc=15MB, #157508)
         (arena=2MB, #1)
- Memory Tracking (reserved=25MB, x=25MB)
                  (malloc=25MB, #1112)
```

其中reserved表示应用可用的内存大小，committed表示应用正在使用的内存大小





 pmap - report memory map of a process(查看进程的内存映像信息)



​	   -x   extended       Show the extended format. 显示扩展格式

​       -d  device         Show the deviceformat.   显示设备格式

​      -q   quiet          Do not display some header/footerlines. 不显示头尾行

​      -V   show version   Displays version of program. 显示版本



###### 扩展格式和设备格式域：

​       Address:  start address ofmap  映像起始地址

​        Kbytes: size of map in kilobytes  映像大小

​       RSS:  resident set size inkilobytes  驻留集大小

​       Dirty:  dirty pages (both sharedand private) in kilobytes  脏页大小

​       Mode:  permissions on map 映像权限: r=read,w=write, x=execute, s=shared, p=private (copy on write) 

​       Mapping:  file backing the map ,or '[ anon ]' for allocated memory, or '[ stack ]' for the program stack.  映像支持文件,[anon]为已分配内存[stack]为程序堆栈

​        Offset: offset into the file  文件偏移

​       Device:  device name(major:minor)  设备名