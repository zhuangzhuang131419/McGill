# Lecture 18. 03.26 Transaction Memory
## Last Time
* PGAS
    * X10
        * async
        * finish
* Task model
    * feed to a thread pool
* Task dependency
    * DAG
    * send tasks with no predecessors to threads, once finish, we delete the task.
    * Which tasks we give to threads matter schedule is important
    * Greedy scheduler
        * choose avaliable tasks without any look ahead.
    T1 -> time to execute all nodes (seq.)
    T(infinity) -> infinity thread time
    * The length of the longest path(critical path) is a lower bound on T(infinity)
    * 

## Greedy Schedule performance
* In any particular step, we have two possible outcome
    * complete step -> more tasks avaliable than threads(choose subset of avaliable tasks)
    * incomplete step -> fewer tasks than threads

## Graham & Brent therom
* Let Tp the time with p threads
* Tp <= T1/p + T(infinity)
    * we connot have more than T1/p complete step
    * we connot have more than the length of the critical path
* Geedy scheduler is always within 2 * potimize schedule 是最优解的两倍
* easy to show 
    * optimal time for p is Tp*
    * Tp >= max(T1/p, T(infinity))
    * T1/p <= max(T1/p, T(infinity))
    * T(infinity) <= max(T1/p, T(infinity))
    * Tp <= T1/p + T(infinity) <= 2 * max(T1/p, T(infinity))  <= 2 * Tp*

## Transcational Memory（事务内存）
* Locking is hard to do
* main Goal -> mutual exclusion
* much easier
    * atomic -> ensure atomicity -> no explicit locks
* *transcation*  是单个线程所执行的一系列操作步骤
* in transcation programming    
    * atomic -> act like transcation
    * How do we do this?
        * pessimistic version
        * optimistic version

### pessimistic version
* 
    ```java
        atomic {        synchronized(global) {
            //     =>       //
        }               }
    ```
    * make all atomic blocks mutually exclusive
    * weakness
        * eg.
         ```java
                atomic {
                    x = 1;
                    b = y;
                    c = 3;
                }
                
                atomic {
                    x = 2;
                    a = x;
                    c = y;
                }
         ```
        * use a single global lock(assume no data-race)
            * a single global lock does not allow thread to be done in parallel
            * more fine-grain
                * find all data accessed in an atomic block
                *
                ```
                atomic {        Arw u.lock()
                    A;      =>      A;
                }               Arw u.unlock()
                ```
                * this way independent atomic section can be done in parallel
                    * some issue
                        * find Arw
                            ```java
                            atomic {
                                while (p != null) {
                                    p.data = 1;
                                    p = p.next;
                                }
                            }
                            ```
                        * data accessed among not be obvious ahead of time, can also turn into a lot of locking & unlocking
                    * need to avoid dead lock
                        * tryLock -> exception
                        * data
### optimistic version
* avoid locking altogether, rely on contention being race(usually)
* no locking overhead
* still need correct execution
    * execute optimistically (with no locking)
    * need to detect if any data is modified while we execute our transcation
        * eg.
        ```Java
        atomic {
            x = 1;
            y = x;
            z = x + y;
        }
        ```
         * if any Reads or Write x, y, z -> not atomic
         * we need to fix it
            * we could keep an undo-log
            * undo any partial execution and our atomic section & retry section
            * if all data inside data is untouched effectively atomic
               * better if we can avoid visibility of our transcational writes until the transcations succed
               * use isolation
                  * each atomic section, 
                     * read buffer(know what we read)
                     * write buffer(we don't write into main memory within a transcation)
                     * no-conflit
                        * end of the transcations, commit the write buffer
                     * conflict
                        * throw away and restart
## Language Issue
* nested transcation
* atomic solves atomically/ ME
    * wait/ notify
    * allow you to retry
                
                
