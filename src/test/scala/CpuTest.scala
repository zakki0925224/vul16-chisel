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

    it should "assert exit signal" in {
        test(new Top(Seq(0x00, 0x00, 0x00, 0xf8, 0x00, 0x00))) { c =>
            c.io.exit.expect(false.B)
            c.clock.step(1)
            c.io.exit.expect(true.B)
            c.clock.step(1)
            c.io.exit.expect(false.B)
        }
    }

    it should "execute add, sub, addi, and subi instructions" in {
        // addi r1, r0, 5   : opcode=0x1, rd=1, rs=0, imm=5
        val addi_x1_x0_5 = (0x1 << 11) | (1 << 8) | (0 << 5) | (5 & 0x1f)
        // addi r2, r0, 3   : opcode=0x1, rd=2, rs=0, imm=3
        val addi_x2_x0_3 = (0x1 << 11) | (2 << 8) | (0 << 5) | (3 & 0x1f)
        // add r3, r1, r2   : opcode=0x0, rd=3, rs1=1, rs2=2, reserved=0
        val add_x3_x1_x2 = (0x0 << 11) | (3 << 8) | (1 << 5) | (2 << 2)
        // sub r4, r1, r2   : opcode=0x2, rd=4, rs1=1, rs2=2, reserved=0
        val sub_x4_x1_x2 = (0x2 << 11) | (4 << 8) | (1 << 5) | (2 << 2)
        // subi r5, r1, 2   : opcode=0x3, rd=5, rs=1, imm=2
        val subi_x5_x1_2 = (0x3 << 11) | (5 << 8) | (1 << 5) | (2 & 0x1f)
        // exit
        val exit = 0xf800

        val prog = Seq(
            addi_x1_x0_5,
            addi_x2_x0_3,
            add_x3_x1_x2,
            sub_x4_x1_x2,
            subi_x5_x1_2,
            exit
        ).flatMap(i => Seq(i & 0xff, (i >> 8) & 0xff))

        test(new Top(prog)) { c =>
            val gpRegs = c.io.gpRegs
            // addi r1, r0, 5
            gpRegs(1).expect(5.U)
            c.clock.step(1)
            // addi r2, r0, 3
            gpRegs(2).expect(3.U)
            c.clock.step(1)
            // add r3, r1, r2
            gpRegs(3).expect(8.U)
            c.clock.step(1)
            // sub r4, r1, r2
            gpRegs(4).expect(2.U)
            c.clock.step(1)
            // subi r5, r1, 2
            gpRegs(5).expect(3.U)
            c.clock.step(1)
            c.io.exit.expect(true.B)
        }
    }
}
