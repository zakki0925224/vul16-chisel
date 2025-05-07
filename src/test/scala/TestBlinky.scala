import core._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BlinkyTest extends AnyFlatSpec with ChiselScalatestTester {
    "Blinky" should "toggle LED every (freq / 2) cycles" in {
        val freq = 10
        test(new Blinky(freq, startOn = false)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.led0.expect(false.B)

            dut.clock.step(5)
            dut.io.led0.expect(true.B)

            dut.clock.step(5)
            dut.io.led0.expect(false.B)

            dut.clock.step(5)
            dut.io.led0.expect(true.B)
        }
    }
}
