# Lecture 20. 2018/04/02
### Two Basic Operations
* P || Q parallel composition
    * eg. P::Q!3||Q::P?3  P给Q发送，Q从P接受
* P; Q sequential composition
    * eg. a single-cell buffer  `A` -> `C` -> `B`
        1) A::C!x
            B::C?x
        2) A::C!x
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

### External Choice
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
        * in many C.S.P (), we use recursion instead
        * `V`::*inslot? $1.00 &rArr; make coffe;`V` []
         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inslot? $1.00 &rArr; make coffe;`V`
            * V || inslot::V!$1.00
        * A::B?x -> Q1 []
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;B?x -> Q2
            * external choice -> choice is driven by the external environment
            * A||B::A!3
                * if the choice are the same, we get non-deterministic result

### Internal Choice
* g1->C1 (internal mark)
* g2->C2 (internal mark)
* g3->C3 (internal mark)
* with internal choice A  B, our process can commit to one of the option anytime, irrespective of its outer context
* eg.
    * V::inslot?$1.00->coffee[]
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inslot?$2.00->tea
    * V'||inslot::V!$1.00;x => coffee||x
        * might get stuck
    * V||inslot::V!$2.00;x => tea||x
    * V'::inslot?$1.00->coffee (internal mark)
        inslot?$2.00->tea
---

## Multi Buffer
### Ordered Buffer
* `Producer` -> `Buffer` -> `Consumer`
    * P:B!produce() -> P
    * C:B?x;consume(x);c
    * B:P?x->C!x;B
    * P->B1->B2->C
    * P::B1!produce();P
    * C::B2?x;consume(x);C
    * B1::P?x->B2!x;B1
    * B2::B1?x->C!x;B2
* This gives an ordered buffer

### Unordered Buffer
* 见iPad图
* B.entry::P?x;
* B.entry::B1!x -> B.entry[]
* B.entry::B2!x -> B.entry[]
* B.entry::B3!x -> B.entry[]
* B1::B.entry?x -> B.x!x; B1
---
CSP was the basis for OCCAM, common is between processor via channels

## Java CSP
* Indentity
    * 见iPad图
    * Identity(in, out)::In?x->Out!x -> Identity(in, out)
* Increment
    * 见iPad图
    * Inc(in, out)::identity?x->Out!(x + 1) -> Inc(in, out)
* Add
    * 见iPad图
    * +(in1, in2, out)::in1?x -> in2?y -> out!(x + y)
    * +(in1, in2, out)||P::in1!3 -> in2?7 -> 
    +(in1, in2, out)||P::in2!7 -> in1!3 ->
    +(in1, in2, out)::
        in1?x -> in2?y -> out!(x + y)[]
        in2?y -> in1?x -> out!(x + y)
* Prefix
    * 见iPad图
    * Prefix(in, out)::out!&emptyset->Identity(in, out)
* 应用











