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

