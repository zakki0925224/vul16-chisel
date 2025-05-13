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

| Instruction | Type | Opcode | Format           | Implementation           |
| ----------- | ---- | ------ | ---------------- | ------------------------ |
| ADD         | R    | 0x00   | add rd, rs1, rs2 | r[rd] = r[rs1] + r[rs2]  |
| ADDI        | I    | 0x01   | addi rd, rs, imm | r[rd] = r[rs] + imm      |
| SUB         | R    | 0x02   | sub rd, rs1, rs2 | r[rd] = r[rs1] - r[rs2]  |
| SUBI        | I    | 0x03   | subi rd, rs, imm | r[rd] = r[rs] - imm      |
| AND         | R    | 0x04   | and rd, rs1, rs2 | r[rd] = r[rs1] & r[rs2]  |
| ANDI        | I    | 0x05   | andi rd, rs, imm | r[rd] = r[rs] & imm      |
| OR          | R    | 0x06   | or rd, rs1, rs2  | r[rd] = r[rs1] \| r[rs2] |
| ORI         | I    | 0x07   | ori rd, rs, imm  | r[rd] = r[rs] \| imm     |
| XOR         | R    | 0x08   | xor rd, rs1, rs2 | r[rd] = r[rs1] ^ r[rs2]  |
| XORI        | I    | 0x09   | xori rd, rs, imm | r[rd] = r[rs] ^ imm      |
