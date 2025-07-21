package parser;

import lexer.Token;

/**
 * Excepción lanzada cuando el analizador sintáctico encuentra un error.
 * Proporciona información detallada sobre la posición del error y símbolos esperados.
 */
public class ParserException extends Exception {
    private final Token errorToken;
    private final String expectedSymbols;
    private final int errorState;
    
    /**
     * Constructor básico para errores de parsing.
     * 
     * @param message Mensaje descriptivo del error
     * @param errorToken Token donde ocurrió el error
     */
    public ParserException(String message, Token errorToken) {
        super(message);
        this.errorToken = errorToken;
        this.expectedSymbols = "";
        this.errorState = -1;
    }
    
    /**
     * Constructor completo para errores de parsing.
     * 
     * @param message Mensaje descriptivo del error
     * @param errorToken Token donde ocurrió el error
     * @param expectedSymbols Símbolos que se esperaban en ese punto
     */
    public ParserException(String message, Token errorToken, String expectedSymbols) {
        super(message);
        this.errorToken = errorToken;
        this.expectedSymbols = expectedSymbols;
        this.errorState = -1;
    }
    
    /**
     * Constructor con información del estado del parser.
     * 
     * @param message Mensaje descriptivo del error
     * @param errorToken Token donde ocurrió el error
     * @param expectedSymbols Símbolos que se esperaban
     * @param errorState Estado del parser donde ocurrió el error
     */
    public ParserException(String message, Token errorToken, String expectedSymbols, int errorState) {
        super(message);
        this.errorToken = errorToken;
        this.expectedSymbols = expectedSymbols;
        this.errorState = errorState;
    }
    
    /**
     * Obtiene el token donde ocurrió el error.
     * 
     * @return Token del error
     */
    public Token getErrorToken() {
        return errorToken;
    }
    
    /**
     * Obtiene los símbolos que se esperaban.
     * 
     * @return Cadena con los símbolos esperados
     */
    public String getExpectedSymbols() {
        return expectedSymbols;
    }
    
    /**
     * Obtiene el estado del parser donde ocurrió el error.
     * 
     * @return Estado del parser (-1 si no está disponible)
     */
    public int getErrorState() {
        return errorState;
    }
    
    /**
     * Obtiene la línea donde ocurrió el error.
     * 
     * @return Número de línea
     */
    public int getLine() {
        return errorToken != null ? errorToken.getLine() : -1;
    }
    
    /**
     * Obtiene la columna donde ocurrió el error.
     * 
     * @return Número de columna
     */
    public int getColumn() {
        return errorToken != null ? errorToken.getColumn() : -1;
    }
    
    /**
     * Genera un mensaje de error detallado.
     * 
     * @return Mensaje formateado con toda la información disponible
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        
        if (errorToken != null) {
            sb.append("\n  Token: ").append(errorToken.getLexeme());
            sb.append(" (").append(errorToken.getType()).append(")");
            sb.append(" at line ").append(errorToken.getLine());
            sb.append(", column ").append(errorToken.getColumn());
        }
        
        if (expectedSymbols != null && !expectedSymbols.isEmpty()) {
            sb.append("\n  Expected: ").append(expectedSymbols);
        }
        
        if (errorState >= 0) {
            sb.append("\n  Parser state: ").append(errorState);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ParserException: " + getDetailedMessage();
    }
}