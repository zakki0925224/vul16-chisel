package core

import chisel3._

object Consts {
    val WORD_LEN    = 16
    val NUM_GP_REGS = 8
    val MEM_SIZE    = 16384
    val START_ADDR  = 0x0000

    val MEM_INIT: Seq[Int] = Seq(
        0x00,
        0x00,
        0x00,
        0x00
    )

    // opcodes
    val OP_ADD  = 0x00
    val OP_ADDI = 0x01
    val OP_SUB  = 0x02
    val OP_SUBI = 0x03
    val OP_EXIT = 0x1f // temporary
}
