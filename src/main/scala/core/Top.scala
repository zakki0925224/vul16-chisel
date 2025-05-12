package core

import chisel3._
import core.Consts._

class Top(memInit: Seq[Int] = MEM_INIT) extends Module {
    val io = IO(new Bundle {
        val exit   = Output(Bool())
        val pc     = Output(UInt(WORD_LEN.W))
        val inst   = Output(UInt(WORD_LEN.W))
        val gpRegs = Output(Vec(NUM_GP_REGS, UInt(WORD_LEN.W)))
    })

    val cpu = Module(new Cpu())
    val mem = Module(new Memory(memInit))

    mem.io.dataAddr   := cpu.io.memDataAddr
    mem.io.dataIn     := cpu.io.memDataIn
    cpu.io.memDataOut := mem.io.dataOut
    mem.io.dataLoad   := cpu.io.memDataLoad
    mem.io.instAddr   := cpu.io.pc
    cpu.io.inst       := mem.io.instOut
    io.pc             := cpu.io.pc
    io.inst           := cpu.io.inst
    io.gpRegs         := cpu.io.gpRegs
    io.exit           := cpu.io.exit
}
