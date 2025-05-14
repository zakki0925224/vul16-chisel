# vul16-chisel

Vulcan-16 - 16bit CPU

## Architecture

-   General Purpose Register (R0-R7)
-   Program Counter (PC)
-   Control and Status Register (CSR)

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

### List

-   X.i - Signed
-   X[M:N] - Bit range

| Instruction | Type | Opcode | Format            | Implementation                        |
| ----------- | ---- | ------ | ----------------- | ------------------------------------- |
| ADD         | R    | 0x00   | add rd, rs1, rs2  | r[rd] = r[rs1].i + r[rs2].i           |
| ADDI        | I    | 0x01   | addi rd, rs, imm  | r[rd] = r[rs].i + imm.i               |
| SUB         | R    | 0x02   | sub rd, rs1, rs2  | r[rd] = r[rs1].i - r[rs2].i           |
| SUBI        | I    | 0x03   | subi rd, rs, imm  | r[rd] = r[rs].i - imm.i               |
| AND         | R    | 0x04   | and rd, rs1, rs2  | r[rd] = r[rs1] & r[rs2]               |
| ANDI        | I    | 0x05   | andi rd, rs, imm  | r[rd] = r[rs] & imm                   |
| OR          | R    | 0x06   | or rd, rs1, rs2   | r[rd] = r[rs1] \| r[rs2]              |
| ORI         | I    | 0x07   | ori rd, rs, imm   | r[rd] = r[rs] \| imm                  |
| XOR         | R    | 0x08   | xor rd, rs1, rs2  | r[rd] = r[rs1] ^ r[rs2]               |
| XORI        | I    | 0x09   | xori rd, rs, imm  | r[rd] = r[rs] ^ imm                   |
| SLL         | R    | 0x0a   | sll rd, rs1, rs2  | r[rd] = r[rs1] << r[rs2][4:0]         |
| SLLI        | I    | 0x0b   | slli rd, rs, imm  | r[rd] = r[rs] << imm[4:0]             |
| SRL         | R    | 0x0c   | srl rd, rs1, rs2  | r[rd] = r[rs1] >> r[rs2][4:0]         |
| SRLI        | I    | 0x0d   | srli rd, rs, imm  | r[rd] = r[rs] >> imm[4:0]             |
| SRA         | R    | 0x0e   | sra rd, rs1, rs2  | r[rd] = r[rs1].i >> r[rs2][4:0]       |
| SRAI        | I    | 0x0f   | srai rd, rs, imm  | r[rd] = r[rs].i >> imm[4:0]           |
| SLT         | R    | 0x10   | slt rd, rs1, rs2  | r[rd] = (r[rs1].i < r[rs2].i) ? 1 : 0 |
| SLTI        | I    | 0x11   | slti rd, rs, imm  | r[rd] = (r[rs].i < imm.i) ? 1 : 0     |
| SLTU        | R    | 0x12   | sltu rd, rs1, rs2 | r[rd] = (r[rs1] < r[rs2]) ? 1 : 0     |
| SLTIU       | I    | 0x13   | sltiu rd, rs, imm | r[rd] = (r[rs] < imm) ? 1 : 0         |
