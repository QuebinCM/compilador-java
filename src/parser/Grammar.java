package parser;

import java.util.*;

/**
 * Definición de la gramática para el lenguaje simple.
 *
 */
public class Grammar {

    /**
     * Representa una producción de la gramática.
     */
    public static class Production {

        private final int id;
        private final String left;
        private final List<String> right;

        public Production(int id, String left, String... right) {
            this.id = id;
            this.left = left;
            this.right = Arrays.asList(right);
        }

        public int getId() {
            return id;
        }

        public String getLeft() {
            return left;
        }

        public List<String> getRight() {
            return right;
        }

        public int getLength() {
            return right.size();
        }

        @Override
        public String toString() {
            return id + ". " + left + " -> " + String.join(" ", right);
        }
    }

    /**
     * Representa un ítem LR(0).
     */
    public static class Item {

        private final Production production;
        private final int dotPosition;

        public Item(Production production, int dotPosition) {
            this.production = production;
            this.dotPosition = dotPosition;
        }

        public Production getProduction() {
            return production;
        }

        public int getDotPosition() {
            return dotPosition;
        }

        /**
         * Obtiene el símbolo después del punto (null si está al final).
         */
        public String getNextSymbol() {
            if (dotPosition >= production.getRight().size()) {
                return null;
            }
            return production.getRight().get(dotPosition);
        }

        /**
         * Verifica si el ítem está completo (punto al final).
         */
        public boolean isComplete() {
            return dotPosition >= production.getRight().size();
        }

        /**
         * Crea un nuevo ítem con el punto avanzado.
         */
        public Item advance() {
            return new Item(production, dotPosition + 1);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Item)) {
                return false;
            }
            Item item = (Item) obj;
            return dotPosition == item.dotPosition
                    && production.getId() == item.production.getId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(production.getId(), dotPosition);
        }

        @Override
        public String toString() {
            List<String> right = new ArrayList<>(production.getRight());
            right.add(dotPosition, "•");
            return production.getId() + ". " + production.getLeft() + " -> "
                    + String.join(" ", right);
        }
    }

    private static final List<Production> PRODUCTIONS = Arrays.asList(
            // Producción inicial y estructura del programa
            new Production(0, "S'", "program"),
            new Production(1, "program", "stmt_list"),
            new Production(2, "stmt_list", "stmt"),
            new Production(3, "stmt_list", "stmt_list", "stmt"),
            // Asignación
            new Production(4, "stmt", "expr_stmt"),
            new Production(5, "stmt", "decl_stmt"),
            new Production(6, "expr_stmt", "expr", "SEMICOLON"),
            new Production(7, "decl_stmt", "type", "IDENTIFIER", "SEMICOLON"),
            new Production(8, "decl_stmt", "type", "IDENTIFIER", "ASSIGN", "expr", "SEMICOLON"),
            // Tipos
            new Production(9, "type", "BYTE"),
            new Production(10, "type", "SHORT"),
            new Production(11, "type", "INT"),
            new Production(12, "type", "LONG"),
            new Production(13, "type", "FLOAT"),
            new Production(14, "type", "DOUBLE"),
            new Production(15, "type", "BOOLEAN"),
            new Production(16, "type", "CHAR"),
            new Production(17, "type", "STRING"),
            new Production(18, "type", "VOID"),
            // Funciones
            new Production(19, "stmt", "function_decl"),
            new Production(20, "function_decl", "type", "IDENTIFIER", "LEFT_PAREN", "params", "RIGHT_PAREN", "block"),
            new Production(21, "params", "param_list"),
            new Production(22, "params", "ε"),
            new Production(23, "param_list", "param"),
            new Production(24, "param_list", "param_list", "COMMA", "param"),
            new Production(25, "param", "type", "IDENTIFIER"),
            // Control de flujo
            //IF
            new Production(26, "stmt", "if_stmt"),
            new Production(27, "if_stmt", "IF", "LEFT_PAREN", "expr", "RIGHT_PAREN", "block"),
            new Production(28, "if_stmt", "IF", "LEFT_PAREN", "expr", "RIGHT_PAREN", "block", "ELSE", "block"),
            //SWITCH
            new Production(29, "stmt", "switch_stmt"),
            new Production(30, "switch_stmt", "SWITCH", "LEFT_PAREN", "expr", "RIGHT_PAREN", "LEFT_BRACE", "case_list", "RIGHT_BRACE"),
            new Production(31, "case_list", "case"),
            new Production(32, "case_list", "case_list", "case"),
            new Production(33, "case_list", "case_list", "default_case"),
            new Production(34, "case", "CASE", "literal", "COLON", "case_body"),
            new Production(35, "default_case", "DEFAULT", "COLON", "case_body"),
            new Production(36, "case_body", "stmt_list"),
            new Production(37, "case_body", "stmt_list", "break_stmt"),
            new Production(38, "stmt", "break_stmt"),
            new Production(39, "break_stmt", "BREAK", "SEMICOLON"),
            new Production(40, "stmt", "return_stmt"),
            new Production(41, "return_stmt", "RETURN", "expr", "SEMICOLON"),
            // Estructuras de bucle
            //WHILE
            new Production(42, "stmt", "while_stmt"),
            new Production(43, "while_stmt", "WHILE", "LEFT_PAREN", "expr", "RIGHT_PAREN", "block"),
            //DO-WHILE
            new Production(44, "stmt", "do_while_stmt"),
            new Production(45, "do_while_stmt", "DO", "block", "WHILE", "LEFT_PAREN", "expr", "RIGHT_PAREN", "SEMICOLON"),
            //FOR
            new Production(46, "stmt", "for_stmt"),
            new Production(47, "for_stmt", "FOR", "LEFT_PAREN", "for_init", "SEMICOLON", "for_cond", "SEMICOLON", "for_update", "RIGHT_PAREN", "block"),
            new Production(48, "for_init", "decl_stmt_inline"),
            new Production(49, "for_init", "expr_stmt_inline"),
            new Production(50, "for_init", "ε"),
            new Production(51, "for_cond", "expr"),
            new Production(52, "for_cond", "ε"),
            new Production(53, "for_update", "expr"),
            new Production(54, "for_update", "ε"),
            // Bloques
            new Production(55, "block", "LEFT_BRACE", "stmt_list", "RIGHT_BRACE"),
            new Production(56, "block", "stmt"),
            // Expresiones
            // Nivel 1: OR
            new Production(57, "expr", "expr", "OR", "and_expr"),
            new Production(58, "expr", "and_expr"),
            // Nivel 2: AND
            new Production(59, "and_expr", "and_expr", "AND", "eq_expr"),
            new Production(60, "and_expr", "eq_expr"),
            // Nivel 3: Igualdad
            new Production(61, "eq_expr", "eq_expr", "EQUAL_EQUAL", "rel_expr"),
            new Production(62, "eq_expr", "eq_expr", "NOT_EQUAL", "rel_expr"),
            new Production(63, "eq_expr", "rel_expr"),
            // Nivel 4: Relacionales
            new Production(64, "rel_expr", "rel_expr", "GREATER", "add_expr"),
            new Production(65, "rel_expr", "rel_expr", "LESS", "add_expr"),
            new Production(66, "rel_expr", "rel_expr", "GREATER_EQUAL", "add_expr"),
            new Production(67, "rel_expr", "rel_expr", "LESS_EQUAL", "add_expr"),
            new Production(68, "rel_expr", "add_expr"),
            // Nivel 5: Suma/resta
            new Production(69, "add_expr", "add_expr", "PLUS", "term"),
            new Production(70, "add_expr", "add_expr", "MINUS", "term"),
            new Production(71, "add_expr", "term"),
            // Nivel 6: Multiplicación/división
            new Production(72, "term", "term", "MULTIPLY", "factor"),
            new Production(73, "term", "term", "DIVIDE", "factor"),
            new Production(104, "term", "term", "MODULO", "factor"),
            new Production(74, "term", "postfix_expr"),
            // Nivel 7: Unarios y postfijos
            new Production(75, "unary_expr", "INCREMENT", "unary_expr"),
            new Production(76, "unary_expr", "DECREMENT", "unary_expr"),
            new Production(77, "unary_expr", "factor"),
            new Production(78, "postfix_expr", "unary_expr"),
            new Production(79, "postfix_expr", "postfix_expr", "INCREMENT"),
            new Production(80, "postfix_expr", "postfix_expr", "DECREMENT"),
            // Factores
            new Production(81, "factor", "NOT", "factor"),
            new Production(82, "factor", "INTEGER"),
            new Production(83, "factor", "FLOAT_NUMBER"),
            new Production(84, "factor", "IDENTIFIER"),
            new Production(85, "factor", "LEFT_PAREN", "expr", "RIGHT_PAREN"),
            new Production(86, "factor", "TRUE"),
            new Production(87, "factor", "FALSE"),
            new Production(88, "factor", "STRING_LITERAL"),
            new Production(89, "factor", "CHAR_LITERAL"),
            new Production(90, "factor", "IDENTIFIER", "LEFT_PAREN", "args", "RIGHT_PAREN"),
            // Argumentos de llamada
            new Production(91, "args", "arg_list"),
            new Production(92, "args", "ε"),
            new Production(93, "arg_list", "expr"),
            new Production(94, "arg_list", "arg_list", "COMMA", "expr"),
            // Producciones auxiliares
            new Production(95, "decl_stmt_inline", "type", "IDENTIFIER", "ASSIGN", "expr"),
            new Production(96, "decl_stmt_inline", "type", "IDENTIFIER"),
            new Production(97, "expr_stmt_inline", "expr"),
            // Literales para case
            new Production(98, "literal", "INTEGER"),
            new Production(99, "literal", "FLOAT_NUMBER"),
            new Production(100, "literal", "CHAR_LITERAL"),
            new Production(101, "literal", "STRING_LITERAL"),
            new Production(102, "literal", "TRUE"),
            new Production(103, "literal", "FALSE"),
            // Asignaciones compuestas
            new Production(110, "expr_stmt", "IDENTIFIER", "compound_assign", "expr", "SEMICOLON"),
            new Production(111, "compound_assign", "ASSIGN"),
            new Production(112, "compound_assign", "PLUS_ASSIGN"),
            new Production(113, "compound_assign", "MINUS_ASSIGN"),
            new Production(114, "compound_assign", "MULTIPLY_ASSIGN"),
            new Production(115, "compound_assign", "DIVIDE_ASSIGN"),
            new Production(116, "compound_assign", "MODULO_ASSIGN"),
            // Modificadores de acceso
            new Production(117, "modifier", "PUBLIC"),
            new Production(118, "modifier", "PRIVATE"),
            new Production(119, "modifiers", "modifier"),
            new Production(120, "modifiers", "modifiers", "modifier"),
            new Production(121, "modifiers", "ε"),
            // Declaración de clases
            new Production(122, "stmt", "class_decl"),
            new Production(123, "class_decl", "modifiers", "CLASS", "IDENTIFIER", "LEFT_BRACE", "class_members", "RIGHT_BRACE"),
            // Miembros de clase
            new Production(124, "class_members", "class_member"),
            new Production(125, "class_members", "class_members", "class_member"),
            new Production(126, "class_members", "ε"),
            new Production(127, "class_member", "field_decl"),
            new Production(128, "class_member", "method_decl"),
            new Production(129, "class_member", "constructor_decl"),
            // Campos
            new Production(130, "field_decl", "modifiers", "type", "IDENTIFIER", "SEMICOLON"),
            new Production(131, "field_decl", "modifiers", "type", "IDENTIFIER", "ASSIGN", "expr", "SEMICOLON"),
            // Métodos
            new Production(132, "method_decl", "modifiers", "function_decl"),
            // Constructores
            new Production(133, "constructor_decl", "modifiers", "IDENTIFIER", "LEFT_PAREN", "params", "RIGHT_PAREN", "block"),
            // Expresiones para POO
            new Production(134, "factor", "NEW", "IDENTIFIER", "LEFT_PAREN", "args", "RIGHT_PAREN"),
            new Production(135, "factor", "THIS", "DOT", "IDENTIFIER")
    );

    private static final Set<String> TERMINALS = Set.of(
            "AND", "ASSIGN", "BOOLEAN", "BYTE", "CHAR", "CHAR_LITERAL", "COMMA",
            "DIVIDE", "DOUBLE", "ELSE", "EQUAL_EQUAL", "FALSE", "FLOAT", "GREATER",
            "GREATER_EQUAL", "IDENTIFIER", "IF", "INT", "INTEGER", "LESS",
            "LESS_EQUAL", "LONG", "MINUS", "MULTIPLY", "NOT", "NOT_EQUAL",
            "OR", "PLUS", "RETURN", "RIGHT_BRACE", "RIGHT_PAREN", "SEMICOLON",
            "SWITCH", "CASE", "DEFAULT", "COLON", "BREAK","SHORT", "STRING", 
            "STRING_LITERAL", "TRUE", "VOID", "WHILE", "FOR", "FLOAT_NUMBER",
            "MODULO","PLUS_ASSIGN", "MINUS_ASSIGN", "MULTIPLY_ASSIGN", "DIVIDE_ASSIGN", 
            "MODULO_ASSIGN", "LEFT_BRACE", "LEFT_PAREN", "INCREMENT", "DECREMENT",
            "CLASS", "PUBLIC", "PRIVATE", "NEW", "THIS", "DOT", "DO", "$", "ε"
    );

    private static final Set<String> NON_TERMINALS = Set.of(
            "S'", "program", "stmt_list", "stmt", "expr_stmt",
            "decl_stmt", "type", "expr", "and_expr", "eq_expr",
            "rel_expr", "add_expr", "term", "factor",
            "if_stmt", "while_stmt", "block", "function_decl",
            "params", "param_list", "param","return_stmt",
            "args", "arg_list", "for_stmt", "for_init", "for_cond",
            "for_update", "decl_stmt_inline", "expr_stmt_inline", "postfix_expr",
            "unary_expr", "do_while_stmt", "switch_stmt", "case_list", "case", 
            "default_case", "literal", "case_body", "break_stmt", "compound_assign",
            "modifier", "modifiers", "class_decl", "class_members", "class_member",
            "field_decl", "constructor_decl", "method_decl"
    );

    public static boolean isNullable(String symbol) {
        if (isTerminal(symbol)) {
            return symbol.equals("ε");  // Solo ε es nullable como terminal
        }
        return PRODUCTIONS.stream()
                .filter(p -> p.getLeft().equals(symbol))
                .anyMatch(p -> p.getRight().isEmpty()
                || (p.getRight().size() == 1 && p.getRight().get(0).equals("ε")));
    }

    public static List<Production> getProductions() {
        return PRODUCTIONS;
    }

    public static Set<String> getTerminals() {
        return TERMINALS;
    }

    public static Set<String> getNonTerminals() {
        return NON_TERMINALS;
    }

    public static Production getProduction(int id) {
        return PRODUCTIONS.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public static boolean isTerminal(String symbol) {
        return TERMINALS.contains(symbol) || symbol.equals("ε");
    }

    public static boolean isNonTerminal(String symbol) {
        return NON_TERMINALS.contains(symbol);
    }
}
