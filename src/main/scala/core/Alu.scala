package core

import chisel3._
import chisel3.util._
import core.Consts._

object AluOpcode extends ChiselEnum {
    val Add  = Value
    val Sub  = Value
    val And  = Value
    val Or   = Value
    val Xor  = Value
    val Sll  = Value
    val Srl  = Value
    val Sra  = Value
    val Slt  = Value
    val Sltu = Value
}

class Alu extends Module {
    val io = IO(new Bundle {
        val a   = Input(UInt(WORD_LEN.W))
        val b   = Input(UInt(WORD_LEN.W))
        val op  = Input(AluOpcode.Type())
        val out = Output(UInt(WORD_LEN.W))
    })

    io.out := 0.U(WORD_LEN.W)

    switch(io.op) {
        is(AluOpcode.Add) { io.out := (io.a.asSInt + io.b.asSInt).asUInt }
        is(AluOpcode.Sub) { io.out := (io.a.asSInt - io.b.asSInt).asUInt }
        is(AluOpcode.And) { io.out := io.a & io.b }
        is(AluOpcode.Or) { io.out := io.a | io.b }
        is(AluOpcode.Xor) { io.out := io.a ^ io.b }
        is(AluOpcode.Sll) { io.out := io.a << io.b(4, 0) }
        is(AluOpcode.Srl) { io.out := io.a >> io.b(4, 0) }
        is(AluOpcode.Sra) { io.out := (io.a.asSInt >> io.b(4, 0)).asUInt }
        is(AluOpcode.Slt) { io.out := (io.a.asSInt < io.b.asSInt).asUInt }
        is(AluOpcode.Sltu) { io.out := (io.a < io.b).asUInt }
    }
}
