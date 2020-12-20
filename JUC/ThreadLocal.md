# ThreadLocal

```java
package java.lang;
import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ThreadLocal<T> {
    
    private final int threadLocalHashCode = nextHashCode();

    /** The next hash code to be given out. Updated atomically. Starts at zero.  */
    private static AtomicInteger nextHashCode = new AtomicInteger();

    /**
     HASH_INCREMENT 是为了让哈希码能均匀的分布在2的N次方的数组里。
     使用的是斐波那契（Fibonacci）散列算法
     */
    private static final int HASH_INCREMENT = 0x61c88647;//1640531527

    private static int nextHashCode() {  return nextHashCode.getAndAdd(HASH_INCREMENT); }

    protected T initialValue() {   return null;  }

    /**
     * @since 1.8  //lambda 表达式支持ThreadLocal 初始化value
     */
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }

    public ThreadLocal() {  }

    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }

    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }

     public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)  m.remove(this);
     }

    ThreadLocalMap getMap(Thread t) {   return t.threadLocals;  }

    void createMap(Thread t, T firstValue) {   t.threadLocals = new ThreadLocalMap(this, firstValue);   }


    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    T childValue(T parentValue) { throw new UnsupportedOperationException();  }

    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {  return supplier.get();  }
    }

    static class ThreadLocalMap {

        static class Entry extends WeakReference<ThreadLocal<?>> {
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        private static final int INITIAL_CAPACITY = 16;

        private Entry[] table; 
        private int size = 0; 
        private int threshold; // Default to 0， 默认长度的2/3、即加载因子为2/3，即初始化阈值为16*2/3=10

        private void setThreshold(int len) {   threshold = len * 2 / 3;    }

        //ThreadLocalMap使用的是线性探测法(开放地址法)、而HashMap 使用的是链表地址法,i是key的hashCode&(len-1)得到的数组下标
        private static int nextIndex(int i, int len) {  return ((i + 1 < len) ? i + 1 : 0);  }

        private static int prevIndex(int i, int len) {  return ((i - 1 >= 0) ? i - 1 : len - 1);  }

        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.get() == key)
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }

        //key不存在后获取Entry。重新获取一次Entry，没获取到则会清理过期key，并返回null
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null)
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        private void set(ThreadLocal<?> key, Object value) {

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);

            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
            //没有发生清理过期key，并且size(数组已使用的大小)大于等于阈值则进行rehash，rehash中会再次判断是否扩容.
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }
        
        /**
        */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        // Stale 陈旧的，方法意思是替换陈旧的Entry，staleSlot 老的key为空的槽位。
        // 当前该方法只有ThreadLocal.set调用。
        private void replaceStaleEntry(ThreadLocal<?> key, Object value, int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            //Expunge 抹去、删去
            int slotToExpunge = staleSlot;
            
            //获取老的槽位的前一个，且Entry不为空
            /**  | 0 | 1 | 2 | 3| ... | m |...| n-1 | n |
            假设 staleSlot 等于 m ，循环获取 m 前直到Entry为空，slotToExpunge等于最靠前的Key为空的下标。
            */
            for (int i = prevIndex(staleSlot, len);  (e = tab[i]) != null;  i = prevIndex(i, len) )
                if (e.get() == null)  slotToExpunge = i; 
			
            /**  | 0 | 1 | 2 | 3| ... | m |...| n-1 | n |
            假设 staleSlot 等于 m ，循环获取 m 后面直到Entry为空。
            */
            for (int i = nextIndex(staleSlot, len); (e = tab[i]) != null; i = nextIndex(i, len) ) {
                ThreadLocal<?> k = e.get();

                if (k == key) {//m 向后找到相同的key则替换value
                    e.value = value;

                    tab[i] = tab[staleSlot];//当前下标i与staleSlot同时指向e。 什么情况下i与staleSlot不相等？
                    tab[staleSlot] = e; 
                    
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                if (k == null && slotToExpunge == staleSlot)  slotToExpunge = i;
            }

            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            if (slotToExpunge != staleSlot)   cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }
        
        

        /**
         * 清除老的Entry，从staleSlot下标开始向后遍历，如果entry的key为空则清理掉，不为空且rehash下标不是当前下标i。则重新选择下标，并置空当前下标i的Entry。
         最后返回遍历的当前下标i。
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // expunge entry at staleSlot
            tab[staleSlot].value = null;//help gc
            tab[staleSlot] = null; 
            size--;

            // Rehash until we encounter null
            Entry e;
            int i;
            //从staleSlot开始向后遍历，并清除Entry不为空key却为空的Entry。遍历直到遇到第一个为空的Entry。
            //如果遍历遇到Entry不为空，key也不为空，则rehash从新获取下标，
            //下标与当前不相同(可能发生缩/扩容，也可能没有，只不过是使用了链表地址法延后了下标)置空当前i下标的Entry，重新选择一个下标插下。
            for (i = nextIndex(staleSlot, len); (e = tab[i]) != null; i = nextIndex(i, len)) { 
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;
                        
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }
		
        /**
         清理槽位（entry不为空，key为空的槽位）;
         从i开始，直到n/2!=0时结束，如果遇到需要被清理的Entry，n被赋值为tab.length,且i也赋值为expungeStaleEntry的返回值。
         返回值是 是否发生过移除，有则为true。
        */
        /**
         * 启发式地清理slot,
         * i对应entry是非无效（指向的ThreadLocal没被回收，或者entry本身为空）
         * n是用于控制控制扫描次数的
         * 正常情况下如果log n次扫描没有发现无效slot，函数就结束了
         * 但是如果发现了无效的slot，将n置为table的长度len，做一次连续段的清理
         * 再从下一个空的slot开始继续扫描
         * 
         * 这个函数有两处地方会被调用，一处是插入的时候可能会被调用，另外个是在替换无效slot的时候可能会被调用，
         * 区别是前者传入的n为元素个数，后者为table的容量
         */
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                i = nextIndex(i, len);
                Entry e = tab[i];
                if (e != null && e.get() == null) {
                    n = len;
                    removed = true;
                    i = expungeStaleEntry(i);
                }
            } while ( (n >>>= 1) != 0);
            return removed;
        }

        //rehash 目前只被ThreadLocal.set 调用。
        private void rehash() {
            expungeStaleEntries();//清理老的Entry们
            
            //tab.length*2/3 * 3/4 =x 如果size大于等于x则进行扩容。为什么要重新判断。不是线程安全吗？调用方不是判断了吗？
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * Double the capacity of the table.
         容器扩展为之前的两倍，重新hash 并且会清理过期的key。
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        //清理老的Entry们,从0开始到tab.length 结算。遇到Entry不为空，Entry.key为空则调用expungeStaleEntry清理
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    expungeStaleEntry(j);
            }
        }
    }
}

```



## Thread

```java
package java.lang;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.LockSupport;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;

public class Thread implements Runnable {
    
    private static native void registerNatives();
    static { registerNatives(); }

    private volatile String name;
    private int            priority;
    private Thread         threadQ;
    private long           eetop;

    private boolean     single_step;

    private boolean     daemon = false;

    /* JVM state */
    private boolean     stillborn = false;

    /* What will be run. */
    private Runnable target;

    /* The group of this thread */
    private ThreadGroup group;

    /* The context ClassLoader for this thread */
    private ClassLoader contextClassLoader;

    /* The inherited AccessControlContext of this thread */
    private AccessControlContext inheritedAccessControlContext;

    /* For autonumbering anonymous threads. */
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {  return threadInitNumber++; }

    ThreadLocal.ThreadLocalMap threadLocals = null;

    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;

    private long stackSize;

    private long nativeParkEventPointer;

    //Thread ID
    private long tid;

    /* For generating thread ID */
    private static long threadSeqNumber;

    private volatile int threadStatus = 0;

    private static synchronized long nextThreadID() { return ++threadSeqNumber;  }

    volatile Object parkBlocker;

    private volatile Interruptible blocker;
    private final Object blockerLock = new Object();
    
    void blockedOn(Interruptible b) { synchronized (blockerLock) {  blocker = b; } }

    public final static int MIN_PRIORITY = 1;

    public final static int NORM_PRIORITY = 5;

    public final static int MAX_PRIORITY = 10;

    public static native Thread currentThread();

    public static native void yield();

    public static native void sleep(long millis) throws InterruptedException;

    public static void sleep(long millis, int nanos) throws InterruptedException {
        if (millis < 0) {  throw new IllegalArgumentException("timeout value is negative");   }

        if (nanos < 0 || nanos > 999999) {   throw new IllegalArgumentException( "nanosecond timeout value out of range");}

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) { millis++; }

        sleep(millis);
    }

    /**
     * Initializes a Thread with the current AccessControlContext.
     * @see #init(ThreadGroup,Runnable,String,long,AccessControlContext,boolean)
     */
    private void init(ThreadGroup g, Runnable target, String name,  long stackSize) {  init(g, target, name, stackSize, null, true); }
    
    private void init(ThreadGroup g, Runnable target, String name, long stackSize, AccessControlContext acc, boolean inheritThreadLocals) {
        if (name == null) {  throw new NullPointerException("name cannot be null"); }
        this.name = name;

        Thread parent = currentThread();
        SecurityManager security = System.getSecurityManager();
        if (g == null) {
            /* Determine if it's an applet or not */

            /* If there is a security manager, ask the security manager
               what to do. */
            if (security != null) {
                g = security.getThreadGroup();
            }

            /* If the security doesn't have a strong opinion of the matter
               use the parent thread group. */
            if (g == null) {
                g = parent.getThreadGroup();
            }
        }

        /* checkAccess regardless of whether or not threadgroup is
           explicitly passed in. */
        g.checkAccess();

        /*
         * Do we have the required permissions?
         */
        if (security != null) {
            if (isCCLOverridden(getClass())) {
                security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }

        g.addUnstarted();

        this.group = g;
        this.daemon = parent.isDaemon();
        this.priority = parent.getPriority();
        if (security == null || isCCLOverridden(parent.getClass()))
            this.contextClassLoader = parent.getContextClassLoader();
        else
            this.contextClassLoader = parent.contextClassLoader;
        this.inheritedAccessControlContext =
                acc != null ? acc : AccessController.getContext();
        this.target = target;
        setPriority(priority);
        if (inheritThreadLocals && parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals =
                ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        /* Stash the specified stack size in case the VM cares */
        this.stackSize = stackSize;

        /* Set thread ID */
        tid = nextThreadID();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {  throw new CloneNotSupportedException(); }
    
    public Thread() {   init(null, null, "Thread-" + nextThreadNum(), 0);    }
    
    public Thread(Runnable target) {  init(null, target, "Thread-" + nextThreadNum(), 0); }

    Thread(Runnable target, AccessControlContext acc) { init(null, target, "Thread-" + nextThreadNum(), 0, acc, false);}

    public Thread(ThreadGroup group, Runnable target) {   init(group, target, "Thread-" + nextThreadNum(), 0);}

    public Thread(String name) {  init(null, null, name, 0);}
    
    public Thread(ThreadGroup group, String name) {  init(group, null, name, 0);   }
    
    public Thread(Runnable target, String name) {   init(null, target, name, 0); }

    public Thread(ThreadGroup group, Runnable target, String name) { init(group, target, name, 0);}
    
    public Thread(ThreadGroup group, Runnable target, String name,long stackSize) { init(group, target, name, stackSize);   }
    
    public synchronized void start() {
        if (threadStatus != 0)   throw new IllegalThreadStateException();
        group.add(this);
        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) { group.threadStartFailed(this);  }
            } catch (Throwable ignore) { }
        }
    }

    private native void start0();

    @Override
    public void run() {
        if (target != null) { target.run(); }
    }

    private void exit() {
        if (group != null) {
            group.threadTerminated(this);
            group = null;
        }
        /* Aggressively null out all reference fields: see bug 4006245 */
        target = null;
        /* Speed the release of some of these resources */
        threadLocals = null;
        inheritableThreadLocals = null;
        inheritedAccessControlContext = null;
        blocker = null;
        uncaughtExceptionHandler = null;
    }

    
    @Deprecated
    public final void stop() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            checkAccess();
            if (this != Thread.currentThread()) {
                security.checkPermission(SecurityConstants.STOP_THREAD_PERMISSION);
            }
        }
        // A zero status value corresponds to "NEW", it can't change to
        // not-NEW because we hold the lock.
        if (threadStatus != 0) {
            resume(); // Wake up thread if it was suspended; no-op otherwise
        }

        // The VM can handle all thread states
        stop0(new ThreadDeath());
    }

    @Deprecated
    public final synchronized void stop(Throwable obj) {  throw new UnsupportedOperationException(); }
    
    public void interrupt() {
        if (this != Thread.currentThread())   checkAccess();

        synchronized (blockerLock) {
            Interruptible b = blocker;
            if (b != null) {
                interrupt0();           // Just to set the interrupt flag
                b.interrupt(this);
                return;
            }
        }
        interrupt0();
    }
    
    public static boolean interrupted() {  return currentThread().isInterrupted(true); }

    public boolean isInterrupted() {  return isInterrupted(false);  }

    private native boolean isInterrupted(boolean ClearInterrupted);

    @Deprecated
    public void destroy() { throw new NoSuchMethodError(); }
    
    public final native boolean isAlive();
    
    @Deprecated
    public final void suspend() {
        checkAccess();
        suspend0();
    }
    
    @Deprecated
    public final void resume() {
        checkAccess();
        resume0();
    }
    
    public final void setPriority(int newPriority) {
        ThreadGroup g;
        checkAccess();
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {  throw new IllegalArgumentException();   }
        if((g = getThreadGroup()) != null) {
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority();
            }
            setPriority0(priority = newPriority);
        }
    }
    
    public final int getPriority() {  return priority;  }

    public final synchronized void setName(String name) {
        checkAccess();
        if (name == null) {  throw new NullPointerException("name cannot be null");  }

        this.name = name;
        if (threadStatus != 0) {  setNativeName(name);  }
    }
    
    public final String getName() { return name;  }
    
    public final ThreadGroup getThreadGroup() {    return group;  }

    public static int activeCount() { return currentThread().getThreadGroup().activeCount();  }
    
    public static int enumerate(Thread tarray[]) { return currentThread().getThreadGroup().enumerate(tarray); }

    @Deprecated
    public native int countStackFrames();
    
    public final synchronized void join(long millis)
    throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;

        if (millis < 0) { throw new IllegalArgumentException("timeout value is negative"); }

        if (millis == 0) {
            while (isAlive()) {   wait(0);  }
        } else {
            while (isAlive()) {
                long delay = millis - now;
                if (delay <= 0) { break; }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }
    
    public final synchronized void join(long millis, int nanos)throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException( "nanosecond timeout value out of range");
        }

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        join(millis);
    }
    
    public final void join() throws InterruptedException { join(0); }
    
    public static void dumpStack() { new Exception("Stack trace").printStackTrace();}

    public final void setDaemon(boolean on) {
        checkAccess();
        if (isAlive()) {  throw new IllegalThreadStateException();  }
        daemon = on;
    }

    public final boolean isDaemon() { return daemon;  }

    public final void checkAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkAccess(this);
        }
    }
    
    public String toString() {
        ThreadGroup group = getThreadGroup();
        if (group != null) { 
            return "Thread[" + getName() + "," + getPriority() + "," +group.getName() + "]";
        } else {
            return "Thread[" + getName() + "," + getPriority() + "," +"" + "]";
        }
    }
    
    @CallerSensitive
    public ClassLoader getContextClassLoader() {
        if (contextClassLoader == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(contextClassLoader,
                                                   Reflection.getCallerClass());
        }
        return contextClassLoader;
    }
    
    public void setContextClassLoader(ClassLoader cl) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        contextClassLoader = cl;
    } 
    
    public static native boolean holdsLock(Object obj);

    private static final StackTraceElement[] EMPTY_STACK_TRACE   = new StackTraceElement[0];

    public StackTraceElement[] getStackTrace() {
        if (this != Thread.currentThread()) {
            // check for getStackTrace permission
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
            }
            // optimization so we do not call into the vm for threads that
            // have not yet started or have terminated
            if (!isAlive()) {
                return EMPTY_STACK_TRACE;
            }
            StackTraceElement[][] stackTraceArray = dumpThreads(new Thread[] {this});
            StackTraceElement[] stackTrace = stackTraceArray[0];
            // a thread that was alive during the previous isAlive call may have
            // since terminated, therefore not having a stacktrace.
            if (stackTrace == null) {
                stackTrace = EMPTY_STACK_TRACE;
            }
            return stackTrace;
        } else {
            // Don't need JVM help for current thread
            return (new Exception()).getStackTrace();
        }
    }
    
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }

        // Get a snapshot of the list of all threads
        Thread[] threads = getThreads();
        StackTraceElement[][] traces = dumpThreads(threads);
        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
        for (int i = 0; i < threads.length; i++) {
            StackTraceElement[] stackTrace = traces[i];
            if (stackTrace != null) {
                m.put(threads[i], stackTrace);
            }
            // else terminated so we don't put it in the map
        }
        return m;
    }


    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION = new RuntimePermission("enableContextClassLoaderOverride");

    private static class Caches { 
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =   new ConcurrentHashMap<>();
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = new ReferenceQueue<>();
    }
    
    private static boolean isCCLOverridden(Class<?> cl) {
        if (cl == Thread.class) return false;

        processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
        WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
        Boolean result = Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key, result);
        }

        return result.booleanValue();
    }
    
    private static boolean auditSubclass(final Class<?> subcl) {
        Boolean result = AccessController.doPrivileged( new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    for (Class<?> cl = subcl; cl != Thread.class;cl = cl.getSuperclass()) {
                        try {
                            cl.getDeclaredMethod("getContextClassLoader", new Class<?>[0]);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException ex) {}
                        
                        try {
                            Class<?>[] params = {ClassLoader.class};
                            cl.getDeclaredMethod("setContextClassLoader", params);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException ex) {}
                    }
                    return Boolean.FALSE;
                }
            }
        );
        return result.booleanValue();
    }

    private native static StackTraceElement[][] dumpThreads(Thread[] threads);
    private native static Thread[] getThreads();

    public long getId() {
        return tid;
    }

    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED;
    }
    public State getState() {
        return sun.misc.VM.toThreadState(threadStatus);
    }
    
    @FunctionalInterface
    public interface UncaughtExceptionHandler {
        void uncaughtException(Thread t, Throwable e);
    }

    // null unless explicitly set
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    // null unless explicitly set
    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(
                new RuntimePermission("setDefaultUncaughtExceptionHandler")
                    );
        }

         defaultUncaughtExceptionHandler = eh;
     }

    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
        return defaultUncaughtExceptionHandler;
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler != null ?
            uncaughtExceptionHandler : group;
    }
    
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        checkAccess();
        uncaughtExceptionHandler = eh;
    }
    
    private void dispatchUncaughtException(Throwable e) {
        getUncaughtExceptionHandler().uncaughtException(this, e);
    } 
    
    static void processQueue(ReferenceQueue<Class<?>> queue,  ConcurrentMap<? extends WeakReference<Class<?>>, ?> map) {
        Reference<? extends Class<?>> ref;
        while((ref = queue.poll()) != null) { map.remove(ref);  }
    }
    
    static class WeakClassKey extends WeakReference<Class<?>> {
       
        private final int hash;

        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            hash = System.identityHashCode(cl);
        }

        @Override
        public int hashCode() {  return hash; }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof WeakClassKey) {
                Object referent = get();
                return (referent != null) &&  (referent == ((WeakClassKey) obj).get());
            } else {
                return false;
            }
        }
    }

    /** The current seed for a ThreadLocalRandom */
    @sun.misc.Contended("tlr")
    long threadLocalRandomSeed;

    /** Probe hash value; nonzero if threadLocalRandomSeed initialized */
    @sun.misc.Contended("tlr")
    int threadLocalRandomProbe;

    /** Secondary seed isolated from public ThreadLocalRandom sequence */
    @sun.misc.Contended("tlr")
    int threadLocalRandomSecondarySeed;

    /* Some private helper methods */
    private native void setPriority0(int newPriority);
    private native void stop0(Object o);
    private native void suspend0();
    private native void resume0();
    private native void interrupt0();
    private native void setNativeName(String name);
}

```



ThreadLocal 是保存线程变量的一个工具类和存储结构的定义类。其中变量是对其他线程时隔离的。

ThreadLocal 具有以下特性：

1、并发性：在多线程并发场景下使用。

2、传递数据：在同一线程的上下文中传递参数

3、线程隔离：每个线程变量都是独立的、不会相互影响。



ThreadLocalMap 初始化大小16  加载因子2/3 



面试题：

ThreadLocal底层的Hash算法是什么？

```text
斐波那契（Fibonacci）散列法
公式：f(k) = ((k * 2654435769) >> X) << Y对于常见的32位整数而言，也就是 f(k) = (k * 2654435769) >> 28

public class ThreadHashTest {
    public static void main(String[] args) {
        long l1 = (long) ((1L << 32) * (Math.sqrt(5) - 1)/2);
        System.out.println("as 32 bit unsigned: " + l1);
        int i1 = (int) l1;
        System.out.println("as 32 bit signed:   " + i1);
        System.out.println("MAGIC = " + 0x61c88647);
    }
}

结果 ：
as 32 bit unsigned: 2654435769
as 32 bit signed:   -1640531527
MAGIC = 1640531527


斐波那契数列：
数列从第3项开始,每一项都等于前两项之和。 例子:数列 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 23...
```



ThreadLocal如何解决Hash冲突？

```
hash冲突使用链表地址法，即 hashCode&(tab.length-1) 得到数组下标i，如果数组下标i存在Entry元素即冲突，即tab[i]！=null。则重新计算下标i=i+1.
```

ThreadLocal底层的扩容机制是什么？

```
新容量为之前的两倍并且重新hash，重新计算阈值，并且会清理过期的Entry 
```

ThreadLocal的get、set的方法的实现流程



ThreadLocalLMap的key是强引用，还是弱引用？为什么？

```
弱引用，如果是强应用，线程不消亡很容易发生内存泄漏。
```

ThreadLocalMap中key可能过期么？set、get可能会清理过期key的相关Entry么？

```
可能，因为key是一个WeakReference,jvm内存不足时会被回收。key就会过期，key=null。set、get会清理。
```

如何防止ThreadLocal发生内存泄漏？

```
手动执行set、remove、get方法  在set时会判断原来的key(ThreadLocal)是否为空，为空则会调用expungeStaleEntry方法进行清理。remove遇到Entry不为空，Entry.key为空也会清理。
```

ThreadLocal的应用场景有那些？

```
ContextHolder 常用于线程上下文
```

使用ThreadLocal有哪些注意事项？

```
1、注意ThreadLocal NullPointException 问题 即基本类型与包装类的装换（装箱与拆箱）
```

