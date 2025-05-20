# vul16-chisel

Vulcan-16 - 16bit CPU

## Architecture

-   General Purpose Register (R0-R7)
-   Program Counter (PC)

## ISA

-   16bit fixed-length instruction set like RISC-V

### Type-R format

| 15-11  | 10-8 | 7-5 | 4-2 | 1-0      |
| ------ | ---- | --- | --- | -------- |
| opcode | rd   | rs1 | rs2 | reserved |

### Type-I format

| 15-11  | 10-8 | 7-5 | 4-0 |
| ------ | ---- | --- | --- |
| opcode | rd   | rs  | imm |

### Type-J format

| 15-11  | 10-8 | 7-0    |
| ------ | ---- | ------ |
| opcode | rd   | offset |

### Type-B format

| 15-11  | 10-8 | 7-5 | 4-0    |
| ------ | ---- | --- | ------ |
| opcode | rs1  | rs2 | offset |

### List

-   pc - Program Counter
-   r[X] - General Purpose Register
-   m[X] - Memory
-   X.i - Signed
-   X[M:N] - Bit range

| Instruction | Type | Opcode | Format                  | Implementation                                                      |
| ----------- | ---- | ------ | ----------------------- | ------------------------------------------------------------------- |
| ADD         | R    | 0x00   | add rd, rs1, rs2        | r[rd] = r[rs1].i + r[rs2].i                                         |
| ADDI        | I    | 0x01   | addi rd, rs, imm        | r[rd] = r[rs].i + imm.i                                             |
| SUB         | R    | 0x02   | sub rd, rs1, rs2        | r[rd] = r[rs1].i - r[rs2].i                                         |
| AND         | R    | 0x03   | and rd, rs1, rs2        | r[rd] = r[rs1] & r[rs2]                                             |
| ANDI        | I    | 0x04   | andi rd, rs, imm        | r[rd] = r[rs] & imm                                                 |
| OR          | R    | 0x05   | or rd, rs1, rs2         | r[rd] = r[rs1] \| r[rs2]                                            |
| ORI         | I    | 0x06   | ori rd, rs, imm         | r[rd] = r[rs] \| imm                                                |
| XOR         | R    | 0x07   | xor rd, rs1, rs2        | r[rd] = r[rs1] ^ r[rs2]                                             |
| XORI        | I    | 0x08   | xori rd, rs, imm        | r[rd] = r[rs] ^ imm                                                 |
| SLL         | R    | 0x09   | sll rd, rs1, rs2        | r[rd] = r[rs1] << r[rs2][4:0]                                       |
| SLLI        | I    | 0x0a   | slli rd, rs, imm        | r[rd] = r[rs] << imm[4:0]                                           |
| SRL         | R    | 0x0b   | srl rd, rs1, rs2        | r[rd] = r[rs1] >> r[rs2][4:0]                                       |
| SRLI        | I    | 0x0c   | srli rd, rs, imm        | r[rd] = r[rs] >> imm[4:0]                                           |
| SRA         | R    | 0x0d   | sra rd, rs1, rs2        | r[rd] = r[rs1].i >> r[rs2][4:0]                                     |
| SRAI        | I    | 0x0e   | srai rd, rs, imm        | r[rd] = r[rs].i >> imm[4:0]                                         |
| SLT         | R    | 0x0f   | slt rd, rs1, rs2        | r[rd] = (r[rs1].i < r[rs2].i) ? 1 : 0                               |
| SLTI        | I    | 0x10   | slti rd, rs, imm        | r[rd] = (r[rs].i < imm.i) ? 1 : 0                                   |
| SLTU        | R    | 0x11   | sltu rd, rs1, rs2       | r[rd] = (r[rs1] < r[rs2]) ? 1 : 0                                   |
| SLTIU       | I    | 0x12   | sltiu rd, rs, imm       | r[rd] = (r[rs] < imm) ? 1 : 0                                       |
| LB          | I    | 0x13   | lb rd, rs, **offset**   | r[rd] = m[r[rs] + offset.i].i                                       |
| LBU         | I    | 0x14   | lbu rd, rs, **offset**  | r[rd] = m[r[rs] + offset.i]                                         |
| LH          | I    | 0x15   | lh rd, rs, **offset**   | r[rd] = (m[r[rs] + offset.i + 1] << 8) \| m[r[rs] + offset.i].i     |
| SB          | I    | 0x16   | sb rd, rs, **offset**   | m[r[rs] + offset.i] = r[rd]                                         |
| SH          | I    | 0x17   | sh rd, rs, **offset**   | m[r[rs] + offset.i] = r[rd], m[r[rs] + offset.i + 1] = (r[rd] >> 8) |
| JMP         | J    | 0x18   | jmp rd, offset          | r[rd] = pc + 2, pc += offset.i                                      |
| JMPR        | I    | 0x19   | jmpr rd, rs, **offset** | t = pc + 2, pc = (r[rs] + offset.i) & ~1, r[rd] = t                 |
| BEQ         | B    | 0x1a   | beq rs1, rs2, offset    | if(r[rs1] == r[rs2]) pc += offset.i                                 |
| BNE         | B    | 0x1b   | bne rs1, rs2, offset    | if(r[rs1] != rs[rs2]) pc += offset.i                                |
| BLT         | B    | 0x1c   | blt rs1, rs2, offset    | if(r[rs1].i < rs[rs2].i) pc += offset.i                             |
| BGE         | B    | 0x1d   | bge rs1, rs2, offset    | if(r[rs1].i >= rs[rs2].i) pc += offset.i                            |
| BLTU        | B    | 0x1e   | bltu rs1, rs2, offset   | if(r[rs1] < rs[rs2]) pc += offset.i                                 |
| BGEU        | B    | 0x1f   | bgeu rs1, rs2, offset   | if(r[rs1] >= rs[rs2]) pc += offset.i                                |
