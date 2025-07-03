package core

import chisel3._
import chisel3.util._
import core.Consts._

class Cpu extends Module {
    def signExtend(x: UInt, fromWidth: Int, toWidth: Int): UInt = {
        val sign = x(fromWidth - 1)
        val ext  = Fill(toWidth - fromWidth, sign)
        Cat(ext, x)
    }

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
                imm := 0.U(WORD_LEN.W)
            }
            is(FormatType.I) {
                rd  := inst(10, 8)
                rs1 := inst(7, 5)
                imm := signExtend(inst(4, 0), 5, WORD_LEN)
            }
            is(FormatType.J) {
                rd  := inst(10, 8)
                imm := signExtend(inst(7, 0), 8, WORD_LEN)
            }
            is(FormatType.B) {
                rs1 := inst(10, 8)
                rs2 := inst(7, 5)
                imm := signExtend(inst(4, 0), 5, WORD_LEN)
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
                out_imm := 0.U(WORD_LEN.W)
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
        val memDataReq   = Output(Bool())
        val memDataDone  = Input(Bool())
        val memInstReq   = Output(Bool())
        val memInstDone  = Input(Bool())

        val inst   = Input(UInt(WORD_LEN.W))
        val pc     = Output(UInt(WORD_LEN.W))
        val gpRegs = Output(Vec(NUM_GP_REGS, UInt(WORD_LEN.W)))

        val debugHalt = Input(Bool())
        val debugStep = Input(Bool())
    })

    io.memDataAddr  := 0.U(WORD_LEN.W)
    io.memDataIn    := 0.U(BYTE_LEN.W)
    io.memDataWrite := false.B

    // general purpose registers
    val gpRegs = VecInit(Seq.fill(NUM_GP_REGS)(Module(new Register()).io))
    for (i <- 0 until NUM_GP_REGS) {
        // zero register
        if (i == 0) {
            gpRegs(0).in    := 0.U(WORD_LEN.W)
            gpRegs(0).write := false.B
            io.gpRegs(0)    := 0.U(WORD_LEN.W)
        } else {
            gpRegs(i).in    := gpRegs(i).out
            gpRegs(i).write := false.B
            io.gpRegs(i)    := gpRegs(i).out
        }
    }

    val pc = Module(new Register(START_ADDR.U(WORD_LEN.W)))
    io.pc       := pc.io.out
    pc.io.in    := pc.io.out
    pc.io.write := false.B

    val memDataReq = RegInit(false.B)
    val memInstReq = RegInit(false.B)
    io.memDataReq := memDataReq
    io.memInstReq := memInstReq

    val alu = Module(new Alu())
    alu.io.a  := 0.U(WORD_LEN.W)
    alu.io.b  := 0.U(WORD_LEN.W)
    alu.io.op := AluOpcode.Add

    // state machine
    val sFetch :: sDecode :: sExec :: sExec2 :: Nil = Enum(4)
    val state                                       = RegInit(sFetch)

    // load / store 16 bit data instructions state
    val lshSFetch :: lshSPendingHighValue :: lshSPendingLowValue :: Nil = Enum(3)
    val lshState                                                        = RegInit(lshSFetch)

    val op  = RegInit(Opcode.Add)
    val rd  = RegInit(0.U(3.W))
    val rs1 = RegInit(0.U(3.W))
    val rs2 = RegInit(0.U(3.W))
    val imm = RegInit(0.U(WORD_LEN.W))

    val dataBuf = RegInit(0.U(BYTE_LEN.W))

    val allowStep = !io.debugHalt || (io.debugHalt && io.debugStep)

    when(allowStep) {
        switch(state) {
            is(sFetch) {
                // request instruction
                memInstReq := true.B
                state      := sDecode
            }
            is(sDecode) {
                when(io.memInstDone) {
                    memInstReq := false.B
                    val (dOp, dRd, dRs1, dRs2, dImm) = decode(io.inst)
                    op    := dOp
                    rd    := dRd
                    rs1   := dRs1
                    rs2   := dRs2
                    imm   := dImm
                    state := sExec
                    // printf(p"pc=${pc.io.out}, op=$op, rd=$rd, rs1=$rs1, rs2=$rs2, imm=$imm\n")
                }
            }
            is(sExec) {
                when(op === Opcode.Add) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Add

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Addi) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.Add

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Sub) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Sub

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.And) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.And

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Andi) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.And

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Or) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Or

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Ori) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.Or

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Xor) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Xor

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Xori) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.Xor

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Sll) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Sll

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Slli) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.Sll

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Srl) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Srl

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Srli) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.Srl

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Sra) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Sra

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Srai) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.Sra

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Slt) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Slt

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Slti) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := imm
                    alu.io.op := AluOpcode.Slt

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Sltu) {
                    alu.io.a  := gpRegs(rs1).out
                    alu.io.b  := gpRegs(rs2).out
                    alu.io.op := AluOpcode.Sltu

                    gpRegs(rd).in    := alu.io.out
                    gpRegs(rd).write := true.B

                    pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                    pc.io.write := true.B
                }.elsewhen(op === Opcode.Lb) {
                    io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                    io.memDataWrite := false.B
                    memDataReq      := true.B
                }.elsewhen(op === Opcode.Lbu) {
                    io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                    io.memDataWrite := false.B
                    memDataReq      := true.B
                }.elsewhen(op === Opcode.Lh) {
                    io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                    io.memDataWrite := false.B
                    memDataReq      := true.B
                    lshState        := lshSPendingHighValue
                }.elsewhen(op === Opcode.Sb) {
                    io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                    io.memDataIn    := gpRegs(rd).out(7, 0)
                    io.memDataWrite := true.B
                    memDataReq      := true.B
                }.elsewhen(op === Opcode.Sh) {
                    io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt + 1.S).asUInt
                    io.memDataIn    := gpRegs(rd).out(15, 8)
                    io.memDataWrite := true.B
                    memDataReq      := true.B
                    lshState        := lshSPendingHighValue
                }
                    // pc
                    .elsewhen(op === Opcode.Jmp) {
                        // regs
                        gpRegs(rd).in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        gpRegs(rd).write := true.B

                        // pc
                        pc.io.in    := (pc.io.out.asSInt + imm.asSInt).asUInt
                        pc.io.write := true.B
                    }
                    .elsewhen(op === Opcode.Jmpr) {
                        val t = pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        gpRegs(rd).in    := t
                        gpRegs(rd).write := true.B
                        pc.io.in         := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt & ~1.U
                        pc.io.write      := true.B
                    }
                    .elsewhen(op === Opcode.Beq) {
                        // pc
                        when(gpRegs(rs1).out === gpRegs(rs2).out) {
                            pc.io.in := (pc.io.out.asSInt + imm.asSInt).asUInt
                        }.otherwise {
                            pc.io.in := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        }

                        pc.io.write := true.B
                    }
                    .elsewhen(op === Opcode.Bne) {
                        // pc
                        when(gpRegs(rs1).out =/= gpRegs(rs2).out) {
                            pc.io.in := (pc.io.out.asSInt + imm.asSInt).asUInt
                        }.otherwise {
                            pc.io.in := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        }

                        pc.io.write := true.B
                    }
                    .elsewhen(op === Opcode.Blt) {
                        // pc
                        when(gpRegs(rs1).out.asSInt < gpRegs(rs2).out.asSInt) {
                            pc.io.in := (pc.io.out.asSInt + imm.asSInt).asUInt
                        }.otherwise {
                            pc.io.in := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        }

                        pc.io.write := true.B
                    }
                    .elsewhen(op === Opcode.Bge) {
                        // pc
                        when(gpRegs(rs1).out.asSInt >= gpRegs(rs2).out.asSInt) {
                            pc.io.in := (pc.io.out.asSInt + imm.asSInt).asUInt
                        }.otherwise {
                            pc.io.in := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        }

                        pc.io.write := true.B
                    }
                    .elsewhen(op === Opcode.Bltu) {
                        // pc
                        when(gpRegs(rs1).out < gpRegs(rs2).out) {
                            pc.io.in := (pc.io.out.asSInt + imm.asSInt).asUInt
                        }.otherwise {
                            pc.io.in := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        }

                        pc.io.write := true.B
                    }
                    .elsewhen(op === Opcode.Bgeu) {
                        // pc
                        when(gpRegs(rs1).out >= gpRegs(rs2).out) {
                            pc.io.in := (pc.io.out.asSInt + imm.asSInt).asUInt
                        }.otherwise {
                            pc.io.in := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        }
                        pc.io.write := true.B
                    }

                // state
                when(
                    op === Opcode.Lb ||
                        op === Opcode.Lbu ||
                        op === Opcode.Lh ||
                        op === Opcode.Sb ||
                        op === Opcode.Sh
                ) {
                    state := sExec2
                }.otherwise {
                    state := sFetch
                }
            }
            is(sExec2) {
                when(io.memDataDone) {
                    memDataReq := false.B
                    when(op === Opcode.Lb) {
                        gpRegs(rd).in    := signExtend(io.memDataOut, BYTE_LEN, WORD_LEN)
                        gpRegs(rd).write := true.B
                        pc.io.in         := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        pc.io.write      := true.B
                        state            := sFetch
                    }.elsewhen(op === Opcode.Lbu) {
                        gpRegs(rd).in    := io.memDataOut
                        gpRegs(rd).write := true.B
                        pc.io.in         := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        pc.io.write      := true.B
                        state            := sFetch
                    }.elsewhen(op === Opcode.Lh && lshState === lshSPendingHighValue) {
                        dataBuf         := io.memDataOut
                        io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt + 1.S).asUInt
                        io.memDataWrite := false.B
                        memDataReq      := true.B
                        lshState        := lshSPendingLowValue
                    }.elsewhen(op === Opcode.Lh && lshState === lshSPendingLowValue) {
                        val result = Cat(io.memDataOut, dataBuf)
                        gpRegs(rd).in    := result
                        gpRegs(rd).write := true.B
                        pc.io.in         := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        pc.io.write      := true.B
                        lshState         := lshSFetch
                        state            := sFetch
                    }.elsewhen(op === Opcode.Sb) {
                        pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        pc.io.write := true.B
                        state       := sFetch
                    }.elsewhen(op === Opcode.Sh && lshState === lshSPendingHighValue) {
                        io.memDataAddr  := (gpRegs(rs1).out.asSInt + imm.asSInt).asUInt
                        io.memDataIn    := gpRegs(rd).out(7, 0)
                        io.memDataWrite := true.B
                        memDataReq      := true.B
                        lshState        := lshSPendingLowValue
                    }.elsewhen(op === Opcode.Sh && lshState === lshSPendingLowValue) {
                        pc.io.in    := pc.io.out + (WORD_LEN.U / BYTE_LEN.U)
                        pc.io.write := true.B
                        lshState    := lshSFetch
                        state       := sFetch
                    }
                }
            }
        }
    }
}
