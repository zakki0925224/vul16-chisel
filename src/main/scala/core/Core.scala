package core

import chisel3._
import core.Consts._

class Core() extends Module {
    val io = IO(new Bundle {
        val pc     = Output(UInt(WORD_LEN.W))
        val inst   = Output(UInt(WORD_LEN.W))
        val gpRegs = Output(Vec(NUM_GP_REGS, UInt(WORD_LEN.W)))

        // memory
        val memDataAddr  = Output(UInt(WORD_LEN.W))
        val memDataIn    = Output(UInt(BYTE_LEN.W))
        val memDataOut   = Input(UInt(BYTE_LEN.W))
        val memDataWrite = Output(Bool())

        val memInst = Input(UInt(WORD_LEN.W))

        // debug
        val debug_halt = Input(Bool())
        val debug_step = Input(Bool())
    })

    val cpu = Module(new Cpu())

    io.memDataAddr    := cpu.io.memDataAddr
    io.memDataIn      := cpu.io.memDataIn
    cpu.io.memDataOut := io.memDataOut
    io.memDataWrite   := cpu.io.memDataWrite
    cpu.io.inst       := io.memInst
    io.pc             := cpu.io.pc
    io.gpRegs         := cpu.io.gpRegs

    io.pc     := cpu.io.pc
    io.inst   := cpu.io.inst
    io.gpRegs := cpu.io.gpRegs

    cpu.io.debug_halt := io.debug_halt
    cpu.io.debug_step := io.debug_step
}
