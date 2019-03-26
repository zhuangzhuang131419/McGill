# Lecture 14. 2018/03/12

## JVM
* Happen before - consistency
* caused cycle
    * out of thin-air
* "justification" process
    * program execution
    * look for data reace 
    * if no races, all threads see writes happen before
 
* eg.
```Java
    x = y = o
    T0:             T1:
    r1 = x;         r2 = y;
    y = 1;          x = 1;
```
Can we get r1 == r2 == 1? Yes!

```Java
    x = y = 0;
    T0:                     T1:
    lock m1;                lock m2;
    r1 = x;                 r2 = y;
    unlock m1;              unlock m2;
    lock m2;                lock m1;
    y = 1;                  x = 1;
    unlock m2;              unlock m1
```
* No race conditions! Assume S.C.

### When is a race-condition(data race)?
* Occurs at runtime!(Not in the code) 
* eg.
```Java
    x = y = 0 
    T0:                     T1:
    do{                     do {
        r1 = x;                 r2 = y;
    } while (r1 == 0)       } while (r2 == 0)
    y = 42                  x = 42             // never reachable
```
* No data-race! 
* It is race-free by divergence, we never executes the writes to x and y

##JMM:
* H.B.-graph
    * Runtime execution 
    * Intra-thread order 
    * Sync-order 
---
## C++ Memory Model
* Use pthreads 
* Very similar to JAVA
    * ‘Sequence-before’(very much like ‘Happen-Before’) 
    * Intra-thread + inter-thread 
    * This is a partial order even in intrathread(from the language) 
    * Like JAVA, data-race-free program -> S.C.
    * If have data-race, implementation-defined.
* Avoid data-races!
```Java
unsigned int i = x; // i is a shared variable
if (i < 2) { // i must be 0 or 1
    // if i is changed by another thread here, then it will have problem
    switch (i) {
        case 0: // ...
        case 1: // ...
        default: // unreachable
    }
}
```

---
## ABA & Lock-free
* ‘wait-free’:
    * must finish in finite time
    * fault tolerant
* ‘lock-free’
    * ![Lock Free Definition](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/LookFreeDefine.png)
    * 一个Lock free的程序能够确保执行它的所有线程中至少有一个能够继续往下执行。
    * Somebody make progress 
    * Avoid locking
    * Guarantee that infinitely often SOME method calls finish, in a finite # of steps.
    * 在一系列访问 Lock-Free操作的线程中，如果某一个线程被挂起，那么其绝对不会阻止其他线程继续运行（Non-Blocking）。
* Locking(blocking) is expensive. 
    1) OS schedule is involved
    2) Use spin-locks(by using CAS, TS, etc…)
    Some difficulties: Lock-free stack?
---
* 1983 -> IBM(370) was making lock-free stack, but bugs exist.
ABA problems Simple lock-free stack 
```Java
    push(Node n) {
        do {
            Node t = TOS; // Top of stack
            n.next = t;
        } while (!CAS(TOS, t, n)); // 
    }
    
    pop() {
        Node t, n;
        do {
            t = TOS;
            if (t == null) {
                return "empty";
            }
            n = t.next;
        } while (!CAS(TOS, t, n))
        return t;
    }
```
BUG:
* eg. 
    * T0: pop()
    * T1: ------ pop()-> pop()-> push()
|`x`| <- TOS
|`y`|
|`z`|
    * T0: pop() -> t = `x` -> n = `y`
    * T1: -----------------pop() -> t = `x` -> n = `y` -> CAS(TOS, `x`, `y`) == true -> return `x`
|`x`|
|`y`| <- TOS
|`z`|
    * T0: -----------------------------------------------------------------
    * T1: pop() -> t = `y` -> n = `z` -> CAS(TOS, `y`, `z`) == true -> return y -> **free(y)**
|`x`|
|`z`| <- TOS
    * T0: ------------------------------------------------------------------
    * T1: push(`x`) -> t = `z` -> `x`.next = z -> CAS(TOS, `z`, `x`) == true 
|`x`|  |`x`| <- TOS
&nbsp; &nbsp; &nbsp;|`z`|
    * T0: -> CAS(TOS, `x`, `y`) == true -> **CRASH!!!**
*  `y` has been freed, unallocated!
*  Here CAS tells us TOS is ‘still’ `x`, but the stack itself has changed.

* Text claim that JAVA does not have an ABA problem. Since JAVA have a garbage collection, y will not be freed. But we could have reuse y for any purpose.(y 可
能已经被做过很多操作，不应当属于这个stack了) 

---
## Solution
* LL/SC
    * Load-Linked, State-Conditional 
    * 2 operations that let us construct tests to ensure a memory address has not been written(even with the exactly same value) 
    * In computer science, load-link and store-conditional (LL/SC) are a pair of instructions used in multithreading to achieve synchronization. Load-link returns the current value of a memory location, while a subsequent store-conditional to the same memory location will store a new value only if no updates have occurred to that location since the load-link. 
* LL/SL
    * Not Intel 
    * PPC, ARM, Alpha... 
    * PPC: 
```Java
    lwarx(x) {
        temp = x;
        x is reserved by the process/CPU
        return temp;
    }
```
```Java
    stwcx(x, y) { 
        if (x is still reserved for us) { 
            x = y; 
            Remove reservation; 
            return true; 
        } 
        return false;
    }
```
```Java
redoit:
    lwarx r1, 0, xR
    addi r1, r1, 1
    stwcx r1, 0, xN
    bne redoit
    // redoit if reservation failed
```
-> reservation disappear if
    1) Someone else does LL/SL
    2) x is written Now, if we use LL/SC, instead of CAS in our stack, NO ABA problem 
---
* JAVA does not give an LL/SC. 
* If we used CAS on a ‘wider value’ <TOS, #version> AtomicStampedReference