# Lecture 22. 04.09 Dataflow
* Last time
    * P -> Q (P send data to Q)
    * Dataflow
        * Static dataflow
            * Regular actors(does exactly same thing every time. Fixed number of inputs and fixed number of outputs)
            * Switch & merge(irregular)
            * Schema
            * Capacity for each channel
            * Equations for channel
                * P outputs 3 items, Q takes 2 items ==> 3P = 2Q, then we need 2 P and 3 Q
                * We need at least 1 nontrivial solution
                * F(1 in 2 out) -> P(2 in 1 out) -> D(1 in 2 out)
                * F = P, 2D = F, P = D ==> F = P = D = 2D (No nontrivial solution)
        * Dynamic dataflow
            * Actors are not limited to being regular. They can do all sorts of things.
            * What do we compute?
                * What can actors do?
                * Partial sums: 
                * First give it a 1, output 1
                * Another 1, output 2
                * Another 1, output 3
                * ...
                * Not I/O mapping(same input, different output)
                * This is not a function in terms of individual tokens.
                * The actor: if it receives a sequence of 1s, it emits the N
                * Partial sum: 1^N -> N, it is a function in terms of streams.
            * ![CDR](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Lecture22/CDR.png)
            * CDR discards its first input and then acts as the identity.（删去第一个输入，后面不变） 
            * I.e.: we get the Fibonacci sequence. 
            * To figure this out, we flowed data around and observe the stream on each channel.
    * Some minor domains
        * Partial order
            * Order between some(but not necessarily all) elements
            * LUB, GLB:
                * Least Upper Bound: a LUB of two elements is the smallest element greater than both of them.
                    * eg. Subsets, ordered by ⊆
                    * { a } ⊆ {a, b}
                    * { a } ⊄ { b }
                    * { b } ⊄ { a }
                    * LUB looks like U(Union)
                    * { a } LUB { b } = {a, b}
                * Greatest Lower Bound: Largest element smaller than both
                * { a } GLB {a, b} -> { a }, { }
            * A complete partial order(CPO) is a partially ordered set that has a LUB for all increasing sequences {  } ⊆ { a } ⊆ { a, b } (even for infinite sequence)
            * N.B. CPO’s often have a ⊥: ‘bottom’(a least element, like {  } above)
            * We are interested in streams
                * We can organize our streams as CPOs
                * ⊥(bottom): empty stream
                * s1 ⊆ s2 (s1 is a prefix of s2)
                * End of stream marker ·
                * ![streamMarker](https://raw.githubusercontent.com/zhuangzhuang131419/McGill/master/COMP409/Lecture22/streamMarker.png)
                * Out actors compute functions in our stream domain
            * Monotonic function
                * Order preserving
                * x <= y
                * f(x) <= f(y)
                * Note: not the same as ‘Monotonically increasing’ 
                    * eg. Z <= f(x) = x - 1 is a monotonic function
                    * 2 <= 1003
                    * f(2) <= f(1003)
                    * 1 <= 1002
                    * But it is not the case that x <= f(x)
            * A continuous function f is one that preserve LUBs for all increasing sequences
                * f(a) U f(b) = f(a U b)
                * More generally, U{ f(a), f(b), f(c)... } = f(U{ a, b, c... })
                * N.B. continuous => monotonic but monotonic !=> continuous
                * An actor f: x => f(x), x <= y -> f(x) <= f(y) 
                    * We use continuous(also monotonic functions as actors)
                    * Network of continuous functions
                    * Theorem: Kahn’s principle
                        * A network of continuous functions is described by a system of (recursive) equation, the least solution of which describes the temporal history of the network.
                        * Kahn’s principle states that if each process in a dataflow network computes a continuous input/output function, then so does the entire network.
            * 示意图
            * Static
                * Regular functions
            * Dynamic
                * Continuous function in domain of stream

    * Glitches
        * When 2 things happen at the same time, which is first?
        * 14th century, Jean Buridan.
        * We have 2 piles of hay, and a donkey in between, which have the exactly same distance between it and these two piles of hays. Which one will it go to first?
        * Buridan’s principle
            * For any device, making a decision among a finite number of possible outcomes based on a continuous of possible inputs, there will be inputs for which the device takes arbitrarily long.
