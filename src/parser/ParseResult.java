package parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado del análisis sintáctico que encapsula tanto el éxito como los errores.
 */
public class ParseResult {
    private final boolean success;
    private final SLR1Parser.ParseTreeNode parseTree;
    private final List<ParserException> errors;
    private final List<String> warnings;
    private final long parseTime;
    
    /**
     * Constructor para resultado exitoso.
     */
    public ParseResult(SLR1Parser.ParseTreeNode parseTree, long parseTime) {
        this.success = true;
        this.parseTree = parseTree;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.parseTime = parseTime;
    }
    
    /**
     * Constructor para resultado con errores.
     */
    public ParseResult(List<ParserException> errors, List<String> warnings, long parseTime) {
        this.success = false;
        this.parseTree = null;
        this.errors = new ArrayList<>(errors);
        this.warnings = new ArrayList<>(warnings);
        this.parseTime = parseTime;
    }
    
    /**
     * Constructor para resultado con errores y advertencias.
     */
    public ParseResult(SLR1Parser.ParseTreeNode parseTree, List<String> warnings, long parseTime) {
        this.success = true;
        this.parseTree = parseTree;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>(warnings);
        this.parseTime = parseTime;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public SLR1Parser.ParseTreeNode getParseTree() {
        return parseTree;
    }
    
    public List<ParserException> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    public long getParseTime() {
        return parseTime;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Obtiene un resumen del resultado del parsing.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        
        if (success) {
            sb.append("✓ Análisis sintáctico completado exitosamente");
        } else {
            sb.append("✗ Análisis sintáctico falló");
        }
        
        sb.append(String.format(" (tiempo: %d ms)", parseTime));
        
        if (hasErrors()) {
            sb.append(String.format("\n  Errores: %d", errors.size()));
        }
        
        if (hasWarnings()) {
            sb.append(String.format("\n  Advertencias: %d", warnings.size()));
        }
        
        return sb.toString();
    }
    
    /**
     * Obtiene todos los mensajes de error formateados.
     */
    public String getFormattedErrors() {
        if (errors.isEmpty()) {
            return "No hay errores sintácticos.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("ERRORES SINTÁCTICOS:\n");
        
        for (int i = 0; i < errors.size(); i++) {
            ParserException error = errors.get(i);
            sb.append(String.format("%d. %s\n", i + 1, error.getDetailedMessage()));
        }
        
        return sb.toString();
    }
    
    /**
     * Obtiene todas las advertencias formateadas.
     */
    public String getFormattedWarnings() {
        if (warnings.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("ADVERTENCIAS:\n");
        
        for (int i = 0; i < warnings.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, warnings.get(i)));
        }
        
        return sb.toString();
    }
}