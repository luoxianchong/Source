# **Synchronized** 



[jvm对象]: ..\JVM\javaObjectMemeory.md



![](..\image\synchronize-monitor.jpg)

### **锁升级**

锁的4中状态：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态（级别从低到高）



###### 无锁-->偏向锁

当前锁对象为无锁状态，线程A 进行一次CAS 将对象头(Mark Word)的偏向锁指向当前线程 (即将对象头的23bit或54bit设置为线程A的ID)，当前线程重入是比较线上ID即可，无需CAS加锁操作。如果比较失败，则需要判断偏向锁的标识。如果标识被设置为0（表示当前是无锁状态），则使用CAS竞争锁；如果标识设置成1（表示当前是偏向锁状态），则尝试使用CAS将对象头的偏向锁指向当前线程，触发偏向锁的撤销。偏向锁只有在竞争出现才会释放锁。当其他线程尝试竞争偏向锁时，程序到达全局安全点后（没有正在执行的代码），它会查看Java对象头中记录的线程是否存活，如果没有存活，那么锁对象被重置为无锁状态，其它线程可以竞争将其设置为偏向锁；如果存活，那么立刻查找该线程的栈帧信息，如果还是需要继续持有这个锁对象，那么暂停当前线程，撤销偏向锁，升级为轻量级锁，如果线程1不再使用该锁对象，那么将锁对象状态设为无锁状态，重新偏向新的线程。



###### 偏向-->轻量级

轻量级锁是指当锁是偏向锁的时候，却被另外的线程所访问，此时偏向锁就会升级为轻量级锁，轻量级锁的获取主要由两种情况：① 当关闭偏向锁功能时；② 由于多个线程竞争偏向锁导致偏向锁升级为轻量级锁。

在代码进入同步块的时候，如果同步对象锁状态为无锁状态，虚拟机将首先在当前线程的栈帧中建立一个名为锁记录（Lock Record）的空间，用于存储锁对象目前的 Mark Word 的拷贝，然后将对象头中的 Mark Word 复制到锁记录中。

拷贝成功后，虚拟机将使用 CAS 操作尝试将对象的 Mark Word 更新为指向 Lock Record 的指针，并将 Lock Record 里的 owner 指针指向对象的 Mark Word。

如果这个更新动作成功了，那么这个线程就拥有了该对象的锁，并且对象 Mark Word 的锁标志位设置为“00”，表示此对象处于轻量级锁定状态。

如果轻量级锁的更新操作失败了，虚拟机首先会检查对象的 Mark Word 是否指向当前线程的栈帧，如果是就说明当前线程已经拥有了这个对象的锁，那就可以直接进入同步块继续执行，否则说明多个线程竞争锁。

若当前只有一个等待线程，则该线程将通过自旋进行等待。但是当自旋超过一定的次数时，轻量级锁便会升级为重量级锁（锁膨胀）。

另外，当一个线程已持有锁，另一个线程在自旋，而此时又有第三个线程来访时，轻量级锁也会升级为重量级锁（锁膨胀）。





###### 轻量级-->重量级









<img src="E:\201320180110\source\image\synchronized-supper.png" style="zoom:200%;" />

```
public class SynchronizedTest {
    public static void main(String[] args) throws Exception {
        // 直接休眠5秒，或者用-XX:BiasedLockingStartupDelay=0关闭偏向锁延迟
        Thread.sleep(5000);
        // 反射获取sun.misc的Unsafe对象，用来查看锁的对象头的信息
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        final Unsafe unsafe = (Unsafe) theUnsafe.get(null);

        // 锁对象
        final Object lock = new Object();
        // TODO 64位JDK对象头为 64bit = 8Byte，如果是32位JDK则需要换成unsafe.getInt
        printf("1_无锁状态：" + getLongBinaryString(unsafe.getLong(lock, 0L)));

        // 如果不执行hashCode方法，则对象头的中的hashCode为0，
        // 但是如果执行了hashCode（identity hashcode，重载过的hashCode方法则不受影响），会导致偏向锁的标识位变为0（不可偏向状态），
        // 且后续的加锁不会走偏向锁而是直接到轻量级锁（被hash的对象不可被用作偏向锁）
//        lock.hashCode();
//        printf("锁对象hash：" + getLongBinaryString(lock.hashCode()));

        printf("2_无锁状态：" + getLongBinaryString(unsafe.getLong(lock, 0L)));

        printf("主线程hash：" +getLongBinaryString(Thread.currentThread().hashCode()));
        printf("主线程ID：" +getLongBinaryString(Thread.currentThread().getId()) + "\n");
        // 无锁 --> 偏向锁
        new Thread(() -> {
            synchronized (lock) {
                printf("3_偏向锁：" +getLongBinaryString(unsafe.getLong(lock, 0L)));
                printf("偏向线程hash：" +getLongBinaryString(Thread.currentThread().hashCode()));
                printf("偏向线程ID：" +getLongBinaryString(Thread.currentThread().getId()) + "\n");
                // 如果锁对象已经进入了偏向状态，再调用hashCode()，会导致锁直接膨胀为重量级锁
//                lock.hashCode();
            }
            // 再次进入同步快，lock锁还是偏向当前线程
            synchronized (lock) {
                printf("4_偏向锁：" +getLongBinaryString(unsafe.getLong(lock, 0L)));
                printf("偏向线程hash：" +getLongBinaryString(Thread.currentThread().hashCode()));
                printf("偏向线程ID：" +getLongBinaryString(Thread.currentThread().getId()) + "\n");
            }
        }).start();
        Thread.sleep(1000);

        // 可以看到就算偏向的线程结束，锁对象的偏向锁也不会自动撤销
        printf("5_偏向线程结束：" +getLongBinaryString(unsafe.getLong(lock, 0L)) + "\n");

        // 偏向锁 --> 轻量级锁
        synchronized (lock) {
            // 对象头为：指向线程栈中的锁记录指针
            printf("6_轻量级锁：" + getLongBinaryString(unsafe.getLong(lock, 0L)));
            // 这里获得轻量级锁的线程是主线程
            printf("轻量级线程hash：" +getLongBinaryString(Thread.currentThread().hashCode()));
            printf("轻量级线程ID：" +getLongBinaryString(Thread.currentThread().getId()) + "\n");
        }
        new Thread(() -> {
            synchronized (lock) {
                printf("7_轻量级锁：" +getLongBinaryString(unsafe.getLong(lock, 0L)));
                printf("轻量级线程hash：" +getLongBinaryString(Thread.currentThread().hashCode()));
                printf("轻量级线程ID：" +getLongBinaryString(Thread.currentThread().getId()) + "\n");
            }
        }).start();
        Thread.sleep(1000);

        // 轻量级锁 --> 重量级锁
        synchronized (lock) {
            int i = 123;
            // 注意：6_轻量级锁 和 8_轻量级锁 的对象头是一样的，证明线程释放锁后，栈帧中的锁记录并未清除，如果方法返回，锁记录是否保留还是清除？
            printf("8_轻量级锁：" + getLongBinaryString(unsafe.getLong(lock, 0L)));
            // 在锁已经获取了lock的轻量级锁的情况下，子线程来获取锁，则锁会膨胀为重量级锁
            new Thread(() -> {
                synchronized (lock) {
                    printf("9_重量级锁：" +getLongBinaryString(unsafe.getLong(lock, 0L)));
                    printf("重量级线程hash：" +getLongBinaryString(Thread.currentThread().hashCode()));
                    printf("重量级线程ID：" +getLongBinaryString(Thread.currentThread().getId()) + "\n");
                }
            }).start();
            // 同步块中睡眠1秒，不会释放锁，等待子线程请求锁失败导致锁膨胀（见轻量级加锁过程）
            Thread.sleep(1000);
        }
        Thread.sleep(500);
    }

    private static String getLongBinaryString(long num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            if ((num & 1) == 1) {
                sb.append(1);
            } else {
                sb.append(0);
            }
            num = num >> 1;
        }
        return sb.reverse().toString();
    }
    private static void printf(String str) {
        System.out.printf("%s%n", str);
    }
}
```



### 当Java处在偏向锁、重量级锁状态时，hashcode值存储在哪？

- 当一个对象已经计算过identity hash code，它就无法进入偏向锁状态；
- 当一个对象当前正处于偏向锁状态，并且需要计算其identity hash code的话，则它的偏向锁会被撤销，并且锁会膨胀为重量锁；
- 重量锁的实现中，ObjectMonitor类里有字段可以记录非加锁状态下的mark word，其中可以存储identity hash code的值。或者简单说就是重量锁可以存下identity hash code。


请一定要注意，这里讨论的hash code都只针对identity hash code。用户自定义的hashCode()方法所返回的值跟这里讨论的不是一回事。Identity hash code是未被覆写的 java.lang.Object.hashCode() 或者 java.lang.System.identityHashCode(Object) 所返回的值。







互斥锁的属性：

1、PTHREAD_MUTEX_TIMED_NP ：缺省值，普通锁，当一个线程加锁后其余请求锁的线程将会形成一个等待队列，并且在解锁后按照优先级获取到锁，这种策略可以确保资源分配的公平性

2、PTHREAD_MUTEX_RECURSTIVE_NP：嵌套锁，允许一个线程对同一个锁成功获取多次，并通过unlock解锁，如果是不同线程请求，则在加锁线程解锁时重新进行竞争。

3、PTHREAD_MUTEX_ERRORCHAECK_NP：检错锁，如果一个线程请求同一个锁，则返回EDEADLK，否则与PTHREAD_MUTEX_TIME_NP类型动作相同，这样就保证了当不允许多次加锁时不会出现最简单情况的下的死锁。

4、PTHERAD_MUTEX_ADAPTIVE_NP：适应锁，动作最简单的锁类型，仅仅等待解锁后重新竞争。