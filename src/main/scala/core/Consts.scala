package core

import chisel3._

object Consts {
    val WORD_LEN    = 16
    val BYTE_LEN    = 8
    val NUM_GP_REGS = 8
    val START_ADDR  = 0x0000

    // opcodes
    val OP_ADD   = 0x00
    val OP_ADDI  = 0x01
    val OP_SUB   = 0x02
    val OP_AND   = 0x03
    val OP_ANDI  = 0x04
    val OP_OR    = 0x05
    val OP_ORI   = 0x06
    val OP_XOR   = 0x07
    val OP_XORI  = 0x08
    val OP_SLL   = 0x09
    val OP_SLLI  = 0x0a
    val OP_SRL   = 0x0b
    val OP_SRLI  = 0x0c
    val OP_SRA   = 0x0d
    val OP_SRAI  = 0x0e
    val OP_SLT   = 0x0f
    val OP_SLTI  = 0x10
    val OP_SLTU  = 0x11
    val OP_SLTIU = 0x12
    val OP_LB    = 0x13
    val OP_LBU   = 0x14
    val OP_LH    = 0x15
    val OP_SB    = 0x16
    val OP_SH    = 0x17
    val OP_JMP   = 0x18
    val OP_JMPR  = 0x19
    val OP_BEQ   = 0x1a
    val OP_BNE   = 0x1b
    val OP_BLT   = 0x1c
    val OP_BGE   = 0x1d
    val OP_BLTU  = 0x1e
    val OP_BGEU  = 0x1f
}
