# CountDownLatch、CyclicBarrier、Semaphore、Phaser、Exchanger简单使用

CountDownLatch：当初始化state扣减为0时执行await后面代码，扣减动作为countDown。否则await讲一直阻塞。



CyclicBarrier：当线程await阻塞数量达到初始化设置的parties时将继续执行各自的await后面的代码。



Semaphore：控制执行某段任务的线程数，最多permits（许可）个线程在执行。进入是需要acquire ，完成时需要release。



Phaser：



Exchanger： 两个线程之间交换数据 通过 from =exchange( to ) ,to是要换出的数据，from是获得的新数据。没有时间限制的话会一直阻塞至另一线程拿出数据来交换。