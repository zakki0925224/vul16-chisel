package core

import chisel3._
import core.Consts._

class Core(memInit: Option[Seq[Int]] = None) extends Module {
    val io = IO(new Bundle {
        val pc     = Output(UInt(WORD_LEN.W))
        val inst   = Output(UInt(WORD_LEN.W))
        val gpRegs = Output(Vec(NUM_GP_REGS, UInt(WORD_LEN.W)))

        val mmioInAddr  = Input(UInt(WORD_LEN.W))
        val mmioIn      = Input(UInt(BYTE_LEN.W))
        val mmioOut     = Output(UInt(BYTE_LEN.W))
        val mmioOutAddr = Output(UInt(WORD_LEN.W))
    })

    val cpu = Module(new Cpu())
    val mem = Module(new Memory(memInit))

    val isMmio = cpu.io.memDataAddr >= MMIO_START_ADDR.U

    mem.io.dataAddr   := cpu.io.memDataAddr
    mem.io.dataIn     := cpu.io.memDataIn
    cpu.io.memDataOut := mem.io.dataOut
    mem.io.dataWrite  := Mux(isMmio, false.B, cpu.io.memDataWrite)
    mem.io.instAddr   := cpu.io.pc
    cpu.io.inst       := mem.io.instOut
    io.pc             := cpu.io.pc
    io.inst           := cpu.io.inst
    io.gpRegs         := cpu.io.gpRegs

    mem.io.mmioInAddr := io.mmioInAddr
    mem.io.mmioIn     := io.mmioIn
    io.mmioOutAddr    := Mux(isMmio, cpu.io.memDataAddr, 0.U)
    io.mmioOut        := Mux(isMmio, cpu.io.memDataIn, 0.U)
}
