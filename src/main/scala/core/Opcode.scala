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
    val Exit    = Value
    val Invalid = Value
}
