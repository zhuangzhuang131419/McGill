# Lecture 9. 02.12 Termination and Priority
## Resource Deadlock
* condition required
* Coffman's consition 1971
* 获得死锁的条件
    1) Serially reusable resources.
        * eg. chopsticks
    2) Incremented Acquirment
        * Processes acquire multiple resources on at a time
    3) No pre-emption
        * A process holding a resource cannot have it taken away in-volunteerly
    4) Dependency cycle exists
        * A critical chain of waiting process
        * We can eliminate by ordering

## Volunteer on deadlock
* 2 trains are waiting each other
* communicating, they are doing something, but not useful to break the condition
* « Live lock »
        
## Termination
* `Asynchronous` termination may easily corrupt the state
    * Eg terminating thread when it's in the middle of a syscall
    * Stop threads instead by using a polling mechanism
* Java does not have cancellation, but instead has interrupts
    * Many operations will capture it by default and throw an `InterruptedException` to be handled. Note that some exceptions may be thrown by spurious wakeups. Threads can also use `.interrupted()` to check its interruption status.
* What is the thread doing when stopped?
    * You own data &rarr; corrupted
    * in a system call?
    * in the middle of locking?

### Vosix
* Cooperative mechanism (ask to stop rather than force to stop)
* Set a flag: thread check it and stop itself
    * T0: ----> pThreadCancelation(T1)--------
    * T1: ----> sets the cancelation flag ---------> check the cancel flag
* Don't want to check too often &rarr; slow
    * Need to set specific cancellation points
        * System calls that wait tend to be cancellation point.(sleep() or similar)
        * Mutex-lock is `NOT` cancellation-able.
        * Pthread_test_cancel(); (atomic)
* Java has thread interrupts
    * Thread.interrupt(): sets a flag
    * Checked in place that can throw a InterruptException
    * Instead of exiting, we can wait, sleep or join.
    * But some execution is un-cancellable.
    
## Priorities
* Nominatively priority pre-emptive
* Java - 10 priorities
* Pthreads
    * SCHED-RR, SCHED-FIFO
    * 32 levels
* WindowsNT
    * 7 levels

## Priority Problem
1.Priority Inversion

* Low level thread locks and executes &rarr; high priority thread enters and attempts to acquire lock &rarr; medium priority thread comes in as well and acquires lock from low priority &rarr; high priority thread must wait for medium priority thread to finish before the lock can be acquired 
* 见iPad图

2.Mars Pathfinder
* High priority thread - bus manager
* Medium priority thread - communication
* Low priority thread - meteor logical
* Once priority inversion occurs, another watchdog thread will reset everything

---

Solutions

Priority Inheritance
* 优先级继承是当任务A 申请共享资源S 时， 如果S正在被任务C 使用，通过比较任务C 与自身的优先级，如发现任务C的优先级小于自身的优先级，则将任务C的优先级提升到自身的优先级，任务C 释放资源S 后，再恢复任务C 的原优先级。
* Thread holding a lock will temporarily acquire the priority of the highest priority thread waiting for the lock

Priority Ceilings
* 优先级天花板是当任务申请某资源时，把该任务的优先级提升到可访问这个资源的所有任务中的最高优先级， 这个优先级称为该资源的优先级天花板。
* Locks are associated with priority
    * High priority lock &rarr; high priority thread
---

## Thread specific Data
* Thread has local data
    * register
    * stack
* Shared data
---
## ERRNO
* error code from system calls
    * global variable
    * x = read(); ---> System sets errno ----> if (errno == ???) {}
* In a multithread context:
    * T0: x = read() ----> errno = EBADF --> println(errno);
    * T1: -------> y= read() -------------> errno = OK 
    * Every thread needs its own version of errno.

---
* Thread load storage (Java) 
* Thread Specific Data (pthread) 
* Save variables holds different values for different threads.
```java
Class foo { 
    Static Threadlocal X = new Threadlocal(); 
    T0: 
    x.set(0); 
    x.get() -> 0 
    T1: 
    x.set(1); 
    x.get() -> 1;
} 
```
* Imagine an implementation: Thread is the key Store a value (Something like a hashtable)

