# Lecture 8. 02.07 DeadLock
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
    UnLock(e);
    
Writer:
    Lock(e);
    if (nr > 0 || nw > 0 || wr > 0) { // 有其他reader或者writer在工作
        ww++;
        wait(e, okWrite);
    }
    else {
        nw++
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
if (rand() % 2 == 0) {
    P(c[i]);
    P(c[(i + 1) % 5]);
} else {
    P(c[(i + 1) % 5]);
    P(c[i]);
}
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
