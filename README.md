# vul16-chisel

Vulcan-16 - 16bit CPU implemented in Chisel HDL

## Architecture

### Core Components

-   **General Purpose Registers**

#### Branch Instructions

| Instruction | Type | Opcode | Format                | Implementation                           |
| ----------- | ---- | ------ | --------------------- | ---------------------------------------- |
| BEQ         | B    | 0x1a   | beq rs1, rs2, offset  | if(r[rs1] == r[rs2]) pc += offset.i      |
| BNE         | B    | 0x1b   | bne rs1, rs2, offset  | if(r[rs1] != r[rs2]) pc += offset.i      |
| BLT         | B    | 0x1c   | blt rs1, rs2, offset  | if(r[rs1].i < rs[rs2].i) pc += offset.i  |
| BGE         | B    | 0x1d   | bge rs1, rs2, offset  | if(r[rs1].i >= rs[rs2].i) pc += offset.i |
| BLTU        | B    | 0x1e   | bltu rs1, rs2, offset | if(r[rs1] < rs[rs2]) pc += offset.i      |
| BGEU        | B    | 0x1f   | bgeu rs1, rs2, offset | if(r[rs1] >= rs[rs2]) pc += offset.i     |

## Implementation Details

### Technology Stack

-   **HDL**: Chisel 6.6.0 (Hardware Description Language)
-   **Build Tool**: sbt (Scala Build Tool)
-   **Target Output**: Verilog and SystemVerilog
-   **Testing**: ChiselTest framework

### File Structure

```
src/main/scala/core/
├── Core.scala      # Top-level CPU core with memory interface
├── Cpu.scala       # Main CPU implementation with state machine
├── Alu.scala       # Arithmetic Logic Unit
├── Register.scala  # Register file implementation
├── Consts.scala    # Constants and opcodes
└── Opcode.scala    # Instruction decoding logic
```

### Memory Interface

The CPU uses a handshake protocol for memory access:

-   **Request/Done signals**: `memInstReq`/`memInstDone` for instruction fetch
-   **Request/Done signals**: `memDataReq`/`memDataDone` for data access
-   **Address/Data buses**: Separate for instruction and data memory
-   **Write enable**: For store operation registers (R0-R7)
    -   R0: Zero register (always 0x0000)
    -   R1-R7: General purpose registers (16-bit each)
-   **Program Counter (PC)**: 16-bit register for instruction address
-   **Arithmetic Logic Unit (ALU)**: Supports arithmetic, logical, and comparison operations
-   **Memory Interface**: Byte-addressable memory with separate instruction and data access

### CPU Pipeline

The CPU implements a 4-stage state machine:

1. **Fetch**: Request instruction from memory
2. **Decode**: Decode instruction and extract operands
3. **Execute**: Perform operation using ALU or memory access
4. **Execute2**: Handle multi-cycle operations (load/store word)

### Memory Architecture

-   **Word Size**: 16 bits
-   **Byte Size**: 8 bits
-   **Address Space**: 16-bit addressing (64KB)
-   **Memory Interface**: Separate instruction and data memory access with handshake protocol
-   **Endianness**: Little-endian for word operations

### Debug Features

-   **Halt Mode**: Pause CPU execution
-   **Step Mode**: Execute single instruction when halted

## ISA (Instruction Set Architecture)

-   16-bit fixed-length instruction set inspired by RISC-V
-   4 instruction formats: R-type, I-type, J-type, B-type
-   32 instructions total (opcodes 0x00-0x1f)

### Instruction Formats

#### R-type format (Register-Register operations)

| 15-11  | 10-8 | 7-5 | 4-2 | 1-0      |
| ------ | ---- | --- | --- | -------- |
| opcode | rd   | rs1 | rs2 | reserved |

#### I-type format (Immediate operations, Load/Store)

| 15-11  | 10-8 | 7-5 | 4-0 |
| ------ | ---- | --- | --- |
| opcode | rd   | rs  | imm |

#### J-type format (Jump operations)

| 15-11  | 10-8 | 7-0    |
| ------ | ---- | ------ |
| opcode | rd   | offset |

#### B-type format (Branch operations)

| 15-11  | 10-8 | 7-5 | 4-0    |
| ------ | ---- | --- | ------ |
| opcode | rs1  | rs2 | offset |

### Instruction Set

**Legend:**

-   `pc` - Program Counter
-   `r[X]` - General Purpose Register X
-   `m[X]` - Memory at address X
-   `X.i` - Signed interpretation of X
-   `X[M:N]` - Bit range from M to N

#### Arithmetic Instructions

| Instruction | Type | Opcode | Format           | Implementation              |
| ----------- | ---- | ------ | ---------------- | --------------------------- |
| ADD         | R    | 0x00   | add rd, rs1, rs2 | r[rd] = r[rs1].i + r[rs2].i |
| ADDI        | I    | 0x01   | addi rd, rs, imm | r[rd] = r[rs].i + imm.i     |
| SUB         | R    | 0x02   | sub rd, rs1, rs2 | r[rd] = r[rs1].i - r[rs2].i |

#### Logical Instructions

| Instruction | Type | Opcode | Format           | Implementation           |
| ----------- | ---- | ------ | ---------------- | ------------------------ |
| AND         | R    | 0x03   | and rd, rs1, rs2 | r[rd] = r[rs1] & r[rs2]  |
| ANDI        | I    | 0x04   | andi rd, rs, imm | r[rd] = r[rs] & imm      |
| OR          | R    | 0x05   | or rd, rs1, rs2  | r[rd] = r[rs1] \| r[rs2] |
| ORI         | I    | 0x06   | ori rd, rs, imm  | r[rd] = r[rs] \| imm     |
| XOR         | R    | 0x07   | xor rd, rs1, rs2 | r[rd] = r[rs1] ^ r[rs2]  |
| XORI        | I    | 0x08   | xori rd, rs, imm | r[rd] = r[rs] ^ imm      |

#### Shift Instructions

| Instruction | Type | Opcode | Format           | Implementation                  |
| ----------- | ---- | ------ | ---------------- | ------------------------------- |
| SLL         | R    | 0x09   | sll rd, rs1, rs2 | r[rd] = r[rs1] << r[rs2][4:0]   |
| SLLI        | I    | 0x0a   | slli rd, rs, imm | r[rd] = r[rs] << imm[4:0]       |
| SRL         | R    | 0x0b   | srl rd, rs1, rs2 | r[rd] = r[rs1] >> r[rs2][4:0]   |
| SRLI        | I    | 0x0c   | srli rd, rs, imm | r[rd] = r[rs] >> imm[4:0]       |
| SRA         | R    | 0x0d   | sra rd, rs1, rs2 | r[rd] = r[rs1].i >> r[rs2][4:0] |
| SRAI        | I    | 0x0e   | srai rd, rs, imm | r[rd] = r[rs].i >> imm[4:0]     |

#### Comparison Instructions

| Instruction | Type | Opcode | Format            | Implementation                        |
| ----------- | ---- | ------ | ----------------- | ------------------------------------- |
| SLT         | R    | 0x0f   | slt rd, rs1, rs2  | r[rd] = (r[rs1].i < r[rs2].i) ? 1 : 0 |
| SLTI        | I    | 0x10   | slti rd, rs, imm  | r[rd] = (r[rs].i < imm.i) ? 1 : 0     |
| SLTU        | R    | 0x11   | sltu rd, rs1, rs2 | r[rd] = (r[rs1] < r[rs2]) ? 1 : 0     |
| SLTIU       | I    | 0x12   | sltiu rd, rs, imm | r[rd] = (r[rs] < imm) ? 1 : 0         |

#### Load/Store Instructions

| Instruction | Type | Opcode | Format             | Implementation                                                          |
| ----------- | ---- | ------ | ------------------ | ----------------------------------------------------------------------- |
| LB          | I    | 0x13   | lb rd, rs, offset  | r[rd] = m[r[rs] + offset.i].i (sign-extended byte)                      |
| LBU         | I    | 0x14   | lbu rd, rs, offset | r[rd] = m[r[rs] + offset.i] (zero-extended byte)                        |
| LW          | I    | 0x15   | lw rd, rs, offset  | r[rd] = (m[r[rs] + offset.i + 1] << 8) \| m[r[rs] + offset.i]           |
| SB          | I    | 0x16   | sb rd, rs, offset  | m[r[rs] + offset.i] = r[rd][7:0]                                        |
| SW          | I    | 0x17   | sw rd, rs, offset  | m[r[rs] + offset.i] = r[rd][7:0], m[r[rs] + offset.i + 1] = r[rd][15:8] |

#### Jump Instructions

| Instruction | Type | Opcode | Format              | Implementation                                      |
| ----------- | ---- | ------ | ------------------- | --------------------------------------------------- |
| JMP         | J    | 0x18   | jmp rd, offset      | r[rd] = pc + 2, pc += offset.i                      |
| JMPR        | I    | 0x19   | jmpr rd, rs, offset | t = pc + 2, pc = (r[rs] + offset.i) & ~1, r[rd] = t |

#### Branch Instructions

| BEQ  | B   | 0x1a | beq rs1, rs2, offset  | if(r[rs1] == r[rs2]) pc += offset.i     |
| ---- | --- | ---- | --------------------- | --------------------------------------- |
| BNE  | B   | 0x1b | bne rs1, rs2, offset  | if(r[rs1] != r[rs2]) pc += offset.i     |
| BLT  | B   | 0x1c | blt rs1, rs2, offset  | if(r[rs1].i < r[rs2].i) pc += offset.i  |
| BGE  | B   | 0x1d | bge rs1, rs2, offset  | if(r[rs1].i >= r[rs2].i) pc += offset.i |
| BLTU | B   | 0x1e | bltu rs1, rs2, offset | if(r[rs1] < r[rs2]) pc += offset.i      |
| BGEU | B   | 0x1f | bgeu rs1, rs2, offset | if(r[rs1] >= r[rs2]) pc += offset.i     |

## Other tools

-   [vul16-asm](https://github.com/zakki0925224/vul16-asm) - Assembler
-   [vul16-tang-primer-20k](https://github.com/zakki0925224/vul16-tang-primer-20k) - vul16 implementation for Sipeed Tang Primer 20K

## Usage

### Prerequisites

-   Java 8 or higher
-   sbt (Scala Build Tool)

### Build and Run

```bash
git clone https://github.com/zakki0925224/vul16-chisel.git
cd vul16-chisel

# Generate Verilog files
sbt run

# Run all tests
sbt test
```

### Generated Output

The generated Verilog files will be placed in the `output/` directory:

-   `Core.sv` - SystemVerilog output

### Testing

The project includes comprehensive tests:c

-   **Unit tests**: ALU operations, register functionality
-   **Integration tests**: Full CPU instruction execution
-   **Assembly tests**: Real program execution
