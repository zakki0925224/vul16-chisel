import chisel3._
import chiseltest._
import core._
import org.scalatest.flatspec.AnyFlatSpec

class AluTest extends AnyFlatSpec with ChiselScalatestTester {
    it should "perform basic operations" in {
        test(new Alu) { c =>
            c.io.a.poke(10.U)
            c.io.b.poke(3.U)
            c.io.op.poke(AluOpcode.Add)
            c.clock.step()
            c.io.out.expect(13.U)

            c.io.a.poke(10.U)
            c.io.b.poke(3.U)
            c.io.op.poke(AluOpcode.Sub)
            c.clock.step()
            c.io.out.expect(7.U)

            c.io.a.poke("b1100".U)
            c.io.b.poke("b1010".U)
            c.io.op.poke(AluOpcode.And)
            c.clock.step()
            c.io.out.expect("b1000".U)

            c.io.a.poke("b1100".U)
            c.io.b.poke("b1010".U)
            c.io.op.poke(AluOpcode.Or)
            c.clock.step()
            c.io.out.expect("b1110".U)
        }
    }
}
