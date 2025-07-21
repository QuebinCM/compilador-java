package lexer;

/**
 * Representa un token generado por el analizador léxico.
 * Contiene información sobre el tipo, valor, y posición en el código fuente.
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;
    
    /**
     * Constructor para crear un nuevo token.
     * 
     * @param type Tipo del token
     * @param lexeme Valor literal del token (lexema)
     * @param line Número de línea donde se encontró el token
     * @param column Número de columna donde se encontró el token
     */
    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public String getLexeme() {
        return lexeme;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    @Override
    public String toString() {
        return String.format("Token{type=%s, lexeme='%s', line=%d, column=%d}", 
                           type, lexeme, line, column);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return line == token.line && 
               column == token.column && 
               type == token.type && 
               lexeme.equals(token.lexeme);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, lexeme, line, column);
    }
}