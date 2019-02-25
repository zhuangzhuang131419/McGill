# Lecture 1. 2018/01/08

## Parallelism vs. Concurrency
* Parallelism（并行）
    * Multiple processors &rarr; 单线程永远无法达到并行状态。
    * User initialize process
    * Referee
        * set the rule &mdash; process act otherwise independently.
    * Control the ways things happen at the same time.
* Concurrency（并发）
    * Multiple processors &rarr; 单核单线程能支持并发。
    * You give direction (人为设计的结构)
    * Two tasks can start, run, and complete in overlapping time periods
    * Coach
    * Define constraints on execution, which is influenced by other aspects such as the OS.
    * Performance
        * schecduling
        * load-belonging
* Parallelism vs. Concurrency
    * Different concurrent designs enable different ways to  parallelize.
    * 并发设计让并发执行成为可能，而并行是并发执行的一种模式
    * Parallelism指物理上同时执行，Concurrency指能够让多个任务在逻辑上交织执行的程序设计

## Process vs. Thread
* Multiprocessing &mdash; large heavyweight with its own address space and handles
* Multithreading &mdash; lightweight with shared address space
    * Efficiently switched &mdash; changing threads may just involve changing some pointers, as the rest of the space may be the same

* Asynchronous Execution &mdash; threads execute at their own rate (dictated by OS)
    * Synchronisation
    * Resource Consistency
    * Visibility
    * Fairness

## Threads
* Light weight &rarr; switch efficiently
* Executes on a CPU
* An independent flow of control within a process, composed of a context (register and process context) && a sequence of instructions
* OS Part
    * Thread ID
    * Schedule policy
    * Priority
    * Signed mask
    * For this course, wew will assume little or no control (black box)
* User Part
    * Register set
    * Stack pointer (separate stacks)
    * PC
    * Shared memory
    * 
* Amdahl's Law
    * Total time is based on two pieces: sequential part(s) + parallel part(p)
    * Parallel part can be distributed among the $$n$$ threads: $$t = s + p / n$$
    * Speedup: $$\frac{1}{1-p+\frac{p}{n}}$$
        * Linear speed up &rarr; ideal but not usually seen
        * 一般的speed-up最后可能会下降

* Threads are good for 
    * Hiding latency (cache miss, pipeline stalls)
    * Switching context efficiently
    * Keeping CPU busy
    * Increasing responsiveness
        * Eg. long running execution + GUI thread for listening for inputs & interrupts

* Appropriate Parallelism
    * Web server &mdash; requests are naturally parallel

* Threads are not good for 
    * Overhead
    * May be difficult to debug

* Is concurrency fundamentally different?
    * We may reason that with Turing Machines, n TMs does not give any more power than 1 TM. However, with parallel execution, there are some subtle differences.
    * Example 1 &mdash; consider the following conversions & constraints:
        **a &rarr; ab** 
        **b &rarr; ab**
        There cannot be any sequence of `aa` or `bb`.

        If we started with `ab`, we would not be able to make any conversions.
        However, if we did both conversions in parallel and ignore the middle transition, we will be able to transition to `abab`
    * Example 2 &mdash; consider 5 items spaced evenly in a circle, with equal distance from the center point. Our goal is to rotate them all at the same time. If we were to do it sequentially, there will be a speed for which an item will cross over another. However, if we were to do it all in parallel, which would not be an issue.

# Lecture 2. 2018/01/15

## Hardware

* UP &mdash; Basic uniprocessor
    * CPU &mdash; cache &mdash; memory (示意图见iPad)
    * No course parallelism, still low-level concurrency
        * pipelining (more than one instruction)
        * multifunction unit: multi-issue
        * modern
        * super-scalar processor
    * Mimic 
        * switch between threads frequently （只是看上去像parallelism）
    * Cache helps with performance
        * threads can complete resource of memory 
* MP &mdash; Multiprocessor
    * Many [CPU &mdash; cache] to shared memory（示意图见iPad）
    * Keeping the cache consistent is important
* UMA &mdash; Uniform memory access
    * All memory accesses cost the same (modulo cache)
    * Note some caches need to reflect the same view on memory
    * Caches need to be consistent
* NUMA &mdash; Non-UMA
    * Many [CPU &mdash; cache] to shared memory (slow) & many local memories (fast)
* SMP &mdash; Symmetric MP
    * Multiprocessor with same CPUs （与Multiprocessors结构类似）
    * Advantage &mdash; "true" parallelism 
    * Disadvantage &mdash; 
        * given that lots of programs are single threaded and the CPUs are independent
        * some of them may be doing wasted or duplicate work
* CMP &mdash; on-Chip MP
    * CPUs & caches are on a chip
    * Faster cache communication
    * Same disadvantage as SMP
* CMT/FMT &mdash; Coarse/fine grained multi-threaded
    * CMT &mdash; context switch every so many cycles
        * 由很多register set组成，每个set里都是multiple hardware context
            * hardware contect switch （见iPad示意图）
        * One big uniprocessor support for hardware thread 
        * Advantage &mdash; single threaded is effective
        * Disadvantage &mdash; no real parallelism
    * FMT &mdash; context switch every cycle
        * May be referred to as barrel-processing
        * Cray's TERA was FMT based
            * Had 70 threads
            * Had no data-cache, meaning data access involves 70 cycles. However, due to the threads, there is no idle
            * Not necessarily responsive, but has high throughput
* SMT &mdash; Simultaneous MT
    * Support "true" parallelism but not suffer in single-thread &mdash; act like a CMP （示意图见iPad，线程互相之间的界限没有特别明显）
    * Multiple functional units
    * When single-thread execution, act like one big chip.
    * In Intell, this is called hyper-threading, early versions were NOT the same as having 2 real cores.

## Atomicity
* In concurrent system, we are interested in thread interactions
    * threads are not entirely independent 
* Thread attribute definitions
    * Read set &mdash; set of variables read from but not written to
    * Write set &mdash; set of variables written to (and perhaps read from)
    * Independence &mdash; when each write set is disjoint from all other read & write sets

---

Example

Consider the case where x = y = z = 0, and where
* Thread 1 executes x = y + z
* Thread 2 executes y = 1, z = 2

What is x in the end? <br>
1. <br>
    * T0: y + z
    * T1: y = 1
    * T1: z = 2
    &rarr; x = 0
2. <br>
    * T1: y = 1
    * T1: z = 2
    * T0: x = y + z
    &rarr; x = 3
3. <br>
    * T1: y = 1
    * T0: x = y + z
    * T1: z = 2
    &rarr; x = 1
Result depends on how the statement interleave.
    
What is being interleave?
* We assume the statement were atomic
  * Indivisible execution
  * no intermediate is visiable
    
Thread1 execution is not atomic. Instead, it is:
* load r1, [y]
* load r2, [z]
* add r3, r1 + r2
* store r3, [x]

To find out the possible result, we have to interleave atomic option.

Given that no assumptions can be made about the speeds of either CPU, thread 2 may execute its two instructions in between any of the executions of thread 1. So instead of having the desired response of x = 0, thread 1 may product three different outputs.

---
Need to know what is atomic
* y = 1 usually atomic
* for a **word-sized** variable (eg. 32-bits write on 32-bits machine)
* Longer data size &rarr; maybe not 
    * long long x = 0;
    * 64-bits data on 32-bitsmachine
        * T1: x = 0 (x.upper = 0 &rarr; x.lower = 0)
        * T2: x = -1 (x.upper = -1 &rarr; x.lower = -1)
        &rarr; 00..0011...11

---

Usually

* Assignment of constant to machine word sized value is atomic
    * Assignments to bigger sizes may be separated

## In Java

* Assignment to 32-bit or smaller type is guaranteed atomic
    * Includes aspects like x = y + z
* 64-types (long, double, etc) are not necessarily atomic; can be declared as **volatile** then 64-bits R/W is atomic

---
x = y+z+w/q
* If all these variables are local/unshared, no one care about atomic.
* No one can see  an alter on x, y, w, q
* Effective atomic

---

We can generalize:
* Let '$$x$$ = *expr*’ be a statement, where x is a word-sized variable, and expr is an expression.
* Define: *expr* has a **critical reference** if it uses variables that change in another thread.
* Define: ‘x-*expr*’ has an At-Most-Once(AMO) property if either: <br>
        1. *expr* has exactly 1 C.R. and x is not read by another thread. <br>
        2. *expr* has no C.R. (in which case x can be read by another thread)
*  A statement that has AMO appears atomic.
---
### Example
1. x = y = 0 

* Thread 1 | Thread 2
  --- | ---
  x = y + 1 | y = y + 1
  1 CR, x not read | 0 CR, y is read

* As AMO is satisfied in both cases, there are no unexpected values to be considered. The process will happen with one expression before the other without interleaving

2. x = y = 0
* Thread 1 | Thread 2
  --- | ---
  x = y + 1 | y = x + 1
  1 CR, x is read | 1 CR, y is read
  load r1, [y]<br>inc r1<br>str r1, [x] | load r1, [x]<br>inc r1<br>str r1, [y]
* Neither satisfy AMO, so there may be interleaving (eg resulting in x = 1, y = 1)

# Lecture 3. 2018/01/17

### interleavings 
* n threads, each doing m atomic instructions 
* $$\frac{(nm)!}{(m!)^n}$$ &rarr; For n = 2, m = 3 &rarr; 20 possibilities
To resolve this, we should only allow 1 thread to execute such changes at one time &rarr; critical section

## Mutual Exclusion 
* Ensure, for some piece of code, two threads are not execute at same time
* **Critical Section** &mdash; protected to ensure mutual exclusive
    * various solution &mdash; we will use busy-waiting (spining)  

#### Solution
Assume 2 threads with unique ids 0 and 1.
We will go through the stages of `init`, `enter protocol`, `exit protocol`
1. (works)
```java
init()
    turn = 0

enter(int id)
    while (turn != id)  // spin 当turn与id相等的时候，才准许进入

exit(int id)
    turn = 1 - id

```

* Drawbacks: 
    * In this case, given that the turn is initially for thread 0, if thread 1 shows up first and thread 0 has no intention of entering the CS, thread 1 will need to wait unnecessarily.
    * Thread 0 can't go in the critical section twice in a row.
---
2. (doesn't work)
```java
init()
    flag[0] = flag[1] = false // indicates interest for thread[id]

enter(int id)
    while (flag[1 - id])  // spin
    flag[id] = true       // indicate self interest

exit(int id) 
    flag[id] = false
```

`enter` is not actually atomic. If both show up at the same time with both flags set to false, both will pass the spin and set their own flags to true

---
3. (doesn't work)
```java
init()
    flag[0] = flag[1] = false // indicates interest for thread[id]

enter(int id)
    // same as previous but with sequence switched
    flag[id] = true       // indicate self interest
    while (flag[1 - id])  // spin

exit(int id) 
    flag[id] = false
```

This case will now enforce ME, but there may be an issue when both threads show up, both threads set their flag to true, and both threads spin forever (deadlock).

---
4.
```java
init()
    flag[0] = flag[1] = false // indicates interest for thread[id]

enter(int id)
    flag[id] = true       // indicate self interest
    while (flag[1 - id]) 
        flag[id] = false  // give up self interest
        randSleep()       // sleep for some random time
        flag[id] = true   // show self interest again

exit(int id) 
    flag[id] = false
```

For this to work, our delays cannot sync together. Though our sleep uses random durations, it is possible for both threads to wait the same time, set both their flags, then repeat (livelock)

How long should we wait?
* small delay &rarr; increased chance of lock-step behaviour
* big delay &rarr; more likely for long unnecessary wait

---

## Peterson's Algorithm
Add another state variable to break the symmetric
```java

init()
    turn = 0
    flag[0] = flag[1] = false

enter(int id)
    flag[id] = true  // show self interest
    turn = id        
    while (turn == id && flag[1 - id]) // spin

exit(int id)
    flag[id] = false
```

Thread 0 | Thread 1
--- | ---
flag[0] = true | flag[1] = true
turn = 0 | turn = 1
while (turn == 0 && flag[1]) | while (turn == 1 && flag[0])

Turn will be set to either `0` or `1` in all cases. Without loss of generality, we will assume `turn` ends as `1`. In this case, thread 0 has set `turn` first, and gets to execute first.

---

**`Warning`**
flag 在 trun 之前

What we are looking for
1. ME over the CS - one thread at a time
2. Absence of deadlock &mdash; if multiple threads try to get into critical section, one succeeds
3. No unnecessary delay &mdash; if no one is in the critical section, we should get in promptly
4. Eventual entry &mdash; threads should not have to wait to enter the critical section if no other thread is in it. (starvation free)

## Java & PThreads

* POSIX - standard and standalone library - links to apps

* Better if integrated into language

### Java

* Multithreading built in
* Core language/API
* Higher level API - `java.util.concurrent`
* | Thread | |
  --- | ---
  `runnable` | interface determining code to execute
  `start()` | native code - gets the thread running
  `run()` | runs `runnnable` if not null
* Two ways to create new thread in practice
    * Threads may be created by subclassing thread to run what we want
    * By creating a runnable object and passing it to the thread constructor.
* Typically the latter is used; the former is if you wish to change the behaviour of threads, not just the code it runs.


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
* A thread that has been started may not necessarily be running - OS may choose to switch it to a scheduled or de-scheduled state

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
* Threads may also sleep, which goes to a waiting mode, or be woken up
* Threads may be terminated, leading to a stopped mode
## 线程的各个state
* 见iPad
* More threads will be ready to execute &rarr; then we have CPU

## Java's Thread Model
* Priority based
* Nominatively priority-preemptive
    * nominatively: not much is formally guaranteed
    * preemptive: threads at highest level is executed in preference
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
`yield()` | Give up time slice; OS can ignore it, no guarantee other thread will execute; 当一个线程使用过这个方法后，它就会把CPU执行时间让掉，让**自己**或者其他线程运行
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
    * eg. x++;
* Non-determine problem
    * 已经保证了两个线程里的是atomic，但结果任然无法确定。(Depends on thread speed)
    * It's not a mistake. It's more like an error of concurrent programming.
* Why do we do care about data-races. (Race condition)
    * Data race — **BAD!** We want to avoid.
    * A data-race in a Multithread program occurs when 2 threads access the same memory location with `no ordering constrains` such that at least one of them is a write.
* How to solve it? 
    1. Use synchronized() to lock both thread.
    2. Declare x to be volatile.
        All data accessed by 2 or more threads must be only accessed under synchronization or declare volatile.
        
        
# Lecture 5. 2018/01/24
## PThreads

* POSIX library - link to various applications
* `pthread.create(&threadHandle, attributes, startRoutine, args)`
    * startRoutine(args);
    * threadHandle &rarr; reference parameter
    * attributes &rarr; specify properties
* Detached (daemon)
    * May not be joined
    * Act as services
* Joinable (non-daemon)
    * Default
    * Must join with them, otherwise resource leak

| Scheduling Model| |
---|---
SCHED_RR | round-robin, time sliced, priority preemptive
SCHED_FIFO | run to completion, no time slice, 自愿结束
SCHED_OTHER | offered by OS

## Basic synchronization
* Mutual exclusion
* PThread.create_mutux
    * one bit lock      
        * PThread_mutux.lock(m)
        * PThread_mutux.unlock(m)
    &hArr;
    * Synchronized {}
        * Java is safer. (避免出现了T0 lock(), T1 unlock()的情况)
    * Notice the thread unlocking is not synchronically guarantee to be the same threat that looked it.
        * Implement Define
            * It does not be default support `recursive looking`
            * Render extension

## Locks
What about more than 2 threads?
* Filter Lock
    * Generalization of the Peterson's 2-process tie breaker
    * n - stages, n threads, 1 thread "wins"
    * Two properties
        * At each stage, ensure that at least one thread trying to get in is successful (ensure progress); 
        * If more than 1 thread is trying to enter, at least one is blocked

```java
init() {
    // Flag
    // The value of stage[n] indicates the highest level that 
    // thread n is trying to enter.
    int stage[n] // stage: id -> stage 
    
    // Turn
    // Each stage has a distinct 'waiting' field used to "filter out"
    // one thread, excluding it from the next stage.
    
    // The value of waiting[n] indicates that the last thread is trying 
    // to get into a given stage.
    int waiting[n] // waiting stage -> id 
}


enter(int id) {
    for (int i = 1; i < n; i++) { // through all stages
        // finite
        stage[id] = i; // thread(id) is trying to enter stage[i]
        waiting[i] = id; 
        // arbitrary
        do {
            spin = false; 
            for (int j = 0; j < n; j++) { // through other threads
                if (j == id) { continue; } // if it is myself, continue
                if (stage[j] >= i && waiting[i] == id) {
                    spin = true; // 被过滤掉了，无法离开这个do-while循环
                    break; // from for loop
                }
            }
        } while (spin)
    }
}
    
exit(int id) {
    stage[id] = 0;
}
    
```
---
* Proof it works
    * Lemma: There are at most n - i threads at stage i in $$0 \leq c \leq n$$ . So, at stage n - 1, we have n - (n - 1) = 1 thread.
    * Induction on i:
        *  Base case: i = 0 &rArr; (n - 0) threads
        *  Induction hypothesis: 
            * Assume we have <= n - i + 1 threads at level i - 1 
            * Assume the contrary, i.e. there are n - i + 1 thread at level I 
                * Let Ta be the last thread to write (waiting[1] = Ta)
                * &rArr;: Tx: write(waiting[i] = Tx) &rarr; Ta: write(waiting[i] = Ta)
            * Now T reads stage[Tx], stage is written before waiting 
                * &rArr; Tx: write(stage[Tx] = i) &rarr; Tx: write(waiting[i] = Tx) &rarr; Ta: write(waiting[i] = Ta) &rarr; Ta: read(stage[Tx])
            * Now Ta reads stage[Tx] only after writes into waiting, Ta must see stage[Tx] >= i, 
            * but Ta was the last to write to wait 
                * &rArr; waiting[i] = Ta, otherwise Ta must have spin && not enter stage i 
---
## Some Lock Properties
*  2-process algorithm
    * We have "First come First serve"(FCFS) properties &rarr; "fair lock"
    * "Starvation free" -- a thread trying to enter critical section eventually succeed.
*  Filter Lock
    *  "Starvation free"
    *  But it is not "First come First serve"
* How do we define FCFS ?
    * We devide locking protocol into 2 stages.
        * Doorway(finite) -> spin(arbitrary) -> enter  (对应前面的sample code)
    * If Ta finished doorway before thread Tb, then Ta gets in critical section before Tb

    
### Ticket Algorithm

* classic locks
    * take next #
    * wait for # to be called

```java
init() {
    next = 0;
    int[] turn = 0;
    number = 0;
}

enter(int id) {
    turn[id] = number++;      // needs to be atomic
    while (turn[id] != next); // spin (有点像叫号)
}

exit() {
    number++;
}
```

# Lecture 6. 2018/01/29
### Bakery Algorithm
* Broken ticket dispenser

```Java
init() {
    turn[id] = 0;
}
   

enter(int id) {
    turn[id] = max(turn[j], [0, n]) + 1 // need to be atomic
    // look through all the other threads
    for (int i = 0; i < n; i++) { 
        if (i == id) { return; }
        // spin 
        while (turn[i] != 0 && turn[id] >= turn[i]); // 自己的排号更靠后
    }
}

exit(int id) {
    turn[id] = 0;
}
```

* be careful about wrap-arounds, as there is eventual overflow
* hardware primitive - special instructions

---
## Locking via Hardware
* Test and set (TS)
    
```java
// all atomic
TS(x y) {
    temp = x;
    x = y;
    return temp;
}

init() {
    lock = 0;
}

enter(int id) {
    while (TS(lock, 1) == 1); // spin
}
    
exit(int id) {
    lock = 0;
}
```
    Drawback: spin doing TS over and over which is very slow
---
    Solution: 
* Test and Test and Set

```java
enter(int id) {
    while (lock == 1);
    // if modified by other threads during this period
    while (TS(lock, 1) == 1) {
         while (lock == 1);
    }
}
```
    Not FCFS
    Not necessarily starvation free
---
* Another Variation
    * We could add exponential backoff(指数后退)
```Java
enter(int id) {
    int delay = 1;
    while(true) {
        while(lock); // spin
        if (TS(lock, 1) == 0 ) {
            break;
        }
        Thread.sleep(random(delay));
        delay *= 2; // 可以设置一个maximize
    }
}
```
---
* Fetch and Add
```java
// all atomic
FA(v, c) {
    temp = v;
    v += c;
    return temp;
}
```
---
* Compare and Swap 
    * set a variable to a value if it is alread is a certain value
```java
// all atomic, return type indicates success/fail
CAS(x, a, b) {
    if (x != a) {
        return false;
    }
    else {
        x = b;
        return true;
    }
}
```
---
## Queue Locks
* 每个线程检测其前驱线程是否已完成来判断是否轮到自己
* 2 different queue locks
    * MCS
    * CLH
    * MCS与CLH不同的是，锁链表是显示的而不是虚拟的，整个链表通过QNode对象里的next所体现
* Works for arbitrary # of threads
* Maintains explicit queue
    ```java
    class Node {
        Node next
        boolean locked
    }
    ```
    Each thread has node and a static tail(global pointer)

```java
enter(int id) {
    Node me = new Node();
    me.next = null;
    
    // 使自己的结点成为队列的尾部，同时返回一个指向pred的引用
    Node pred = TS(tail, me);
    if (pred != null) { // 前一个在critical section
        me.locked = true;
        pred.next = me;
        while (me.locked);         // spin 等待pred exit
    }
}

exit(int id) {
    if (me.next == null) {
        if (CAS(tail, me, null)) { // 如果自己现在就是最后一个
            return;
        }
    }
    while (me.next == null);   // spin 等待后一个进入
    me.next.lock = false
    me.next = null
}
```
* Advantages
    * First come, first serve
    * Cache friendly
    * Space efficient
* Every thread needs one node
* Disadvantages
    * Needs both TS & CAS
    * Can spin on exit
---

CLH Lock

```java
class Node {
    boolean locked;
}

static Node tail = new Node();

class Thread {
    Node me = new Node();
    Node myPred = null;
}

enter(int id) {
    me.locked = true;        // signifies to others that they should be locked
    myPred = TS(tail, me);
    while (myPred.locked);   // 注意：这里是让myPredspin
}

exit(int id) {
    me.locked = false;
    me = myPred;
}

```

* Notice that nodes get moved around, and there's no spin on exit
* Still FCFS
* Doesn't require (CAS), just TS
---

## Lock Design
* We want synchronized to be fast -> Locking to be frequent
* Optimize the common case at the expense of the uncommon
* Hierarchy(越往下越不可能发生)
    1. Locking an unlocked object.
    2. Shallow recursive locking
    3. Deep recursive locking
    4. Shollow contention(looked but no other thread waiting)
    5. Deep contention



# Lecture 7. 2018/01/31
## Java's Locks (synchronous)
* 如果我们把monitor_enter/monitor_exit看成是Fat Lock方式，则可以把Thin Lock看成是一种基于CAS（Compare and Swap）的简易实现。
* 而基于CAS方式的实现，线程进入竞争状态的，获得锁的线程，会让其他线程处于自旋状态（也称之为Spin Mode，即自旋），这是一种while(Lock_release) doStuff()的Busy-Wait方式，是一种耗CPU的方式；而Fat Lock方式下，一个线程获得锁的时候，其他线程可以先sleep，等锁释放后，再唤醒（Notify）。
* CAS的优点是快，如果没有线程竞争的情况下，因为CAS只需要一个指令便获得锁，所以称之为Thin Lock，缺点也是很明显的，即如果频繁发生线程竞争，CAS是低效，主要表现为，排斥在锁之外的线程是Busy Wait状态；而monitor_enter/monitor_exit/monitor_notify方式，则是重量级的，在线程产生竞争的时候，Fat Lock在OS mutex方式下，可以实现no busy-wait。
* "Thin" Lock (Bacon lock)
    * Optimized lock, contention is rare
    * Each object has lock word
        * Divided into 8, 8, 15, 1 bits
        * First 8 bits is reserved （没有用到）
        * Second 8 bits - recursive lock count -1 （第一次不需要显示1）
        * 15 bits represent thread id of owner
            * 0 represents no thread, and consequently no lock
        * Last 1 bit = lock shape - 0 = cheap (thin); 1 = expensive (fat) lock
    * Lock done using `CAS(lockword, 0, id << IDOFFSET)`
        * true &rarr; we own it &rarr; locked
        * false &rarr; check the shape bit, verify it is a 0(i.e. thin lock)
            * false &rarr; check ownership
                * ownership: true &rarr; locking again, add 1 to lock count. Make sure there is no overflow for the count
        * If fail occurs anywhere, transition into "fat lock"（变成flat school）
            * Transition to fat lock is done by user.
            * If we don't own it, we spin until we do, or until it becomes a fat lock (known from shape bit)

* "Fat" Lock
    * Need mutex
    * Lock divided into 8, 24, 1
    * 24 bit - points to the pointer to mutex 
    * 1 bit - shape bit (1)
* 如果T1, T2，T3，T4...产生线程竞争，则T1通过CAS获得锁(此时是Thin Lock方式)，如果T1在CAS期间获得锁，则T2，T3进入SPIN状态直到T1释放锁；而第二个获得锁的线程，比如T2，会将锁升级（Inflation）为Fat Lock，于是，以后尝试获得锁的线程都使用Mutex方式获得锁。
* Tasuki锁为这种方式做了2个优化：
    * avoid spinning as much as possible
    * allow fat lock "deflation" (退化) back to thin lock
    * uses a little more space

---
## Blocking 
* So far &rarr; mostly spin-locks
* Drawbacks
    * spinning uses CPU
    * for longer spinning situation, it better if we don't need schedule at all.
    * Solution:
        * Instead of spinning, we go to sleep();
        * Intention &rarr; whoever leaves the lock wakes us up
            * T0: 
            if (locked) {
                // GAP!!!
                sleep();
            }
            * T1:
            unlock();
            wakeup();
            * Drawback: easy to lose a wake up ("lost wakeup" problem)
---            
## Semaphore & Mutexer

Semaphore
* Blocking synchronize &rarr; sleep & wake
* Abstraction
* Semaphone value: Integer (>=0)
* P(S) "down" (all atomic)
    ```java
    // atomic
    while (s == 0) {
        sleep();
        s--;
    }
    ```
* V(s) "up" (all atomic)
    ```java
    // atomic
    s++;
    wakeup();    // call some wakeup
    ```
Q: Who gets woken up?
A: Arbitrary

Binary Semaphore
* Always 0 or 1
* Starts at 1
    * lock = P()
    * unlock = V()
Q:What if our binary semaphore start at 0 ?
A:一个线程运行先被阻断，另一个线程唤醒它。这就是signal

Counting Semaphore (General semaphore)
* Not just 0 & 1
* Useful for representing resource

Producer/Consumer (bounded buffer)
* Data buffer[n]
* Semaphore spaces = n
* Semaphore filled = 0
* int prodId = 0;
* int conId = 0;

```java
produce:
    while (true) {
        Data d = produce(); // Get the data
        P(spaces); // P()
        buffer[prodId] = d;
        prodId = (prodId + 1) % n;
        V(filled);
    }

consumer:
    while (true) {
        p(filled);
        Data d = buffer[conId];
        conId = (conId + 1) % n;
        V(spaces);
        consume(d);
    }
```

Other Binary Semaphore
* Starts at 0 &rarr; signalling
    * Makes threads wait for each other

Drawbacks
* 2 ideas together - mutual exclusion & signalling(communication)
* P(), V() &rarr; separate, fragile design 
    * Java是用大括号括起来锁住的，就不会忘记

## Monitors
* ADT
    * shared data as private variable synchronized method, R/W can only happen through monitor 

* Dijkstra, Per Brinch Hansen
    * Package data and Operations are mutually-exclusive

* 兼具mutual exclusive && signalling 的性质 (可以上锁，也可以提供信号)
mutual exclusive part
```java
class Monitor {
    private int d1, d2, ...

    synchronized void foo() { ... }

    synchronized int bar() { ... }
}
```


signalling part
* Condition variable for thread communication, always associated with monitor (mutex)
* 2 ops
        | Pthread | Java |
        --- | ---
        `sleep()` | `wait()`
        `signal()` | `notify()` (can only invoke inside monitor)
* Behaviour
    * enter the monitor
        * check some state
        * Calling `wait()` inside a monitor will give it up & sleep (atomic)
        * When another thread calls `notify()`, sleeping thread may be woken up. Note that a thread that is woken cannot continue on until it has reacquired the lock

Q: How does it work?
A: We can think of that `2 queues` association with monitor
* Have a queue (set) of threads trying to get into the monitor (mq)
* Each CV implies another queue &rarr; cvq

atomic ops
* mq &rarr; all the threads trying to acquire a lock
* cq &rarr; all threads waiting on a conditional variable

```java
enter T:
    // while (anyone else in monitor) {
    //   add T to mq & put T to sleep
    // }

exit (T) {
    // take a thread out of mq & wake it up
}
    
wait (T cvq) {
    // add T to condition variable queue(cvq)
    // relax monitor & wake it up
}

notify() {
    // take a thread from cq & put to mq
}
   
notifyAll() {
    // move all threads from cvq to mq
}
    
```

* Notice that we wake one thread with `notify()`
* Upon being woken up, conditions may not hold; hence conditions should be checked again (typically with while loop)
* Spurious wakeups - may be woken up without being notified
    * Also solved by waiting in a while loop

## Different semantics for CV
* Signal & Continue
    * Notifier retains control of monitor
    * Woken thread competes with other threads for entry
* Signal & Wait
    * Notifier transfers monitor control to the woken thread
    * Notifier needs to re-acquire lock to continue
* Signal & Urgent Wait
    * Same as signal & wait, except a woken thread will give control back to the notifier once finished
* Reader/Writer Lock
    * DB context - only 1 write at a time
    * Multiple concurrent readers

    # Lecture 8. 2018/02/07
In Java concurrency, every synchronized block has a single unnamed "conditional variable" (Access to C.V in Java is with Object.wait() and Object.notify() )
* While 和 wait() 搭配使用
    * wait() 释放锁
---
Readers & Writers
Large database: 
* write on a DB &rarr; exclusive action
* reading is not an exclusive action
* Readers are a class & writers are individuals
    * reader 一个获得了锁，所有的都可以访问
    * writer 每一个都有一个独立的锁
* Either 1 writer access the Database or any number of readers access the Database

R/W Lock

```java
int readers = 0
Mutex r = 1, rw = 1; // 1: lock is avaliable; 0: lock is occupy
// r: lock to modify the number of readers
// rw: lock for mutual exclusion between class of readers and any individual writer

reader: 
    while (true) {
        // 要统计reader的个数
        down(r);
        readers++;
        if (readers == 1) { // acquire lock if first reader
            down(rw);
        }
        up(r);
        read(); // read the database
        
        // 要统计reader的个数
        down(r);
        readers--;
        if (readers == 0) {  // give up lock if last reader
            up(rw);
        }
        up(r);
    }

writer:
    while (true) {
        down(rw);
        write();
        up(rw);
    }

```

* Note that this gives preference to readers
    * A continuous stream of readers may starve waiting writers
* There are variants for writer's preference & fair solution
* 



```java
int nr, nw; // Track the number of R/W in the Database
int wr, ww; // Number of waiting R&W
Mutex e;
C.V okRead, okWrite;

Readers:
    Lock(e);
    if (nw > 0 || ww > 0) { // 有writer在等待
        wr++;
        wait(e, okRead); // 等待writer结束
    } else {
        nr++;
    }
    UnLock(e);
    ReadFromDB();
    Lock(e);
    nr--;
    if (nr == 0) { // 最后一个reader
        if (ww > 0) {
            ww--;
            nw = 1;
            notify(okWrite);
        }
    }
    
Writer:
    Lock(e);
    if (nr > 0 || nw > 0 || wr > 0) { // 有其他reader或者writer在工作
        ww++;
        wait(e, okWrite);
    }
    else {
        ww++
    }
    UnLock(e);
    // writes on DB
    Lock(e);
    nw--;

    if (wr != 0) { // 还有reader在等
        nr = wr;
        wr = 0;
        // The last reader leave and lets in writer preferably
        notifyAll(okRead);
    }
    else if (ww > 0) {
        ww--;
        nw = 1;
        notify(okWrite);
    }
    UnLock(e);
    // The writers add readers preferably

```

--- 

## Dining Philosopher Problem

5 philosophers at a round table with 5 plates and 5 utensils. Each will think, eat, and repeat. Eating requires getting two utensils adjacent to the philosophers. Goal is to avoid deadlock.

Solutions

---

1.Single Global Mutex

```java
think()
P(global)
eat()
V(global)
```

Works, but not very concurrent

---

2.Mutex per Utensil

c = mutex[5]

```java
think()
P(c[i])
P(c[(i + 1) % 5])
eat()
V(c[(i + 1) % 5])
V(c[i])
```
Doesn't actually work. If everyone tries to grab left utensil, no one will be able to grap right utensil and complete `eat()`

---

3.Create Global Lock

```java
lock = mutex // global lock
c = mutex[5] // lock per utensil

think()
P(lock)
P(c[i])
P(c[(i + 1) % 5])
V(lock)
eat()
V(c[(i + 1) % 5])
V(c[i])
```

Works, but still not very concurrent.
If philosopher 1 eats, and philosopher 2 attempts to eat, philosopher 2 will hold the global lock and block others who could have eaten from eating.

---

4.Randomise Lock
```java
P(lock)
if (rand() % 2 == 0) {
    P(c[i]);
    P(c[(i + 1) % 5]);
} else {
    P(c[(i + 1) % 5]);
    P(c[i]);
}
V(lock)
eat();
V(c[i]);
V(c[(i + 1) % 5];
```
deadlock less likely

5.Allow only 4 philosophers to sit to the table at the same time.
```java
int seats = 4;
P(seats); // 现在可以有四个人同时进入
P(c[i]);
P(c[i + 1] % 5);
eat();
V(c[i + 1] % 5);
V(c[i]);
V(seats);
```

6.Acquire & Release
* We can detect lock failure

7.Ordering Resource
* Partial order: acquire mutiple resource must respect order
* this also breaks the dependency cycle

```java
think()
j = ((i + 1) % 5)
P(min(i, j))
P(max(i, j))
eat()
V(max(i, j)) // order here isn't as important
V(min(i, j)) // but reverse unlock is good practice
```

# Lecture 9. 2018/02/12
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


# Lecture 10. 2018/02/14

## Barrier
* Want thread to wait for each other &rarr; wait everyone finish phase1 before finishing phase2

1.Simple Barrier
```java
// n-thread barries

volatile int numThreads = n;
// for each thread
while (true) {
    // work
    if (FA(numThreads, -1) == -1) {
        numThreads = n;
    }
    else {
        while (numThreads != 0) {
            yield();
        }
    }
}
```
Drawback: works only once

```Java
Class Barries {
    int numThread = n;
    synchronized void await() {
        numThread--;
        if (numThread == 0) {
            // 全部就绪，重新设置barrier
            numThread = n;
            notifyAll();
        }
        else {
            while (numThread != 0) {
                wait();
            }
        }
    }
}
```
2.Sense Reversing Barrier

```java
// The initialize phase must be different
boolean phase = true
boolean phases[n] // all false

if (FA(numThreads, -1) == -1) {
    // if it is the last one
    numThreads = n;
    phase = phases[id]; // toggle the phase
}
else {
    while (phase != phase[id]); // spin
}

exit(int id) {
    phases[id] = !phases[id]; // toggle
}
```

## Consensus

* Lots of synchronized primitives  (TS, FA, CAS)
* We can build locks with any/all of those

```java
caslock = 0;
CAS(x, a, b) {
    bool rc;
    while (TS(caslock, 1) == 1); // spin
    if (x == a) {
        x = b;
        rc = true;
    }
    else {
        rc = false;
    }
    caslock = 0;
    return rc;
}

testlock = 0;
TS(x, y) {
    while (CAS(testlock, 1, 1)); // spin
    temp = x;
    x = y;
    testlock = 0;
    return temp;
}
```
* CAS original - fixed amount of time to execute (wait free)
* Simulate CAS - spins &rarr; may have a finite cost, potentially indefinitely
* "Wait free" Property
    * Finite # of steps
    * Fault-tolerant
* Consensus problem
    * n threads, each with different values
    * Want to agree on the value
    * Requirements
        * Consistent - all agree in the end
        * Valid - agreed value is one of the starting values
        * Wait-free - finite # steps, fault tolerant
            * cannot spin

* Consensus number of a synchronized primitive is the max # of threads for which they can solve the consensus problem
 
---
### R/W - consensus #1
* Basic R/W atomicity
    * Proof:
        * T0: reads a variable
            * T1 &rarr; might read or write
            * T1 tree also necessarily exists before T0's action - not univalent
        * Therefore both T0 & T1 must both write
            * Could write different vars
        * T0 & T1 write different vars
        * They both write the same var x
            * Cannot be univalent &rarr; cannot be critical
        * R/w primitives have a consensus # of 1
---
* Binary consensus `{0, 1}`
    * Show we cannot solve 2-consensus
    * Start in a bivalent state
        * System &rarr; `{0, 1}` 
    * End up in a univalent state
        * `{0}` or `{1}`

&rarr; there must exist a `critical state`
* Node(`{0}`, `{0, 1}`, `{1}`) or Node(`{1}`, `{0, 1}`, `{0}`) 
* Node(`{0, 1}`, `{0, 1}`, `{1}`) is not a critical state
* this is the point we are making decision


### FA, TS, consensus #2
```java
int decide (int v):
    x = TS(decider, v) // x is old value
    if (x == bottom) { // bottom is the value not in the input
        return v;
    }
    return x;
```
Solves 2 consensus, but not 3

### CAS, consensus # &infin;
```java
int decide (int v):
    CAS(decider, bottom, v)
    return decider
```
---
hierarchy
* R/W < TS/FA < CAS

# Lecture 11. 2018/02/19
## Processes and Synchronization:  Scheduling Policies and Fairness
[Reference Website](http://www.it.uom.gr/teaching/distrubutedSite/cisumassd/chap02/chap02_20.html)
* A `scheduling policy` decides which of the eligible atomic actions will be excuted next
* `Fairness`, is a living property and it is an attribute of a scheduler or algorithm that guarantees that every delayed process gets a chance to proceed
* `Unconditional fairness`: a scheduling policy is unconditionally fair if every unconditional atomic action that is eligible is executed eventually
    * eg. for example: round-robin would be unconditionally fair scheduling policy on a single processor, and parallel excution would be unconditionally fair policy on a multiprocessor.
* When a program contains conditional atomic actions - 
    * await(B) C -> when execute C, B must be true
* `Weak fairness`: a scheduling policy is weakly fair if
    (1) it is unconditionally fair
    (2) every conditional atomic action that is eligible is executed eventually, assuming that its condition becomes true and then remains true until it is seen by the process executing the conditional atomic action
    * weak fairness is not sufficient to ensure that any eligible await statement eventually executes. This is because the condition might change value from false to true and back to false while a process is delayed
* `Strong fairness`: a scheduling policy is strongly fair if 
    (1) it is unconditionally fair, and 
    (2) every conditional atomic action that is eligible is executed eventually, assuming that its condition is infinitely often true
    * to be stronly fair, a scheduling policy cannot happen only to select an action when the condition is true; it must sometimes select an action when the condition is false

## Linearization

* Concurrent programs
    * In general, not functional
    * Output can depend on timing/scheduling

* Correctness
    * If whatever happens in our concurrent program has an equivalent sequential program producing the same output

---

Example - 2 queues 
p, q - enqueue, dequeue

T0 | T1
--- | ---
1: p.enq(x) | 4: q.enq(y)
2: q.enq(x) | 5: p.enq(y)
3: p.deq() -> returns y | 6: q.deq() -> returns x

(numbers are just for future reference and do not refer to runtime order)

is this linearizable?

* Since p returns y, 5 must occur before 1 (FIFO)
* Since q returns x, 2 must occur before 4
* However, given that the calls in within each thread is sequential, we know that this result isn't possible

If sequential correctness depends on proper stack operation &rarr; assume every operaton take place at a single instant in time

* Linearization point
    * All function calls have a linearization point at some instant between their invocation and their response.
    * Then we can interleave easily => equivalent to sequential execution
* We can do this without linearization point
    * much more complex
    * there is a gap between invoke and return

--- 

## Memory models

Memory consistency 
* define which write can be seen by which reads -> order properties

T0 | T1
--- | ---
x = 1 | y = 2
b = y  | a = x

(variables start at 0)

Possibilities: (a, b) = (1, 0), (0, 2), (1, 2)

* This relize on thinking of concurrent execution as interleaving
Note that cases like (0, 0) is not possible; invalid interleaving 

--- 
Write-buffering


| P0 | WB0 | Mem | P1 | WB1
| --- | --- | --- | --- | ---
| x = 1 | &rarr; x = 1 | x = 0 | y = 2 | &rarr; y = 2 
| - | - | y = 0 | - | -
| b = y | &rarr; b = 0 | a = 0 | - | -
| - | - | - | a = x | &rarr; a = 0
| - | writes x | x = 1 | - | -
| - | - | y = 2 | - | writes y
| - | - | a = 0 | - | -
| - | - | b = 0 | - | -

As a result, with buffering, we can have (0, 0).

&therefore; we cannot just think of interleavings

* Model types
    * Strict consistency (内存一致性模型)
        * operations necessarily follow program order (intra-threads)
        * 但是线性一致性太难实现了，因为这里需要一个全局同步的时钟
    * Sequential Consistency (SC) (顺序一致性模型)
        * 这里全局的时钟变得不再需要，转而需要的是各个处理器局部的时钟，
        * more pratical in pratice
        * operations appear to execute in program order
    * Both cases result in a global timeline for operations

* Coherence
    * The write to a given variable must be seen in the same order by all threads
    * Doesn't require sequential consistency. Every variable has their own timeline

* Process Consistency (PC)
    *  T0 | T1 | T2 | T3
       ---|---|---|---
       x = 1 | x = 3 | a = x(1) | d = x (3)
       y = 2 | y = 4 | b = y (2) | e = y (4)
       | - | - | c = x (3) | f = x (1)

    * T2 sees T0 before T1
    * T3 sees T1 before T0
    * Note that this is not coherent

* Note
    * SC implies cohrerence and PC
    * PC does not imply coherence
    * Coherence does not imply PC

# Lecture 12. 2018/02/21
Intel/AMD
* Memory management implied in the hardware manual
* White paper
    * Informal rules
    * "litmus" tests
* Some kind of casual consistency
* IRIW - independent read independent write
    * Probably don't want, but not ruled out

  P0 | P1 | P2 | P3 
  ---|---|---|---
  x = 1 | y = 1 | eax = x, ebx = y | ecx = y, edx = x

  For P2, it sees eax = 1, ebx = 0, meaning P0 happened before P1
  For P3, it sees ecx = 1, edx = 0, meaning P1 happened before P0

* Also cases that should not happen, but can be obsered in practice **n6**

  P0 | P1 
  --- | ---
  x = 1 | y = 2
  eax = x | x = 2
  ebx = y | |

  Could observe that eax = 1, ebx = 0, x = 1

* Could happen with write buffers
    * Order for which they are committed to main memory is undefined
    * 
So Intel fixed the documents
* Specified disallowed IRIW
* Added in MFENCE - memory fence to monitor read write access
* Same processor writes are ordered and observed in the same order by all others
* Constraints on inter-process ordering
    * Any 2 stores are seeing consistent ordering by processes other than those doing the write. Leaves opens another case n5
    * n5

      P0 | P1
      ---|---
      x = 1 | x = 2
      eax = x | ebx = x

      eax = 2, ebx = 1 (Here P0 sees x = 1, x = 2; P1 sees x = 2, x = 1)
      not disallowed, but also not observed
      
* x86-TSO - abstract model by academics
    * 见iPad示意图
    * Allow everything observed
    * Respect litmus tests
    * Every hardware thread has its own write buffer
        * All writes stored in order
        * Committed in order
        * `Has no overwrites`
    * Global lock that can be acquired by any hardware thread (with constraints)
    * MFENCE
        * All Write Bufer flushes must be done before
        * Write Buffer is FIFO
    * Some instructions
        * LOCK: prefix - thread must acquire global lock
            * While lock is held, no other thread may read
            * Buffered write can be committed to memory at any time except when another thread holds the lock
            * Before releasing lock, Write Bufer must be empty
            * A thread is _blocked_ if it does not hold the lock & someone else does
        * Set of (abstract) primitive operations
            * Rp[a] = v : `p` can read value `v` from address `a` if `p` is not blocked; there are no writes to `a` in WB<sub>p</sub> and mem[a] = v
            * Rp[a] = v : `p` can read `v` from address `a` if `p` is not blocked, and p has `a` (latest) store "a = v" in WB<sub>p</sub>
            * Wp[a] = v : `p` can write `a = v` into WB<sub>p</sub> at any time
            * &tau;p : if `p` is not blocked, it can silently send the oldest write in WB<sub>p</sub> to memory
            * Fp : if WB<sub>p</sub> is empty, `p` can issue an MFENCE instruction
            * Lp : if the lock is not held by another process, `p` can acquire it
            * Up : if `p` has the lock & WB<sub>p</sub> is empty, we can release the lock
        * progress condition : all buffered writes are eventually committed
        * Example

          | P: WB<sub>p</sub> = [0x55] = 0 | Q: WB<sub>q</sub> = [0x55] = 7 |
          |---|---|
          | Lock : Inc [0x55] | |
          | Lp | |
          | Rp[0x55] = 0 | |
          | Wp[0x55] = 1 | |
          | &tau;p (0x55 = 0) | |
          | &tau;p (0x55 = 1) | |
          | Up | &tau;q |

            * Without lock, p can begin increment, q can set to 7, and p can increment the new value by 1 &rarr; 8
