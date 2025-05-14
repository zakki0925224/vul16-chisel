package core

import chisel3._

object Consts {
    val WORD_LEN    = 16
    val BYTE_LEN    = 8
    val NUM_GP_REGS = 8
    val MEM_SIZE    = 16384
    val START_ADDR  = 0x0000

    val MEM_INIT: Seq[Int] = Seq(
        0x00, 0x00, 0x00, 0xf8, 0x00, // exit
        0x00
    )

    // opcodes
    val OP_ADD   = 0x00
    val OP_ADDI  = 0x01
    val OP_SUB   = 0x02
    val OP_SUBI  = 0x03
    val OP_AND   = 0x04
    val OP_ANDI  = 0x05
    val OP_OR    = 0x06
    val OP_ORI   = 0x07
    val OP_XOR   = 0x08
    val OP_XORI  = 0x09
    val OP_SLL   = 0x0a
    val OP_SLLI  = 0x0b
    val OP_SRL   = 0x0c
    val OP_SRLI  = 0x0d
    val OP_SRA   = 0x0e
    val OP_SRAI  = 0x0f
    val OP_SLT   = 0x10
    val OP_SLTI  = 0x11
    val OP_SLTU  = 0x12
    val OP_SLTIU = 0x13
    val OP_EXIT  = 0x1f // temporary
}
