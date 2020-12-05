# CMS (Concurrent Mark Sweep)垃圾收集器

###### cms大致流程图

![](..\image\jvm\cms-process.png)



**1、initial mark(初始标记):** 标记GC Root直接引用的对象，GC Root直接引用的对象不多，所以很快。（CMS的第一个STW阶段）



**2、concurrent-mark（并发标记）：**由第一阶段标记过的对象出发，所有可达的对象都在本阶段标记



**3、concurrent-preclean（并发预清理）：**也是一个并发执行的阶段。在本阶段，会查找前一阶段执行过程中,从新生代晋升或新分配或被更新的对象。通过并发地重新扫描这些对象，预清理阶段可以减少下一个stop-the-world 重新标记阶段的工作量。



**4、concurrent-abortable-preclean （并发可中止的预清理）：**这个阶段其实跟上一个阶段做的东西一样，也是为了减少下一个STW重新标记阶段的工作量。增加这一阶段是为了让我们可以控制这个阶段的结束时机，比如扫描多长时间（默认5秒）或者Eden区使用占比达到期望比例（默认50%）就结束本阶段。



**5、remark （重标记）：**暂停所有用户线程，从GC Root开始重新扫描整堆，标记存活的对象。需要注意的是，虽然CMS只回收老年代的垃圾对象，但是这个阶段依然需要扫描新生代，因为很多GC Root都在新生代，而这些GC Root指向的对象又在老年代，这称为“跨代引用”。（CMS的第二个STW阶段）



**6、concurrent-sweep（并发清理）**



![](..\image\jvm\cms-gc-log.png)



**7、concurrent-reset （并发重置）**







调优参数：

- `-XX:CMSMaxAbortablePrecleanTime=5000` ，默认值5s，代表该阶段最大的持续时间

- `-XX:CMSScheduleRemarkEdenPenetration=50` ，默认值50%，代表Eden区使用比例超过50%就结束该阶段进入remark

- `-XX:+CMSScavengeBeforeRemark`,CMS提供了CMSScavengeBeforeRemark参数，尝试在remark阶段之前进行一次Minor GC，以降低新生代的占用。

  

  CMS收集器还提供了几个用于内存压缩整理的算法。

- **-XX:+UseCMSCompactAtFullCollection** 使CMS在垃圾收集完成后，进行一次内存碎片整理。内存碎片的整理并不是并发进行的,默认为true

- **-XX:CMSFullGCsBeforeCompaction=3** 用于设定进行多少次CMS回收后，进行一次内存整理，例子中每3次foreground CMS后才会有1次采用MSC算法压缩堆内存，默认为0。

- **-XX:+UseConcMarkSweepGC** 可以要求新生代使用parNew，老年代使用CMS。

- **-XX:+UseCMSInitiatingOccupancyOnly**  是否启用cms初始占用率，即CMSInitiatingOccupancyFraction的回收阈值，如果不指定，jvm仅在第一次使用设定值，后续则自动调整。默认为false

- **-XX:CMSInitiatingOccupancyFraction=80 **  是指设定CMS在对内存占用率达到80%的时候开始GC(因为CMS会有浮动垃圾,所以一般都较早启动GC)

- **-XX:+ExplicitGCInvokesConcurrent** 当调用System.gc()的时候，执行cms并行gc 而不执行full gc,只有在CMS或者G1下该参数才有效

- **-XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses**保证当有系统GC调用时，永久代也被包括进CMS垃圾回收的范围内

- **-XX:+CMSParallelRemarkEnabled** 并行运行最终标记阶段，加快最终标记的速度

- **-XX:+CMSClassUnloadingEnabled** 让CMS可以收集永久带，默认不会收集

- **-XX:+CMSIncrementalMode** 该标志将开启CMS收集器的增量模式。增量模式经常暂停CMS过程，以便对应用程序线程作出完全的让步。因此，收集器将花更长的时间完成整个收集周期。因此，只有通过测试后发现正常CMS周期对应用程序线程干扰太大时，才应该使用增量模式。由于现代服务器有足够的处理器来适应并发的垃圾收集，所以这种情况发生得很少

- 





CMS 缺点：

​	1.CMS收集器对CPU资源非常敏感 ：在并发阶段，虽然不会导致用户线程停顿，但是会因为占用了一部分线程使应用程序变慢，总吞吐量会降低，为了解决这种情况，虚拟机提供了一种“增量式并发收集器” 的CMS收集器变种， 就是在并发标记和并发清除的时候让GC线程和用户线程交替运行，尽量减少GC 线程独占资源的时间，这样整个垃圾收集的过程会变长，但是对用户程序的影响会减少。（效果不明显，不推荐） 

2. CMS处理器无法处理浮动垃圾 CMS在并发清理阶段线程还在运行， 伴随着程序的运行自然也会产生新的垃圾，这一部分垃圾产生在标记过程之后，CMS无法再当次过程中处理，所以只有等到下次gc时候在清理掉，这一部分垃圾就称作“浮动垃圾” ， 

3. CMS是基于“标记--清除”算法实现的，所以在收集结束的时候会有大量的空间碎片产生。空间碎片太多的时候，将会给大对象的分配带来很大的麻烦，往往会出现老年代还有很大的空间剩余，但是无法找到足够大的连续空间来分配当前对象的，只能提前触发 full gc。 为了解决这个问题，CMS提供了一个开关参数，用于在CMS顶不住要进行full gc的时候开启内存碎片的合并整理过程，内存整理的过程是无法并发的，空间碎片没有了，但是停顿的时间变长了 





### CMS 触发条件

CMS GC 在实现上分成：

​	 foreground collector 

​	 background collector

#### foreground collector

```
foreground collector 触发条件比较简单，一般是遇到对象分配但空间不够，就会直接触发 GC，来立即进行空间回收。采用的算法是 mark sweep，不压缩

foreground collector 是没有 Precleaning、AbortablePreclean 阶段的
```



#### background collector

background collector 的流程是通过 CMS 后台线程不断的去扫描，主要是判断是否符合 background collector 的触发条件，一旦有符合的情况，就会进行一次 background 的 collect



```c
void ConcurrentMarkSweepThread::run() {//cms 后台线程。
  ...//省略
  while (!_should_terminate) {
    sleepBeforeNextCycle();//执行前sleep判断
    if (_should_terminate) break;
    GCCause::Cause cause = _collector->_full_gc_requested ?
      _collector->_full_gc_cause : GCCause::_cms_concurrent_mark;
    _collector->collect_in_background(false, cause);
  }
  ...//省略
}
```

```c
void ConcurrentMarkSweepThread::sleepBeforeNextCycle() {
  while (!_should_terminate) {
    if (CMSIncrementalMode) {//cms 增量模式
      icms_wait();
      if(CMSWaitDuration >= 0) {//等 CMSWaitDuration 时间,可配置 –XX:CMSWaitDuration=5000 单位ms,默认2000
        // Wait until the next synchronous GC, a concurrent full gc request or a timeout, whichever is earlier.
        wait_on_cms_lock_for_scavenge(CMSWaitDuration);
      }
      return;
    } else {
      if(CMSWaitDuration >= 0) {
        // Wait until the next synchronous GC, a concurrent full gc request or a timeout, whichever is earlier.
        wait_on_cms_lock_for_scavenge(CMSWaitDuration);
      } else {
        // Wait until any cms_lock event or check interval not to call shouldConcurrentCollect permanently
        wait_on_cms_lock(CMSCheckInterval);
      }
    }
    // Check if we should start a CMS collection cycle
    if (_collector->shouldConcurrentCollect()) {
      return;
    }
    // .. collection criterion not yet met, let's go back and wait some more
  }
}

```



```c
bool CMSCollector::shouldConcurrentCollect() {

  // 第一种触发情况
  if (_full_gc_requested) {
    if (Verbose && PrintGCDetails) {
      gclog_or_tty->print_cr("CMSCollector: collect because of explicit gc request (or gc_locker)");
    }
    return true;
  }
  
  // For debugging purposes, change the type of collection.
  // If the rotation is not on the concurrent collection type, don't start a concurrent collection.
  NOT_PRODUCT(
    if (RotateCMSCollectionTypes &&
        (_cmsGen->debug_collection_type() !=
          ConcurrentMarkSweepGeneration::Concurrent_collection_type)) {
      assert(_cmsGen->debug_collection_type() !=
        ConcurrentMarkSweepGeneration::Unknown_collection_type,
        "Bad cms collection type");
      return false;
    }
  )
  FreelistLocker x(this);
  // ------------------------------------------------------------------
  // Print out lots of information which affects the initiation of a collection.
  if (PrintCMSInitiationStatistics && stats().valid()) {
    gclog_or_tty->print("CMSCollector shouldConcurrentCollect: ");
    gclog_or_tty->stamp();
    gclog_or_tty->print_cr("");
    stats().print_on(gclog_or_tty);
    gclog_or_tty->print_cr("time_until_cms_gen_full %3.7f", stats().time_until_cms_gen_full());
    gclog_or_tty->print_cr("free="SIZE_FORMAT, _cmsGen->free());
    gclog_or_tty->print_cr("contiguous_available="SIZE_FORMAT, _cmsGen->contiguous_available());
    gclog_or_tty->print_cr("promotion_rate=%g", stats().promotion_rate());
    gclog_or_tty->print_cr("cms_allocation_rate=%g", stats().cms_allocation_rate());
    gclog_or_tty->print_cr("occupancy=%3.7f", _cmsGen->occupancy());
    gclog_or_tty->print_cr("initiatingOccupancy=%3.7f", _cmsGen->initiating_occupancy());
    gclog_or_tty->print_cr("metadata initialized %d",  MetaspaceGC::should_concurrent_collect());
  }
  // ------------------------------------------------------------------
  
  // 第二种触发情况
  // If the estimated time to complete a cms collection (cms_duration())
  // is less than the estimated time remaining until the cms generation is full, start a collection.
   //如果完成cms gc的预估时间小于cms生成完成之前的估计剩余时间，则开始收集
  if (!UseCMSInitiatingOccupancyOnly) {
    if (stats().valid()) {//判断统计数据是否有效
      if (stats().time_until_cms_start() == 0.0) {//获取
        return true;
      }
    } else {//然而第一次 CMS GC 时，统计数据还没有形成是无效的，这时会跟据 Old Gen 的使用占比来进行判断是否要进行 GC
        //占50%开始回收(bootstrapoccupancy 的值)在没有配置 UseCMSInitiatingOccupancyOnly 时，老年代占比到 50% 就进行了一次 CMS GC 。
      // We want to conservatively collect somewhat early in order to try and "bootstrap" our CMS/promotion statistics;
      // this branch will not fire after the first successful CMS collection because the stats should then be valid.
      if (_cmsGen->occupancy() >= _bootstrap_occupancy) {
        if (Verbose && PrintGCDetails) {
          gclog_or_tty->print_cr( "CMSCollector:collect for bootstrapping statistics:occupancy=%f,boot occupancy=%f", _cmsGen->occupancy(),
            _bootstrap_occupancy);
        }
        return true;
      }
    }
  }
  
  // 第三种触发情况
  // Otherwise, we start a collection cycle if old gen want a collection cycle started. 
  // Each may use an appropriate criterion for making this decision.
  // XXX We need to make sure that the gen expansion criterion dovetails well with this. XXX NEED TO FIX THIS
  if (_cmsGen->should_concurrent_collect()) {
    if (Verbose && PrintGCDetails) {
      gclog_or_tty->print_cr("CMS old gen initiated");
    }
    return true;
  }

  // 第四种触发情况
  // We start a collection if we believe an incremental collection may fail;
  // this is not likely to be productive in practice because it's probably too
  // late anyway.
  GenCollectedHeap* gch = GenCollectedHeap::heap();
  assert(gch->collector_policy()->is_two_generation_policy(),"You may want to check the correctness of the following");
  if (gch->incremental_collection_will_fail(true)) {
    if (Verbose && PrintGCDetails) { gclog_or_tty->print("CMSCollector: collect because incremental collection will fail ");  }
    return true;
  }
  
  // 第五种触发情况
  if (MetaspaceGC::should_concurrent_collect()) {
      if (Verbose && PrintGCDetails) { gclog_or_tty->print("CMSCollector: collect for metadata allocation ");}
      return true;
    }

  return false;
}

```



##### background collector 5 种触发条件：

1、是否是并行 Full GC

​		指的是在 GC cause 是 _gc_locker 且配置了 GCLockerInvokesConcurrent 参数, 或者 GC cause 是_java_lang_system_gc（就是 System.gc()调用）且配置了 ExplicitGCInvokesConcurrent 参数，这是会触发一次 background collector。



2、根据统计数据动态计算（参数UseCMSInitiatingOccupancyOnly=false时,默认为false）

​		判断逻辑是如果预测 CMS GC 完成所需要的时间大于预计的老年代将要填满的时间，则进行 GC



3、根据 Old Gen 情况判断

```c
bool ConcurrentMarkSweepGeneration::should_concurrent_collect() const {
  
  assert_lock_strong(freelistLock());
    //occupancy()是 Old Gen 当前空间的使用占比,initiating_occupancy是 CMSInitiatingOccupancyFraction 配置值或者默认的92%
  if (occupancy() > initiating_occupancy()) {
    if (PrintGCDetails && Verbose) {
      gclog_or_tty->print(" %s: collect because of occupancy %f / %f ", short_name(), occupancy(), initiating_occupancy());
    }
    return true;
  }
  if (UseCMSInitiatingOccupancyOnly) {  return false; }
  if (expansion_cause() == CMSExpansionCause::_satisfy_allocation) {
    if (PrintGCDetails && Verbose) { gclog_or_tty->print(" %s: collect because expanded for allocation ", short_name());  }
    return true;
  }
  if (_cmsSpace->should_concurrent_collect()) {
    if (PrintGCDetails && Verbose) { gclog_or_tty->print(" %s: collect because cmsSpace says so ", short_name()); }
    return true;
  }
  return false;
}

```

 从源码中可以分4中小情况

- Old Gen 空间使用占比与阈值比较，如果大于阈值则进行 CMS GC

  initiating_occupancy ：

  ```c
  void ConcurrentMarkSweepGeneration::init_initiating_occupancy(intx io, uintx tr) {
   assert(io <= 100 && tr <= 100, "Check the arguments");
   if (io >= 0) {
     _initiating_occupancy = (double)io / 100.0;//CMSInitiatingOccupancyFraction 参数配置值大于 0，则为io/100.0
   } else {//CMSInitiatingOccupancyFraction 未配置(-1)，或者小于0,则计算后的结果为92%
     _initiating_occupancy = ((100 - MinHeapFreeRatio) + (double)(tr * MinHeapFreeRatio) / 100.0)/ 100.0;
   }
  }
  ```

  > java -XX:+PrintFlagsFinal -version|grep    MinHeapFreeRatio 或者 CMSTriggerRatio 查看默认值如下
  >
  > uintx MinHeapFreeRatio                          = 40                           {manageable}
  >
  > uintx CMSTriggerRatio                           = 80                                  {product}



- 未配置UseCMSInitiatingOccupancyOnly 则未false
- Old Gen 因为对象分配空间而进行扩容，且成功分配空间，这时会考虑进行一次 CMS GC
- 根据 CMS Gen 空闲链判断，较为复杂，默认为false





4、根据增量 GC 是否可能会失败（悲观策略）

​	 意思就是Young GC 已经失败或者可能会失败，JVM 就认为需要进行一次 CMS GC。

​	     **young gc失败：**Old Gen 没有足够的空间来容纳晋升的对象

 	   **可能失败:** 通过判断当前 Old Gen 剩余的空间大小是否大于 Young GC 晋升的对象大小。Young GC 到底要晋升多少是无法提前知道的，因此通过统计平均每次 Young GC 晋升的大小和当前 Young GC 可能晋升的最大大小来进行比较。



5、根据 meta space 情况判断

​	主要看 metaspace 的 _should_concurrent_collect 标志，这个标志在 meta space 进行扩容前如果配置了 CMSClassUnloadingEnabled 参数时，会进行设置。这种情况下就会进行一次 CMS GC。因此经常会有应用启动不久，Old Gen 空间占比还很小的情况下，进行了一次 CMS GC，让你很莫名其妙，其实就是这个原因导致的。 





##### cms优化参考

https://www.jianshu.com/p/d6441e775dd9   https://club.perfma.com/article/190389



```
-XX:+UseConcMarkSweepGC -XX:+UseParNewGC  //开启cms和parNew gc
-Xms5114m -Xmx5114m -XX:MaxNewSize=1967m -XX:NewSize=1967m 
-XX:SurvivorRatio=22  -XX:PermSize=384m -XX:MaxPermSize=384m -Xss512k -XX:OldPLABSize=16 
-XX:+UseCMSCompactAtFullCollection //开启full gc后内存碎片整理
-XX:+UseCMSInitiatingOccupancyOnly //是否根据设置的初始占用率计算
-XX:CMSFullGCsBeforeCompaction=0 //cms 每次触发full gc内存都整理 
-XX:CMSInitiatingOccupancyFraction=75 //cms 老年代内存超过75%能触发gc
-XX:InitialCodeCacheSize=128m //初始化code cache 大小
-XX:+PrintClassHistogram //打印class柱状图
-XX:PrintFLSStatistics=1 
-XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails 
-XX:+PrintGCTimeStamps 
-XX:+PrintHeapAtGC 
-XX:+PrintPromotionFailure 
-XX:+PrintTenuringDistribution 
-XX:ReservedCodeCacheSize=128m//code cache 保留大小 默认240m
-XX:+StartAttachListener 
-XX:+UseCompressedClassPointers //开启类指针压缩
-XX:+UseCompressedOops //开启普通对象指针压缩
-XX:+DisableExplicitGC //禁用system.gc()    Explicit  显式，清晰的，明确的; 直言的
```

