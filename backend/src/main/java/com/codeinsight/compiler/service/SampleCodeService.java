package com.codeinsight.compiler.service;

import com.codeinsight.web.dto.SampleProgramDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SampleCodeService {
    private final List<SampleProgramDto> samples = new ArrayList<>();

    public SampleCodeService() {
        samples.add(new SampleProgramDto(
                "demo",
                "Compiler Construction Mini-Project Demo",
                "Demonstrates constant propagation, constant folding, algebraic simplification, and branch analysis.",
                "// Welcome to CodeInsight Static Code Analyzer!\n" +
                "// Compiler Construction Mini-Project Demo\n" +
                "int base = 10;\n" +
                "int height = 5;\n" +
                "\n" +
                "// Constant Folding & Arithmetic:\n" +
                "int area = (base * height) / 2;\n" +
                "\n" +
                "// Optimization candidate (x + 0):\n" +
                "int adjusted = area + 0;\n" +
                "\n" +
                "if (adjusted > 20) {\n" +
                "    int bonus = 5;\n" +
                "    adjusted = adjusted + bonus;\n" +
                "}\n" +
                "\n" +
                "return adjusted;\n"
        ));

        samples.add(new SampleProgramDto(
                "showcase",
                "Full Optimizer Showcase",
                "Demonstrates constant folding, algebraic identities, strength reduction, dead stores, and unreachable code in a single program.",
                "int compute(int x, int y) {\n" +
                "    // 1. Constant Folding opportunity (10 + 20 * 2 -> 50)\n" +
                "    int base = 10 + 20 * 2;\n" +
                "\n" +
                "    // 2. Algebraic Simplification (x * 1 + 0 -> x)\n" +
                "    int adjusted = x * 1 + 0;\n" +
                "\n" +
                "    // 3. Redundant / Dead assignment\n" +
                "    int temp = 999;\n" +
                "    temp = base + adjusted;\n" +
                "\n" +
                "    // 4. Strength Reduction (temp * 2 -> temp << 1)\n" +
                "    int doubled = temp * 2;\n" +
                "\n" +
                "    // 5. Common Subexpression Elimination\n" +
                "    int part1 = (x + y) * 3;\n" +
                "    int part2 = (x + y) + 7;\n" +
                "\n" +
                "    // 6. Unused variable\n" +
                "    int unusedVar = 100;\n" +
                "\n" +
                "    // 7. Loop invariant code motion\n" +
                "    int sum = 0;\n" +
                "    int i = 0;\n" +
                "    while (i < 10) {\n" +
                "        int invariantVal = x * 5 + 4;\n" +
                "        sum = sum + invariantVal + i;\n" +
                "        i = i + 1;\n" +
                "    }\n" +
                "\n" +
                "    // 8. Tautological condition & unreachable code\n" +
                "    if (10 > 5) {\n" +
                "        return sum + doubled + part1 + part2;\n" +
                "    } else {\n" +
                "        int unreachableVal = 404;\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n"
        ));

        samples.add(new SampleProgramDto(
                "constant_folding",
                "Constant Folding & Propagation",
                "Evaluates compile-time literal math and propagates known constant definitions forward.",
                "int calculateConstants() {\n" +
                "    int secondsInDay = 60 * 60 * 24;\n" +
                "    int magic = 100 + 250 - 50;\n" +
                "    int a = 15;\n" +
                "    int b = a + 10;\n" +
                "    return secondsInDay + magic + b;\n" +
                "}\n"
        ));

        samples.add(new SampleProgramDto(
                "dead_code",
                "Dead Code & Unreachable CFG",
                "Highlights basic blocks that have no incoming execution path from ENTRY.",
                "int testReachability(int status) {\n" +
                "    int result = 0;\n" +
                "    if (status > 0) {\n" +
                "        return 1;\n" +
                "    } else {\n" +
                "        return -1;\n" +
                "    }\n" +
                "    // Statements after returns in both branches are unreachable\n" +
                "    result = 999;\n" +
                "    return result;\n" +
                "}\n"
        ));

        samples.add(new SampleProgramDto(
                "semantic_errors",
                "Semantic & Type Violations",
                "Triggers compiler type checking diagnostics, arity mismatches, and undeclared identifiers.",
                "int multiply(int a, int b) {\n" +
                "    return a * b;\n" +
                "}\n\n" +
                "int testErrors() {\n" +
                "    int num = true; // SEM004: Type mismatch\n" +
                "    undeclaredVar = 42; // SEM001: Undeclared\n" +
                "    multiply(10); // SEM008: Arity mismatch\n" +
                "    if (123) { // SEM005: Non-boolean condition\n" +
                "        return 0;\n" +
                "    }\n" +
                "    return num;\n" +
                "}\n"
        ));
    }

    public List<SampleProgramDto> getSamples() {
        return Collections.unmodifiableList(samples);
    }
}
