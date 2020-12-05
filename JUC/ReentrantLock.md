## ReentrantLock

### 非公平锁（NonfairSync）

lock：

1、cas 判断是不是第一次锁，是 设置当前线程并独占资源

2、如果不是，则首先尝试获得（根据state重入次数和当前占有资源的线程），成功设置当前线程并独占资源。

3、不成功，则尝试加入链表队列，加入链表的线程节点模式为独占。成功则return

>  ①  lock
>
>  ```java
>  exclusiveOwnerThread = Thread.currentThread();
>  ```
>
>  ② lock  进入等待
>
>  ```java
>  //自旋入队
>  private Node enq(final Node node) {
>       for (;;) {
>           Node t = tail;
>           if (t == null) { // Must initialize
>               if (compareAndSetHead(new Node()))
>                   tail = head;
>           } else {
>               node.prev = t;
>               if (compareAndSetTail(t, node)) {
>                   t.next = node;
>                   return t;//注意返回的是tail 
>               }
>           }
>       }
>   }
>  ```
>
>  ![](E:\201320180110\source\image\wait-nonfairlock.png)
>
>  head=tail=new Node(Thread.currentThread(), Node.EXCLUSIVE);
>
>  ③ lock 进入等待
>
>  head=wait1--->wait2=tail=new Node(Thread.currentThread(), Node.EXCLUSIVE);

​		

4、还没成功则中断这个线程



### 公平锁（FairSync）

1、在获取是比非公平锁多一个判断，即判断当前线程是不是队首

```java
public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t && ((s = h.next) == null || s.thread != Thread.currentThread());
 }
```



```java
package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.Collection;

public class ReentrantLock implements Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414699L; 
    private final Sync sync;

    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        abstract void lock();

        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() { return new ConditionObject();  }

        final Thread getOwner() {  return getState() == 0 ? null : getExclusiveOwnerThread();        }

        final int getHoldCount() {   return isHeldExclusively() ? getState() : 0;        }

        final boolean isLocked() {  return getState() != 0;        }

        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }
    }

    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        final void lock() {
            if (compareAndSetState(0, 1)) setExclusiveOwnerThread(Thread.currentThread());
            else  acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() { acquire(1); }

        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)  throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    public ReentrantLock() {  sync = new NonfairSync(); }

    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    public void lock() {  sync.lock(); }

    public void lockInterruptibly() throws InterruptedException { sync.acquireInterruptibly(1); }

    public boolean tryLock() { return sync.nonfairTryAcquire(1); }

    public boolean tryLock(long timeout, TimeUnit unit)  throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    public void unlock() { sync.release(1);  }

    public Condition newCondition() { return sync.newCondition();  }

    public int getHoldCount() {  return sync.getHoldCount(); }
    
    public boolean isHeldByCurrentThread() {  return sync.isHeldExclusively(); }

    public boolean isLocked() { return sync.isLocked(); }

    public final boolean isFair() {  return sync instanceof FairSync; }
    
    protected Thread getOwner() {  return sync.getOwner(); }

    public final boolean hasQueuedThreads() { return sync.hasQueuedThreads();  }
    
    public final boolean hasQueuedThread(Thread thread) {  return sync.isQueued(thread);  }
    
    public final int getQueueLength() {  return sync.getQueueLength();   }

    protected Collection<Thread> getQueuedThreads() { return sync.getQueuedThreads(); }

    public boolean hasWaiters(Condition condition) {
        if (condition == null) throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    public int getWaitQueueLength(Condition condition) {
        if (condition == null) throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null) throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?  "[Unlocked]" : "[Locked by thread " + o.getName() + "]");
    }
}

```

