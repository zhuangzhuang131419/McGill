## Lecture21. 04.04 Dataflow

* Last Time
    * C.S.P.
        * Synchronized communication
        * process
        * Guarded choice
        * External choice
        * Communication via buffers
            * Via ‘channels’

* Dataflow
    * static & dynamic
    * Functional programming
        * Functions are side-effect free
        * e.g.:  h(f(x, y), g(x, z), y)
        * 
        * Suggests a strategy
            * Data flows along the arrows(tokens)
            * Process of firing(fire):
                * Our functions wait for data tokens on their input channels
                * Then compute something
                * Emit data tokens on output channels
    * static dataflow
        * Functions as ‘actors’
        * Channels(directed) that connect actors&along which data flows.
        * Channels are FIFO and infinite capacity
        * Homogeneous actors
            * Fire:
                * Consume exactly 1 token on each input and emit one token on each output.
        * Regular actors
            * Fire:
                * Input lines I1, I2, …, fixed number of tokens for each Ii that are consumed.
                * Output lines, O1, O2, …, fixed number of tokens for each Oi that are emitted.
        * For static overflow, restricted to regular actors.
     
        * (T/F, control, data)
        * This is not regular, always consumes 1 token from each input, but does not emit the same number of tokens on output lines.
     
        * (bool, control, T, F, merge)
        * This is not regular as well, and the figure shown cannot fire at all since control input is F and there is no data in F branch.
        * static dataflow = regular actors + switch + merge
        * More in if-schema
            * if(…) f(x);
            * else  g(x);
     
            * (…: the boolean input shown above, x, switch, T, F, merge)
            * e.g.: a loop up to 10
            * 
            * This network is reusable.
            * Compute iterations of a function
                * i.e.: we want f^10(x)
                * (merge, merge, switch)
            * Can we bind the capacity of our channels and still allow the network to execute.
            * We can figure capacities:
                * Only regular actors(*)
                * (Obstructed schemata)
                * E.g.: 
                        
                    * What capacity do we need for each channel?
                        * Every time a fires -> 2 tokens along line(iii)
                        * Every time b fires -> consume 3 tokens on line(iii)
                    * Equation for each line
                        * (iii). 2a = 3b
                        * (i).    a = c
                        * (ii).  3b = 2c
                        * (iv).  3b = 2a
                    * Different solution
                        * Trivial solution
                            * a = b = c = 0
                        * At least one non-trivial solution
                            * b = 2, a = c = 3
                            * We can have multiples solutions like this





















