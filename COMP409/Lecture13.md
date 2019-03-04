# Lecture 13. 2018/02/28
* Linux spin-lock 
    * Kernel lock(acquired frequently) 
    * Start: Lock: Dec[EAX] <— lock(atomic)
        * JNS enter —> decrement from 1 
    * Spin: CMP[EAX], 0
        * JLE spin
        * JMP start 
    * Exit: Lock: MOV [EAX], 1
        * (but do we need Lock this?)
        * No, we do not need this.
### What about Java?
* Languages always come up with a memory model.
* Java has a memory model —> JMM Old model is complex and ill-defined.
    * Global memory -> working memory -> execution engine -> W.M. -> G.M.
* It was stronger: coherence 
    * Recall: One timeline per variable 
    * OK -> Coherence is a ‘lower bound’ on programmability 
* eg. 
    * int i = p.x;                  int i = p.x;
    * int j = q.x;  == Optimize ==> int j = q.x;
    * int h = p.x;                  int h = i;
    * But suppose p&q are the same object and another thread set p.x = 1; 
    * And when i = 0, j = 1, if we set h to i, that is 0, it is no longer coherence. So this optimization is not allowed!
---
* The new JMM 
    * Tries to solve 2 problems
    1. Weak ordering so we can optimize.
    2. Strong ordering for programmers. 
    * Divide programs into 2 groups
    1. Correctly synchronized program 
        * Contains NO data races.
        * Then you can assume sequential consistency(great for programmers)
    2. Incorrectly-synchronized program Contains at least one data-race A more complet semantics Start with the orderings 
        * x = 1 
            -> y = 2
            -> a = x
            -> b = y
            -> c = 3 
            * Intra-thread ordering follows your program order.
                * intra-thread semantics 允许那些在单线程内，不会改变单线程程序执行结果的重排序。  
            * Also another ordering 
                * Synchronization order 
                * Inter-thread order 
                * Total order over synchronized events (a runtime) 
                * eg. 
                T0: m.lock() --intra--> m.unlock();
                T1: m.lock() --intra--> m.unlock();
                    * T0 -> T1: runtime T0 acquire m before T1
                    * T1 -> T0: runtime T1 acquire m before T0
            * Synchronization order
                * unlocks(m) precedes locks(m)
                * volatile write precedes all subsequent result.
            * thread-start precedes the first operation by the thread 
                * The last statement of a thread precedes a join on that thread
            * Sync order + intra-thread order 
                * Happens-Before 
                * Ordering on continue trace 
                * Partial ordering 
            * We can construct a graph of an execution
                * Nodes -> operations/actions
                * Arrows will be intra-thread sync order
                * eg.x = y = 0 
                    * T0: r1 = y, x = 1, x = 2, r2 = x, x = 3
                    * T1: r3 = x, y = 1 
                * 示意图见iPad
                * Happens-Before Graph 
                * Dash lines are 'can-see edges' 
                * Which writes can a given read ‘see’? 
            * We have 2 rules to constraint that:
                1. R(x) should not precede W(x) 
                    * r2 = x -> x = 3; r2 cannot be 3
                2. W(x) -> W’(x) -> R(x) 
                    * x = 1 -> x = 2 -> r2 = x; r2 cannot be 1 
                So r1 in above figure can se either 0 or 1. 
---
### HappensBefore - consistency
* There are weird things in HappensBefore- consistency
* eg. x = y = 0
    * T0: r1 = x -> y = r1
    * T1: r2 = y -> x = r2
* Can x == y == 42 ?(out-of-thin-air value: comes out of nowhere)
    * y can see y = 0 and y = r1 
    * r1 comes from x 
    * x can see x = 0 and x = r2 
    * r2 comes from y 
Finally we get: y comes from y, x comes from x. HB-consistency do NOT forbid this. But this is NOT good.

* Can we forbid this out of thin air value? 
* We had a causal cycle: y was caused by y, x was caused by x.
* Can we forbid causal cycle?
    * No! 
    * eg. a = b = 0
        * T0: i = a -> j = a -> if (i == j) b = 2
        * T1: k = b -> a = k
    * Can we get i == j == k == 2?

* But we could optimize this code to:
    * eg. a = b = 0
        * T0: b = 2 -> i = a -> j = i
        * T1: k = b -> a = k
        * Now we can have i == j == k == 2 by only interleaving.
        * To allow such optimization, we must allow causal cycle.

* To allow some cycles but not OOTA values 
    * Justification precess 
    * Iterative process
        * Compute a well-behaved execution 
        * Only see writes that HB before the read 
        * No data races -> done 
        * If there are data races
            * Pick a data race
            * Resolve it 
            * R/W 
                * Chose which write the read saw
            * Now committed 
        * ‘Restart’ the execution with assumption 
        * Then check if there are more data races
