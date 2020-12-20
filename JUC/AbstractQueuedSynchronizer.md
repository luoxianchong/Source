# AbstractQueuedSynchronizer

```java
package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer  implements java.io.Serializable {
    private static final long serialVersionUID = 7373984972572414691L;
    protected AbstractQueuedSynchronizer() { }

    static final class Node { 
        static final Node SHARED = new Node(); 
        static final Node EXCLUSIVE = null;
        static final int CANCELLED =  1;//表示当前结点已取消调度。当timeout或被中断（响应中断的情况下），会触发变更为此状态，进入该状态后的结点将不会再变化。
        static final int SIGNAL    = -1;//表示后继结点在等待当前结点唤醒。后继结点入队时，会将前继结点的状态更新为SIGNAL。
       
        //表示结点等待在Condition上，当其他线程调用了Condition的signal()方法后，CONDITION状态的结点将从等待队列转移到同步队列中，等待获取同步锁。
        static final int CONDITION = -2;
        static final int PROPAGATE = -3;//共享模式下，前继结点不仅会唤醒其后继结点，同时也可能会唤醒后继的后继结点。

        /**
         * Status field, taking on only the values:
         *   SIGNAL:     The successor of this node is (or will soon be)  blocked (via park), so the current node must
         *               unpark its successor when it releases or  cancels. To avoid races, 
         				 acquire methods must first indicate they need a signal,
         *               then retry the atomic acquire, and then, on failure, block.
         *   CANCELLED:  This node is cancelled due to timeout or interrupt.  Nodes never leave this state. In particular,
         *               a thread with cancelled node never again blocks.
         *   CONDITION:  This node is currently on a condition queue. It will not be used as a sync queue node
         *               until transferred, at which time the status will be set to 0. (Use of this value here has
         *               nothing to do with the other uses of the  field, but simplifies mechanics.)
         *   PROPAGATE:  A releaseShared should be propagated to other  nodes. This is set (for head node only) in
         *               doReleaseShared to ensure propagation continues, even if other operations have since intervened.
         *   0:          None of the above
         *
         * The values are arranged numerically to simplify use. Non-negative values mean that a node doesn't need to
         * signal. So, most code doesn't need to check for particular  values, just for sign.
         *
         * The field is initialized to 0 for normal sync nodes, and  CONDITION for condition nodes.  It is modified using CAS
         * (or when possible, unconditional volatile writes).
         */
        volatile int waitStatus;
        
        volatile Node prev;
        volatile Node next;
        
        volatile Thread thread;
        
        Node nextWaiter;
        
        final boolean isShared() { return nextWaiter == SHARED;  }

        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)  throw new NullPointerException();
            else  return p;
        }

        Node() {    } // Used to establish initial head or SHARED marker

        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
    
    private transient volatile Node head;

    private transient volatile Node tail;

    private volatile int state;
    
    protected final int getState() {  return state;  }
    protected final void setState(int newState) { state = newState;  }
    
    protected final boolean compareAndSetState(int expect, int update) { 
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }
    
    static final long spinForTimeoutThreshold = 1000L;
    
    /**自旋入队，并初始化队列，head中的Thread为null*/
    private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))  tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
    
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        Node pred = tail;
        //如果已经有线程获取了锁，并且此时没有其他线程与当前线程发生竞争（并发）。则快速入队，不执行enq，减少自旋耗时。
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);//此时有其他线程与当前线程发生竞争。
        return node;
    }
    
    private void setHead(Node node) {//设置头结点
        head = node;
        node.thread = null;
        node.prev = null;
    }
    
    private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0) compareAndSetWaitStatus(node, ws, 0);
        
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev){
                 if (t.waitStatus <= 0)  s = t;
            }
        }
        if (s != null)  LockSupport.unpark(s.thread);
    }
    
    private void doReleaseShared() {
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))  continue;    // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))  continue;  // loop on failed CAS
            }
            if (h == head) break;   // loop if head changed
        }
    }

    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);
        
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared()) {doReleaseShared();}
        }
    }

    private void cancelAcquire(Node node) {
        if (node == null) return;

        node.thread = null;

        // Skip cancelled predecessors
        Node pred = node.prev;
        while (pred.waitStatus > 0){node.prev = pred = pred.prev;}
        
        Node predNext = pred.next;
        node.waitStatus = Node.CANCELLED;

        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }
    
    //判断当前节点获取锁失败后是否需要park，
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;//获取当前节点前一个节点的等待状态
        if (ws == Node.SIGNAL)   return true;//当前节点的前一个节点是signal状态，则需要park
        
        if (ws > 0) {//节点状态为取消
            do {
                node.prev = pred = pred.prev;//移除节点状态为取消的节点，直至状态不为取消的节点。
            } while (pred.waitStatus > 0);//前一个节点状态为取消。
            pred.next = node;
        } else {
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
    
    static void selfInterrupt() { Thread.currentThread().interrupt(); }
    
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }
    
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;//标记是否成功拿到资源
        try {
            boolean interrupted = false;//标记等待过程中是否被中断过
            for (;;) {
                final Node p = node.predecessor();//获取前一个节点
                //如果前节点是head，即该结点已成第二，那么便有资格去尝试获取资源（可能是head释放完资源唤醒自己的，当然也可能被interrupt了）。
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                //shouldParkAfterFailedAcquire 判断获取锁失败后是否需要park，如果需要park则执行parkAndCheckInterrupt
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {interrupted = true;}
            }
        } finally {
            if (failed)  cancelAcquire(node);
        }
    }

    private void doAcquireInterruptibly(int arg) throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()){ throw new InterruptedException(); }
            }
        } finally {
            if (failed)  cancelAcquire(node);
        }
    }
    
    private boolean doAcquireNanos(int arg, long nanosTimeout)  throws InterruptedException {
        if (nanosTimeout <= 0L)  return false;
        
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L) return false;
                if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold){
                    LockSupport.parkNanos(this, nanosTimeout);
                }
                if (Thread.interrupted()) throw new InterruptedException();
            }
        } finally {
            if (failed) cancelAcquire(node);
        }
    }
    
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted) selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()){  interrupted = true;  }
            }
        } finally {
            if (failed) cancelAcquire(node);
        }
    }

    private void doAcquireSharedInterruptibly(int arg) throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) throw new InterruptedException();
            }
        } finally {
            if (failed) cancelAcquire(node);
        }
    }
    
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)  throws InterruptedException {
        if (nanosTimeout <= 0L)  return false;
        
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)  return false;
                if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold){
                    LockSupport.parkNanos(this, nanosTimeout);
                }
                
                if (Thread.interrupted()) throw new InterruptedException();
            }
        } finally {
            if (failed) cancelAcquire(node);
        }
    }

    /******************************待子类实现******************************/
    //之所以没有定义成abstract，是因为独占模式下只用实现tryAcquire-tryRelease，而共享模式下只用实现tryAcquireShared-tryReleaseShared。
    //如果都定义成abstract，那么每个模式也要去实现另一模式下的接口。说到底，Doug Lea还是站在咱们开发者的角度，尽量减少不必要的工作量。
    
    protected boolean tryAcquire(int arg) { throw new UnsupportedOperationException();  }

    protected boolean tryRelease(int arg) { throw new UnsupportedOperationException(); }
    
    protected int tryAcquireShared(int arg) { throw new UnsupportedOperationException(); }
    
    protected boolean tryReleaseShared(int arg) { throw new UnsupportedOperationException(); }
    
    protected boolean isHeldExclusively() {  throw new UnsupportedOperationException(); }
    
	/**============================================================*/
    
    
    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&  acquireQueued(addWaiter(Node.EXCLUSIVE), arg))  selfInterrupt();
    } 
    
    public final void acquireInterruptibly(int arg) throws InterruptedException {
        if (Thread.interrupted())  throw new InterruptedException();
        if (!tryAcquire(arg))  doAcquireInterruptibly(arg);
    }

    public final boolean tryAcquireNanos(int arg, long nanosTimeout)  throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)  unparkSuccessor(h);
            return true;
        }
        return false;
    }

    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)  doAcquireShared(arg);
    }

    public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        if (tryAcquireShared(arg) < 0) doAcquireSharedInterruptibly(arg);
    }
    
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||  doAcquireSharedNanos(arg, nanosTimeout);
    }

    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }
    
    public final boolean hasQueuedThreads() {   return head != tail; }

    public final boolean hasContended() {  return head != null;   }

    public final Thread getFirstQueuedThread() { return (head == tail) ? null : fullGetFirstQueuedThread();}
    
    private Thread fullGetFirstQueuedThread() {
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st; 

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)  firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    public final boolean isQueued(Thread thread) {
        if (thread == null) throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread) return true;
        return false;
    }

    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&  (s = h.next)  != null && !s.isShared() && s.thread != null;
    }

    public final boolean hasQueuedPredecessors() {
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t &&  ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }
    
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null) list.add(t);
        }
        return list;
    }

    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)  list.add(t);
            }
        }
        return list;
    }
    
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)  list.add(t);
            }
        }
        return list;
    }

    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +  "[State = " + s + ", " + q + "empty queue]";
    }
    
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)  return false;
        if (node.next != null) return true;
        
        return findNodeFromTail(node);
    }

    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node) return true;
            if (t == null)  return false;
            t = t.prev;
        }
    }
    
    final boolean transferForSignal(Node node) {
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))  return false; 
        
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))  LockSupport.unpark(node.thread);
        return true;
    }
    
    final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        
        while (!isOnSyncQueue(node))  Thread.yield();
        return false;
    }
    
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)  node.waitStatus = Node.CANCELLED;
        }
    }
    
    public final boolean owns(ConditionObject condition) {  return condition.isOwnedBy(this); }
    
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition)) throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))  throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }
    
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))  throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        
        private transient Node firstWaiter;
        private transient Node lastWaiter;
        
        public ConditionObject() { }

        private Node addConditionWaiter() {
            Node t = lastWaiter;
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null) lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }
        
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null){ firstWaiter = next;} 
                    else{ trail.nextWaiter = next;}
                    
                    if (next == null) {lastWaiter = trail;}
                } 
                else {trail = t;}
                
                t = next;
            }
        }
        
        public final void signal() {
            if (!isHeldExclusively())  throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)  doSignal(first);
        }

        public final void signalAll() {
            if (!isHeldExclusively()) throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }
        
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted()) interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)  selfInterrupt();
        }
        
        private static final int REINTERRUPT =  1;
        private static final int THROW_IE    = -1;

        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }
        
        private void reportInterruptAfterWait(int interruptMode) throws InterruptedException {
            if (interruptMode == THROW_IE) throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)   selfInterrupt();
        }

        public final void await() throws InterruptedException {
            if (Thread.interrupted()) throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE) interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)   unlinkCancelledWaiters();
            if (interruptMode != 0) reportInterruptAfterWait(interruptMode);
        }

        public final long awaitNanos(long nanosTimeout) throws InterruptedException {
            if (Thread.interrupted()) throw new InterruptedException();
            
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)  interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) unlinkCancelledWaiters();
            if (interruptMode != 0)  reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        public final boolean awaitUntil(Date deadline) throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted()) throw new InterruptedException();
            
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE) interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)  unlinkCancelledWaiters();
            if (interruptMode != 0) reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        public final boolean await(long time, TimeUnit unit)  throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())  throw new InterruptedException();
            
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)   LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)  break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {interruptMode = REINTERRUPT;}
            if (node.nextWaiter != null)  unlinkCancelledWaiters();
            if (interruptMode != 0)  reportInterruptAfterWait(interruptMode);
            return !timedout;
        }
        
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        protected final boolean hasWaiters() {
            if (!isHeldExclusively()) throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())  throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) ++n;
            }
            return n;
        }
        
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())  throw new IllegalMonitorStateException();
            
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null) list.add(t);
                }
            }
            return list;
        }
    }

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    } 
    
    private static final boolean compareAndSetWaitStatus(Node node, int expect,  int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
    }

    private static final boolean compareAndSetNext(Node node,  Node expect,  Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}

```