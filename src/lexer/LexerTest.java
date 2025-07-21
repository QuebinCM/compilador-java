package lexer;

import java.util.List;

/**
 * Clase de prueba para el analizador léxico.
 * Demuestra el uso del lexer con diferentes tipos de código.
 */
public class LexerTest {
    public static void main(String[] args) {
        // Código de prueba
        String code = """
            if (x > 10) {
                y = x + 5;
                return y;
            }
            while (i < n) {
                sum = sum + i;
                i = i + 1;
            }
            """;
        
        System.out.println("Código a analizar:");
        System.out.println(code);
        System.out.println("\nTokens generados:");
        System.out.println("=".repeat(60));
        
        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();
            
            for (Token token : tokens) {
                System.out.printf("%-15s %-10s L:%d C:%d%n", 
                                token.getType(), 
                                "'" + token.getLexeme() + "'", 
                                token.getLine(), 
                                token.getColumn());
            }
            
            System.out.println("\nTotal de tokens: " + tokens.size());
            
        } catch (LexerException e) {
            System.err.println("Error léxico: " + e.getMessage());
        }
        
        // Prueba con error léxico
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Prueba con error léxico:");
        testLexerError();
    }
    
    private static void testLexerError() {
        String errorCode = "int x = 10; y === 20;";
        
        try {
            Lexer lexer = new Lexer(errorCode);
            lexer.tokenize();
        } catch (LexerException e) {
            System.out.println("Error capturado correctamente: " + e.getMessage());
            System.out.println("Línea: " + e.getLine() + ", Columna: " + e.getColumn());
        }
    }
}