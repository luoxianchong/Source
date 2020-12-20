# Select、poll、epoll



### select

```c
 int select(int nfds, fd_set *readfds, fd_set *writefds,fd_set *exceptfds, struct timeval *timeout);

```



 fd_set : bitMap[1024]  最多1024个文件描述符，其中有0、1、2 是一个进程开启默认占用的文件描述符0 表示标准输入，1表示标准输出、2 表示标准错误。



nfds: 文件描述符个数

readfds: 读文件描述符数组指针。

writefds: 写文件描述符数组指针。

exceptfds: 异常文件描述符数组指针。

timeOut:超时结构体，其中包含两个属性：tv_sec（秒）、tv_usec （微秒）

 

struct timeval {
               long    tv_sec;         /* seconds */
               long    tv_usec;        /* microseconds */
  };



example：

```
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>

 int main(void) {
           fd_set rfds;
           struct timeval tv;
           int retval;

           /* Watch stdin (fd 0) to see when it has input. */
           FD_ZERO(&rfds);
           FD_SET(0, &rfds);

           /* Wait up to five seconds. */
           tv.tv_sec = 5;
           tv.tv_usec = 0;

           retval = select(1, &rfds, NULL, NULL, &tv);
           /* Don't rely on the value of tv now! */

           if (retval == -1)
               perror("select()");
           else if (retval)
               printf("Data is available now.\n");
               /* FD_ISSET(0, &rfds) will be true. */
           else
               printf("No data within five seconds.\n");

           exit(EXIT_SUCCESS);
}


```



### poll

```
int poll(struct pollfd *fds, nfds_t nfds, int timeout);
```

fds: 文件描述符结构体指针，并且fds指向的是pollfd类型的数组。

nfds：nfds_t类型的参数，用于标记数组fds中的结构体元素的总数量；

timeOut: 是poll函数调用阻塞的时间，单位：毫秒；

```c
 struct pollfd {
     int   fd;         /* file descriptor */  //标记的是一个开启的文件描述符,如果小于0
     short events;     /* requested events */
     short revents;    /* returned events */
};

events：是一个bit类型的掩码，以下是其此掩码的真实类型：

POLLIN ：There is data to read.

POLLPRI：There is urgent data to read (e.g., out-of-band data on TCP socket;  pseudoterminal  master  in packet mode has seen state change in slave).

POLLOUT:Writing  is  now  possible,  though a write larger that the available space in a socket or pipe will still block (unless O_NONBLOCK is set).

 POLLRDHUP:(since Linux 2.6.17) Stream socket peer closed connection, or shut down writing half of connection.  The _GNU_SOURCE feature  test 			macro must be defined (before including any header files) in order to obtain this definition.

POLLERR:Error condition (only returned in revents; ignored in events).

POLLHUP:Hang up (only returned in revents; ignored in events).  Note that when reading from  a  channel  such  as a pipe or a stream socket, 		this event merely indicates that the peer closed its end of the channel.  Subsequent reads from the channel will return 0 (end of file) 		only after all out‐standing data in the channel has been consumed.

POLLNVAL：Invalid request: fd not open (only returned in revents; ignored in events).
    
    
#define POLLIN 0x0001
#define POLLPRI 0x0002
#define POLLOUT 0x0004
#define POLLERR 0x0008
#define POLLHUP 0x0010
#define POLLNVAL 0x0020
 
#define POLLRDNORM 0x0040
#define POLLRDBAND 0x0080
#define POLLWRNORM 0x0100
#define POLLWRBAND 0x0200
#define POLLMSG 0x0400
#define POLLREMOVE 0x1000
#define POLLRDHUP 0x2000

```



返回值:
>0：数组fds中准备好读、写或出错状态的那些socket描述符的总数量；
>
>==0：数组fds中没有任何socket描述符准备好读、写，或出错；此时poll超时，超时时间是timeout毫秒；换句话说，如果所检测的socket描述符上没有任何事件发生的话，那么poll()函数会阻塞timeout所指定的毫秒时间长度之后返回，如果timeout==0，那么poll() 函数立即返回而不阻塞，如果timeout==INFTIM，那么poll() 函数会一直阻塞下去，直到所检测的socket描述符上的感兴趣的事件发生是才返回，如果感兴趣的事件永远不发生，那么poll()就会永远阻塞下去；
>
>-1： poll函数调用失败，同时会自动设置全局变量errno；





### epoll 

##### api:

int epoll_create(int size)  //建立一个epoll实例，size参数已经废弃，早期是指创建epoll实例中红黑树的大小。返回的是一个epoll的文件描述符句柄。



int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);

     op:
     
     EPOLL_CTL_ADD： Register the target file descriptor fd on the epoll instance referred to by the file descriptor epfd and asso‐ ciate the event event with the internal file linked to fd.注册新的文件描述符fd到epoll实例中。
    
     EPOLL_CTL_MOD:  Change the event event associated with the target file descriptor fd.修改文件描述符fd关联的事件。
    
     EPOLL_CTL_DEL：Remove  (deregister)  the target file descriptor fd from the epoll instance referred to by epfd.  The event is ignored and can be NULL (but see BUGS below [man epoll_ctl]). 从epoll实例中移除文件描述符fd
    
     struct epoll_event *event
     
    struct epoll_event {
        uint32_t     events;      /* Epoll events */
        epoll_data_t data;        /* User data variable */
     };
     
    typedef union epoll_data {
         void        *ptr;
         int          fd;
         uint32_t     u32;
         uint64_t     u64;
    } epoll_data_t;
    
    The events member is a bit mask composed using the following available event types:
    
           EPOLLIN： The associated file is available for read(2) operations.//对应文件描述符可读
    
           EPOLLOUT：The associated file is available for write(2) operations.//对应文件描述符可写
    
           EPOLLRDHUP:(since Linux 2.6.17) Stream socket peer closed connection, or shut down writing half of connection.  (This flag is especially  					useful for writing simple code to detect peer shutdown when using Edge Triggered monitoring.)
    
           EPOLLPRI：There is urgent data available for read(2) operations.
    
           EPOLLERR： Error condition happened on the associated file descriptor.  epoll_wait(2) will always wait for this event; it
                  is not necessary to set it in events.
    
           EPOLLHUP：Hang up happened on the associated file descriptor.  epoll_wait(2) will always wait for this event; it is  not
                  necessary  to set it in events.  Note that when reading from a channel such as a pipe or a stream socket, this
                  event merely indicates that the peer closed its end of the channel.  Subsequent reads from  the  channel  will
                  return 0 (end of file) only after all outstanding data in the channel has been consumed.
    
           EPOLLET: Sets  the Edge Triggered behavior for the associated file descriptor.  The default behavior for epoll is Level
                  Triggered.  See epoll(7) for more detailed information about  Edge  and  Level  Triggered  event  distribution
                  architectures.
    
           EPOLLONESHOT: (since Linux 2.6.2) Sets  the  one-shot behavior for the associated file descriptor.  This means that after an event is 			   pulled out with epoll_wait(2) the associated file descriptor is internally disabled and no other events will be  reported
                  by the epoll interface.  The user must call epoll_ctl() with EPOLL_CTL_MOD to rearm the file descriptor with a
                  new event mask.
    
           EPOLLWAKEUP:(since Linux 3.5) If EPOLLONESHOT and EPOLLET are clear and the process has the CAP_BLOCK_SUSPEND capability,  ensure  that  			  the system  does  not enter "suspend" or "hibernate" while this event is pending or being processed.  The event is
                  considered as being "processed" from the time when it is returned by a call to epoll_wait(2)  until  the  next
                  call  to  epoll_wait(2) on the same epoll(7) file descriptor, the closure of that file descriptor, the removal
                  of the event file descriptor with EPOLL_CTL_DEL, or the clearing of EPOLLWAKEUP for the event file  descriptor
                  with EPOLL_CTL_MOD.  See also BUGS.
    


int epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout);

epfd:

maxevents:

timeout:  是epoll函数调用阻塞的时间，单位：毫秒；



     The struct epoll_event is defined as:
    	 typedef union epoll_data {
               void    *ptr;
               int      fd;
               uint32_t u32;
               uint64_t u64;
           } epoll_data_t;
    
           struct epoll_event {
               uint32_t     events;    /* Epoll events */
               epoll_data_t data;      /* User data variable */
           };


第一步：epoll_create()系统调用。此调用返回一个句柄，之后所有的使用都依靠这个句柄来标识。

第二步：epoll_ctl()系统调用。通过此调用向epoll对象中添加、删除、修改感兴趣的事件，返回0标识成功，返回-1表示失败。

第三部：epoll_wait()系统调用。通过此调用收集收集在epoll监控中已经发生的事件。



![](.\image\epoll.jpg)





**水平触发(level-triggered，也被称为条件触发)LT**: 只要满足条件，就触发一个事件(只要有数据没有被获取，内核就不断通知你)

**边缘触发(edge-triggered)ET**: 每当状态变化时，触发一个事件
     “举个读socket的例子，假定经过长时间的沉默后，现在来了100个字节，这时无论边缘触发和条件触发都会产生一个read ready notification通知应用程序可读。应用程序读了50个字节，然后重新调用api等待io事件。这时水平触发的api会因为还有50个字节可读从 而立即返回用户一个read ready notification。而边缘触发的api会因为可读这个状态没有发生变化而陷入长期等待。 因此在使用边缘触发的api时，要注意每次都要读到socket返回EWOULDBLOCK为止，否则这个socket就算废了。而使用条件触发的api 时，如果应用程序不需要写就不要关注socket可写的事件，否则就会无限次的立即返回一个write ready notification。大家常用的select就是属于水平触发这一类，长期关注socket写事件会出现CPU 100%的毛病。

