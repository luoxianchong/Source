# wait、notify、notifyAll、sleep、park、unpark

### wait

```java
/**
     * Causes the current thread to wait until another thread invokes the
     * {@link java.lang.Object#notify()} method or the
     * {@link java.lang.Object#notifyAll()} method for this object.
     * In other words, this method behaves exactly as if it simply
     * performs the call {@code wait(0)}.
     * <p>
     * The current thread must own this object's monitor. The thread
     * releases ownership of this monitor and waits until another thread
     * notifies threads waiting on this object's monitor to wake up
     * either through a call to the {@code notify} method or the
     * {@code notifyAll} method. The thread then waits until it can
     * re-obtain ownership of the monitor and resumes execution.
     * <p>
     * As in the one argument version, interrupts and spurious wakeups are
     * possible, and this method should always be used in a loop:
     * <pre>
     *     synchronized (obj) {
     *         while (&lt;condition does not hold&gt;)
     *             obj.wait();
     *         ... // Perform action appropriate to condition
     *     }
     * </pre>
     * This method should only be called by a thread that is the owner
     * of this object's monitor. See the {@code notify} method for a
     * description of the ways in which a thread can become the owner of
     * a monitor.
     *
     * @throws  IllegalMonitorStateException  if the current thread is not
     *               the owner of the object's monitor.
     * @throws  InterruptedException if any thread interrupted the
     *             current thread before or while the current thread
     *             was waiting for a notification.  The <i>interrupted
     *             status</i> of the current thread is cleared when
     *             this exception is thrown.
     * @see        java.lang.Object#notify()
     * @see        java.lang.Object#notifyAll()
     */
    public final void wait() throws InterruptedException {
        wait(0);
    }


从javadoc中可以知道执行wait方法前需要获取wait方法实例的monitor 即锁监视器，否则异常，
并且建议while循环中执行。当当前线程调用wait方法是线程将释放实例的monitor锁监视器
```



### sleep

```java
  /**
     * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds, subject to
     * the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors. 
     * 当前线程执行指定毫秒的sleep（暂时停止执行），指定时间取决于系统定时器和调度器的精度和准确性。 但线程不会释放任何监视器的所有权。
     * @param  millis
     *         the length of time to sleep in milliseconds
     *
     * @throws  IllegalArgumentException
     *          if the value of {@code millis} is negative
     *
     * @throws  InterruptedException
     *          if any thread has interrupted the current thread. The
     *          <i>interrupted status</i> of the current thread is
     *          cleared when this exception is thrown.
     */
    public static native void sleep(long millis) throws InterruptedException;
```



### join

```java
 /**
     * Waits at most {@code millis} milliseconds for this thread to
     * die. A timeout of {@code 0} means to wait forever.
     *
     * <p> This implementation uses a loop of {@code this.wait} calls
     * conditioned on {@code this.isAlive}. As a thread terminates the
     * {@code this.notifyAll} method is invoked. It is recommended that
     * applications not use {@code wait}, {@code notify}, or
     * {@code notifyAll} on {@code Thread} instances.
     
     该方法的实现是使用循环判断该线程isAlive()方法，如果为true,那么就调用wait()方法进行等待。当线程结束时，notifyAll()方法被调用。如果调用join()后再调用notify()/notifyAll()则可能会时join()方法失效。
     *
     */
    public final synchronized void join(long millis) throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;

        if (millis < 0) { throw new IllegalArgumentException("timeout value is negative");  }

        if (millis == 0) {
            while (isAlive()) {  wait(0); }
        } else {
            while (isAlive()) {
                long delay = millis - now;
                if (delay <= 0) {  break;  }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }

注意：阻塞的是调用该方法的线程，而不是该方法的实例。
```



notifyAll

```java
 /**
     * Wakes up a single thread that is waiting on this object's monitor. If any threads are waiting on this object, one of them
     * is chosen to be awakened. The choice is arbitrary and occurs at the discretion of the implementation. A thread waits on an object's
     * monitor by calling one of the {@code wait} methods.
     * <p>
     * The awakened thread will not be able to proceed until the current thread relinquishes the lock on this object. The awakened thread will
     * compete in the usual manner with any other threads that might be actively competing to synchronize on this object; for example, the
     * awakened thread enjoys no reliable privilege or disadvantage in being the next thread to lock this object.
     * <p>
     * This method should only be called by a thread that is the owner of this object's monitor. A thread becomes the owner of the
     * object's monitor in one of three ways:
     * <ul>
     * <li>By executing a synchronized instance method of that object.
     * <li>By executing the body of a {@code synchronized} statement
     *     that synchronizes on the object.
     * <li>For objects of type {@code Class,} by executing a
     *     synchronized static method of that class.
     * </ul>
     * <p>
     * Only one thread at a time can own an object's monitor.
     *
     * @throws  IllegalMonitorStateException  if the current thread is not
     *               the owner of this object's monitor.
     * @see        java.lang.Object#notifyAll()
     * @see        java.lang.Object#wait()
     */
    public final native void notify();

notify 唤醒的是等待集合里的其中一个，选择是随机的。唤醒后的线程时没有持有对象锁的，他讲正常的与其他线程竞争锁。 
如果在一个同步代码块里使用wait，synchronized 使其拥有锁，wait使其失去锁，notify又使其拥有与其他线程竞争锁的权利。


```


为什么线程协作的 wait() 方法需要写在循环里
```java
synchronized int get() throws InterruptedException {
      while (list.size() == 0) {
          wait();
      }
      int v = list.remove(0);
      notifyAll();
      return v;
  }
  ```

