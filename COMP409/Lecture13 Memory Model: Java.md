# Lecture 13. 02.28 Memory Model Java
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
* 内存模型解决并发问题主要采用两种方式：**限制处理器优化**和**使用内存屏障**。
* Java has a memory model —> JMM Old model is complex and ill-defined.
    * Global memory -> working memory -> execution engine -> W.M. -> G.M.
* It was stronger: coherence 
    * Recall: One timeline per variable 
    * OK -> Coherence is a ‘lower bound’ on programmability 
* eg. 
    * int i = p.x;                  int i = p.x;
    * int j = q.x;                  int j = q.x;
    * int h = p.x;  == Optimize ==> int h = i;
    * But suppose p&q are the same object and another thread set p.x = 1; 
    * And when i = 0, j = 1, if we set h to i, that is 0, it is no longer coherence. So this optimization is not allowed!
---
* The new JMM 
    * ![JavaMemoryModel](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/JavaMemoryModel.png)
    * JMM决定一个线程对共享变量的写入何时对另一个线程可见
    * 线程之间的共享变量储存在主内存(main memory)中，每个线程都有一个私有的本地内存(local memory)，本地内存中储存了该线程以读/写共享变量的副本。
    * Tries to solve 2 problems
    1. Weak ordering so we can optimize.
    2. Strong ordering for programmers.
    * Divide programs into 2 groups
    1. Correctly synchronized program 
        * Contains NO data races.
        * Then you can assume sequential consistency(great for programmers)
    2. Incorrectly-synchronized program Contains at least one data-race 
        * A more complet semantics 
        * Start with the orderings 
        * x = 1 -> y = 2 -> a = x -> b = y -> c = 3 
            * Intra-thread ordering follows your program order.
                * intra-thread semantics 
                * 允许那些在单线程内，不会改变单线程程序执行结果的重排序。  
            * Also another ordering 
                1. Synchronization order 
                    * unlocks(m) precedes locks(m)
                    * volatile write precedes all subsequent result.
                    *  eg. 
                    T0: m.lock() --intra--> m.unlock();
                    T1: m.lock() --intra--> m.unlock();
                        * T0 -> T1: runtime T0 acquire m before T1
                        * T1 -> T0: runtime T1 acquire m before T0
                2. Inter-thread order 
                3. Total order over synchronized events (a runtime) 
                    * Thread-start precedes the first operation by the thread, the last statement of a thread precedes a join on that thread
            * Sync order + intra-thread order 
                * Happens-Before 前一个操作的执行结果对后一个是可见的
                * Ordering on continue trace 
                * Partial ordering 
---
## Graph
We can construct a graph of an execution
* Nodes -> operations/actions
* Arrows will be intra-thread sync order
* eg.x = y = 0 
    * T0: r1 = y, x = 1, x = 2, r2 = x, x = 3
    * T1: r3 = x, y = 1 
* ![HappensBeforeGraph](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/HappensBeforeGraph.png)
    * Happens-Before Graph 
    * Dash lines are 'can-see edges' 
    * Which writes can a given read ‘see’? 
---
We have 2 rules to constraint that:
1. R(x) should not precede W(x) 
* r2 = x -> x = 3; r2 cannot be 3
2. W(x) -> W’(x) -> R(x) 
* x = 1 -> x = 2 -> r2 = x; r2 cannot be 1 
So r1 in above figure can se either 0 or 1. 
---
### HappensBefore - consistency
* 在JMM中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须存在happens-before关系。
* Define
    1. 程序次序规则：`一个线程内`，按照代码顺序，书写在前面的操作先行发生于书写在后面的操作；
    2. 锁定规则：一个unLock操作先行发生于后面对同一个锁额lock操作；
    3. volatile变量规则：对一个变量的写操作先行发生于后面对这个变量的读操作；
    4. 传递规则：如果操作A先行发生于操作B，而操作B又先行发生于操作C，则可以得出操作A先行发生于操作C；
    5. 线程启动规则：Thread对象的start()方法先行发生于此线程的每个一个动作；
    6. 线程中断规则：对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生；
    7. 线程终结规则：线程中所有的操作都先行发生于线程的终止检测，我们可以通过Thread.join()方法结束、Thread.isAlive()的返回值手段检测到线程已经终止执行；
    8. 对象终结规则：一个对象的初始化完成先行发生于他的finalize()方法的开始；
* 
    ```Java
    private int i = 0;
    
    T0:
    public void write(int j ){
        i = j;
    }
    T1:
    public int read(){
        return i;
    }
    ```
    * 我们约定线程T0执行write()，线程T1执行read()，且线程T0优先于线程T1执行，那么线程B获得结果是什么？
    * 我们无法通过happens-before原则推导出线程T0 happens-before 线程T1 ，虽然可以确认在时间上线程T0优先于线程T1指定，但是就是无法确认线程T1获得的结果是什么，所以这段代码不是线程安全的。
* There are weird things in HappensBefore- consistency
* eg. x = y = 0
    * T0: r1 = x -> y = r1
    * T1: r2 = y -> x = r2
* Can x == y == 42 ?(out-of-thin-air value: comes out of nowhere)
    * y can see y = 0 and y = r1 
    * r1 comes from x 
    * x can see x = 0 and x = r2 
    * r2 comes from y 

* Finally we get: y comes from y, x comes from x. HB-consistency do NOT forbid this. But this is NOT good.

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
    * Justification process 
    * Iterative process(迭代解决)
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
