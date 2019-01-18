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
* Program stops when all threads are finished
* Subtlety - program exists when all non-daemon threads (default) finish
    * Daemon threads are intended as services, and terminate automatically when appropriate
* A thread that has been started may not necessarily be running - OS may choose to switch it to a scheduled or de-scheduled state
* Threads may also sleep, which goes to a waiting mode, or be woken up
* Threads may be terminated, leading to a stopped mode
