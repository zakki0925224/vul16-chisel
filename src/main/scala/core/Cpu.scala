package core

import chisel3._
import core.Consts._

class Cpu extends Module {
    val io = IO(new Bundle {
        val imem = Flipped(new ImemPort())
        val exit = Output(Bool())
    })

    val pc = RegInit(START_ADDR.U(WORD_LEN.W))
    pc           := pc + 2.U(WORD_LEN.W)
    io.imem.addr := pc

    val inst = io.imem.inst
    io.exit := (inst === 0x1234.U(WORD_LEN.W))
}
