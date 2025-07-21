package lexer;

import java.util.*;

/**
 * Analizador léxico que convierte código fuente en una secuencia de tokens.
 * Implementa reconocimiento de palabras reservadas, identificadores, operadores,
 * delimitadores y números enteros.
 */
public class Lexer {
    private final String source;
    private final int length;
    private int current;
    private int line;
    private int column;
    private int lineStart; // Índice donde inicia la línea actual
    
    // Mapa de palabras reservadas para búsqueda eficiente
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    
    static {
        KEYWORDS.put("boolean", TokenType.BOOLEAN);
        KEYWORDS.put("byte", TokenType.BYTE);
        KEYWORDS.put("char", TokenType.CHAR);
        KEYWORDS.put("switch", TokenType.SWITCH);
        KEYWORDS.put("break", TokenType.BREAK);
        KEYWORDS.put("default", TokenType.DEFAULT);
        KEYWORDS.put("do", TokenType.DO);
        KEYWORDS.put("double", TokenType.DOUBLE);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("float", TokenType.FLOAT);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("int", TokenType.INT);
        KEYWORDS.put("long", TokenType.LONG);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("short", TokenType.SHORT);
        KEYWORDS.put("String", TokenType.STRING);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("void", TokenType.VOID);
        KEYWORDS.put("while", TokenType.WHILE);
        KEYWORDS.put("case", TokenType.CASE);
        KEYWORDS.put("class", TokenType.CLASS);
        KEYWORDS.put("public", TokenType.PUBLIC);
        KEYWORDS.put("private", TokenType.PRIVATE);
        KEYWORDS.put("new", TokenType.NEW);
        KEYWORDS.put("this", TokenType.THIS);
    }
    
    /**
     * Constructor que inicializa el lexer con el código fuente.
     * 
     * @param source Código fuente a analizar
     */
    public Lexer(String source) {
        this.source = source != null ? source : "";
        this.length = this.source.length();
        this.current = 0;
        this.line = 1;
        this.column = 1;
        this.lineStart = 0;
    }
    
    /**
     * Analiza todo el código fuente y retorna una lista de tokens.
     * 
     * @return Lista de tokens encontrados
     * @throws LexerException Si encuentra un carácter no reconocido
     */
    public List<Token> tokenize() throws LexerException {
        List<Token> tokens = new ArrayList<>();
        
        while (!isAtEnd()) {
            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }
        
        // Agregar token EOF al final
        //tokens.add(new Token(TokenType.EOF, "EOF", line, column));
        return tokens;
    }
    
    /**
     * Obtiene el siguiente token del código fuente.
     * 
     * @return El siguiente token o null si es whitespace
     * @throws LexerException Si encuentra un carácter no reconocido
     */
    public Token nextToken() throws LexerException {
        skipWhitespace();
        
        if (isAtEnd()) {
            return null;
        }
        
        int tokenLine = line;
        int tokenColumn = column;
        char c = advance();
        
        // Literales de cadena
        if (c == '"') {
            return stringLiteral(tokenLine, tokenColumn);
        }

        // Literales de carácter
        if (c == '\'') {
            return charLiteral(tokenLine, tokenColumn);
        }

        // Números (enteros y flotantes)
        if (isDigit(c)) {
            return number(tokenLine, tokenColumn);
        }
        
        // Identificadores y palabras reservadas
        if (isAlpha(c)) {
            return identifier(tokenLine, tokenColumn);
        }
        
        // Operadores y delimitadores
        switch (c) {
            case '*':
                if (match('=')) {
                    return new Token(TokenType.MULTIPLY_ASSIGN, "*=", tokenLine, tokenColumn);
                }
            case '/':
                if (match('=')) {
                    return new Token(TokenType.DIVIDE_ASSIGN, "/=", tokenLine, tokenColumn);
                }
            case '%':
                if (match('=')) {
                    return new Token(TokenType.MODULO_ASSIGN, "%=", tokenLine, tokenColumn);
                }
                return new Token(TokenType.MODULO, "%", tokenLine, tokenColumn);
            case ';': return new Token(TokenType.SEMICOLON, ";", tokenLine, tokenColumn);
            case '(': return new Token(TokenType.LEFT_PAREN, "(", tokenLine, tokenColumn);
            case ')': return new Token(TokenType.RIGHT_PAREN, ")", tokenLine, tokenColumn);
            case '{': return new Token(TokenType.LEFT_BRACE, "{", tokenLine, tokenColumn);
            case '}': return new Token(TokenType.RIGHT_BRACE, "}", tokenLine, tokenColumn);
            case ',': return new Token(TokenType.COMMA, ",", tokenLine, tokenColumn);
            case '.': return new Token(TokenType.DOT, ".", tokenLine, tokenColumn);
            case '@': return new Token(TokenType.AT, "@", tokenLine, tokenColumn);
            case '[': return new Token(TokenType.LEFT_BRACKET, "[", tokenLine, tokenColumn);
            case ']': return new Token(TokenType.RIGHT_BRACKET, "]", tokenLine, tokenColumn);
            case '\n': 
                newLine();
                //return new Token(TokenType.NEWLINE, "\\n", tokenLine, tokenColumn);
                return null;
            
            // Operadores de dos caracteres
            case '+':
                if (match('+')) {
                    return new Token(TokenType.INCREMENT, "++", tokenLine, tokenColumn);
                }
                if (match('=')) {
                    return new Token(TokenType.PLUS_ASSIGN, "+=", tokenLine, tokenColumn);
                }
                return new Token(TokenType.PLUS, "+", tokenLine, tokenColumn);

            case '-':
                if (match('-')) {
                    return new Token(TokenType.DECREMENT, "--", tokenLine, tokenColumn);
                }
                if (match('=')) {
                    return new Token(TokenType.MINUS_ASSIGN, "-=", tokenLine, tokenColumn);
                }
                return new Token(TokenType.MINUS, "-", tokenLine, tokenColumn);
            case ':':
                if (match('=')) {
                    return new Token(TokenType.ASSIGN, ":=", tokenLine, tokenColumn);
                }
                return new Token(TokenType.COLON, ":", tokenLine, tokenColumn);
                
            case '=':
                if (match('=')) {
                    return new Token(TokenType.EQUAL_EQUAL, "==", tokenLine, tokenColumn);
                }
                return new Token(TokenType.EQUALS, "=", tokenLine, tokenColumn);
                
            case '>':
                if (match('=')) {
                    return new Token(TokenType.GREATER_EQUAL, ">=", tokenLine, tokenColumn);
                }
                return new Token(TokenType.GREATER, ">", tokenLine, tokenColumn);
                
            case '<':
                if (match('=')) {
                    return new Token(TokenType.LESS_EQUAL, "<=", tokenLine, tokenColumn);
                }
                return new Token(TokenType.LESS, "<", tokenLine, tokenColumn);
                
            case '!':
                if (match('=')) {
                    return new Token(TokenType.NOT_EQUAL, "!=", tokenLine, tokenColumn);
                }
                return new Token(TokenType.NOT, "!", tokenLine, tokenColumn);
                
            case '&':
                if (match('&')) {
                    return new Token(TokenType.AND, "&&", tokenLine, tokenColumn);
                }
                throw new LexerException("Carácter no reconocido", tokenLine, tokenColumn, c);
                
            case '|':
                if (match('|')) {
                    return new Token(TokenType.OR, "||", tokenLine, tokenColumn);
                }
                throw new LexerException("Carácter no reconocido", tokenLine, tokenColumn, c);
                
            case '≠': // Carácter Unicode para no igual
                return new Token(TokenType.NOT_EQUAL, "≠", tokenLine, tokenColumn);
                
            default:
                throw new LexerException("Carácter no reconocido", tokenLine, tokenColumn, c);
        }
    }
    
    /**
     * Procesan un cadenas.
     */
    
    private Token stringLiteral(int tokenLine, int tokenColumn) throws LexerException {
        int start = current - 1;
        StringBuilder value = new StringBuilder();

        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\\') { // Manejar secuencias de escape
                advance();
                switch (peek()) {
                    case 'n': value.append('\n'); break;
                    case 't': value.append('\t'); break;
                    default: value.append(peek());
                }
                advance();
            } else {
                value.append(advance());
            }
        }

        if (isAtEnd()) {
            throw new LexerException("Cadena no terminada", tokenLine, tokenColumn);
        }

        advance(); // Consumir el '"' final
        return new Token(TokenType.STRING_LITERAL, value.toString(), tokenLine, tokenColumn);
    }
    
    private Token charLiteral(int tokenLine, int tokenColumn) throws LexerException {
        char c = advance();
        if (c == '\\') { // Secuencia de escape
            c = advance();
            switch (c) {
                case 'n':
                    c = '\n';
                    break;
                case 't':
                    c = '\t';
                    break;
            }
        }

        if (advance() != '\'') {
            throw new LexerException("Carácter no terminado", tokenLine, tokenColumn);
        }

        return new Token(TokenType.CHAR, String.valueOf(c), tokenLine, tokenColumn);
    }
    
    /**
     * Procesa un número.
     */
    private Token number(int tokenLine, int tokenColumn) {
        int start = current - 1;
        boolean isFloat = false;

        while (isDigit(peek())) {
            advance();
        }

        // Parte decimal
        if (peek() == '.') {
            isFloat = true;
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }

        // Parte exponencial
        if (peek() == 'e' || peek() == 'E') {
            isFloat = true;
            advance();
            if (peek() == '+' || peek() == '-') {
                advance();
            }
            while (isDigit(peek())) {
                advance();
            }
        }

        String value = source.substring(start, current);
        return new Token(isFloat ? TokenType.FLOAT_NUMBER : TokenType.INTEGER, value, tokenLine, tokenColumn);
    }
    
    /**
     * Procesa un identificador o palabra reservada.
     */
    private Token identifier(int tokenLine, int tokenColumn) {
        int start = current - 1;
        
        while (isAlphaNumeric(peek())) {
            advance();
        }
        
        String value = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(value, TokenType.IDENTIFIER);
        return new Token(type, value, tokenLine, tokenColumn);
    }
    
    /**
     * Omite espacios en blanco y tabulaciones.
     */
    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else {
                break;
            }
        }
    }
    
    /**
     * Avanza al siguiente carácter y actualiza la posición.
     */
    private char advance() {
        if (isAtEnd()) return '\0';
        
        char c = source.charAt(current++);
        column++;
        return c;
    }
    
    /**
     * Verifica si el carácter actual coincide con el esperado.
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        column++;
        return true;
    }
    
    /**
     * Mira el carácter actual sin avanzar.
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    /**
     * Actualiza contadores al encontrar una nueva línea.
     */
    private void newLine() {
        line++;
        column = 1;
        lineStart = current;
    }
    
    /**
     * Verifica si hemos llegado al final del código fuente.
     */
    private boolean isAtEnd() {
        return current >= length;
    }
    
    /**
     * Verifica si un carácter es un dígito.
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    /**
     * Verifica si un carácter es una letra o guión bajo.
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || 
               (c >= 'A' && c <= 'Z') || 
               c == '_';
    }
    
    /**
     * Verifica si un carácter es alfanumérico o guión bajo.
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    /**
     * Obtiene información de posición actual para depuración.
     */
    public String getPositionInfo() {
        return String.format("Línea: %d, Columna: %d", line, column);
    }
    
    /**
     * Reinicia el lexer para analizar nuevo código.
     */
    public void reset(String newSource) {
        throw new UnsupportedOperationException("Use new Lexer(newSource) instead");
    }
}