package core

import chisel3._

class Top() extends Module {
    val io = IO(new Bundle {
        val exit = Output(Bool())
    })

    val cpu = Module(new Cpu())
    val mem = Module(new Memory())

    cpu.io.imem <> mem.io.imem
    io.exit := cpu.io.exit
}
