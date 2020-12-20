# ThreadPoolExecutor



```java
package java.util.concurrent;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class ThreadPoolExecutor extends AbstractExecutorService {
    /**
     * 
     * The runState provides the main lifecycle control, taking on values:
     *
     *   RUNNING:  Accept new tasks and process queued tasks
     *   SHUTDOWN: Don't accept new tasks, but process queued tasks
     *   STOP:     Don't accept new tasks, don't process queued tasks, and interrupt in-progress tasks
     *   TIDYING:  All tasks have terminated, workerCount is zero,
     			   the thread transitioning to state TIDYING will run the terminated() hook method
     *   TERMINATED: terminated() has completed
     
     RUNNING 接受新任务并且处理已经进入队列的任务
	 SHUTDOWN 不接受新任务，但是处理已经进入队列的任务
	 STOP 不接受新任务，不处理已经进入队列的任务，并且中断正在执行的任务
	 TIDYING 所有任务执行完成，workerCount为0。线程转到了状态TIDYING会执行terminated()钩子方法
	 TERMINATED terminated()已经执行完成
     
     *
     * The numerical order among these values matters, to allow ordered comparisons. 
       The runState monotonically increases over time, 
       but need not hit each state. The transitions are:
     *
     * RUNNING -> SHUTDOWN
     *    On invocation of shutdown(), perhaps implicitly in finalize()
     * (RUNNING or SHUTDOWN) -> STOP
     *    On invocation of shutdownNow()
     * SHUTDOWN -> TIDYING
     *    When both queue and pool are empty
     * STOP -> TIDYING
     *    When pool is empty
     * TIDYING -> TERMINATED
     *    When the terminated() hook method has completed
     
     状态之间相互转换
	 RUNNING -> SHUTDOWN 调用了shutdown()方法
	 (RUNNING 或 SHUTDOWN) -> STOP 调用了shutdownNow()
	 SHUTDOWN -> TIDYING 当队列和线程池为空
	 STOP -> TIDYING 当线程池为空
	 TIDYING -> TERMINATED 当terminated()钩子方法执行完成
     
     *
     * Threads waiting in awaitTermination() will return when the
     * state reaches TERMINATED.
     *
     * Detecting the transition from SHUTDOWN to TIDYING is less straightforward than you'd like because the queue may become
     * empty after non-empty and vice versa during SHUTDOWN state, but we can only terminate if, after seeing that it is empty, we see
     * that workerCount is 0 (which sometimes entails a recheck -- see  below).
     */
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    private static final int COUNT_BITS = Integer.SIZE - 3;//29      Integer.Size=32
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1; //(2^29)-1 500 million（5亿）  00011111 11111111 11111111 1111111

    /* runState is stored in the high-order bits  线程池运行状态
     -1 二进制 11111111 11111111 11111111 11111111  32个1
     int  4个字节
    */
    
    //高3位
    private static final int RUNNING    = -1 << COUNT_BITS;//11100000 00000000 00000000 00000000 
    private static final int SHUTDOWN   =  0 << COUNT_BITS;//00000000 00000000 00000000 00000000 //32个0
    private static final int STOP       =  1 << COUNT_BITS;//00100000 00000000 00000000 00000000
    private static final int TIDYING    =  2 << COUNT_BITS;//01000000 00000000 00000000 00000000
    private static final int TERMINATED =  3 << COUNT_BITS;//01100000 00000000 00000000 00000000

    // Packing and unpacking ctl  拆包函数
    // c 中 高3位是线程运行状态， 低29位是工作者数量
    private static int runStateOf(int c)     { return c & ~CAPACITY; } // c & 11100000 00000000 00000000 00000000  取c 的高3位
    private static int workerCountOf(int c)  { return c & CAPACITY; } //  c & 00001111 11111111 11111111 11111111  取c 的低29位
    
    //打包函数，rs 代表runState 表示运行状态，即：RUNNING，SHUTDOWN，STOP，TIDYING，TERMINATED。wc 工作线程数量，rs 或 wc 构成上述 c。
    private static int ctlOf(int rs, int wc) { return rs | wc; } //| 运算 有1则为1, & 都为1才是1

    /*
     * Bit field accessors that don't require unpacking ctl. These depend on the bit layout and on workerCount being never negative.
     */

    //小于runState, s表示状态
    private static boolean runStateLessThan(int c, int s) { return c < s; }

    //大于等于runSate,s表示状态
    private static boolean runStateAtLeast(int c, int s) { return c >= s; }

    //运行中，c 小于-1 ，由于c的高位存储的是状态位，如果c是运行中则高3位必须是111，表示负数。所以运行中c讲小于-1
    private static boolean isRunning(int c) {  return c < SHUTDOWN; }

    //工作线程数量使用CAS自增
    private boolean compareAndIncrementWorkerCount(int expect) {  return ctl.compareAndSet(expect, expect + 1);}

    //工作线程数量使用CAS自减
    private boolean compareAndDecrementWorkerCount(int expect) { return ctl.compareAndSet(expect, expect - 1); }

    //仅在线程突然终止时调用（请参阅processworkerexit）. 其他自减在gettask中执行
    private void decrementWorkerCount() { do {} while (! compareAndDecrementWorkerCount(ctl.get())); }

    /**
     * 用于保存任务并传递给工作线程的队列.  我们不要求workQueue.poll（）返回null必然意味着.isEmpty(), 
     因此，仅依赖isEmpty来查看队列是否为空（例如，在决定是否从SHUTDOWN过渡到TIDYING时，我们必须这样做）
     这适用于一些特殊用途的队列，比如DelayQueues，poll（）可以返回null，即使在延迟过期时它可能返回非null。
     */
    private final BlockingQueue<Runnable> workQueue;
    
    private final ReentrantLock mainLock = new ReentrantLock();

    //包含所有工作线程的集合，只有当持有mainLock锁才可以访问
    private final HashSet<Worker> workers = new HashSet<Worker>();

    //终止的等待条件
    private final Condition termination = mainLock.newCondition();

    //表示线程池从创建到现在，池中线程的数量出现过最大值
    private int largestPoolSize;

    //完成任务数
    private long completedTaskCount;

    //生产线程的工厂类。
    private volatile ThreadFactory threadFactory;

    //拒绝策略
    private volatile RejectedExecutionHandler handler;

    //最大空闲时间
    private volatile long keepAliveTime;

    //所有线程超时时间，包含核心线程，当线程空闲时间超过 keepAliveTime 值。将xxx
    private volatile boolean allowCoreThreadTimeOut;

    //核心线程数
    private volatile int corePoolSize;

    //最大线程，实际的最大值在内部受CAPACITY的限制。
    private volatile int maximumPoolSize;

    //默认拒绝策略
    private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();
    
    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");
    private final AccessControlContext acc;

    private final class Worker  extends AbstractQueuedSynchronizer implements Runnable  {
        private static final long serialVersionUID = 6138294804551838833L;

        final Thread thread; 
        Runnable firstTask;
        volatile long completedTasks;
        
        Worker(Runnable firstTask) {
            setState(-1); // 禁止中断直到runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        /** Delegates main run loop to outer runWorker  */
        public void run() { runWorker(this);  }
        
        //0 表示锁释放，非0 表示已锁状态
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try { t.interrupt(); } catch (SecurityException ignore) {  }
            }
        }
    }

    /**
     * 将运行状态转换为给定的目标，或者如果已经是小于给定的状态，则将其保留。
     *
     * @param targetState the desired state, either SHUTDOWN or STOP
     *        (but not TIDYING or TERMINATED -- use tryTerminate for that)
     */
    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) || ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }

    /**
    	尝试中断，runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()),showdownNow()
    */
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            if (isRunning(c) || runStateAtLeast(c, TIDYING) || (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            if (workerCountOf(c) != 0) { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }

    /*
     * Methods for controlling interrupts to worker threads.
     */

    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }

    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Common form of interruptIdleWorkers, to avoid having to
     * remember what the boolean argument means.
     */
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    /*
     * Misc utilities, most of which are also exported to
     * ScheduledThreadPoolExecutor
     */

    /**
     * Invokes the rejected execution handler for the given command.
     * Package-protected for use by ScheduledThreadPoolExecutor.
     */
    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * Performs any further cleanup following run state transition on
     * invocation of shutdown.  A no-op here, but used by
     * ScheduledThreadPoolExecutor to cancel delayed tasks.
     */
    void onShutdown() {
    }

    /**
     * State check needed by ScheduledThreadPoolExecutor to
     * enable running tasks during shutdown.
     *
     * @param shutdownOK true if should return true if SHUTDOWN
     */
    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(ctl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    /**
     * Drains the task queue into a new list, normally using drainTo. 
     * But if the queue is a DelayQueue or any other kind of queue for which poll or drainTo may fail to remove some
     * elements, it deletes them one by one.
     drain 排空，耗竭，耗空
     把阻塞队列中的任务移到新的ArrayList,并清空阻塞队列
     */
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

    /*
     * Methods for creating, running and cleaning up after workers
     */

    /**
    新增工作线程，并做了线程池状态校验，工作线程数变量的自增。最大
    */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);//线程池状态

            // Check if queue empty only if necessary.
            /** 
            rs >= SHUTDOWN 指线程池等于 STOP 、TIDYING、TERMINATED、SHUTDOWN
            并且排除（rs==SHUTDOWN 且 firstTask == null 且 workQueue 非空 ）的情况。
            问题：这是一种什么情况，线程池shutdown,且还有未执行完的任务
            */
            if (rs >= SHUTDOWN && ! (rs == SHUTDOWN && firstTask == null && ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c);//获取工作线程数
                //工作线程数大于(2^29)-1,或者大于核心线程，或者指定的最大线程，都返回false
                if (wc >= CAPACITY ||  wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c)) //线程数量自增成功则结束retry。
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }
        
        //上述整段for循环只是做了校验和线程数自增。

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();//问题：为什么加锁，保护的又是啥资源，largestPoolSize 是受mainLock保护，还有其他资源吗？
                try { 
                    int rs = runStateOf(ctl.get());

                    //rs<SHUTDOWN 即RUNING，运行状态。 或者SHUTDOWMN状态，但任务（firstTask）是空
                    if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive())  throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null)
                workers.remove(w);
            decrementWorkerCount();
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }

        tryTerminate();

        int c = ctl.get();
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
            addWorker(null, false);
        }
    }

    /**
     * Performs blocking or timed wait for a task, depending on
     * current configuration settings, or returns null if this worker
     * must exit because of any of:
     * 1. There are more than maximumPoolSize workers (due to a call to setMaximumPoolSize).
     * 2. The pool is stopped.
     * 3. The pool is shutdown and the queue is empty.
     * 4. This worker timed out waiting for a task, and timed-out
     *    workers are subject to termination (that is,
     *    {@code allowCoreThreadTimeOut || workerCount > corePoolSize})
     *    both before and after the timed wait, and if the queue is
     *    non-empty, this worker is not the last thread in the pool.
     *
     * @return task, or null if the worker must exit, in which case
     *         workerCount is decremented
     */
    private Runnable getTask() {
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {//死循环
            int c = ctl.get();
            int rs = runStateOf(c);

            // 队列空判断，rs>=SHUTDOWN SHUTDOWN,STOP，TIDYING ,TERMINATED
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            // Are workers subject to culling? culling 剔除，subject to 使...服从, 检查线程是否需要被淘汰。
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            if ((wc > maximumPoolSize || (timed && timedOut)) && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c)) return null;
                continue;
            }

            try {
                //如果线程过多需要被淘汰，则执行任务限时出队（poll）；否则执行take获取。如果都取到任务则不用淘汰线程。
                //否则修改timedOut标志，再次执行循环并判断是否有任务,无任务返回空线程淘汰。
                // 线程淘汰还有一种机制就是运行过程中出现异常，runWorker()方法中completedAbruptly=true
                Runnable r = timed ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : workQueue.take();
                if (r != null)  return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    /**
     * Main worker run loop.  Repeatedly gets tasks from queue and
     * executes them, while coping with a number of issues:
     *
     * 1. We may start out with an initial task, in which case we don't need to get the first one. Otherwise, as long as pool is running, 
     * we get tasks from getTask. If it returns null then the worker exits due to changed pool state or configuration
     * parameters.  Other exits result from exception throws in external code, in which case completedAbruptly holds, 
     * which usually leads processWorkerExit to replace this thread.
     *
     * 2. Before running any task, the lock is acquired to prevent other pool interrupts while the task is executing, and then we
     * ensure that unless pool is stopping, this thread does not have its interrupt set.
     *
     * 3. Each task run is preceded by a call to beforeExecute, which might throw an exception, 
     * in which case we cause thread to die (breaking loop with completedAbruptly true) without processing the task.
     *
     * 4. Assuming beforeExecute completes normally, we run the task,
     * gathering any of its thrown exceptions to send to afterExecute.
     * We separately handle RuntimeException, Error (both of which the
     * specs guarantee that we trap) and arbitrary Throwables.
     * Because we cannot rethrow Throwables within Runnable.run, we
     * wrap them within Errors on the way out (to the thread's
     * UncaughtExceptionHandler).  Any thrown exception also
     * conservatively causes thread to die.
     *
     * 5. After task.run completes, we call afterExecute, which may
     * also throw an exception, which will also cause thread to
     * die. According to JLS Sec 14.20, this exception is the one that
     * will be in effect even if task.run throws.
     *
     * The net effect of the exception mechanics is that afterExecute
     * and the thread's UncaughtExceptionHandler have as accurate
     * information as we can provide about any problems encountered by
     * user code.
     *
     * @param w the worker
     */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {//执行完一个任务，再次从任务队列里获取getTask()。
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                if ((runStateAtLeast(ctl.get(), STOP) || (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) && !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);//工作线程退出，从works(HashSet)中移除。上述抛出过异常则completedAbruptly=true
        }
    }

   

    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
         /*
         * Proceed in 3 steps:
         *1、判断线程数是不是小于核心线程数，是则新增线程并添加任务，否则入队 
         *
         * 2.加入阻塞队列，加入成功，再检查一次线程池状态,如果是非running则移除任务，并执行拒绝策略，否则
         *
         * 3. 如果入队失败（1、队列满了），则新增线程执行，如果也失败了，则执行拒绝策略
         */
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))
            reject(command);
    }

    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(SHUTDOWN);
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return ! isRunning(ctl.get());
    }

    public boolean isTerminating() {
        int c = ctl.get();
        return ! isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runStateAtLeast(ctl.get(), TERMINATED))
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    protected void finalize() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null || acc == null) {
            shutdown();
        } else {
            PrivilegedAction<Void> pa = () -> { shutdown(); return null; };
            AccessController.doPrivileged(pa, acc);
        }
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null) throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }
    
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null)  throw new NullPointerException();
        this.handler = handler;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0) throw new IllegalArgumentException();
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(ctl.get()) > corePoolSize)
            interruptIdleWorkers();
        else if (delta > 0) {
            // We don't really know how many new threads are "needed".
            // As a heuristic, prestart enough new workers (up to new
            // core size) to handle the current number of tasks in
            // queue, but stop if queue becomes empty while doing so.
            int k = Math.min(delta, workQueue.size());
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty())
                    break;
            }
        }
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public boolean prestartCoreThread() {
        return workerCountOf(ctl.get()) < corePoolSize &&  addWorker(null, true);
    }

    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize)
            addWorker(null, true);
        else if (wc == 0)
            addWorker(null, false);
    }

    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, true))
            ++n;
        return n;
    }
    
    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0)  throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value) interruptIdleWorkers();
        }
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)  throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(ctl.get()) > maximumPoolSize)  interruptIdleWorkers();
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }
    
    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0) throw new IllegalArgumentException();
        if (time == 0 && allowsCoreThreadTimeOut()) throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0) interruptIdleWorkers();
    }
    
    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    /* User-level queue utilities */

    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }
    
    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    } 
    
    //清理任务队列中已取消的任务,并尝试Terminate
    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    it.remove();
            }
        } catch (ConcurrentModificationException fallThrough) {
            for (Object r : q.toArray())
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    q.remove(r);
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }

    /* Statistics */
 
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            //获取线程池数量，Terminate状态则为0，否则线程此数量
            return runStateAtLeast(ctl.get(), TIDYING) ? 0 : workers.size();
        } finally {
            mainLock.unlock();
        }
    }
 
    //获取正在运行任务中，或者正在运行任务路上的线程数，实际就是执行Worker.run的线程数量。
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers)
                if (w.isLocked()) ++n;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }
    
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked())   ++n;
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)  n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    public String toString() {
        long ncompleted;
        int nworkers, nactive;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            ncompleted = completedTaskCount;
            nactive = 0;
            nworkers = workers.size();
            for (Worker w : workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked()) ++nactive;
            }
        } finally {  mainLock.unlock();   }
        
        int c = ctl.get();
        String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" :  (runStateAtLeast(c, TERMINATED) ? "Terminated" :  "Shutting down"));
        return super.toString() +
            "[" + rs + ", pool size = " + nworkers + ", active threads = " + nactive +  ", queued tasks = " + workQueue.size() +
            ", completed tasks = " + ncompleted +  "]";
    }

    /* Extension hooks */
    
    protected void beforeExecute(Thread t, Runnable r) { }
    protected void afterExecute(Runnable r, Throwable t) { }
    protected void terminated() { }

    /* Predefined RejectedExecutionHandlers */

    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public CallerRunsPolicy() { }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {  r.run();  }
        }
    }

    public static class AbortPolicy implements RejectedExecutionHandler {
        public AbortPolicy() { }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + e.toString());
        }
    }

    public static class DiscardPolicy implements RejectedExecutionHandler {
        public DiscardPolicy() { }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) { }
    }

    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
       
        public DiscardOldestPolicy() { }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
    
    
    // Public constructors and methods

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,  TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,  TimeUnit unit,
                              BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    } 
    
    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,  TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,  RejectedExecutionHandler handler) {
        
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,  Executors.defaultThreadFactory(), handler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,  BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        
        if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0)
            throw new IllegalArgumentException();
        
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        
        this.acc = System.getSecurityManager() == null ?  null :  AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
}

```



```java
package java.util.concurrent;
import java.util.*;

public abstract class AbstractExecutorService implements ExecutorService {

    /*构建一个FutureTask,
    最后还是保存到了Executors中的内部类，然后获得一个Callable 保存在FutureTask中
    static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;
        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }
        public T call() {
            task.run();
            return result;
        }
    }
    
    参数：runnable ,value 指定返回值，原本runnable是无返回值的，callable 才有。
    
    */
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new FutureTask<T>(runnable, value);
    }
  
    /*构建一个FutureTask,Callable保存在FutureTask中*/
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(callable);
    }
    
    /**提交一个任务*/
    public Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);//java.util.concurrent.ThreadPoolExecutor#execute
        return ftask;
    }

    // 参数：runnable ,value 指定返回值，原本runnable是无返回值的，callable才有,所以此方法才支持指定返回值。
    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks, boolean timed, long nanos)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null) throw new NullPointerException();
        int ntasks = tasks.size();
        if (ntasks == 0) throw new IllegalArgumentException();
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(ntasks);
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(this);

        try {
            ExecutionException ee = null;
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Iterator<? extends Callable<T>> it = tasks.iterator();

            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;

            for (;;) {
                Future<T> f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    }
                    else if (active == 0)   break;
                    else if (timed) {
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                        if (f == null)  throw new TimeoutException();
                        nanos = deadline - System.nanoTime();
                    }
                    else
                        f = ecs.take();
                }
                if (f != null) {
                    --active;
                    try {
                        return f.get();
                    } catch (ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }

            if (ee == null)  ee = new ExecutionException();
            throw ee;

        } finally {
            for (int i = 0, size = futures.size(); i < size; i++)
                futures.get(i).cancel(true);
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    /** 指定执行tasks */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (tasks == null) throw new NullPointerException();
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f);
                execute(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (CancellationException|ExecutionException ignore) {   }
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(true);
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        if (tasks == null)  throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> t : tasks) futures.add(newTaskFor(t));

            final long deadline = System.nanoTime() + nanos;
            final int size = futures.size();

            for (int i = 0; i < size; i++) {
                execute((Runnable)futures.get(i));
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L)
                    return futures;
            }

            for (int i = 0; i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    if (nanos <= 0L)  return futures;
                    try {
                        f.get(nanos, TimeUnit.NANOSECONDS);
                    } catch (CancellationException | ExecutionException ignore) {
                    } catch (TimeoutException toe) {
                        return futures;
                    }
                    nanos = deadline - System.nanoTime();
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(true);
        }
    }

}
```



###### ExecutorCompletionService有三个私有属性，分别是executor、aes和completionQueue，其中completionQueue就是存储已完成任务的队列

## 方法解析

ExecutorCompletionService实现了CompletionService接口，在CompletionService接口中定义了如下这些方法：

- Future<V> submit(Callable<V> task):提交一个Callable类型任务，并返回该任务执行结果关联的Future；
- Future<V> submit(Runnable task,V result):提交一个Runnable类型任务，并返回该任务执行结果关联的Future；
- Future<V> take():从内部阻塞队列中获取并移除第一个执行完成的任务，阻塞，直到有任务完成；
- Future<V> poll():从内部阻塞队列中获取并移除第一个执行完成的任务，获取不到则返回null，不阻塞；
- Future<V> poll(long timeout, TimeUnit unit):从内部阻塞队列中获取并移除第一个执行完成的任务，阻塞时间为timeout，获取不到则返回null；





**线程池是如何实现的？**

**非核心线程延迟死亡，如何做到？**

**核心线程为什么不会死**

线程池不区分、也没有标记区分核心线程与非核心线程。没获取到任务或获取的任务为null这样的线程就要消亡，从workerSet中移除。

而为什么一直保留核心个数的线程。主要是在获取任务时如果workerSet的数量大于核心数的时候就执行BlockingQueue的poll(timeOut,TimeUnit) 超时就返回空，反之，则通过take一直阻塞直到拿到任务，这也就是线程池里的核心线程为什么不死的原因。





**如何释放核心线程**



**非核心线程能成为核心线程吗?**

线程池不区分核心线程和非核心线程，线程池是期望达到corePoolSize的并发状态，并允许在不得已情况下超载，达到corePoolSize ～ maximumPoolSize 的并发状态