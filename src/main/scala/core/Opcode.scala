package core

import chisel3._
import chisel3.util._
import core.Consts._

object FormatType extends ChiselEnum {
    val R = Value
    val I = Value
    val J = Value
    val B = Value
}

object Opcode extends ChiselEnum {
    val Add     = Value(OP_ADD.U)
    val Addi    = Value(OP_ADDI.U)
    val Sub     = Value(OP_SUB.U)
    val And     = Value(OP_AND.U)
    val Andi    = Value(OP_ANDI.U)
    val Or      = Value(OP_OR.U)
    val Ori     = Value(OP_ORI.U)
    val Xor     = Value(OP_XOR.U)
    val Xori    = Value(OP_XORI.U)
    val Sll     = Value(OP_SLL.U)
    val Slli    = Value(OP_SLLI.U)
    val Srl     = Value(OP_SRL.U)
    val Srli    = Value(OP_SRLI.U)
    val Sra     = Value(OP_SRA.U)
    val Srai    = Value(OP_SRAI.U)
    val Slt     = Value(OP_SLT.U)
    val Slti    = Value(OP_SLTI.U)
    val Sltu    = Value(OP_SLTU.U)
    val Sltiu   = Value(OP_SLTIU.U)
    val Lb      = Value(OP_LB.U)
    val Lbu     = Value(OP_LBU.U)
    val Lh      = Value(OP_LH.U)
    val Sb      = Value(OP_SB.U)
    val Sh      = Value(OP_SH.U)
    val Jmp     = Value(OP_JMP.U)
    val Jmpr    = Value(OP_JMPR.U)
    val Beq     = Value(OP_BEQ.U)
    val Bne     = Value(OP_BNE.U)
    val Blt     = Value(OP_BLT.U)
    val Bge     = Value(OP_BGE.U)
    val Bltu    = Value(OP_BLTU.U)
    val Bgeu    = Value(OP_BGEU.U)
    val Invalid = Value

    def fromInst(inst: UInt): Opcode.Type = {
        val op      = inst(15, 11)
        val decoded = WireDefault(Invalid)

        switch(op) {
            is(OP_ADD.U(5.W)) { decoded := Add }
            is(OP_ADDI.U(5.W)) { decoded := Addi }
            is(OP_SUB.U(5.W)) { decoded := Sub }
            is(OP_AND.U(5.W)) { decoded := And }
            is(OP_ANDI.U(5.W)) { decoded := Andi }
            is(OP_OR.U(5.W)) { decoded := Or }
            is(OP_ORI.U(5.W)) { decoded := Ori }
            is(OP_XOR.U(5.W)) { decoded := Xor }
            is(OP_XORI.U(5.W)) { decoded := Xori }
            is(OP_SLL.U(5.W)) { decoded := Sll }
            is(OP_SLLI.U(5.W)) { decoded := Slli }
            is(OP_SRL.U(5.W)) { decoded := Srl }
            is(OP_SRLI.U(5.W)) { decoded := Srli }
            is(OP_SRA.U(5.W)) { decoded := Sra }
            is(OP_SRAI.U(5.W)) { decoded := Srai }
            is(OP_SLT.U(5.W)) { decoded := Slt }
            is(OP_SLTI.U(5.W)) { decoded := Slti }
            is(OP_SLTU.U(5.W)) { decoded := Sltu }
            is(OP_SLTIU.U(5.W)) { decoded := Sltiu }
            is(OP_LB.U(5.W)) { decoded := Lb }
            is(OP_LBU.U(5.W)) { decoded := Lbu }
            is(OP_LH.U(5.W)) { decoded := Lh }
            is(OP_SB.U(5.W)) { decoded := Sb }
            is(OP_SH.U(5.W)) { decoded := Sh }
            is(OP_JMP.U(5.W)) { decoded := Jmp }
            is(OP_JMPR.U(5.W)) { decoded := Jmpr }
            is(OP_BEQ.U(5.W)) { decoded := Beq }
            is(OP_BNE.U(5.W)) { decoded := Bne }
            is(OP_BLT.U(5.W)) { decoded := Blt }
            is(OP_BGE.U(5.W)) { decoded := Bge }
            is(OP_BLTU.U(5.W)) { decoded := Bltu }
            is(OP_BGEU.U(5.W)) { decoded := Bgeu }
        }

        decoded
    }

    def toFormatType(op: Opcode.Type): FormatType.Type = {
        val fmt = WireDefault(FormatType.R)

        switch(op) {
            is(Add) { fmt := FormatType.R }
            is(Sub) { fmt := FormatType.R }
            is(And) { fmt := FormatType.R }
            is(Or) { fmt := FormatType.R }
            is(Xor) { fmt := FormatType.R }
            is(Sll) { fmt := FormatType.R }
            is(Srl) { fmt := FormatType.R }
            is(Sra) { fmt := FormatType.R }
            is(Slt) { fmt := FormatType.R }
            is(Sltu) { fmt := FormatType.R }
            is(Addi) { fmt := FormatType.I }
            is(Andi) { fmt := FormatType.I }
            is(Ori) { fmt := FormatType.I }
            is(Xori) { fmt := FormatType.I }
            is(Slli) { fmt := FormatType.I }
            is(Srli) { fmt := FormatType.I }
            is(Srai) { fmt := FormatType.I }
            is(Slti) { fmt := FormatType.I }
            is(Sltiu) { fmt := FormatType.I }
            is(Lb) { fmt := FormatType.I }
            is(Lbu) { fmt := FormatType.I }
            is(Lh) { fmt := FormatType.I }
            is(Sb) { fmt := FormatType.I }
            is(Sh) { fmt := FormatType.I }
            is(Jmpr) { fmt := FormatType.I }
            is(Jmp) { fmt := FormatType.J }
            is(Beq) { fmt := FormatType.B }
            is(Bne) { fmt := FormatType.B }
            is(Blt) { fmt := FormatType.B }
            is(Bge) { fmt := FormatType.B }
            is(Bltu) { fmt := FormatType.B }
            is(Bgeu) { fmt := FormatType.B }
            is(Invalid) { fmt := FormatType.R }
        }
        fmt
    }
}
