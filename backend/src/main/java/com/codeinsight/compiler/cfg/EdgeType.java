package com.codeinsight.compiler.cfg;

public enum EdgeType {
    FALLTHROUGH,
    BRANCH_TRUE,
    BRANCH_FALSE,
    LOOP_BACK,
    RETURN_EDGE
}
