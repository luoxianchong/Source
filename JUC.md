# JUC

### Queue

	add        增加一个元索                     如果队列已满，则抛出一个IIIegaISlabEepeplian异常
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