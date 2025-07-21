package semantic;

import java.util.List;

public class SemanticAnalysisResult {
    private final List<SemanticError> errors;
    private final SymbolTable symbolTable;

    public SemanticAnalysisResult(List<SemanticError> errors, SymbolTable symbolTable) {
        this.errors = errors;
        this.symbolTable = symbolTable;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public String getSummary() {
        return String.format("Análisis semántico completado. %d errores encontrados.", errors.size());
    }
}