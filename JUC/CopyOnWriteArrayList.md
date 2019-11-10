# CopyOnWriteArrayList

写时复制



**类说明**

A thread-safe variant of ArrayList in which all mutative operations (add, set, and so on) are implemented by making a fresh copy of the underlying array.
这个类通过生成底层数组的新副本来实现线程安全，是ArrayLis的变体实现

This is ordinarily too costly, but may be more efficient than alternatives when traversal operations vastly outnumber mutations, and is useful when you cannot or don't want to synchronize traversals, yet need to preclude interference among concurrent threads.
这通常代价太高，但当遍历操作远远超过变更时，可能比其他选择更有效，当你不能或不想同步遍历，
却需要排除并发线程间的干扰，这时候(这个类的这个操作)就很有用

The "snapshot" style iterator method uses a reference to the state of the array at the point that the iterator was created.
当快照类型的迭代器创建的那一刻起，就使用了数组的引用

This array never changes during the lifetime of the iterator, so interference is impossible and the iterator is guaranteed not to throw ConcurrentModificationException.
这个数组在迭代器的生命周期内绝不会改变，不会有干扰，也不会抛出ConcurrentModificationException异常,因此可以放心的使用

The iterator will not reflect additions, removals, or changes to the list since the iterator was created.
这个迭代器自创建之后，就不会反应在list中的添加、删除和更改中

Element-changing operations on iterators themselves (remove, set, and add) are not supported.
These methods throw UnsupportedOperationException.
在迭代器中的元素是不可以更改的，如果进行类似remove,set,add的更改操作就会报异常UnsupportedOperationException

All elements are permitted, including null.
元素类型可以允许所有类型，包括null

Memory consistency effects: As with other concurrent collections, actions in a thread prior to placing an object into a CopyOnWriteArrayList happen-before actions subsequent to the access or removal of that element from the CopyOnWriteArrayList in another thread.
内存一致性效果:和其他并发集合一样，如果线程在删除或访问元素的时候有线程添加元素，那么添加元素的操作会优先执行