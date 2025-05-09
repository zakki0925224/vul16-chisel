package core

import chisel3._
import core.Consts._

class Top(memInit: Seq[Int] = MEM_INIT) extends Module {
    val io = IO(new Bundle {
        val exit = Output(Bool())
        val pc   = Output(UInt(WORD_LEN.W))
    })

    val cpu = Module(new Cpu())
    val mem = Module(new Memory(memInit))

    cpu.io.imem <> mem.io.imem
    io.pc   := cpu.io.imem.addr
    io.exit := cpu.io.exit
}
