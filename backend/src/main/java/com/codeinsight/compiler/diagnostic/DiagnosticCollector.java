package com.codeinsight.compiler.diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiagnosticCollector {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void add(Diagnostic diagnostic) {
        if (diagnostic != null) {
            diagnostics.add(diagnostic);
        }
    }

    public void addAll(List<Diagnostic> items) {
        if (items != null) {
            diagnostics.addAll(items);
        }
    }

    public List<Diagnostic> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(d -> d.getSeverity() == Severity.ERROR);
    }

    public long countBySeverity(Severity severity) {
        return diagnostics.stream().filter(d -> d.getSeverity() == severity).count();
    }
}
