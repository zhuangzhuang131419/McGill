# Lecture 15. 03.14 Concurrent data structure
## Lock-Free Stack
* Lock free designs make use of CAS, LL/SL, etc as opposed to locks
* Stack operation -> inherit sequential
* Some time, we can benefit from concurrency
    * push() <----exchange-----> pop()


## Elimination Stack
With a lock-free stack, we still have a lot of contention. Stack ops are fundamentally sequential. However, if `push()` and `pop()` show up at the same time, it might be better to just short-circuit the whole thing. ie, `push()` gives the value to `pop`; `push + pop` cancel/do nothing.
* lock free stack
* exchanger(should also be lock free)

Lock free exchanger (2 threads exchange data)
* state info (pair of <value, state>)
* need atomic operators to r/w pair atomically
* value: data exchange
* state: `EMPTY`, `WAITING`, `BUSY`
    * `EMPTY` - ready to do the swap <br> CAS(pair, null to EMPTY, A to EMPTY)
        * 该线程尝试将他的数据放入槽中，并调用compareAndSet将state设置为`WAITING`
        * If successful:
            * wait for the second thread to show up
            * spin until we see the state as `BUSY`
            * grab item & set state to `EMPTY`(no CAS required here)
        * If we wait for too long (说明其他线程成功，重试)
            * Try & give up
            * 等待线程需要调用compareAndSet将槽重新设置为`EMPTY`(其他线程有可能试图通过把`WAITING`变成`BUSY`)
                * If successful, we go & do push/pop
                * Else complete the exchange    
    * `WAITING` - one thread (second thread to show up)
        * check the state &rarr; not `EMPTY`
        * grab item
        * try CAS(pair, A to `WAITING`, B to `BUSY`)
            * If successful, we have done our part
            * If fail(另一个线程把`WAITING`变成`EMPTY`并完成了交换)
                * restart to resolve push/pop
    * `BUSY` - two threads (third thread to show up &rarr; give up)
        * second thread has completed the exchange
        * grab value `B` 
        * set state to `EMPTY`
        
* if exchange fail then do regular push() pop()

Can associate an exchange for the state
* push(x) is expected to exchange x for null
* pop() is expected to exchange null for x
* if successful, don't need to actually go through the state
* in fact, we can associate an array of exchangers
    * threads above random index to try for an exchange
* try for a while, if a matching push/pop does not show up or a non-matching situation
    * push/posh or pop/pop occurs, give up & resort to the lock free stack
* Java's exchanger is based on an array of exchangers



--- 
## Lock Free Linked List
* always has a head(H) and a tail(T)
* singly linked
* allow addition of item && remove item
```java
tryAdd(Node n, Node prev) {
    n.next = prev.next;
    return CAS(prev.next, n.next n);
}

tryRemove(Node n, Node prev) {
    return CAS(pre.next, n, n.next);
}
```
Sadly these naive methods do not work.
Given a list H &rarr; `x` &rarr; `y` &rarr; `z` &rarr; T:

1. If T0 tries to remove `x`, and T1 tries to add `w` between `x` and `y`, `w` will be lost.
* T0: CAS(H.next, x, y) == true
* T1: w.next = y -> CAS(x.next, y, w) == true
* do delete `x`, but we fail to add `w`
2. If T0 tries to remove `x` and T1 tries to remove `y`, both may succeed with `y` remaining in the list.
* T0: CAS(H.next, x, y) == true
* T1: CAS(x.next, y, z) == true
* delete `x` succussfully, but fail delete `y`

Various solutions exist
* Valois - "auxiliary nodes"
    * eg. H -> `.` -> `x` -> `.` -> `y` -> `.` -> `z` -> `.` -> T
* Time Harris - lazy solution - mark node to be deleted then delete lazily
    * <next, mark> 
        * mark::true -> deleted
        * mark::false -> not deleted
---

```java

tryAdd(Node n, Node prev) {
    next = prev.next;
    // 确保prev没有被delete掉
    return CAS(<prev.next, prev.mark>, 
                <n.next, false>,
                <n, false>);
}
    
tryRemove(Node n, Node prev):
    Node succ = n.next;
    // mark first
    if CAS(<n.next, mark>, <succ, false>, <succ, true>) {
        // delete is ok to fail
        CAS(<prev.next, mark>, <n, false>, <succ, false>)  
        return true
    }
    return false
```
* eg.
    1) T0: remove T1: add `w` after `x`

Now we have deleted things left in the list, we need to clean up
```Java
// H->''->''->''->T
find(int data) {
    while(true) {
        pred = H;
        curr = pred.next;
        while (curr != T) { // restart
            succ = curr.next;
            while (curr.marked) {
                // 确保pred没有被mark
                if (!CAS(<pred.next, marked>, <curr, false>, <succ, false>) {
                    continue; // restart
                }
                curr = succ;
                succ = curr.next;
            }
            if (curr.data == data) {
                return curr;
            }
            pred = curr;
            curr = succ;
        }
        return null
    }
}
```


