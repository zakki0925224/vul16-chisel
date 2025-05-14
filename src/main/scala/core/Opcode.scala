package core

import chisel3._
import chisel3.util._
import core.Consts._

object Opcode extends ChiselEnum {
    val Add     = Value
    val Addi    = Value
    val Sub     = Value
    val Subi    = Value
    val And     = Value
    val Andi    = Value
    val Or      = Value
    val Ori     = Value
    val Xor     = Value
    val Xori    = Value
    val Sll     = Value
    val Slli    = Value
    val Srl     = Value
    val Srli    = Value
    val Sra     = Value
    val Srai    = Value
    val Slt     = Value
    val Slti    = Value
    val Sltu    = Value
    val Sltiu   = Value
    val Lb      = Value
    val Lbu     = Value
    val Lh      = Value
    val Lhu     = Value
    val Sb      = Value
    val Sh      = Value
    val Exit    = Value
    val Invalid = Value
}
