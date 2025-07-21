package semantic;

import lexer.Token;

public class SemanticError {
    private final String message;
    private final int line;
    private final int column;

    public SemanticError(String message, Token token) {
        this.message = message;
        this.line = token.getLine();
        this.column = token.getColumn();
    }

    public SemanticError(String message, int line, int column) {
        this.message = message;
        this.line = line;
        this.column = column;
    }

    // Getters
    public String getMessage() { return message; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        return String.format("Error semántico en línea %d, columna %d: %s", line, column, message);
    }
}