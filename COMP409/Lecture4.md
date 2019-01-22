# Lecture 4. 2018/01/22
## How does a multithread program start?
* Runnable
    * implement Runnable
    ```java
    Thread(Runnable r) {
        run = r
    }
    
    Thread.start();
    
    pubic void run() {
        if (r != null) {
            r.run();
        }
    }
    ```
* Thread
    ```java
    @Override
    public void run() {
        //TODO 
    }
    ```
* Which way should we choose?
    * extend Thread &rarr; change thread behaviour
    * implement Runnable &rarr; for code to execute in a Thread

## How does a multithread program end?
* Sequence program 
    * start --> fall off main --> done
* Multithread program, multiple threads of control
    * **All** threads must finish
        * Initial thread -> falls off of main
        * New threads -> fall off run() method
* Actually, all non-daemon thread must finish
    * Daemon(后台程序) & non-daemon thread(default)
        * Daemon threads act like services, which do not keep the program alive. 
## 线程的各个state
* 见iPad
* More threads will be ready to execute &rarr; then we have CPU

## Java's Thread Model
* Priority based
* Nominatively priority-preemptive
* Threads at highest level is executed in preference
* For threads of same priority, should run with round robin time slices (but not guaranteed)
* If higher priority thread disappear or sleep, then lower priority thread start.
* eg. high priority: A B C
    median priority: D E
    low priority: F G
    * 3 cores
        * A B C run as long as they want to, D E F G `never` execute
    * 2 cores
        * A B C are time slice
        * D E F G `never` execute
    * 4 cores
        * A B C run
        * D E time slice on the remaining core.
    * Nominal(No guaranteed) 
        * We assume time-slicing. (No guaranteed.)
        * We assume priority are respected. (OS may not respect that.)  

## Thread API
| Thread API | |
---|---
`sleep(millis)` | **Lower bound** idle time; 不释放锁
`yield()` | Give up time slice; OS can ignore it, no guarantee other thread will execute
`Thread.currentThread()` | Get reference to current thread
`isAlive()` | returns `true` if thread could be scheduled. Always true if called on self (as it wouldn't be callable otherwise). If called on another thread, returns stale information on live state
`join()` | Wait for another thread to finish before continuing

## How does a multithread program stop?
Asynchronous termination is bad. `stop()` and `destroy()` are such methods and are deprecated.

## Basic synchronization
* Critical section &rarr; enforce mutual exclusion.
* Every object has a lock, for which only 1 thread can acquire at a time

```java
synchronized(lock) {
    // acquire lock
    ...
    // release lock
}
```

* Threads that attempt to access an already locked object will wait until it unlocks.
* Synchronized method
    ```java
    public void m1 () {
        synchronized(this) {
            // ...
        }
    }
    
    public void synchronized m1() { 
        // ... 
    }
    ```
* Recursive lock -- In Java, you can relock locks you own, on the condition that you unlock for the same number of times.
    * eg.
    ```java
    int synchronized fib(n) {
        // ...
        return fib(n - 1)
    }
    
    void synchronized m1 { }
    void synchronized m2 {
        m1();
    }
    ```
## Volatile Keyword
* Used for variables to indicate that it may change arbitrarily/asynchronously
* Helps avoid accidental optimization (eg when a thread checks for a flag and loops, and the thread itself never changes the flag)

## Race conditions
* Atomicity problem 
    * 由于两个线程里的操作不是atomic，会发生interleave.
* Non-determine problem
    * 已经保证了两个线程里的是atomic，但结果任然无法确定。(Depends on thread speed)
* Why do we do care about data-races. (Race condition)
    * Data race — **BAD!** We want to avoid.
    * A data-race in a Multithread program occurs when 2 threads access the same memory location with `no ordering constrains` such that at least one of them is a write.
* How to solve it? 
    1. Use synchronized() to lock both thread.
    2. Declare x to be volatile.
        All data accessed by 2 or more threads must be only accessed under synchronization or declare volatile.
        
        
