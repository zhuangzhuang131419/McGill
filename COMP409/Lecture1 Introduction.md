# Lecture 1. 01.08 Introduction

## Parallelism vs. Concurrency
* Parallelism（并行）
    * Multiple processors &rarr; 单线程永远无法达到并行状态。
    * User initialize process
    * Referee
        * set the rule &mdash; process act otherwise independently.
    * Control the ways things happen at the same time.
* Concurrency（并发）
    * Multiple processors &rarr; 单核单线程能支持并发。
    * You give direction (人为设计的结构)
    * Two tasks can start, run, and complete in overlapping time periods
    * Coach
    * Define constraints on execution, which is influenced by other aspects such as the OS.
    * Performance
        * schecduling
        * load-belonging
* Parallelism vs. Concurrency
    * Different concurrent designs enable different ways to  parallelize.
    * 并发设计让并发执行成为可能，而并行是并发执行的一种模式
    * Parallelism指物理上同时执行，Concurrency指能够让多个任务在逻辑上交织执行的程序设计

## Process vs. Thread
* Multiprocessing &mdash; large heavyweight with its own address space and handles
* Multithreading &mdash; lightweight with shared address space
    * Efficiently switched &mdash; changing threads may just involve changing some pointers, as the rest of the space may be the same

* Asynchronous Execution &mdash; threads execute at their own rate (dictated by OS)
    * Synchronisation
    * Resource Consistency
    * Visibility
    * Fairness

## Threads
* Light weight &rarr; switch efficiently
* Executes on a CPU
* An independent flow of control within a process, composed of a context (register and process context) && a sequence of instructions
* OS Part
    * Thread ID
    * Schedule policy
    * Priority
    * Signed mask
    * For this course, wew will assume little or no control (black box)
* User Part
    * Register set
    * Stack pointer (separate stacks)
    * PC
    * Shared memory
    * 
* Amdahl's Law
    * Total time is based on two pieces: sequential part(s) + parallel part(p)
    * Parallel part can be distributed among the n threads: t = s + p / n
    * Speedup: 1/(1-p+p/n)
        * Linear speed up &rarr; ideal but not usually seen
        * 一般的speed-up最后可能会下降

* Threads are good for 
    * Hiding latency (cache miss, pipeline stalls)
    * Switching context efficiently
    * Keeping CPU busy
    * Increasing responsiveness
        * Eg. long running execution + GUI thread for listening for inputs & interrupts

* Appropriate Parallelism
    * Web server &mdash; requests are naturally parallel

* Threads are not good for 
    * Overhead
    * May be difficult to debug

* Is concurrency fundamentally different?
    * We may reason that with Turing Machines, n TMs does not give any more power than 1 TM. However, with parallel execution, there are some subtle differences.
    * Example 1 &mdash; consider the following conversions & constraints:
        **a &rarr; ab** 
        **b &rarr; ab**
        There cannot be any sequence of `aa` or `bb`.

        If we started with `ab`, we would not be able to make any conversions.
        However, if we did both conversions in parallel and ignore the middle transition, we will be able to transition to `abab`
    * Example 2 &mdash; consider 5 items spaced evenly in a circle, with equal distance from the center point. Our goal is to rotate them all at the same time. If we were to do it sequentially, there will be a speed for which an item will cross over another. However, if we were to do it all in parallel, which would not be an issue.
