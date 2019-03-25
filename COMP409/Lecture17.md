## PGAS

* Various parallel models
    * Shared memory
    * Message passing
    * SIMD/SPSMD
        * Data parallel
    * Partitioned Global Address Space
        * Locality of computation is important

## Titanium
* Based on Java: compiler &rarr; C &rarr; executable
* spmd language - processes execute the same code
* To synchronize, add a barrier
    * All threads must reach barrier before any proceed
    * Problematic if we create barriers on many threads and none on others. Some threads may end up stuck indefinitely.
        * Titanium has an analysis tool
            * Single value data - x expr is a constant or function of only single-valued data
        * Barrier must (only) be reachable through single-valued computations/tests
        
XIO
* PGAS
* APGAS - asynchronous

Basic mechanism:

```java
async {
    ... content
}
```

No guarantee as to when async is done, if at all

```java
finish {
    ... content
}
```

All async's inside the finish block (including nested async's) must be done before this continues

Async indicates that a new thread may be created for the code to run. Often times, the operations may be small, and making a new thread would be extremely inefficient. Some optimizations will be done to see when it is worth making new threads.

Java has executor mechanism:

```java
execute(Runnable r)
```

Given runnables take in nothing and output nothing, they are not always enough. There also exists `Callable<V>` which returns `V` and allows exception throwing

ExecutorServie gives different ways of executing. ThreadPoolExecutor allows for specifications for pools of threads