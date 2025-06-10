package core

import chisel3._
import chisel3.util._
import core.Consts._

class Cpu extends Module {
    // (op, rd, rs1, rs2, imm/offset)
    def decode(inst: UInt): (Opcode.Type, UInt, UInt, UInt, UInt) = {
        val op  = Opcode.fromInst(inst)
        val fmt = Opcode.toFormatType(op)

        val rd  = WireDefault(0.U(3.W))
        val rs1 = WireDefault(0.U(3.W))
        val rs2 = WireDefault(0.U(3.W))
        val imm = WireDefault(0.U(WORD_LEN.W))

        switch(fmt) {
            is(FormatType.R) {
                rd  := inst(10, 8)
                rs1 := inst(7, 5)
                rs2 := inst(4, 2)
            }
            is(FormatType.I) {
                rd  := inst(10, 8)
                rs1 := inst(7, 5)
                imm := inst(4, 0)
            }
            is(FormatType.J) {
                rd  := inst(10, 8)
                imm := inst(7, 0)
            }
            is(FormatType.B) {
                rs1 := inst(10, 8)
                rs2 := inst(7, 5)
                imm := inst(4, 0)
            }
        }

        val out_rd  = WireDefault(0.U(3.W))
        val out_rs1 = WireDefault(0.U(3.W))
        val out_rs2 = WireDefault(0.U(3.W))
        val out_imm = WireDefault(0.U(WORD_LEN.W))

        switch(fmt) {
            is(FormatType.R) {
                out_rd  := rd
                out_rs1 := rs1
                out_rs2 := rs2
                out_imm := 0.U
            }
            is(FormatType.I) {
                out_rd  := rd
                out_rs1 := rs1
                out_rs2 := 0.U
                out_imm := imm
            }
            is(FormatType.J) {
                out_rd  := rd
                out_rs1 := 0.U
                out_rs2 := 0.U
                out_imm := imm
            }
            is(FormatType.B) {
                out_rd  := 0.U
                out_rs1 := rs1
                out_rs2 := rs2
                out_imm := imm
            }
        }

        (op, out_rd, out_rs1, out_rs2, out_imm)
    }

    val io = IO(new Bundle {
        val memDataAddr  = Output(UInt(WORD_LEN.W))
        val memDataIn    = Output(UInt(BYTE_LEN.W))
        val memDataOut   = Input(UInt(BYTE_LEN.W))
        val memDataWrite = Output(Bool())

        val inst   = Input(UInt(WORD_LEN.W))
        val pc     = Output(UInt(WORD_LEN.W))
        val gpRegs = Output(Vec(NUM_GP_REGS, UInt(WORD_LEN.W)))

        val debug_halt = Input(Bool())
        val debug_step = Input(Bool())
    })

    io.memDataAddr  := 0.U(WORD_LEN.W)
    io.memDataIn    := 0.U(BYTE_LEN.W)
    io.memDataWrite := false.B

    // general purpose registers
    val gpRegs = VecInit(Seq.fill(NUM_GP_REGS)(Module(new Register()).io))
    for (i <- 0 until NUM_GP_REGS) {
        gpRegs(i).in    := gpRegs(i).out
        gpRegs(i).write := false.B
        io.gpRegs(i)    := gpRegs(i).out
    }

    val pc = Module(new Register(START_ADDR.U(WORD_LEN.W)))
    pc.io.in    := pc.io.out
    io.pc       := pc.io.out
    pc.io.write := false.B
    // printf(cf"fetch:\n")
    // printf(cf"\tpc: 0x${pc.io.out}%x\n")

    val cycles  = RegInit(0.U(WORD_LEN.W))
    val lhValue = RegInit(0.U(BYTE_LEN.W))

    val inst                    = io.inst
    val (op, rd, rs1, rs2, imm) = decode(inst)
    // printf(cf"decode:\n")
    // printf(cf"\tinst: 0x${inst}%x => op: $op\n")

    val alu = Module(new Alu())
    alu.io.a  := 0.U
    alu.io.b  := 0.U
    alu.io.op := AluOpcode.Add

    // printf(cf"execute (cycles: $cycles):\n")
    val beqResult  = op === Opcode.Beq && gpRegs(rs1).out === gpRegs(rs2).out
    val bneResult  = op === Opcode.Bne && gpRegs(rs1).out =/= gpRegs(rs2).out
    val bltResult  = op === Opcode.Blt && gpRegs(rs1).out.asSInt < gpRegs(rs2).out.asSInt
    val bgeResult  = op === Opcode.Bge && gpRegs(rs1).out.asSInt >= gpRegs(rs2).out.asSInt
    val bltuResult = op === Opcode.Bltu && gpRegs(rs1).out < gpRegs(rs2).out
    val bgeuResult = op === Opcode.Bgeu && gpRegs(rs1).out >= gpRegs(rs2).out

    val allowStep = !io.debug_halt || (io.debug_halt && io.debug_step)

    switch(cycles) {
        is(0.U) {
            when(allowStep) {
                switch(op) {
                    is(Opcode.Add) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Add
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        //// printf(cf"\tadd: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Addi) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Add
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\taddi: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Sub) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Sub
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B

                        // printf(cf"\tsub: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.And) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.And
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tand: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Andi) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.And
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tandi: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Or) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Or
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tor: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Ori) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Or
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tori: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Xor) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Xor
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\txor: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Xori) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Xor
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\txori: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Sll) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Sll
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tsll: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Slli) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Sll
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tslli: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Srl) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Srl
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tsrl: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Srli) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Srl
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tsrli: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Sra) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Sra
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tsra: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Srai) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Sra
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tsrai: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Slt) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Slt
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tslt: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Slti) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Slt
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tslti: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Sltu) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := gpRegs(rs2).out
                        alu.io.op        := AluOpcode.Sltu
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tsltu: rs1: 0x${alu.io.a}%x, rs2: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Sltiu) {
                        alu.io.a         := gpRegs(rs1).out
                        alu.io.b         := imm
                        alu.io.op        := AluOpcode.Sltu
                        gpRegs(rd).in    := alu.io.out
                        gpRegs(rd).write := true.B
                        // printf(cf"\tsltiu: rs: 0x${alu.io.a}%x, imm: 0x${alu.io.b}%x, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Lb) {
                        io.memDataAddr   := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                        io.memDataWrite  := false.B
                        gpRegs(rd).in    := io.memDataOut.asSInt.asUInt
                        gpRegs(rd).write := true.B
                        // printf(
                        //     cf"\tlb: rs: 0x${gpRegs(rs1).out}%x, offset: ${imm.asSInt}, rd: 0x${gpRegs(rd).in}%x\n"
                        // )
                    }
                    is(Opcode.Lbu) {
                        io.memDataAddr   := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                        io.memDataWrite  := false.B
                        gpRegs(rd).in    := io.memDataOut
                        gpRegs(rd).write := true.B
                        // printf(
                        //     cf"\tlbu: rs: 0x${gpRegs(rs1).out}%x, offset: ${imm.asSInt}, rd: 0x${gpRegs(rd).in}%x\n"
                        // )
                    }
                    is(Opcode.Lh) {
                        io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                        io.memDataWrite := false.B
                        lhValue         := io.memDataOut
                        cycles          := 1.U
                    }
                    is(Opcode.Sb) {
                        io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                        io.memDataWrite := true.B
                        io.memDataIn    := gpRegs(rd).out(7, 0)

                        // printf(
                        //     cf"\tsb: rs: 0x${gpRegs(rs1).out}%x, offset: ${imm.asSInt}, rd: 0x${gpRegs(rd).in}%x\n"
                        // )
                    }
                    is(Opcode.Sh) {
                        io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                        io.memDataWrite := true.B
                        io.memDataIn    := gpRegs(rd).out(7, 0)
                        cycles          := 1.U
                    }
                    is(Opcode.Jmp) {
                        gpRegs(rd).in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U).asUInt
                        gpRegs(rd).write := true.B
                        pc.io.in         := (pc.io.out.asSInt + imm.asSInt).asUInt
                        pc.io.write      := true.B
                        // printf(cf"\tjmp: rd: 0x${gpRegs(rd).in}%x, offset: ${imm.asSInt}\n")
                    }
                    is(Opcode.Jmpr) {
                        val newPc = pc.io.out + (WORD_LEN.U / BYTE_LEN.U).asUInt
                        pc.io.in         := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt & ~1.U
                        pc.io.write      := true.B
                        gpRegs(rd).in    := newPc
                        gpRegs(rd).write := true.B
                        // printf(cf"\tjmpr: rs: 0x${gpRegs(rs1).out}%x, offset: ${imm.asSInt}, rd: 0x${gpRegs(rd).in}%x\n")
                    }
                    is(Opcode.Beq) {
                        when(beqResult) {
                            pc.io.in    := (pc.io.out.asSInt + imm.asSInt).asUInt
                            pc.io.write := true.B
                        }
                        // printf(
                        //     cf"\tbeq: rs1: 0x${gpRegs(rs1).out}%x, rs2: 0x${gpRegs(rs2).out}%x, offset: ${imm.asSInt}\n"
                        // )
                    }
                    is(Opcode.Bne) {
                        when(bneResult) {
                            pc.io.in    := (pc.io.out.asSInt + imm.asSInt).asUInt
                            pc.io.write := true.B
                        }
                        // printf(
                        //     cf"\tbne: rs1: 0x${gpRegs(rs1).out}%x, rs2: 0x${gpRegs(rs2).out}%x, offset: ${imm.asSInt}\n"
                        // )
                    }
                    is(Opcode.Blt) {
                        when(bltResult) {
                            pc.io.in    := (pc.io.out.asSInt + imm.asSInt).asUInt
                            pc.io.write := true.B
                        }
                        // printf(
                        //     cf"\tblt: rs1: 0x${gpRegs(rs1).out}%x, rs2: 0x${gpRegs(rs2).out}%x, offset: ${imm.asSInt}\n"
                        // )
                    }
                    is(Opcode.Bge) {
                        when(bgeResult) {
                            pc.io.in    := (pc.io.out.asSInt + imm.asSInt).asUInt
                            pc.io.write := true.B
                        }
                        // printf(
                        //     cf"\tbge: rs1: 0x${gpRegs(rs1).out}%x, rs2: 0x${gpRegs(rs2).out}%x, offset: ${imm.asSInt}\n"
                        // )
                    }
                    is(Opcode.Bltu) {
                        when(bltuResult) {
                            pc.io.in    := (pc.io.out.asSInt + imm.asSInt).asUInt
                            pc.io.write := true.B
                        }
                        // printf(
                        //     cf"\tbltu: rs1: 0x${gpRegs(rs1).out}%x, rs2: 0x${gpRegs(rs2).out}%x, offset: ${imm.asSInt}\n"
                        // )
                    }
                    is(Opcode.Bgeu) {
                        when(bgeuResult) {
                            pc.io.in    := (pc.io.out.asSInt + imm.asSInt).asUInt
                            pc.io.write := true.B
                        }
                        // printf(
                        //     cf"\tbgeu: rs1: 0x${gpRegs(rs1).out}%x, rs2: 0x${gpRegs(rs2).out}%x, offset: ${imm.asSInt}\n"
                        // )
                    }
                }
            }
        }
        is(1.U) {
            when(allowStep) {
                switch(op) {
                    is(Opcode.Lh) {
                        io.memDataAddr   := (gpRegs(rs1).out.asSInt + imm.asSInt + 1.S).asUInt
                        io.memDataWrite  := false.B
                        gpRegs(rd).in    := Cat(io.memDataOut, lhValue).asSInt.pad(WORD_LEN).asUInt
                        gpRegs(rd).write := true.B
                        // printf(
                        //     cf"\tlh: rs: 0x${gpRegs(rs1).out}%x, offset: ${imm.asSInt}, rd: 0x${gpRegs(rd).in}%x\n"
                        // )
                    }
                    is(Opcode.Sh) {
                        io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt + 1.S).asUInt
                        io.memDataWrite := true.B
                        io.memDataIn    := gpRegs(rd).out(15, 8)
                        // printf(
                        //     cf"\tsh: rs: 0x${gpRegs(rs1).out}%x, offset: ${imm.asSInt}, rd: 0x${gpRegs(rd).in}%x\n"
                        // )
                    }
                }
                cycles := 0.U
            }
        }
    }

    when(cycles === 0.U) {
        when(allowStep) {
            when(
                !(op === Opcode.Lh || op === Opcode.Sh || op === Opcode.Jmp || op === Opcode.Jmpr || beqResult || bneResult || bltResult || bgeResult || bltuResult || bgeuResult)
            ) {
                pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U).asUInt
                pc.io.write := true.B
            }
        }
    }

    when(cycles === 1.U) {
        when(allowStep) {
            pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U).asUInt
            pc.io.write := true.B
        }
    }

}
