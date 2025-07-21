package lexer;

/**
 * Enumeración que define los tipos de tokens reconocidos por el analizador léxico.
 */
public enum TokenType {
    // Palabras reservadas
    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),
    CLASS("class"),
    VOID("void"),
    STATIC("static"),
    FINAL("final"),
    NEW("new"),
    THIS("this"),
    SUPER("super"),
    PACKAGE("package"),
    IMPORT("import"),
    IF("if"),
    WHILE("while"),
    RETURN("return"),
    ELSE("else"),
    FOR("for"),
    DO("do"),
    SWITCH("switch"),
    CASE("case"),
    BREAK("break"),
    DEFAULT("default"),
    INT("int"),
    FLOAT("float"),
    BOOLEAN("boolean"),
    BYTE("byte"),
    SHORT("short"),
    LONG("long"),
    DOUBLE("double"),
    CHAR("char"),
    TRUE("true"),
    FALSE("false"),
    FLOAT_NUMBER("FLOAT_NUMBER"),
    // Literales
    STRING("STRING_LITERAL"),
    CHAR_LITERAL("CHAR_LITERAL"),
    STRING_LITERAL("STRING_LITERAL"),
    // Identificadores y literales
    IDENTIFIER("IDENTIFIER"),
    INTEGER("INTEGER"),
    // Operadores aritméticos
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),
    INCREMENT("++"),
    DECREMENT("--"),
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    DIVIDE_ASSIGN("/="),
    MULTIPLY_ASSIGN("%="),
    MODULO_ASSIGN("%="),
    // Operadores de asignación
    ASSIGN(":="),
    EQUALS("="),
    // Operadores relacionales
    GREATER(">"),
    LESS("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    NOT_EQUAL("≠"),
    EQUAL_EQUAL("=="),
    // Operadores lógicos
    AND("&&"),
    OR("||"),
    NOT("!"),
    // Delimitadores
    SEMICOLON(";"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),
    COMMA(","),
    LEFT_BRACKET("["),
    RIGHT_BRACKET("]"),
    DOT("."),
    COLON(":"),
    // Anotaciones
    AT("@"),
    // Especiales
    EOF("EOF"),
    NEWLINE("\\n");
    
    private final String representation;
    
    TokenType(String representation) {
        this.representation = representation;
    }
    
    public String getRepresentation() {
        return representation;
    }
    
    @Override
    public String toString() {
        return representation;
    }
}
