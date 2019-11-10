# Memory Analyzer Tool

官方简述：

The [Eclipse Memory Analyzer](http://eclipse.org/mat) tool (MAT) is a fast and feature-rich heap dump analyzer that helps you find **memory leaks** and analyze high **memory consumption** issues.

With Memory Analyzer one can easily

- find the biggest objects, as MAT provides reasonable accumulated size (retained size)
- explore the object graph, both inbound and outbound references
- compute paths from the garbage collector roots to interesting objects
- find memory waste, like redundant String objects, empty collection objects, etc...

MAT是一个快速且丰富的堆dump分析器，它可以帮助你发现内存泄漏、分析高内存消耗问题

* 发现大对象

* 展示对象图表

* 从gc roots 对象分析路径
* 发现内存泄漏



#### List Objects 

* with outgoing references 当前对象引用对象的集合，当前对象引用的所有对象都称为 Outgoing References
* with incoming references  当前对象被其他对象引用的集合，拥有当前对象的引用的所有对象都称为 Incoming references

#### Paths to GC Roots

 从当前对象到GC roots的路径

#### **Merge Shortest Paths to GC roots**

从GC roots到一个或一组对象的公共路径