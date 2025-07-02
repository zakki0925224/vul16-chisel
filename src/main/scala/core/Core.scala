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
        val memDataReq   = Output(Bool())
        val memDataDone  = Input(Bool())
        val memInstReq   = Output(Bool())
        val memInstDone  = Input(Bool())

        val memInst = Input(UInt(WORD_LEN.W))

        // debug
        val debugHalt = Input(Bool())
        val debugStep = Input(Bool())
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

    cpu.io.debugHalt := io.debugHalt
    cpu.io.debugStep := io.debugStep

    io.memDataReq      := cpu.io.memDataReq
    cpu.io.memDataDone := io.memDataDone
    io.memInstReq      := cpu.io.memInstReq
    cpu.io.memInstDone := io.memInstDone
}
