; https://github.com/zakki0925224/vul16-asm

j #main
addi r0, r0, 0
nop
nop
nop

main:
    addi r0, r0, 1
    addi r0, r0, 2
    addi r0, r0, 3
    ret ; return to addi r0, r0, 0
