import chisel3._
import chiseltest._
import core._
import core.Consts._
import org.scalatest.flatspec.AnyFlatSpec

class CpuTest extends AnyFlatSpec with ChiselScalatestTester {
    it should "increment pc" in {
        test(new Cpu) { c =>
            c.clock.step(1)
            c.io.pc.expect(2.U)
            c.clock.step(1)
            c.io.pc.expect(4.U)
        }
    }

    it should "basic calculate instructions" in {
        // add   r0, r0, r0
        val add_r0_r0_r0 = (0x0 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | ((0 & 0x7) << 2)
        // addi  r1, r0, 10
        val addi_r1_r0_10 = (0x1 << 11) | ((1 & 0x7) << 8) | ((0 & 0x7) << 5) | (10 & 0x1f)
        // addi  r2, r0, 15
        val addi_r2_r0_15 = (0x1 << 11) | ((2 & 0x7) << 8) | ((0 & 0x7) << 5) | (15 & 0x1f)

        // add   r3, r0, r1
        val add_r3_r0_r1 = (0x0 << 11) | ((3 & 0x7) << 8) | ((0 & 0x7) << 5) | ((1 & 0x7) << 2)
        // addi  r3, r3, 5
        val addi_r3_r3_5 = (0x1 << 11) | ((3 & 0x7) << 8) | ((3 & 0x7) << 5) | (5 & 0x1f)

        // sub   r4, r3, r1
        val sub_r4_r3_r1 = (0x2 << 11) | ((4 & 0x7) << 8) | ((3 & 0x7) << 5) | ((1 & 0x7) << 2)

        // and   r5, r4, r2
        val and_r5_r4_r2 = (0x3 << 11) | ((5 & 0x7) << 8) | ((4 & 0x7) << 5) | ((2 & 0x7) << 2)
        // andi  r5, r5, 7
        val andi_r5_r5_7 = (0x4 << 11) | ((5 & 0x7) << 8) | ((5 & 0x7) << 5) | (7 & 0x1f)

        // or    r6, r2, r5
        val or_r6_r2_r5 = (0x5 << 11) | ((6 & 0x7) << 8) | ((2 & 0x7) << 5) | ((5 & 0x7) << 2)
        // ori   r6, r6, 8
        val ori_r6_r6_8 = (0x6 << 11) | ((6 & 0x7) << 8) | ((6 & 0x7) << 5) | (8 & 0x1f)

        // xor   r7, r6, r3
        val xor_r7_r6_r3 = (0x7 << 11) | ((7 & 0x7) << 8) | ((6 & 0x7) << 5) | ((3 & 0x7) << 2)
        // xori  r0, r7, 5
        val xori_r0_r7_5 = (0x8 << 11) | ((0 & 0x7) << 8) | ((7 & 0x7) << 5) | (5 & 0x1f)

        val prog = Seq(
            add_r0_r0_r0,
            addi_r1_r0_10,
            addi_r2_r0_15,
            add_r3_r0_r1,
            addi_r3_r3_5,
            sub_r4_r3_r1,
            and_r5_r4_r2,
            andi_r5_r5_7,
            or_r6_r2_r5,
            ori_r6_r6_8,
            xor_r7_r6_r3,
            xori_r0_r7_5
        ).flatMap(i => Seq(i & 0xff, (i >> 8) & 0xff))

        test(new Top(prog)) { c =>
            val gpRegs = c.io.gpRegs

            // add  r0, r0, r0
            c.clock.step(1)

            // addi r1, r0, 10
            c.clock.step(1)
            gpRegs(1).expect(10.U)

            // addi r2, r0, 15
            c.clock.step(1)
            gpRegs(2).expect(15.U)

            // add  r3, r0, r1
            c.clock.step(1)
            gpRegs(3).expect(10.U)

            // addi r3, r3, 5
            c.clock.step(1)
            gpRegs(3).expect(15.U)

            // sub  r4, r3, r1
            c.clock.step(1)
            gpRegs(4).expect(5.U)

            // and  r5, r4, r2
            c.clock.step(1)
            gpRegs(5).expect(5.U)

            // andi r5, r5, 7
            c.clock.step(1)
            gpRegs(5).expect(5.U)

            // or   r6, r2, r5
            c.clock.step(1)
            gpRegs(6).expect(15.U)

            // ori  r6, r6, 8
            c.clock.step(1)
            gpRegs(6).expect(15.U)

            // xor  r7, r6, r3
            c.clock.step(1)
            gpRegs(7).expect(0.U)

            // xori r0, r7, 5
            c.clock.step(1)
            gpRegs(0).expect(5.U)
        }
    }

    it should "load store instructions" in {
        // addi r0, r0, 3
        val addi_r0_r0_3 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (3 & 0x1f)
        // addi r1, r1, 0x1f
        val addi_r1_r1_0x1f = (0x1 << 11) | ((1 & 0x7) << 8) | ((1 & 0x7) << 5) | (0x1f & 0x1f)
        // sb r0, r1, 0
        val sb_r0_r1_0 = (0x16 << 11) | ((0 & 0x7) << 8) | ((1 & 0x7) << 5)
        // lb r2, r1, 0
        val lb_r2_r1_0 = (0x13 << 11) | ((2 & 0x7) << 8) | ((1 & 0x7) << 5)
        // lh r3, r0, 0
        val lh_r3_r0_0 = (0x15 << 11) | ((3 & 0x7) << 8) | ((0 & 0x7) << 5)

        val prog = Seq(
            addi_r0_r0_3,
            addi_r1_r1_0x1f,
            sb_r0_r1_0,
            lb_r2_r1_0,
            lh_r3_r0_0
        ).flatMap(i => Seq(i & 0xff, (i >> 8) & 0xff))

        test(new Top(prog)) { c =>
            val gpRegs = c.io.gpRegs

            c.clock.step(2)
            gpRegs(0).expect(3.U)
            gpRegs(1).expect(0x1f.U)

            c.clock.step(4)
            gpRegs(2).expect(3.U)

            c.clock.step(2)
            gpRegs(3).expect(0x2009.U)
        }
    }
}
