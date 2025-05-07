package core

import chisel3._
import chisel3.util.Counter

class Blinky(freq: Int, startOn: Boolean = false) extends Module {
    val io = IO(new Bundle {
        val led0 = Output(Bool())
    })
    // Blink LED every second using Chisel built-in util.Counter
    val led              = RegInit(startOn.B)
    val (_, counterWrap) = Counter(true.B, freq / 2)
    when(counterWrap) {
        led := ~led
    }
    io.led0 := led
}
