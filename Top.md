# Top



#### top source

Commands : 1) 'which top'   > /usr/bin/top
2) 'dpkg -S   /usr/bin/top'
\> > procps



 top命令通过解析/proc/<pid>/statm统计VIRT和RES和SHR字段值。

VIRT是申请的虚拟内存总量。

RES是进程**使用**的物理内存总和。

SHR是RES中”映射至文件”的物理内存总和。包括：

*程序的代码段。*

*动态库的代码段。*

*通过mmap做的文件映射。*

*通过mmap做的匿名映射，但指明了MAP_SHARED属性。*

*通过shmget申请的共享内存。*

/proc/<pid>/smaps内Shared_*统计的是**RES中**映射数量>=2的物理内存。

/proc/<pid>/smaps内Private_*统计的是**RES中**映射数量=1的物理内存。