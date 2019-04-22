# Lecture 16. 03.19 OpenMP && Other Programming Model
### Lock Free
* Lock Free algorithms are problematic in the way that references from deallocated objects may be resued unknowingly

* Universal construction - almost any data structure can be made into a lock free version
* Assume your data structure has a single entry point
* Assume data stracture has some interface. We can make another interface that wraps the original to ensure that the invocations are sequential
* 
```java
public interface seqObject() {
    public Response apply(Invocation i);
}
// eg. In a stack, our invocation object would specify push(T data), pop()
//      Response -> data returned from a pop operation
```
* Invoc’s -> modifies the Data Structure
    * Record these as a log of data our Data Structure. changed from its initial state.
    * ![Invocation](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Invocation.png)
    * Need threads to agree on the order of operations in the log &rarr; consensus. So all threads agree on which operation is next.
```java
Thread t: 
    i = new Invocation(...);
    do {
        j = consensus(i);
    } while (i != j)
    // we are now the next operation
    s = tail of the history
    r = tail.state // initial state
    do {
        r = r.apply(s)  
        // without modifying data, check the previous ops to update the state
        s = s.next;
    } while (s != i)
    return r
```

One concern is that our consensus algorithm is one shot and not reusable. However, knowing that we can construct a new consensus object with each invocation, this is not an issue.

## Open MP
* Overlay language (C/C++, Fortran)
* Not a language, but rather a set of directives (structured comments: #pragma) on top of an existing language that makes parallelism easy
    * form of structual paralism
    * basic fork/join
* Define parallel region 
    * Create a team of threads(a master, rest), execute the next statement in parallel, they join sequentially.
    * #of threads -> can control. Can also be selected by the runtime system.

```c
// next block of statement will be executed in parallel
    #pragma omp parallel 
    {
        // ...
    }
```
* Working-sharing 
    * Divide up a for loop among the thread.
    * #pragma omp for
        * Divide the for-loop among the threads.
        * Assume/Restrict to ‘nice’ for-loop.
    * eg.
    ```c
    #programa omp parallel
    #programa omp for
    for (int i = 0; i < 100; i++) {
        A[i] = i;
    }
    
    #programa omp parallel sections
    #programa omp parallel section
    // ...
    // ...
    #programa omp parallel section
    // ...
    // ...
    
    #programa omp single
    // ...   only one thread execute
    
    #programa omp master
    // ..    only the master thread execute
    ```
 
    
---
| | |
| --- | --- |
`#pragma omp parallel` | For single statements
`#pragma omp for` | For loops; will be partitioned amongst the thread team
`#pragma omp sections` | Another way of partitioning work
`#pragma omp single` | Part within section that should only be executed by one thread
`#pragme omp master` | Part that must be executed by a specified thread (master)

```c
#include <omp.h>
#include <stdio.h>

/* A simple example of using a few of the main constructs in openmp.
 * To compile this (on linux):
 *   gcc -o openmptest -fopenmp openmptest.c
 */
int main(int argc,char *argv[]) {
    int i;
    int t=8,n = 10;
    if (argc>1) {
        n = atoi(argv[1]);
        printf("Using %d iterations\n",n);
        if (argc>2) {
            t = atoi(argv[2]);
            printf("Using %d threads\n",t);
        }
    }
    omp_set_num_threads(t);

    /* A parallel for loop, iterations divided amongst the threads */
#pragma omp parallel for
    for (i=0;i<n;i++) {
        printf("Iteration %d done by thread %d\n",
               i,
               omp_get_thread_num());
    }

    /* A parallel code, executed by all threads */
#pragma omp parallel
    printf("Hello from thread %d, nthreads %d\n",
           omp_get_thread_num(),
           omp_get_num_threads());

    /* Two parallel sections doing different work */
#pragma omp parallel
#pragma omp sections
    {
#pragma omp section
        { 
            int j,k=0;
            for (j=0;j<100000;j++)
                k+=j;
            printf("Section 1 from thread %d, nthreads %d, val=%d\n",
                   omp_get_thread_num(),
                   omp_get_num_threads(),k);
        }
#pragma omp section
        { 
            int j,k=0;
            for (j=0;j<100000;j++)
                k+=j;
            printf("Section 2 from thread %d, nthreads %d, val=%d\n",
                   omp_get_thread_num(),
                   omp_get_num_threads(),k);
        }
    }
}

output:
Iteration 2 done by thread 1
Iteration 6 done by thread 4
Iteration 3 done by thread 1
Iteration 5 done by thread 3
Iteration 7 done by thread 5
Iteration 8 done by thread 6
Iteration 9 done by thread 7
Iteration 0 done by thread 0
Iteration 1 done by thread 0
Iteration 4 done by thread 2
Hello from thread 4, nthreads 8
Hello from thread 3, nthreads 8
Hello from thread 5, nthreads 8
Hello from thread 7, nthreads 8
Hello from thread 1, nthreads 8
Hello from thread 6, nthreads 8
Hello from thread 2, nthreads 8
Hello from thread 0, nthreads 8
Section 1 from thread 4, nthreads 8, val=704982704
Section 2 from thread 5, nthreads 8, val=704982704
```

## Data Model

Threads share static, heap data

* `shared(x, y, z)`
* `private(x, y, z)` - each thread has its own copy; uninitialized, not persistent
* `threadPrivate(x, y, z)` - like private, but persistent & presumably initialized
* `firstPrivate` - var is initialized from the present scope
* `lastPrivate` - value is given back to the parent
* `reduction(opList)` - opList can be + (init at 0) or * (init at 1)

---
Synchronization
* single & master critical
* barrier
* atomic operation
    * Read: =x
    * write: x =
    * Update: x++
    * Capture: =x++
* flush - mian maechanism for synchronzing cache & memory
    * commits any pending writes
    * invalidates cached copies
    * rules
        * if intersection of 2 flush sets is non-empty, flushes must be seen in same order by everyone
        * if thread reads, writes, modifies in flush set, program order is respected
        * atomic ops have implicit flush of the vars involved
    * Memory model
        * weak model (见iPad示意图)
        * flush(x, y, z)
            * Send x, y and z back to main memory(no longer in our working memory)
            * If 2 flush operations has intersecting sets of variables, then they must be seen in the same order.
            * If no intersection, no guarantee.
            * Within a single thread, flush(x) —> = x is an order too.(flush and the use of a variable.) or x = , flush(x) (use first, then flush)
            * Atomic -> implies an immediate flush of the var involved.
        * 
        ```Java
        flag0 = flag1 = 1;
        flag0 = 1;
        if (flag1 == 0) {
            enter C.S
        }
        ```
        * 
        ```Java
        T0:                         T1:
        atomic(flag0 = 1);          atomic(flag1 = 1);
        flush(flag0);               flush(flag1);
        
        flush(flag1);               flush(flag0);
        atomic(tmp = false);        atomic(tmp = false);
        if (tmp == 0) {             if (tmp == 0) {
            enter C.S.                  enter C.S.
        }                           }
        ```

 
