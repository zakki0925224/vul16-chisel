import chisel3._
import chiseltest._
import core._
import core.Consts._
import org.scalatest.flatspec.AnyFlatSpec
import java.nio.file.{Files, Paths}

class TestHarness(prog: Option[Seq[Int]] = None) extends Module {
    val io = IO(new Bundle {
        val pc     = Output(UInt(WORD_LEN.W))
        val inst   = Output(UInt(WORD_LEN.W))
        val gpRegs = Output(Vec(NUM_GP_REGS, UInt(WORD_LEN.W)))
    })

    val core = Module(new Core())
    val mem  = Module(new TestMemoryModule(prog))

    io.pc     := core.io.pc
    io.inst   := core.io.inst
    io.gpRegs := core.io.gpRegs

    mem.io.dataAddr     := core.io.memDataAddr
    mem.io.dataIn       := core.io.memDataIn
    core.io.memDataOut  := mem.io.dataOut
    mem.io.dataWrite    := core.io.memDataWrite
    mem.io.instAddr     := core.io.pc
    core.io.memInst     := mem.io.instOut
    core.io.memDataDone := true.B
    core.io.memInstDone := true.B

    core.io.debugHalt := false.B
    core.io.debugStep := false.B
}

class CpuTest extends AnyFlatSpec with ChiselScalatestTester {
    // it should "increment pc" in {
    //     test(new TestHarness()) { c =>
    //         c.clock.step(3)
    //         c.io.pc.expect(2.U)
    //         c.clock.step(3)
    //         c.io.pc.expect(4.U)
    //     }
    // }

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

        test(new TestHarness(Some(prog))) { c =>
            val gpRegs = c.io.gpRegs

            // add  r0, r0, r0
            c.clock.step(3)

            // addi r1, r0, 10
            c.clock.step(3)
            gpRegs(1).expect(10.U)

            // addi r2, r0, 15
            c.clock.step(3)
            gpRegs(2).expect(15.U)

            // add  r3, r0, r1
            c.clock.step(3)
            gpRegs(3).expect(10.U)

            // addi r3, r3, 5
            c.clock.step(3)
            gpRegs(3).expect(15.U)

            // sub  r4, r3, r1
            c.clock.step(3)
            gpRegs(4).expect(5.U)

            // and  r5, r4, r2
            c.clock.step(3)
            gpRegs(5).expect(5.U)

            // andi r5, r5, 7
            c.clock.step(3)
            gpRegs(5).expect(5.U)

            // or   r6, r2, r5
            c.clock.step(3)
            gpRegs(6).expect(15.U)

            // ori  r6, r6, 8
            c.clock.step(3)
            gpRegs(6).expect(15.U)

            // xor  r7, r6, r3
            c.clock.step(3)
            gpRegs(7).expect(0.U)

            // xori r0, r7, 5
            // but zero register should not change
            c.clock.step(3)
            gpRegs(0).expect(0.U)
        }
    }

    it should "load store instructions" in {
        // addi r1, r0, 15   ; r1 = 15 (max positive 5bit)
        val addi_r1_r0_15 = (0x1 << 11) | ((1 & 0x7) << 8) | ((0 & 0x7) << 5) | (15 & 0x1f)
        // addi r2, r0, -8   ; r2 = 0xfff8 (min negative 5bit)
        val addi_r2_r0_m8 = (0x1 << 11) | ((2 & 0x7) << 8) | ((0 & 0x7) << 5) | (0x18 & 0x1f) // 0x18=24, 5bit for -8
        // addi r7, r0, 0      ; r7 = 0 (base address)
        val addi_r7_r0_0 = (0x1 << 11) | ((7 & 0x7) << 8) | ((0 & 0x7) << 5) | (0 & 0x1f)
        // sb r1, r7, 0        ; mem[0] = 0x0f
        val sb_r1_r7_0 = (0x16 << 11) | ((1 & 0x7) << 8) | ((7 & 0x7) << 5)
        // sb r2, r7, 1        ; mem[1] = 0xf8
        val sb_r2_r7_1 = (0x16 << 11) | ((2 & 0x7) << 8) | ((7 & 0x7) << 5) | 1
        // lb r3, r7, 0        ; r3 = 0x0f (sign-extended)
        val lb_r3_r7_0 = (0x13 << 11) | ((3 & 0x7) << 8) | ((7 & 0x7) << 5)
        // lbu r4, r7, 1       ; r4 = 0xf8 (zero-extended)
        val lbu_r4_r7_1 = (0x14 << 11) | ((4 & 0x7) << 8) | ((7 & 0x7) << 5) | 1
        // lh r5, r7, 0        ; r5 = 0xf80f (0xf8 << 8 | 0x0f)
        val lh_r5_r7_0 = (0x15 << 11) | ((5 & 0x7) << 8) | ((7 & 0x7) << 5)
        // sh r1, r7, 2        ; mem[2] = 0x0f, mem[3] = 0x00
        val sh_r1_r7_2 = (0x17 << 11) | ((1 & 0x7) << 8) | ((7 & 0x7) << 5) | 2
        // lh r6, r7, 2        ; r6 = 0x000f
        val lh_r6_r7_2 = (0x15 << 11) | ((6 & 0x7) << 8) | ((7 & 0x7) << 5) | 2

        val prog = Seq(
            addi_r1_r0_15,
            addi_r2_r0_m8,
            addi_r7_r0_0,
            sb_r1_r7_0,
            sb_r2_r7_1,
            lb_r3_r7_0,
            lbu_r4_r7_1,
            lh_r5_r7_0,
            sh_r1_r7_2,
            lh_r6_r7_2
        ).flatMap(i => Seq(i & 0xff, (i >> 8) & 0xff))

        test(new TestHarness(Some(prog))) { c =>
            val gpRegs = c.io.gpRegs

            // addi r1, r0, 15
            c.clock.step(3)
            gpRegs(1).expect(15.U)
            // addi r2, r0, -8
            c.clock.step(3)
            gpRegs(2).expect((0xfffffff8L & 0xffff).U) // -8 (16bit)
            // addi r7, r0, 0 (base address)
            val baseReg = 7
            c.clock.step(3)
            gpRegs(baseReg).expect(0.U)
            // sb r1, r7, 0
            c.clock.step(4)
            // sb r2, r7, 1
            c.clock.step(4)
            // lb r3, r7, 0
            c.clock.step(4)
            gpRegs(3).expect(15.U) // 0x0f (sign-extended)
            // lbu r4, r7, 1
            c.clock.step(5)
            gpRegs(4).expect(248.U) // 0xf8 (zero-extended)
            // lh r5, r7, 0
            c.clock.step(5)
            gpRegs(5).expect(0xf80f.U) // 0xf8 << 8 | 0x0f
            // sh r1, r7, 2
            c.clock.step(5)
            // lh r6, r7, 2
            c.clock.step(5)
            gpRegs(6).expect(15.U) // 0x00 << 8 | 0x0f
        }
    }

    it should "jump instructions" in {
        // jmp r1, 4
        val jmp_r1_4 = (0x18 << 11) | ((1 & 0x7) << 8) | (4 & 0xff)
        // addi r2, r0, 7
        val addi_r2_r0_7 = (0x1 << 11) | ((2 & 0x7) << 8) | ((0 & 0x7) << 5) | (7 & 0x1f)
        // addi r3, r0, 9
        val addi_r3_r0_9 = (0x1 << 11) | ((3 & 0x7) << 8) | ((0 & 0x7) << 5) | (9 & 0x1f)
        // jmpr r4, r1, 2
        val jmpr_r4_r1_2 = (0x19 << 11) | ((4 & 0x7) << 8) | ((1 & 0x7) << 5) | (2 & 0x1f)
        // addi r5, r0, 11
        val addi_r5_r0_11 = (0x1 << 11) | ((5 & 0x7) << 8) | ((0 & 0x7) << 5) | (11 & 0x1f)
        // addi r6, r0, 13
        val addi_r6_r0_13 = (0x1 << 11) | ((6 & 0x7) << 8) | ((0 & 0x7) << 5) | (13 & 0x1f)

        val prog = Seq(
            jmp_r1_4,
            addi_r2_r0_7,
            addi_r3_r0_9,
            jmpr_r4_r1_2,
            addi_r5_r0_11,
            addi_r6_r0_13
        ).flatMap(i => Seq(i & 0xff, (i >> 8) & 0xff))

        test(new TestHarness(Some(prog))) { c =>
            val gpRegs = c.io.gpRegs

            // jmp r1, 4
            c.clock.step(3)
            gpRegs(1).expect(2.U) // r1 = pc+2 (0+2)

            c.clock.step(3)
            gpRegs(3).expect(9.U) // addi r3, r0, 9

            // jmpr r4, r1, 2 (pc = r1+2 = 2+2 = 4, r4 = pc+2 = 8)
            c.clock.step(3)
            gpRegs(4).expect(8.U) // r4 = pc+2 (6+2=8)

            c.clock.step(3)
            gpRegs(6).expect(0.U)
        }
    }

    it should "branch and jump instructions" in {
        // addi r1, r0, 5
        val addi_r1_r0_5 = (0x1 << 11) | ((1 & 0x7) << 8) | ((0 & 0x7) << 5) | (5 & 0x1f)
        // addi r2, r0, 5
        val addi_r2_r0_5 = (0x1 << 11) | ((2 & 0x7) << 8) | ((0 & 0x7) << 5) | (5 & 0x1f)
        // addi r3, r0, 3
        val addi_r3_r0_3 = (0x1 << 11) | ((3 & 0x7) << 8) | ((0 & 0x7) << 5) | (3 & 0x1f)
        // beq r1, r2, 4
        val beq_r1_r2_4 = (0x1a << 11) | ((1 & 0x7) << 8) | ((2 & 0x7) << 5) | (4 & 0x1f)
        // addi r0, r0, 99
        val addi_r0_r0_99 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (99 & 0x1f)
        // bne r1, r3, 4
        val bne_r1_r3_4 = (0x1b << 11) | ((1 & 0x7) << 8) | ((3 & 0x7) << 5) | (4 & 0x1f)
        // addi r0, r0, 88
        val addi_r0_r0_88 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (88 & 0x1f)
        // blt r3, r1, 4
        val blt_r3_r1_4 = (0x1c << 11) | ((3 & 0x7) << 8) | ((1 & 0x7) << 5) | (4 & 0x1f)
        // addi r0, r0, 77
        val addi_r0_r0_77 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (77 & 0x1f)
        // bge r1, r3, 4
        val bge_r1_r3_4 = (0x1d << 11) | ((1 & 0x7) << 8) | ((3 & 0x7) << 5) | (4 & 0x1f)
        // addi r0, r0, 66
        val addi_r0_r0_66 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (66 & 0x1f)
        // bltu r3, r1, 4
        val bltu_r3_r1_4 = (0x1e << 11) | ((3 & 0x7) << 8) | ((1 & 0x7) << 5) | (4 & 0x1f)
        // addi r0, r0, 55
        val addi_r0_r0_55 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (55 & 0x1f)
        // bgeu r1, r3, 4
        val bgeu_r1_r3_4 = (0x1f << 11) | ((1 & 0x7) << 8) | ((3 & 0x7) << 5) | (4 & 0x1f)
        // addi r0, r0, 44
        val addi_r0_r0_44 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (44 & 0x1f)
        // addi r0, r0, 1
        val addi_r0_r0_1 = (0x1 << 11) | ((0 & 0x7) << 8) | ((0 & 0x7) << 5) | (1 & 0x1f)

        val prog = Seq(
            addi_r1_r0_5,
            addi_r2_r0_5,
            addi_r3_r0_3,
            beq_r1_r2_4,
            addi_r0_r0_99,
            bne_r1_r3_4,
            addi_r0_r0_88,
            blt_r3_r1_4,
            addi_r0_r0_77,
            bge_r1_r3_4,
            addi_r0_r0_66,
            bltu_r3_r1_4,
            addi_r0_r0_55,
            bgeu_r1_r3_4,
            addi_r0_r0_44,
            addi_r0_r0_1
        ).flatMap(i => Seq(i & 0xff, (i >> 8) & 0xff))

        test(new TestHarness(Some(prog))) { c =>
            val gpRegs = c.io.gpRegs

            // addi r1, r0, 5
            c.clock.step(3)
            gpRegs(1).expect(5.U)
            // addi r2, r0, 5
            c.clock.step(3)
            gpRegs(2).expect(5.U)
            // addi r3, r0, 3
            c.clock.step(3)
            gpRegs(3).expect(3.U)

            // addi r0, r0, 1
            // but zero register should not change
            c.clock.step(7)
            gpRegs(0).expect(0.U)
        }
    }

    it should "assembler test" in {
        val res = getClass.getResource("/asm_tests/label.bin")
        require(res != null, "label.bin not found in resources")

        val path           = Paths.get(res.toURI())
        val prog: Seq[Int] = Files.readAllBytes(path).map(_ & 0xff).toSeq

        test(new TestHarness(Some(prog))) { c =>
            val gpRegs = c.io.gpRegs
            val pc     = c.io.pc

            pc.expect(0.U)
            c.clock.step(3)
            pc.expect(2.U)
            c.clock.step(3)
            pc.expect(4.U)
            c.clock.step(3)
            pc.expect(8.U) // j #test -> test: addi r3, r1, 4
            c.clock.step(3)
            pc.expect(10.U)
            c.clock.step(3)
            pc.expect(12.U)
            c.clock.step(3)
            pc.expect(14.U)
            c.clock.step(3)
            pc.expect(16.U)
            c.clock.step(3)
            pc.expect(8.U) // j #test -> test: addi r3, r1, 4
            c.clock.step(3)
            pc.expect(10.U)
        }
    }
}
