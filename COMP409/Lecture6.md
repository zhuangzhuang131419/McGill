# Lecture 6. 2018/01/29
### Bakery Algorithm
* Broken ticket dispenser

```
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
    while (myPred.locked);   // spin
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



