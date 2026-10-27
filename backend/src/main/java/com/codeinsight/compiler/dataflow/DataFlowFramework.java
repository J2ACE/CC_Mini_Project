package com.codeinsight.compiler.dataflow;

import com.codeinsight.compiler.cfg.BasicBlock;
import java.util.Collection;
import java.util.Set;

public interface DataFlowFramework<T> {
    boolean isForward();
    Set<T> getInitialIn(BasicBlock block);
    Set<T> getInitialOut(BasicBlock block);
    Set<T> confluence(Collection<BasicBlock> predecessorsOrSuccessors);
    Set<T> transfer(BasicBlock block, Set<T> inOrOut);
}
