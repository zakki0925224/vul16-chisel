package core

import chisel3._
import chisel3.util._
import core.Consts._

class Cpu extends Module {
    def decode(inst: UInt): Opcode.Type = {
        val op = inst(15, 11)

        // type R
        val rd  = inst(10, 8)
        val rs1 = inst(7, 5)
        val rs2 = inst(4, 2)

        // type I
        val rs  = inst(7, 5)
        val imm = inst(4, 0).asSInt

        val opcode = WireDefault(Opcode.Unknown)

        switch(op) {
            is(OP_ADD.U(5.W)) { opcode := Opcode.Add }
            is(OP_ADDI.U(5.W)) { opcode := Opcode.Addi }
            is(OP_SUB.U(5.W)) { opcode := Opcode.Sub }
            is(OP_SUBI.U(5.W)) { opcode := Opcode.Subi }
            is(OP_EXIT.U(5.W)) { opcode := Opcode.Exit }
        }

        opcode
    }

    val io = IO(new Bundle {
        val imem = Flipped(new ImemPort())
        val exit = Output(Bool())
    })

    // general purpose registers
    // val gp_regs = VecInit(Seq.fill(NUM_GP_REGS)(Module(new Register()).io))

    // control status register
    // val csr = Module(new Register())

    // program counter
    // fetch
    val pc = Module(new Register(START_ADDR.U(WORD_LEN.W)))
    pc.io.in     := pc.io.out + 2.U(WORD_LEN.W)
    io.imem.addr := pc.io.out
    pc.io.load   := true.B

    // decode
    val inst = io.imem.inst

    val opcode = decode(inst)
    io.exit := opcode === Opcode.Exit
    printf(cf"pc: ${pc.io.out}, inst: $inst%x => op: $opcode\n")
}
