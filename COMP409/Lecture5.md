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
