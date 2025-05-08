import chisel3._
import chiseltest._
import core._
import core.Consts._
import org.scalatest.flatspec.AnyFlatSpec

class CpuTest extends AnyFlatSpec with ChiselScalatestTester {
    it should "increment pc" in {
        test(new Cpu) { c =>
            c.clock.step(1)
            c.io.imem.addr.expect(2.U)
            c.clock.step(1)
            c.io.imem.addr.expect(4.U)
        }
    }

    it should "assert exit signal" in {
        test(new Top(Seq(0x00, 0x00, 0x00, 0xf8, 0x00, 0x00))) { c =>
            c.io.exit.expect(false.B)
            c.clock.step(1)
            c.io.exit.expect(true.B)
            c.clock.step(1)
            c.io.exit.expect(false.B)
        }
    }
}
