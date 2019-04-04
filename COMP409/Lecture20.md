# Lecture 20. 2018/04/02
Last Time
* Process algebra
    * Message passing (synchronous)
    * &pi;-calculus - g1/g2 - builds on ealier PA called CCS
        * motivated by limitation in CCS
        * calculus of mobility - mobile communication - structure can change dynamically
        * Defined with 2 entities
            * Processes or agents
            * Channels or names
        * Very abstract - no "real" arithmetic/low level data
* ...
* Induction definition for processes
    * &emptyset; - process that does nothing (STOP, HALT)
    * P & Q are processes, then P | Q is a parallel composition
    * Choice operator, ie \&Sigma;<sub>i &in; I></sub> &pi;<sub>i</sub> P<sub>i</sub>
    * Send: \overline{x} y, where x is channel, y is what is sent
    * Receive: x(y), where x is channel, y is what the data will be bound to
    * Ex (x(y) e1 | \overline{x} z e2) reduces to (e1[y &rarr; z] | e2)
* Other constructs
    * !P - replication operator; as many copies of P as we want
    * (&nu; x) p - creates a new ("nu") fresh channel called x in p
* Reduction rules
    * PAR - processes can evolve at independent rates
    * RES
    * STDOUT
    * Equivalence (structural congruence) - (vx)&emptyset; &equiv; &emptyset;, &emptyset; | P &equiv; P | &emptyset; &equiv; P
    * Associative - (P | Q) | W &equiv; P | (Q | W) 

---

* &lambda;-calculus, seq progression
* (&lambda; x (&lambda; y M)) can be simplified to (&lambda; xy M)
* 


### Two Basic Operations
* P || Q parallel composition
    * eg. P::Q!3||Q::P?3  P给Q发送，Q从P接受
* P; Q sequential composition
    * eg. a single-cell buffer  `A` -> `C` -> `B`
        1) A::C!B
            B::C?x
        2) A::C!B
            C::A?y; B!y
            B::C?x

### Guards commands
* execute -> C
* Guard commands only execute if the event can happen, then we can let the event happen & continue with the command
* Guard commands are most useful when we combine with **choice**.
    * choice - if we want to choose one event or another to respond to
    * eg. 
        * A::B?x
        * A::C?x
        * A同时想receive from B 和 C

### External choice
* eg.为什么要用choice
    * A::B?x -> Q1
    * A::C?x -> Q2
    * C::A!7||A::B?x &rarr; Q1; C?y &rarr; Q2 
    * `stuck!!` A 不知道要接受哪一个
* eg. [] 在一个process加一个方块
    * A::B?x -> Q1 []
    * A::C?x -> Q2
    * A||B::A!3 &rArr; Q1 &rArr; Q1||&emptyset;
    * A||C::A!7 &rArr; Q2 &rArr; Q2||&emptyset;
* eg. 
    * Vending Machine
        * V::inslot? $1.00 &rArr; make coffe []
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inslot? $2.00 &rArr; make tea
    * Iteration
        * V::*[ inslot? $1.00 &rArr; make coffe []
         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inslot? $1.00 &rArr; make coffe ]
        * `*` means repetition






