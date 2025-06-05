package core

import chisel3._
import chisel3.util._
import core.Consts._

class Memory(memInit: Option[Seq[Int]] = None) extends Module {
    val io = IO(new Bundle {
        val dataAddr  = Input(UInt(WORD_LEN.W))
        val dataIn    = Input(UInt(BYTE_LEN.W))
        val dataOut   = Output(UInt(BYTE_LEN.W))
        val dataWrite = Input(Bool())

        val instAddr = Input(UInt(WORD_LEN.W))
        val instOut  = Output(UInt(WORD_LEN.W))

        val mmioInAddr = Input(UInt(WORD_LEN.W))
        val mmioIn     = Input(UInt(BYTE_LEN.W))
    })

    val mem = Mem(MEM_SIZE, UInt(8.W))
    memInit.foreach { init =>
        for ((v, i) <- init.zipWithIndex) {
            mem(i) := v.U(8.W)
        }
    }

    io.instOut := Cat(
        mem(io.instAddr + 1.U(WORD_LEN.W)),
        mem(io.instAddr)
    )

    io.dataOut := mem(io.dataAddr)
    when(io.dataWrite) {
        mem(io.dataAddr) := io.dataIn
    }

    when(io.mmioInAddr >= MMIO_START_ADDR.U) {
        mem(io.mmioInAddr) := io.mmioIn
    }
}
