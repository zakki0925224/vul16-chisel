package core

import chisel3._
import chisel3.util._
import core.Consts._

class Cpu extends Module {
    // (op, rd, rs1, rs2, imm)
    def decode(inst: UInt): (Opcode.Type, UInt, UInt, UInt, UInt) = {
        val op = inst(15, 11)

        // type R
        val rd  = inst(10, 8)
        val rs1 = inst(7, 5)
        val rs2 = inst(4, 2)

        // type I
        val imm = inst(4, 0)

        val decoded = WireDefault(Opcode.Invalid)
        switch(op) {
            is(OP_ADD.U(5.W)) { decoded := Opcode.Add }
            is(OP_ADDI.U(5.W)) { decoded := Opcode.Addi }
            is(OP_SUB.U(5.W)) { decoded := Opcode.Sub }
            is(OP_SUBI.U(5.W)) { decoded := Opcode.Subi }
            is(OP_AND.U(5.W)) { decoded := Opcode.And }
            is(OP_ANDI.U(5.W)) { decoded := Opcode.Andi }
            is(OP_OR.U(5.W)) { decoded := Opcode.Or }
            is(OP_ORI.U(5.W)) { decoded := Opcode.Ori }
            is(OP_XOR.U(5.W)) { decoded := Opcode.Xor }
            is(OP_XORI.U(5.W)) { decoded := Opcode.Xori }
            is(OP_SLL.U(5.W)) { decoded := Opcode.Sll }
            is(OP_SLLI.U(5.W)) { decoded := Opcode.Slli }
            is(OP_SRL.U(5.W)) { decoded := Opcode.Srl }
            is(OP_SRLI.U(5.W)) { decoded := Opcode.Srli }
            is(OP_SRA.U(5.W)) { decoded := Opcode.Sra }
            is(OP_SRAI.U(5.W)) { decoded := Opcode.Srai }
            is(OP_EXIT.U(5.W)) { decoded := Opcode.Exit }
        }

        (decoded, rd, rs1, rs2, imm)
    }

    val io = IO(new Bundle {
        val memDataAddr = Output(UInt(WORD_LEN.W))
        val memDataIn   = Output(UInt(BYTE_LEN.W))
        val memDataOut  = Input(UInt(BYTE_LEN.W))
        val memDataLoad = Output(Bool())

        val inst   = Input(UInt(WORD_LEN.W))
        val pc     = Output(UInt(WORD_LEN.W))
        val gpRegs = Output(Vec(NUM_GP_REGS, UInt(WORD_LEN.W)))
        val exit   = Output(Bool())
    })

    // TODO
    io.memDataAddr := 0.U(WORD_LEN.W)
    io.memDataIn   := 0.U(BYTE_LEN.W)
    io.memDataLoad := false.B

    // general purpose registers
    val gpRegs = VecInit(Seq.fill(NUM_GP_REGS)(Module(new Register()).io))
    for (i <- 0 until NUM_GP_REGS) {
        gpRegs(i).in   := gpRegs(i).out
        gpRegs(i).load := false.B
        io.gpRegs(i)   := gpRegs(i).out
    }

    // control status register
    // val csr = Module(new Register())

    // program counter
    // fetch
    val pc = Module(new Register(START_ADDR.U(WORD_LEN.W)))
    pc.io.in   := pc.io.out + 2.U(WORD_LEN.W)
    io.pc      := pc.io.out
    pc.io.load := true.B
    printf(cf"fetch: pc: 0x${pc.io.out}%x, ")

    // decode
    val inst = io.inst

    val (op, rd, rs1, rs2, imm) = decode(inst)
    io.exit := op === Opcode.Exit
    printf(cf"decode: inst: 0x$inst%x => op: $op\n")

    // execute
    val alu = Module(new Alu())
    alu.io.a  := 0.U
    alu.io.b  := 0.U
    alu.io.op := AluOpcode.Add

    gpRegs(rd).in   := 0.U
    gpRegs(rd).load := false.B

    switch(op) {
        is(Opcode.Add) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.Add
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: add: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Addi) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.Add
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: addi: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Sub) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.Sub
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: sub: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Subi) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.Sub
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: subi: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.And) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.And
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: and: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Andi) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.And
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: andi: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Or) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.Or
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: or: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Ori) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.Or
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: ori: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Xor) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.Xor
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: xor: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Xori) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.Xor
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: xori: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Sll) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.Sll
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: sll: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Slli) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.Sll
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: slli: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Srl) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.Srl
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: srl: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Srli) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.Srl
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: srli: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Sra) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := gpRegs(rs2).out
            alu.io.op       := AluOpcode.Sra
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: sra: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
        is(Opcode.Srai) {
            alu.io.a        := gpRegs(rs1).out
            alu.io.b        := imm
            alu.io.op       := AluOpcode.Sra
            gpRegs(rd).in   := alu.io.out
            gpRegs(rd).load := true.B
            printf(cf"execute: srai: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
        }
    }
}
