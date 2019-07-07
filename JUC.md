# JUC

### Queue

	add        增加一个元索                     如果队列已满，则抛出一个IllegaISlabEepeplian异常
	remove     移除并返回队列头部的元素    		  如果队列为空，则抛出一个NoSuchElementException异常
	element    返回队列头部的元素                如果队列为空，则抛出一个NoSuchElementException异常
	offer      添加一个元素并返回true            如果队列已满，则返回false
	poll       移除并返问队列头部的元素           如果队列为空，则返回null
	peek       返回队列头部的元素                如果队列为空，则返回null
	put        添加一个元素                     如果队列满，则阻塞
	take       移除并返回队列头部的元素           如果队列为空，则阻塞
​	ConcurrentLinkedQueue  是一个适用于高并发场景下的队列，无阻塞无界队列

​	BlockingQueue

​		LinkedBlockingQueue 不指定时容量为Integer.MAX_VALUE

​		ArrayBlockingQueue   在构造时需要指定容量， 并可以选择是否需要公平性，如果公平参数被设置true，等待时间最长的线程会优先得到处理（其实就是通过将ReentrantLock设置为true来 达到这种公平性的：即等待时间最长的线程会先操作）。通常，公平性会使你在性能上付出代价，只有在的确非常需要的时候再使用它。它是基于数组的阻塞循环队 列，此队列按 FIFO（先进先出）原则对元素进行排序。

​		SynchronousQueue       队列大小为0，没有缓冲的队列，生产者产生的数据直接会被消费者获取并消费。

​		TransferQueue  

​	DelayQueue 是一个存储Delayed元素无界阻塞队列，只有在延迟期满时才能从中提取Delayed元素，该队列的头部是延迟期满后保存时间最长的 元素。如果延迟都还没有期满，则队列没有头部，并且poll将返回null。当一个元素的 getDelay(TimeUnit.NANOSECONDS) 方法返回一个小于或等于零的值时，则出现期满，poll就以移除这个元素了。此队列不允许使用 null 元素

### Dueue

​	LinkedBlockingDeque  线程安全的双端队列实现，允许在队列的头部活尾部进行出队和入队操作



// 创建一个容量为 Integer.MAX_VALUE 的 LinkedBlockingDeque。
LinkedBlockingDeque()
// 创建一个容量为 Integer.MAX_VALUE 的 LinkedBlockingDeque，最初包含给定 collection 的元素，以该 collection 迭代器的遍历顺序添加。
LinkedBlockingDeque(Collection<? extends E> c)
// 创建一个具有给定（固定）容量的 LinkedBlockingDeque。
LinkedBlockingDeque(int capacity)
// 在不违反容量限制的情况下，将指定的元素插入此双端队列的末尾。
boolean add(E e)
// 如果立即可行且不违反容量限制，则将指定的元素插入此双端队列的开头；如果当前没有空间可用，则抛出 IllegalStateException。
void addFirst(E e)
// 如果立即可行且不违反容量限制，则将指定的元素插入此双端队列的末尾；如果当前没有空间可用，则抛出 IllegalStateException。
void addLast(E e)
// 以原子方式 (atomically) 从此双端队列移除所有元素。
void clear()
// 如果此双端队列包含指定的元素，则返回 true。
boolean contains(Object o)
// 返回在此双端队列的元素上以逆向连续顺序进行迭代的迭代器。
Iterator<E> descendingIterator()
// 移除此队列中所有可用的元素，并将它们添加到给定 collection 中。
int drainTo(Collection<? super E> c)
// 最多从此队列中移除给定数量的可用元素，并将这些元素添加到给定 collection 中。
int drainTo(Collection<? super E> c, int maxElements)
// 获取但不移除此双端队列表示的队列的头部。
E element()
// 获取，但不移除此双端队列的第一个元素。
E getFirst()
// 获取，但不移除此双端队列的最后一个元素。
E getLast()
// 返回在此双端队列元素上以恰当顺序进行迭代的迭代器。
Iterator<E> iterator()
// 如果立即可行且不违反容量限制，则将指定的元素插入此双端队列表示的队列中（即此双端队列的尾部），并在成功时返回 true；如果当前没有空间可用，则返回 false。
boolean offer(E e)
// 将指定的元素插入此双端队列表示的队列中（即此双端队列的尾部），必要时将在指定的等待时间内一直等待可用空间。
boolean offer(E e, long timeout, TimeUnit unit)
// 如果立即可行且不违反容量限制，则将指定的元素插入此双端队列的开头，并在成功时返回 true；如果当前没有空间可用，则返回 false。
boolean offerFirst(E e)
// 将指定的元素插入此双端队列的开头，必要时将在指定的等待时间内等待可用空间。
boolean offerFirst(E e, long timeout, TimeUnit unit)
// 如果立即可行且不违反容量限制，则将指定的元素插入此双端队列的末尾，并在成功时返回 true；如果当前没有空间可用，则返回 false。
boolean offerLast(E e)
// 将指定的元素插入此双端队列的末尾，必要时将在指定的等待时间内等待可用空间。
boolean offerLast(E e, long timeout, TimeUnit unit)
// 获取但不移除此双端队列表示的队列的头部（即此双端队列的第一个元素）；如果此双端队列为空，则返回 null。
E peek()
// 获取，但不移除此双端队列的第一个元素；如果此双端队列为空，则返回 null。
E peekFirst()
// 获取，但不移除此双端队列的最后一个元素；如果此双端队列为空，则返回 null。
E peekLast()
// 获取并移除此双端队列表示的队列的头部（即此双端队列的第一个元素）；如果此双端队列为空，则返回 null。
E poll()
// 获取并移除此双端队列表示的队列的头部（即此双端队列的第一个元素），如有必要将在指定的等待时间内等待可用元素。
E poll(long timeout, TimeUnit unit)
// 获取并移除此双端队列的第一个元素；如果此双端队列为空，则返回 null。
E pollFirst()
// 获取并移除此双端队列的第一个元素，必要时将在指定的等待时间等待可用元素。
E pollFirst(long timeout, TimeUnit unit)
// 获取并移除此双端队列的最后一个元素；如果此双端队列为空，则返回 null。
E pollLast()
// 获取并移除此双端队列的最后一个元素，必要时将在指定的等待时间内等待可用元素。
E pollLast(long timeout, TimeUnit unit)
// 从此双端队列所表示的堆栈中弹出一个元素。
E pop()
// 将元素推入此双端队列表示的栈。
void push(E e)
// 将指定的元素插入此双端队列表示的队列中（即此双端队列的尾部），必要时将一直等待可用空间。
void put(E e)
// 将指定的元素插入此双端队列的开头，必要时将一直等待可用空间。
void putFirst(E e)
// 将指定的元素插入此双端队列的末尾，必要时将一直等待可用空间。
void putLast(E e)
// 返回理想情况下（没有内存和资源约束）此双端队列可不受阻塞地接受的额外元素数。
int remainingCapacity()
// 获取并移除此双端队列表示的队列的头部。
E remove()
// 从此双端队列移除第一次出现的指定元素。
boolean remove(Object o)
// 获取并移除此双端队列第一个元素。
E removeFirst()
// 从此双端队列移除第一次出现的指定元素。
boolean removeFirstOccurrence(Object o)
// 获取并移除此双端队列的最后一个元素。
E removeLast()
// 从此双端队列移除最后一次出现的指定元素。
boolean removeLastOccurrence(Object o)
// 返回此双端队列中的元素数。
int size()
// 获取并移除此双端队列表示的队列的头部（即此双端队列的第一个元素），必要时将一直等待可用元素。
E take()
// 获取并移除此双端队列的第一个元素，必要时将一直等待可用元素。
E takeFirst()
// 获取并移除此双端队列的最后一个元素，必要时将一直等待可用元素。
E takeLast()
// 返回以恰当顺序（从第一个元素到最后一个元素）包含此双端队列所有元素的数组。
Object[] toArray()
// 返回以恰当顺序包含此双端队列所有元素的数组；返回数组的运行时类型是指定数组的运行时类型。
<T> T[] toArray(T[] a)
// 返回此 collection 的字符串表示形式。



### SynchronousQueue:



```java
package java.util.concurrent;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.*;

public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    abstract static class Transferer {
        abstract Object transfer(Object e, boolean timed, long nanos);
    }
	//CPU的个数，用于自旋控制
    static final int NCPUS = Runtime.getRuntime().availableProcessors();
	//最大自旋次数，0或32
    static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32;
    /*在未计时等待中阻塞前要旋转的次数 */
    static final int maxUntimedSpins = maxTimedSpins * 16;
    /*  自旋最大时间 单位：nanoseconds 即1ms */
    static final long spinForTimeoutThreshold = 1000L;

    /** Dual stack */
    static final class TransferStack extends Transferer {
        static final int REQUEST    = 0;//消费者请求类型
        static final int DATA       = 1;//数据类型
        static final int FULFILLING = 2;//消费或生产匹配一致
        static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; }

        /** Node class for TransferStacks. */
        static final class SNode {
            volatile SNode next;        // next node in stack
            volatile SNode match;       // the node matched to this
            volatile Thread waiter;     // to control park/unpark
            Object item;                // data; or null for REQUESTs
            int mode;

            SNode(Object item) {  this.item = item; }

            boolean casNext(SNode cmp, SNode val) {
                return cmp == next &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            boolean tryMatch(SNode s) {
                if (match == null &&
                    UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) {
                    Thread w = waiter;
                    if (w != null) {    // waiters need at most one unpark
                        waiter = null;
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                return match == s;
            }

            //取消，自旋this=null
            void tryCancel() {
                UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
            }

            boolean isCancelled() {
                return match == this;
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long matchOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class k = SNode.class;
                    matchOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("match"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** The head (top) of the stack */
        volatile SNode head;

        boolean casHead(SNode h, SNode nh) {
            return h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
        }

        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) s = new SNode(e);
            s.mode = mode;
            s.next = next;
            return s;
        }

        /**
         * Puts or takes an item.
         */
        Object transfer(Object e, boolean timed, long nanos) {
            SNode s = null; // constructed/reused as needed
            int mode = (e == null) ? REQUEST : DATA;

            for (;;) {
                SNode h = head;
                if (h == null || h.mode == mode) {  // empty or same-mode
                    if (timed && nanos <= 0) {      // can't wait
                        if (h != null && h.isCancelled())
                            casHead(h, h.next);     // pop cancelled node
                        else
                            return null;
                    } else if (casHead(h, s = snode(s, e, h, mode))) {
                        SNode m = awaitFulfill(s, timed, nanos);
                        if (m == s) {               // wait was cancelled
                            clean(s);
                            return null;
                        }
                        if ((h = head) != null && h.next == s)
                            casHead(h, s.next);     // help s's fulfiller
                        return (mode == REQUEST) ? m.item : s.item;
                    }
                } else if (!isFulfilling(h.mode)) { // try to fulfill
                    if (h.isCancelled())            // already cancelled
                        casHead(h, h.next);         // pop and retry
                    else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) {
                        for (;;) { // loop until matched or waiters disappear
                            SNode m = s.next;       // m is s's match
                            if (m == null) {        // all waiters are gone
                                casHead(s, null);   // pop fulfill node
                                s = null;           // use new node next time
                                break;              // restart main loop
                            }
                            SNode mn = m.next;
                            if (m.tryMatch(s)) {
                                casHead(s, mn);     // pop both s and m
                                return (mode == REQUEST) ? m.item : s.item;
                            } else                  // lost match
                                s.casNext(m, mn);   // help unlink
                        }
                    }
                } else {                            // help a fulfiller
                    SNode m = h.next;               // m is h's match
                    if (m == null)                  // waiter is gone
                        casHead(h, null);           // pop fulfilling node
                    else {
                        SNode mn = m.next;
                        if (m.tryMatch(h))          // help match
                            casHead(h, mn);         // pop both h and m
                        else                        // lost match
                            h.casNext(m, mn);       // help unlink
                    }
                }
            }
        }

        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            long lastTime = timed ? System.nanoTime() : 0;
            Thread w = Thread.currentThread();
            SNode h = head;
            int spins = (shouldSpin(s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                if (w.isInterrupted())
                    s.tryCancel();
                SNode m = s.match;
                if (m != null)
                    return m;
                if (timed) {
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                    if (nanos <= 0) {
                        s.tryCancel();
                        continue;
                    }
                }
                if (spins > 0)
                    spins = shouldSpin(s) ? (spins-1) : 0;
                else if (s.waiter == null)
                    s.waiter = w; // establish waiter so can park next iter
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos >  spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * Returns true if node s is at head or there is an active fulfiller.
         */
        boolean shouldSpin(SNode s) {
            SNode h = head;
            return (h == s || h == null || isFulfilling(h.mode));
        }

        /**
         * Unlinks s from the stack.
         */
        void clean(SNode s) {
            s.item = null;   // forget item
            s.waiter = null; // forget thread

            SNode past = s.next;
            if (past != null && past.isCancelled())
                past = past.next;

            // Absorb cancelled nodes at head
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled())
                casHead(p, p.next);

            // Unsplice embedded nodes
            while (p != null && p != past) {
                SNode n = p.next;
                if (n != null && n.isCancelled())
                    p.casNext(n, n.next);
                else
                    p = n;
            }
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class k = TransferStack.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** Dual Queue */
    static final class TransferQueue extends Transferer {
 
        /** Node class for TransferQueue. */
        static final class QNode {
            volatile QNode next;          // next node in queue
            volatile Object item;         // CAS'ed to or from null
            volatile Thread waiter;       // to control park/unpark
            final boolean isData;

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) {
                return next == cmp &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            boolean casItem(Object cmp, Object val) {
                return item == cmp &&
                    UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
            }

            void tryCancel(Object cmp) {
                UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
            }

            boolean isCancelled() { return item == this; }

            boolean isOffList() {
                return next == this;
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long itemOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class k = QNode.class;
                    itemOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        transient volatile QNode head;
        transient volatile QNode tail;
        
        transient volatile QNode cleanMe;

        TransferQueue() {
        	//初始化空节点
            QNode h = new QNode(null, false);
            head = h;
            tail = h;
        }

        void advanceHead(QNode h, QNode nh) {
            if (h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                h.next = h; // forget old next
        }

        /**
         * Tries to cas nt as new tail.
         */
        void advanceTail(QNode t, QNode nt) {
            if (tail == t)
                UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
        }

        /**
         * Tries to CAS cleanMe slot.
         */
        boolean casCleanMe(QNode cmp, QNode val) {
            return cleanMe == cmp &&
                UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
        }

        /**
         * Puts or takes an item.
         */
        Object transfer(Object e, boolean timed, long nanos) {
            QNode s = null; // constructed/reused as needed
            boolean isData = (e != null);

            for (;;) {
                QNode t = tail;
                QNode h = head;
                if (t == null || h == null)         // saw uninitialized value
                    continue;                       // spin

                if (h == t || t.isData == isData) { // empty or same-mode
                    QNode tn = t.next;
                    if (t != tail)                  // inconsistent read
                        continue;
                    if (tn != null) {               // lagging tail
                        advanceTail(t, tn);
                        continue;
                    }
                    if (timed && nanos <= 0)        // can't wait
                        return null;
                    if (s == null)
                        s = new QNode(e, isData);
                    if (!t.casNext(null, s))        // failed to link in
                        continue;

                    advanceTail(t, s);              // swing tail and wait
                    Object x = awaitFulfill(s, e, timed, nanos);
                    if (x == s) {                   // wait was cancelled
                        clean(t, s);
                        return null;
                    }

                    if (!s.isOffList()) {           // not already unlinked
                        advanceHead(t, s);          // unlink if head
                        if (x != null)              // and forget fields
                            s.item = s;
                        s.waiter = null;
                    }
                    return (x != null) ? x : e;

                } else {                            // complementary-mode
                    QNode m = h.next;               // node to fulfill
                    if (t != tail || m == null || h != head)
                        continue;                   // inconsistent read

                    Object x = m.item;
                    if (isData == (x != null) ||    // m already fulfilled
                        x == m ||                   // m cancelled
                        !m.casItem(x, e)) {         // lost CAS
                        advanceHead(h, m);          // dequeue and retry
                        continue;
                    }

                    advanceHead(h, m);              // successfully fulfilled
                    LockSupport.unpark(m.waiter);
                    return (x != null) ? x : e;
                }
            }
        }

        Object awaitFulfill(QNode s, Object e, boolean timed, long nanos) {
            /* Same idea as TransferStack.awaitFulfill */
            long lastTime = timed ? System.nanoTime() : 0;
            Thread w = Thread.currentThread();
            int spins = ((head.next == s) ?
            			(timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                if (w.isInterrupted())
                    s.tryCancel(e);
                Object x = s.item;
                if (x != e)
                    return x;
                if (timed) {
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                    if (nanos <= 0) {
                        s.tryCancel(e);
                        continue;
                    }
                }
                if (spins > 0)
                    --spins;
                else if (s.waiter == null)
                    s.waiter = w;
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * Gets rid of cancelled node s with original predecessor pred.
         */
        void clean(QNode pred, QNode s) {
            s.waiter = null; // forget thread
            while (pred.next == s) { // Return early if already unlinked
                QNode h = head;
                QNode hn = h.next;   // Absorb cancelled first node as head
                if (hn != null && hn.isCancelled()) {
                    advanceHead(h, hn);
                    continue;
                }
                QNode t = tail;      // Ensure consistent read for tail
                if (t == h)
                    return;
                QNode tn = t.next;
                if (t != tail)
                    continue;
                if (tn != null) {
                    advanceTail(t, tn);
                    continue;
                }
                if (s != t) {        // If not tail, try to unsplice
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn))
                        return;
                }
                QNode dp = cleanMe;
                if (dp != null) {    // Try unlinking previous cancelled node
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               // d is gone or
                        d == dp ||                 // d is off list or
                        !d.isCancelled() ||        // d not cancelled or
                        (d != t &&                 // d not tail and
                         (dn = d.next) != null &&  //   has successor
                         dn != d &&                //   that is on list
                         dp.casNext(d, dn)))       // d unspliced
                        casCleanMe(dp, null);
                    if (dp == pred)
                        return;      // s is already saved node
                } else if (casCleanMe(null, pred))
                    return;          // Postpone cleaning s
            }
        }

        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        private static final long tailOffset;
        private static final long cleanMeOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class k = TransferQueue.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
                tailOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
                cleanMeOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("cleanMe"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    private transient volatile Transferer transferer;

    /**
     * Creates a <tt>SynchronousQueue</tt> with nonfair access policy.
     */
    public SynchronousQueue() {
        this(false);
    }

    public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue() : new TransferStack();
    }

    public void put(E o) throws InterruptedException {
        if (o == null) throw new NullPointerException();
        if (transferer.transfer(o, false, 0) == null) {
            Thread.interrupted();
            throw new InterruptedException();
        }
    }

    public boolean offer(E o, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (o == null) throw new NullPointerException();
        if (transferer.transfer(o, true, unit.toNanos(timeout)) != null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }

    public E take() throws InterruptedException {
        Object e = transferer.transfer(null, false, 0);
        if (e != null)
            return (E)e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        Object e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return (E)e;
        throw new InterruptedException();
    }

    public E poll() {
        return (E)transferer.transfer(null, true, 0);
    }

    public boolean isEmpty() {
        return true;
    }

    public int size() {
        return 0;
    }

    public int remainingCapacity() {
        return 0;
    }

    public void clear() {
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public E peek() {
        return null;
    }

    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    public Object[] toArray() {
        return new Object[0];
    }

    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        E e;
        while ( (e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }


    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        E e;
        while (n < maxElements && (e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }



    static class WaitQueue implements java.io.Serializable { }
    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;
    }
    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;
    }
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;
}


示例代码：
public static void main(String[] args) {
		SynchronousQueue<String> queue=new SynchronousQueue<>();
		Thread t=new Thread(()->{
			try {
				long start=System.currentTimeMillis();
				System.out.println("==put before===");
				queue.put("helloworld");
				System.out.println("==put after==="+(System.currentTimeMillis()-start));
			} catch (InterruptedException e) {e.printStackTrace(); }
		});

		t.start();

		try {
			System.out.println("t===>"+t.getState());//runnable
			Thread.sleep(2000);
		} catch (InterruptedException e) { e.printStackTrace(); }
		
		new Thread(()->{
			try {
				System.out.print("take= before=>");
				System.out.println(queue.take());
				System.out.print("take= after=>");
			} catch (InterruptedException e) { e.printStackTrace(); }
		}).start();

		System.out.println(t.getState());//waiting
	}
结果：
t===>RUNNABLE
==put before===
WAITING
take= before=>helloworld
take= after=>==put after===2001
```



