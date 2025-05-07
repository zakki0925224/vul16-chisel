import chisel3._
import chiseltest._
import core.Cpu
import org.scalatest.flatspec.AnyFlatSpec

class CpuTest extends AnyFlatSpec with ChiselScalatestTester {
    "Cpu" should "increment pc" in {
        test(new Cpu) { c =>
            c.clock.step(1)
            c.io.pc.expect(2.U)
        }
    }
}
