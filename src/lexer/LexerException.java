package lexer;

/**
 * Excepción lanzada cuando el analizador léxico encuentra un error.
 * Proporciona información detallada sobre la posición del error.
 */
public class LexerException extends Exception {
    private final int line;
    private final int column;
    private final char unexpectedChar;
    
    /**
     * Constructor para crear una excepción léxica.
     * 
     * @param message Mensaje descriptivo del error
     * @param line Línea donde ocurrió el error
     * @param column Columna donde ocurrió el error
     * @param unexpectedChar Carácter que causó el error
     */
    public LexerException(String message, int line, int column, char unexpectedChar) {
        super(String.format("%s en línea %d, columna %d: carácter inesperado '%c'", 
                          message, line, column, unexpectedChar));
        this.line = line;
        this.column = column;
        this.unexpectedChar = unexpectedChar;
    }
    
    /**
     * Constructor alternativo sin carácter específico.
     */
    public LexerException(String message, int line, int column) {
        super(String.format("%s en línea %d, columna %d", message, line, column));
        this.line = line;
        this.column = column;
        this.unexpectedChar = '\0';
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public char getUnexpectedChar() {
        return unexpectedChar;
    }
}