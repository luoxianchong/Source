# G1 (Garbage First) 

-XX:G1MixedGCLiveThresholdPercent

-XX:G1OldCSetRegionThresholdPercent

-XX:MaxGCPauseMillis=200

-XX:G1HeapRegionSize=5  : 设置 STW 工作线程数的值。将 n 的值设置为逻辑处理器的数量。n 的值与逻辑处理器的数量相同，最多为 8。如果逻辑处理器不止八个，则将 n 的值设置为逻辑处理器数的 5/8 左右。这适用于大多数情况，除非是较大的 SPARC 系统，其中 n 的值可以是逻辑处理器数的 5/16 左右。

-XX:ConcGCThreads=n   设置并行标记的线程数。将 n 设置为并行垃圾回收线程数 (ParallelGCThreads) 的 1/4 左右。

-XX:InitiatingHeapOccupancyPercent=45   设置触发标记周期的 Java 堆占用率阈值。默认占用率是整个 Java 堆的 45%。





G1中未显示设置年轻代的大小，则默认最小年轻代为整个堆大小的5%，默认最大年轻代为整个堆大小的60%，



https://www.oracle.com/cn/technical-resources/articles/java/g1gc.html

https://www.jianshu.com/p/ab37844d0e9e



新生代日志：  

```
[GC pause (G1 Evacuation Pause) (young), 0.0023896 secs]                                 //Evacuation  疏散；撤离；后撤；抽空；排气；排泄；
   [Parallel Time: 1.7 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 378.7, Avg: 378.8, Max: 378.9, Diff: 0.2]
      [Ext Root Scanning (ms): Min: 0.1, Avg: 0.4, Max: 1.5, Diff: 1.4, Sum: 3.2]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         [Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.4, Diff: 0.4, Sum: 0.6]
      [Object Copy (ms): Min: 0.0, Avg: 0.6, Max: 0.8, Diff: 0.8, Sum: 4.6]
      [Termination (ms): Min: 0.0, Avg: 0.4, Max: 0.5, Diff: 0.5, Sum: 3.5]
         [Termination Attempts: Min: 1, Avg: 9.0, Max: 17, Diff: 16, Sum: 72]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.0, Sum: 0.4]
      [GC Worker Total (ms): Min: 1.4, Avg: 1.5, Max: 1.6, Diff: 0.2, Sum: 12.2]
      [GC Worker End (ms): Min: 380.3, Avg: 380.3, Max: 380.3, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.1 ms]
   [Other: 0.6 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.4 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.1 ms]
      [Humongous Register: 0.0 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.0 ms]
   [Eden: 5120.0K(5120.0K)->0.0B(4096.0K) Survivors: 0.0B->1024.0K Heap: 5120.0K(10.0M)->1146.0K(10.0M)]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
```

#### 新生代收集

**Parallel Time**：并行收集任务在运行过程中引发的STW（Stop The World）时间，从新生代垃圾收集开始到最后一个任务结束，共花费1.7ms

 **GC Workers**：有8个线程负责垃圾收集，通过参数`-XX:ParallelGCThreads`设置，这个参数的值的设置，跟CPU有关，如果物理CPU支持的线程个数小于8，则最多设置为8；如果物理CPU支持的线程个数大于8，则默认值为number * 5/8

 **GC Worker Start**：第一个垃圾收集线程开始工作时JVM启动后经过的时间（min）；最后一个垃圾收集线程开始工作时JVM启动后经过的时间（max）；diff表示min和max之间的差值。理想情况下，你希望他们几乎是同时开始，即diff趋近于0。

 **Ext Root Scanning**：扫描root集合（线程栈、JNI、全局变量、系统表等等）花费的时间，扫描root集合是垃圾收集的起点，尝试找到是否有root集合中的节点指向当前的收集集合（CSet）

**Update RS(Remembered Set or RSet)**：每个分区都有自己的RSet，用来记录其他分区指向当前分区的指针，如果RSet有更新，G1中会有一个post-write barrier管理跨分区的引用——新的被引用的card会被标记为dirty，并放入一个日志缓冲区，如果这个日志缓冲区满了会被加入到一个全局的缓冲区，在JVM运行的过程中还有线程在并发处理这个全局日志缓冲区的dirty card。表示允许垃圾收集线程处理本次垃圾收集开始前没有处理好的日志缓冲区，这可以确保当前分区的RSet是最新的。 

 **Scan RS**：扫描每个新生代分区的RSet，找出有多少指向当前分区的引用来自CSet。

 **Code Root Scanning**：扫描代码中的root节点（局部变量）花费的时间

 **Object Copy**：在疏散暂停期间，所有在CSet中的分区必须被转移疏散，Object Copy就负责将当前分区中存活的对象拷贝到新的分区。

**Termination**：当一个垃圾收集线程完成任务时，它就会进入一个临界区，并尝试帮助其他垃圾线程完成任务（steal outstanding tasks），min表示该垃圾收集线程什么时候尝试terminatie，max表示该垃圾收集回收线程什么时候真正terminated。 

 **GC Worker Other**：垃圾收集线程在完成其他任务的时间

 **GC Worker Total**：展示每个垃圾收集线程的最小、最大、平均、差值和总共时间。

 **GC Worker End**：min表示最早结束的垃圾收集线程结束时该JVM启动后的时间；max表示最晚结束的垃圾收集线程结束时该JVM启动后的时间。理想情况下，你希望它们快速结束，并且最好是同一时间结束。



**Code Root Fixup** ：释放用于管理并行垃圾收集活动的数据结构，应该接近于0，该步骤是线性执行的；

 **Code Root Purge**：清理更多的数据结构，应该很快，耗时接近于0，也是线性执行。

 **Clear CT**：清理card table



**Choose CSet**：选择要进行回收的分区放入CSet（G1选择的标准是垃圾最多的分区优先，也就是存活对象率最低的分区优先）

 **Ref Proc**：处理Java中的各种引用——soft、weak、final、phantom、JNI等等。

 **Ref Enq**：遍历所有的引用，将不能回收的放入pending列表

 **Redirty Card**：在回收过程中被修改的card将会被重置为dirty

 **Humongous Register**：JDK8u60提供了一个特性，巨型对象可以在新生代收集的时候被回收——通过`G1ReclaimDeadHumongousObjectsAtYoungGC`设置，默认为true。

 **Humongous Reclaim**：做下列任务的时间：确保巨型对象可以被回收、释放该巨型对象所占的分区，重置分区类型，并将分区还到free列表，并且更新空闲空间大小。

 **Free CSet**：将要释放的分区还回到free列表。



**Eden:5120.0K(5120.0K)->0.0B(4096.0K)**：（1）当前新生代收集触发的原因是Eden空间满了，分配了5120.0K，使用了5120.0K；（2）所有的Eden分区都被疏散处理了，在新生代结束后Eden分区的使用大小成为了0.0B；（3）Eden分区的大小缩小为5120.0K

 **Survivors:0.0B->1024.0K**：由于年轻代分区的回收处理，survivor的空间从0B涨到1024.0K；

 **Heap:5120.0K(10.0M)->1146.0K(10.0M)**：（1）在本次垃圾收集活动开始的时候，堆空间整体使用量是5120.0K，堆空间的最大值是10M；（2）在本次垃圾收集结束后，堆空间的使用量是1146.0K，最大值保持不变。



**Times:**     

> user=0.8：垃圾收集线程在新生代垃圾收集过程中消耗的CPU时间，这个时间跟垃圾收集线程的个数有关，可能会比real time大很多；
>
>  sys=0.0：内核态线程消耗的CPU时间
>
> real=0.03：本次垃圾收集真正消耗的时间；



#### 并行垃圾收集

![](..\image\jvm\G1-paralle collect.png)

1、标志着并发垃圾收集阶段的开始：

-  **GC pause(G1 Evacuation Pause)(young)(initial-mark)**：为了充分利用STW的机会来trace所有可达（存活）的对象，initial-mark阶段是作为新生代垃圾收集中的一部分存在的（搭便车）。initial-mark设置了两个TAMS（top-at-mark-start）变量，用来区分存活的对象和在并发标记阶段新分配的对象。在TAMS之前的所有对象，在当前周期内都会被视作存活的。在G1并发阶段内至少发生了一次YGC

2、表示第并发标记阶段做的第一个事情：根分区扫描

-  **GC concurrent-root-region-scan-start**：根分区扫描开始，根分区扫描主要扫描的是新的survivor分区，找到这些分区内的对象指向当前分区的引用，如果发现有引用，则做个记录；
-  **GC concurrent-root-region-scan-end**：根分区扫描结束，耗时0.0030613s

3、表示并发标记阶段

-  **GC Concurrent-mark-start**：并发标记阶段开始。（1）并发标记阶段的线程是跟应用线程一起运行的，不会STW，所以称为并发；并发标记阶段的垃圾收集线程，默认值是Parallel Thread个数的25%，这个值也可以用参数`-XX:ConcGCThreads`设置；（2）trace整个堆，并使用位图标记所有存活的对象，因为在top TAMS之前的对象是隐式存活的，所以这里只需要标记出那些在top TAMS之后、阈值之前的；（3）记录在并发标记阶段的变更，G1这里使用了SATB算法，该算法要求在垃圾收集开始的时候给堆做一个快照，在垃圾收集过程中这个快照是不变的，但实际上肯定有些对象的引用会发生变化，这时候G1使用了pre-write barrier记录这种变更，并将这个记录存放在一个SATB缓冲区中，如果该缓冲区满了就会将它加入到一个全局的缓冲区，同时G1有一个线程在并行得处理这个全局缓冲区；（4）在并发标记过程中，会记录每个分区的存活对象占整个分区的大小的比率；
-  **GC Concurrent-mark-end**：并发标记阶段结束，耗时0.3055438s

3、重新标记阶段，会Stop the World

-  **Finalize Marking**：Finalizer列表里的Finalizer对象处理，耗时0.0014099s；
-  **GC ref-proc**：引用（soft、weak、final、phantom、JNI等等）处理，耗时0.0000480s；
-  **Unloading**：类卸载，耗时0.0025840s；
- 除了前面这几个事情，这个阶段最关键的结果是：绘制出当前并发周期中整个堆的最后面貌，剩余的SATB缓冲区会在这里被处理，所有存活的对象都会被标记；

4、清理阶段，也会Stop the World

- 计算出最后存活的对象：标记出initial-mark阶段后分配的对象；标记出至少有一个存活对象的分区；
- 为下一个并发标记阶段做准备，previous和next位图会被清理；
- 没有存活对象的老年代分区和巨型对象分区会被释放和清理；
- 处理没有任何存活对象的分区的RSet；
- 所有的老年代分区会按照自己的存活率（存活对象占整个分区大小的比例）进行排序，为后面的CSet选择过程做准备；

5、并发清理阶段

-  **GC concurrent-cleanup-start**：并发清理阶段启动。完成第5步剩余的清理工作；将完全清理好的分区加入到二级free列表，等待最终还会到总体的free列表；
-  **GC concurrent-cleanup-end**：并发清理阶段结束，耗时0.0012954s



#### 混合收集

在并发收集阶段结束后，你会看到混合收集阶段的日志，如下图所示，该日志的大部分跟之前讨论的新生代收集相同，只有第1部分不一样：**GC pause(G1 Evacuation Pause)(mixed),0.0129474s**，这一行表示这是一个混合垃圾收集周期；在混合垃圾收集处理的CSet不仅包括新生代的分区，还包括老年代分区——也就是并发标记阶段标记出来的那些老年代分区。

![](..\image\jvm\G1-mixed collect.png)



#### Full GC

```
[Full GC (Allocation Failure)  1654K->895K(10M), 0.0077900 secs]
   [Eden: 1024.0K(4096.0K)->0.0B(5120.0K) Survivors: 1024.0K->0.0B Heap: 1654.6K(10.0M)->895.9K(10.0M)], [Metaspace: 4379K->4379K(1056768K)]
 [Times: user=0.00 sys=0.00, real=0.01 secs]
```

如果堆内存空间不足以分配新的对象，或者是Metasapce空间使用率达到了设定的阈值，那么就会触发Full GC——你在使用G1的时候应该尽量避免这种情况发生，因为G1的Full Gc是单线程、会Stop The World，代价非常高。Full GC的日志如下图所示，从中你可以看出三类信息

1. Full GC的原因，这个图里是Allocation Failure，还有一个常见的原因是Metadata GC Threshold；
2. Full GC发生的频率，每隔几天发生一次Full GC还可以接受，但是每隔1小时发生一次Full GC则不可接受；
3. Full GC的耗时，这张图里的Full GC耗时150ms（PS：按照我的经验，实际运行中如果发生Full GC，耗时会比这个多很多）





#### 从上可以看出G1在运行过程中主要包含如下4种操作方式：

1. YGC（不同于CMS）:  所有Eden区域满了后触发，并行收集，且完全STW。
2. 并发阶段  :  它的第一个阶段初始化标记和YGC一起发生，这个周期的目的就是找到回收价值最大的Region集合（垃圾很多，存活对象很少），为接下来的Mixed GC服务。
3. 混合模式 :    回收所有年轻代的Region和部分老年代的Region，Mixed GC可能连续发生多次。
4. full GC （一般是G1出现问题时发生）:    非常慢，对OLTP系统来说简直就是灾难，会STW且回收所有类型的Region。 







### 三色标记

​        提到并发标记，我们不得不了解并发标记的三色标记算法。它是描述追踪式回收器的一种有用的方法，利用它可以推演回收器的正确性。

​    **首先，我们将对象分成三种类型的：**

- ​    黑色：跟对象或者该对象与它的子对象都被扫描(标记完成)。
- ​    灰色：对象本身被标记完成，但是还没有扫描完该对象中的子对象
- ​    白色：未被扫描的对象。或者是扫描完成后，最终白色对象为不可达对象即为垃圾对象。

当GC开始扫描对象时，按照如下图步骤进行对象的扫描：

## 对象标记过程

**1、根对象被置为黑色，子对象被置为灰色。**

​             ![img](..\image\jvm\3color-1.png)

**2、继续由灰色遍历,将已扫描了子对象的对象置为黑色。**

![img](..\image\jvm\3color-2.png)

**3、遍历了所有可达的对象后，所有可达的对象都变成了黑色。不可达的对象即为白色，需要被清理。**

![img](..\image\jvm\3color-3.png)

## 问题：

​     这看起来很美好，但是如果在标记过程中，应用程序也在运行，那么对象的指针就有可能改变。这样的话，我们就会遇到一个问题：对象丢失问题

我们看下面一种情况，当垃圾收集器扫描到下面情况时：

​                                                    ![img](..\image\jvm\3color-4.png)

这时候应用程序执行了以下操作：

A.c=C

B.c=null

这样，对象的状态图变成如下情形：

​                                                ![img](..\image\jvm\3color-5.png)

> ​    此时A对象已经被标记为黑色不会在被扫描，而这个是时候程序操作A对象有调用了C，而C对象因A对象为黑色不会被扫描就被当成垃圾对象，从而导致对象丢失问题

这时候垃圾收集器再标记扫描的时候就会下图成这样：

​                                           ![img](..\image\jvm\3color-6.png)

很显然，此时C是白色，被认为是垃圾需要清理掉，显然这是不合理的。

#### 解决办法

   GC标记的对象不丢失呢？有如下2中可行的方式：

1. 在插入的时候记录对象
2. 在删除的时候记录对象

#####    增量更新（Incremental update）

> ​      在CMS采用的是增量更新（Incremental update），只要在写屏障（write barrier）里发现要有一个白对象的引用被赋值到一个黑对象 的字段里，那就把这个白对象变成灰色的。即插入的时候记录下来。

#####     STAB（snapshot-at-the-beginning）

在G1中，使用的是STAB（snapshot-at-the-beginning）的方式，删除的时候记录所有的对象，它有3个步骤：

- 1，在开始标记的时候生成一个快照图标记存活对象
- 2，在并发标记的时候所有被改变的对象入队（在write barrier里把所有旧的引用所指向的对象都变成非白的）
- 3，可能存在游离的垃圾，将在下次被收集

这样，G1到现在可以知道哪些老的分区可回收垃圾最多。 当全局并发标记完成后，在某个时刻，就开始了Mix GC。





#### Per Region Table (PRT)

​      RSet在内部使用Per Region  Table(PRT)记录分区的引用情况。由于RSet的记录要占用分区的空间，如果一个分区非常”受欢迎”，那么RSet占用的空间会上升，从而降低分区的可用空间。G1应对这个问题采用了改变RSet的密度的方式，在PRT中将会以三种模式记录引用：

- 稀少：直接记录引用对象的卡片索引
- 细粒度：记录引用对象的分区索引
- 粗粒度：只记录引用情况，每个分区对应一个比特位

由上可知，粗粒度的PRT只是记录了引用数量，需要通过整堆扫描才能找出所有引用，因此扫描速度也是最慢的。