# Lecture 11. 2018/02/20
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