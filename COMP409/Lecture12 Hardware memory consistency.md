# Lecture 12. 02.21 Hardware memory consistency
Intel/AMD
* Memory management implied in the hardware manual
* White paper
    * Informal rules
    * "litmus" tests
* Some kind of casual consistency
* IRIW - independent read independent write
    * Probably don't want, but not ruled out

  P0 | P1 | P2 | P3 
  ---|---|---|---
  x = 1 | y = 1 | eax = x, ebx = y | ecx = y, edx = x

  For P2, it sees eax = 1, ebx = 0, meaning P0 happened before P1
  For P3, it sees ecx = 1, edx = 0, meaning P1 happened before P0

* Also cases that should not happen, but can be obsered in practice **n6**

  P0 | P1 
  --- | ---
  x = 1 | y = 2
  eax = x | x = 2
  ebx = y | |

  Could observe that eax = 1, ebx = 0, x = 1

* Could happen with write buffers
    * Order for which they are committed to main memory is undefined
    * 
So Intel fixed the documents
* Specified disallowed IRIW
* Added in MFENCE - memory fence to monitor read write access
* Same processor writes are ordered and observed in the same order by all others
* Constraints on inter-process ordering
    * Any 2 stores are seeing consistent ordering by processes other than those doing the write. Leaves opens another case n5
    * n5

      P0 | P1
      ---|---
      x = 1 | x = 2
      eax = x | ebx = x

      eax = 2, ebx = 1 (Here P0 sees x = 1, x = 2; P1 sees x = 2, x = 1)
      not disallowed, but also not observed
      
* x86-TSO - abstract model by academics
    * 见iPad示意图
    * Allow everything observed
    * Respect litmus tests
    * Every hardware thread has its own write buffer
        * All writes stored in order
        * Committed in order
        * `Has no overwrites`
    * Global lock that can be acquired by any hardware thread (with constraints)
    * MFENCE
        * All Write Bufer flushes must be done before
        * Write Buffer is FIFO
    * Some instructions
        * LOCK: prefix - thread must acquire global lock
            * While lock is held, no other thread may read
            * Buffered write can be committed to memory at any time except when another thread holds the lock
            * Before releasing lock, Write Bufer must be empty
            * A thread is _blocked_ if it does not hold the lock & someone else does
        * Set of (abstract) primitive operations
            * Rp[a] = v : `p` can read value `v` from address `a` if `p` is not blocked; there are no writes to `a` in WB<sub>p</sub> and mem[a] = v
            * Rp[a] = v : `p` can read `v` from address `a` if `p` is not blocked, and p has `a` (latest) store "a = v" in WB<sub>p</sub>
            * Wp[a] = v : `p` can write `a = v` into WB<sub>p</sub> at any time
            * &tau;p : if `p` is not blocked, it can silently send the oldest write in WB<sub>p</sub> to memory
            * Fp : if WB<sub>p</sub> is empty, `p` can issue an MFENCE instruction
            * Lp : if the lock is not held by another process, `p` can acquire it
            * Up : if `p` has the lock & WB<sub>p</sub> is empty, we can release the lock
        * progress condition : all buffered writes are eventually committed
        * Example

          | P: WB<sub>p</sub> = [0x55] = 0 | Q: WB<sub>q</sub> = [0x55] = 7 |
          |---|---|
          | Lock : Inc [0x55] | |
          | Lp | |
          | Rp[0x55] = 0 | |
          | Wp[0x55] = 1 | |
          | &tau;p (0x55 = 0) | |
          | &tau;p (0x55 = 1) | |
          | Up | &tau;q |

            * Without lock, p can begin increment, q can set to 7, and p can increment the new value by 1 &rarr; 8
