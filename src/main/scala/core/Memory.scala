package core

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import core.Consts._

class ImemPort extends Bundle {
    val addr = Input(UInt(WORD_LEN.W))
    val inst = Output(UInt(WORD_LEN.W))
}

class Memory(memInit: Seq[Int]) extends Module {
    val io = IO(new Bundle {
        val imem = new ImemPort()
    })

    val mem = Mem(MEM_SIZE, UInt(8.W))
    for ((v, i) <- memInit.zipWithIndex) {
        mem(i) := v.U(8.W)
    }

    io.imem.inst := Cat(
        mem(io.imem.addr + 1.U(WORD_LEN.W)),
        mem(io.imem.addr)
    )
}
