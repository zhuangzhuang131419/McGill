# Lecture 2. 01.15 Hardware, atomicity

## Hardware

* UP &mdash; Basic uniprocessor
    * CPU &mdash; cache &mdash; memory
      * ![Uniprocessor](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Lecture2/Uniprocesspr.png)
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
    * Many [CPU &mdash; cache] to shared memory
      * ![Multiprocessor](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Lecture2/Multiprocesspr.png)
    * Keeping the cache consistent is important
* UMA &mdash; Uniform memory access
    * All memory accesses cost the same (modulo cache)
    * Note some caches need to reflect the same view on memory
    * Caches need to be consistent
* NUMA &mdash; Non-UMA
    * Many [CPU &mdash; cache] to shared memory (slow) & many local memories (fast)
    * ![NUMA](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Lecture2/NUMA.png)
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
    * ![CMP](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Lecture2/CMP.png)
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
    * Support "true" parallelism but not suffer in single-thread &mdash; act like a CMP （线程互相之间的界限没有特别明显）
    * ![SMT](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Lecture2/SMT.png)
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
