package core

import chisel3._

object Consts {
    val WORD_LEN   = 16
    val MEM_SIZE   = 16384
    val START_ADDR = 0x0000

    val MEM_INIT: Seq[Int] = Seq(
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07
    )
}
