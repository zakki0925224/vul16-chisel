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

    it should "perform shift operations" in {
        test(new Alu) { c =>
            c.io.a.poke(10.U)
            c.io.b.poke(2.U)
            c.io.op.poke(AluOpcode.Sll)
            c.clock.step()
            c.io.out.expect(40.U)

            c.io.a.poke(10.U)
            c.io.b.poke(2.U)
            c.io.op.poke(AluOpcode.Srl)
            c.clock.step()
            c.io.out.expect(2.U)

            c.io.a.poke(10.U)
            c.io.b.poke(2.U)
            c.io.op.poke(AluOpcode.Sra)
            c.clock.step()
            c.io.out.expect(2.U)
        }
    }

    it should "perform compare operations" in {
        test(new Alu) { c =>
            c.io.a.poke(10.U)
            c.io.b.poke(3.U)
            c.io.op.poke(AluOpcode.Slt)
            c.clock.step()
            c.io.out.expect(0.U)

            c.io.a.poke(3.U)
            c.io.b.poke(10.U)
            c.io.op.poke(AluOpcode.Slt)
            c.clock.step()
            c.io.out.expect(1.U)

            c.io.a.poke(10.U)
            c.io.b.poke(3.U)
            c.io.op.poke(AluOpcode.Sltu)
            c.clock.step()
            c.io.out.expect(0.U)

            c.io.a.poke(3.U)
            c.io.b.poke(10.U)
            c.io.op.poke(AluOpcode.Sltu)
            c.clock.step()
            c.io.out.expect(1.U)
        }
    }
}
