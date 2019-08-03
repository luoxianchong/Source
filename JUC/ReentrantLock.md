## ReentrantLock

### 非公平锁（NonfairSync）

lock：

1、cas 判断是不是第一次锁，是 设置当前线程并独占资源

2、如果不是，则首先尝试获得（根据state重入次数和当前占有资源的线程），成功设置当前线程并独占资源。

3、不成功，则尝试加入链表队列，加入链表的线程节点模式为独占。成功则return

>  ①  lock
>
> ```java
> exclusiveOwnerThread = Thread.currentThread();
> ```
>
> ② lock  进入等待
>
> ```
> private Node enq(final Node node) {
>         for (;;) {
>             Node t = tail;
>             if (t == null) { // Must initialize
>                 if (compareAndSetHead(new Node()))
>                     tail = head;
>             } else {
>                 node.prev = t;
>                 if (compareAndSetTail(t, node)) {
>                     t.next = node;
>                     return t;//注意返回的是tail 
>                 }
>             }
>         }
>     }
> ```
>
> ![](E:\201320180110\source\image\wait-nonfairlock.png)
>
> head=tail=new Node(Thread.currentThread(), Node.EXCLUSIVE);
>
> ③ lock 进入等待
>
> head=wait1--->wait2=tail=new Node(Thread.currentThread(), Node.EXCLUSIVE);

​		

4、还没成功则中断这个线程



### 公平锁（FairSync）

1、在获取是比非公平锁多一个判断，即判断当前线程是不是队首

```
public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
 }
```

