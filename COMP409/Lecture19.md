# Lecture 19. 2018/03/28
```java
produce() {
    atomic {
        if (g.full()) {
            retry; // 与wait()方法或者显示的条件变量不同，retry并不是简单地让出它自己，从而丢失唤醒故障
            q.add(data);
        }
    }
}
```

## Hardware Support
* IBM's BlueGen/Q
* Intel "Haswell"
* 2 subsystem
    * HLE
        * Hardware lock elision
        * moving away from locking
    * RTM
        * Restricted transcation memory
        * TM

### HLE
* **XACQUIRE** prefix
    * Lock
    * we don't actually acquire the lock
        * **XACQUIRELOCK**
    * but do note the memory address locked
    * execute subsequent code
    * if no one conflicts with the transcation code (lock variable)
        * success!
        * avoid lock overhead
* **XRELEASE**
    * mark the end transcation
    * if the transcation fails
    * we retry, but ignore **XACQUIRE**
    * N.B. a little "stronger" than locking
* **XACQUIRELOCK**
    * 
    ```Java
    a = y;
    b = x; <------------ x = 3 // transcation fail
    c = a;
    XREALEASE
    unlock()
    ```
    
## RTM 
* 
    * offset -> failure handler: receives a bit-code, different bits indicate why the transcation failed.
    *  transcation can fail
        * XABORT
        * conflict
        * buffer overflow
        * special instruction
            * occupied
            * pause
        * interrupts, task shopping
    * 见iPad
```
XBEGIN<offset> 
    XABORT<reason>
    XTEST
XEND
```


### Thread-Level Specialist(TLS)
* atomic parallel program
    * sequential program ==TLS==> parallel program
    * 见iPad示意图
        * notice: spec thread executes in the future, gets input state may not be correct
            * avoid this code being visible
            * at the join point, we test if its input state was correct
            * code for `C` is executed like a transcation
                * Read and Write buffer
                * Write buffer isolate `C` until no commit a discard it
                * Read buffer to unify our acted input state maintain the indicate input state

### Message Passing(M.P.)
* So far, it's been all shared memory. Instead of sharing data, in a M.P. system
    * send & receive data
* 2 forms of message Passing
    * Asynchronous message passing
        * receive data -> blocked until data is ready
            * d = receive()
        * sending data
            * sending operation is non-blocking
            * eg. post office
            * reliable ordering
    * Synchronous message passing
        * receive -> block
        * send -> block
    * Which is more expressive?
        * Synchronous message passing gives us more 
        * eg.
            * Tp: ---------send(Q, 23)
            * Tq: x = 0; x = receive(p)
            * Asynchronous
                * After the send, what does Tp know about Tq?
                    * Q.x is either 0 or 23
            * Synchronous, 
                * After the send, what does Tp have about Tq?
                    * Q.x is 23
    * Common knowledge
        * get from synchronous massage passing

## Process Algebra
* abstract, simple system for describling concurrent program
* "algebra formalism" to understand & compare concurrent program
* lots of formalism
    * C.S.P. communication Sequential Processer
    * C.C.S. Calculus & Concurrent System
    * Meije
    * ACP Algebra of concurrent proceed
* main function
    * interleaving
    * synchronous message passing (asynchronous message passing)
    * theoretical -> process equivalent
    * turned into  real language

## C.S.P
* old with lots variants
    * syntax has changed
* build up process expression
    * parallel
    * message-passing (synchronous)
* P: HALT  // skip
    * name: actual definition
    * process communication: send & receive
    * eg.
        * P::Q!x  // P is a process send e to Q
        * Q::P?x  // Q is a process that receive from P & store in x

    