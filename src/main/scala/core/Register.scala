package core

import chisel3._
import core.Consts._

class Register(initValue: UInt = 0.U(WORD_LEN.W)) extends Module {
    val io = IO(new Bundle {
        val in    = Input(UInt(WORD_LEN.W))
        val out   = Output(UInt(WORD_LEN.W))
        val write = Input(Bool())
    })

    val reg = RegInit(initValue)

    when(io.write) {
        reg := io.in
    }
    io.out := reg
}
