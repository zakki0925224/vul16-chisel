; https://github.com/zakki0925224/vul16-asm

addi r1, r0, 1
slli r1, r1, 2
j #test
addi r2, r0, 3 ; unreachable

test:
addi r3, r1, 4
slli r3, r3, 1
addi r4, r2, 5
slli r4, r4, 3
j #test
