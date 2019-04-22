# Lecture 17. 03.21 Task Models
## PGAS
* partitioned global address space(PGAS) is a parallel programming model
* It assumes a global memory address space that is logically partitioned and a portion of it is local to each process, thread, or processing element.

* Various parallel models
   * Category
        * Chapel
        * Titanium
        * UPC++
        * Shared memory
        * Message passing
        * SIMD/SPSMD
            * Data parallel
    * Partitioned Global Address Space
        * Locality of computation is important
### Basic mechanism
* async
* a void explicit thread, a async specifies a statement can be execute asynchronous
* Nested asyncs 
* ![asyncGraph](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/asyncGraph.png)
```Java
x = 0;
async {
    x = 1;
    async {
        x = 2;
    }
    x = 3;
    async {
        x = 4;
    }
    x = 5;
}
x = 6;


for (int i = 0; i < 100; i++) {
    async {
        a[i] = a[i] + 1;
    }
    // any of our updates may or may not be done 不一定已经完成
}
```
* finish
    * finish S<sub>i</sub> 
        * all asyncs in S<sub>i</sub> must be finished, we cannot go beyond this
```java
finish {
    for (int i = 0; i < 100; i++) {
        async {
            a[i] = a[i] + 1;
        }
    }
}
```
## Place / Location
* location
    * data and our asyncs live in different logged space
    * send computation to the data
    * data in the same area, more efficient
---
## Tasks
* rather than specifically allocating work to thread & coordinate the threads
* eg. make multiply 矩阵乘法
    * create N * M tasks, one each to compute each cell
    * create a pool of tasks && use some #threads to execute
        * Whenever a thread is done, grab another task
        * Creating a "thread pool"
            * some #id threads
            * give flush

---
在Java中，线程池被称作执行者服务(ExecutorService)。它为我们提供了提交任务、等待已提交任务完成以及撤销未完成任务的能力。
没有返回值的task被表示成Runnable对象，由run()来实现。
返回值类型为T的任务则被表示为Callable<T>对象，由call()来实现
* Java gives you various thread pool construct
    * Executor
        * lanuch a new Thread to execute a task  
    * DirectExecutor
        * single thread execution, pass a Runnable(more complex)
    * AsyncExecutor
        * creates a new thread to execute a Runnable

## ExecutorService
* Callable interface instead of Runnable, return data with an Executor Service
* Pass in (submit) a Callable and execute it.

##ThreadPoolExecutor
* min/max #threads
* passed in jobs 
* fixed #threads

```Java
c = new Runnable();
Future s = ex.submit(c); 
// how to get the return value without blocking
.get()

Future f = new Future(task); // task may or may not execute in parallel
f.get(); // block until all results are ready

public class Future {
    Callable c;
    public Future (Callable c) {
        this.c = c;
    }
    
    public object get() {
        return c.call();
    }
}
```

## DAG 无环有向图
* 见iPad图
* Performance depends on the order in which we choose text
* Need a test schedule
* Criticla Path: longest chain of dependencies

