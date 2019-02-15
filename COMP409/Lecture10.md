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
    if (x == bottom) { // bottom is the value set by first thread
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
