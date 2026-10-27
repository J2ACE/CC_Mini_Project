import { SampleProgram } from '../types';

export const DEFAULT_SAMPLES: SampleProgram[] = [
  {
    id: "demo",
    name: "Compiler Construction Mini-Project Demo",
    description: "Demonstrates constant propagation, constant folding, algebraic simplification, and branch analysis.",
    sourceCode: `// Welcome to CodeInsight Static Code Analyzer!
// Compiler Construction Mini-Project Demo
int base = 10;
int height = 5;

// Constant Folding & Arithmetic:
int area = (base * height) / 2;

// Optimization candidate (x + 0):
int adjusted = area + 0;

if (adjusted > 20) {
    int bonus = 5;
    adjusted = adjusted + bonus;
}

return adjusted;`
  },
  {
    id: "showcase",
    name: "Full Optimizer Showcase",
    description: "Demonstrates constant folding, algebraic identities, strength reduction, dead stores, and unreachable code in a single program.",
    sourceCode: `int compute(int x, int y) {
    // 1. Constant Folding opportunity (10 + 20 * 2 -> 50)
    int base = 10 + 20 * 2;

    // 2. Algebraic Simplification (x * 1 + 0 -> x)
    int adjusted = x * 1 + 0;

    // 3. Redundant / Dead assignment
    int temp = 999;
    temp = base + adjusted;

    // 4. Strength Reduction (temp * 2 -> temp << 1)
    int doubled = temp * 2;

    // 5. Common Subexpression Elimination
    int part1 = (x + y) * 3;
    int part2 = (x + y) + 7;

    // 6. Unused variable
    int unusedVar = 100;

    // 7. Loop invariant code motion
    int sum = 0;
    int i = 0;
    while (i < 10) {
        int invariantVal = x * 5 + 4;
        sum = sum + invariantVal + i;
        i = i + 1;
    }

    // 8. Tautological condition & unreachable code
    if (10 > 5) {
        return sum + doubled + part1 + part2;
    } else {
        int unreachableVal = 404;
        return 0;
    }
}`
  },
  {
    id: "constant_folding",
    name: "Constant Folding & Propagation",
    description: "Evaluates compile-time literal math and propagates known constant definitions forward.",
    sourceCode: `int calculateConstants() {
    int secondsInDay = 60 * 60 * 24;
    int magic = 100 + 250 - 50;
    int a = 15;
    int b = a + 10;
    return secondsInDay + magic + b;
}`
  },
  {
    id: "dead_code",
    name: "Dead Code & Unreachable CFG",
    description: "Highlights basic blocks that have no incoming execution path from ENTRY.",
    sourceCode: `int testReachability(int status) {
    int result = 0;
    if (status > 0) {
        return 1;
    } else {
        return -1;
    }
    // Statements after returns in both branches are unreachable
    result = 999;
    return result;
}`
  },
  {
    id: "semantic_errors",
    name: "Semantic & Type Violations",
    description: "Triggers compiler type checking diagnostics, arity mismatches, and undeclared identifiers.",
    sourceCode: `int multiply(int a, int b) {
    return a * b;
}

int testErrors() {
    int num = true; // SEM004: Type mismatch
    badVar = 50; // SEM001: Undeclared
    multiply(10); // SEM008: Arity mismatch
    if (123) { // SEM005: Non-boolean condition
        return 0;
    }
    return num;
}`
  }
];
