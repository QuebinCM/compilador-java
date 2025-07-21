package parser;

import lexer.Token;
import lexer.TokenType;
import java.util.*;

/**
 * Implementación del parser SLR(1) que analiza una secuencia de tokens
 * y construye un árbol sintáctico usando el algoritmo LR con pila.
 */
public class SLR1Parser {
    
    /**
     * Representa los pasos del análisis sintáctico.
     */
    public static class ParseStep {
    private final int step;
    private final String stack;
    private final String input;
    private final String action;
    private final String production;
    
    public ParseStep(int step, String stack, String input, String action, String production) {
        this.step = step;
        this.stack = stack;
        this.input = input;
        this.action = action;
        this.production = production;
    }
    
    // Getters
    public int getStep() { return step; }
    public String getStack() { return stack; }
    public String getInput() { return input; }
    public String getAction() { return action; }
    public String getProduction() { return production; }
}

    private List<ParseStep> parseSteps = new ArrayList<>();
    private int currentStep = 0;

    /**
     * Representa un nodo del árbol sintáctico.
     */    
    public static class ParseTreeNode {
        private final String symbol;
        private final String value;
        private final List<ParseTreeNode> children;
        private final boolean isTerminal;
        private final Token token; // Para nodos terminales
        
        // Constructor para nodos terminales
        public ParseTreeNode(Token token) {
            this.token = token;
            this.symbol = token.getType().name();
            this.value = token.getLexeme();
            this.children = new ArrayList<>();
            this.isTerminal = true;
        }
        
        // Constructor para nodos no terminales
        public ParseTreeNode(String symbol) {
            this.symbol = symbol;
            this.value = null;
            this.children = new ArrayList<>();
            this.isTerminal = false;
            this.token = null;
        }
        
        public String getSymbol() { return symbol; }
        public String getValue() { return value; }
        public List<ParseTreeNode> getChildren() { return new ArrayList<>(children); }
        public boolean isTerminal() { return isTerminal; }
        public Token getToken() { return token; }
        
        public void addChild(ParseTreeNode child) {
            children.add(child);
        }
        
        public void addChildren(List<ParseTreeNode> children) {
            this.children.addAll(children);
        }
        
        @Override
        public String toString() {
            if (isTerminal) {
                return symbol + "(" + value + ")";
            } else {
                return symbol;
            }
        }
        
        /**
         * Genera una representación en forma de árbol del nodo y sus hijos.
         */
        public String toTreeString() {
            return toTreeString(0);
        }
        
        private String toTreeString(int depth) {
            StringBuilder sb = new StringBuilder();
            String indent = "  ".repeat(depth);
            
            sb.append(indent).append(toString()).append("\n");
            
            for (ParseTreeNode child : children) {
                sb.append(child.toTreeString(depth + 1));
            }
            
            return sb.toString();
        }
    }
        
    private final SLR1Table table;
    private final Stack<Integer> stateStack;
    private final Stack<ParseTreeNode> nodeStack;
    private List<Token> tokens;
    private int currentTokenIndex;
    
    public SLR1Parser() {
        // Construir componentes del parser
        FirstFollowSets firstFollow = new FirstFollowSets();
        LR0Automaton automaton = new LR0Automaton();
        this.table = new SLR1Table(automaton, firstFollow);
        
        this.stateStack = new Stack<>();
        this.nodeStack = new Stack<>();
    }
    
    /**
     * Analiza una lista de tokens y construye el árbol sintáctico.
     */
    public ParseTreeNode parse(List<Token> tokens) throws ParserException {
        parseSteps.clear();
        currentStep = 0;
        
        this.tokens = new ArrayList<>(tokens);
        this.currentTokenIndex = 0;
        
        // Agregar token EOF si no existe
        if (tokens.isEmpty() || tokens.get(tokens.size() - 1).getType() != TokenType.EOF) {
            Token eofToken = new Token(TokenType.EOF, "$", 
                                     tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).getLine(),
                                     tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).getColumn() + 1);
            this.tokens.add(eofToken);
        }
        
        // Inicializar pilas
        stateStack.clear();
        nodeStack.clear();
        stateStack.push(0); // Estado inicial
        
        while (true) {
            Token currentToken = getCurrentToken();
            String terminal = mapTokenToTerminal(currentToken);
            int currentState = stateStack.peek();

            SLR1Table.Action action = table.getAction(currentState, terminal);

            // Registrar paso ANTES de ejecutar la acción
            String productionStr = "";
            if (action.getType() == SLR1Table.ActionType.REDUCE) {
                productionStr = Grammar.getProduction(action.getValue()).toString();
            }

            parseSteps.add(new ParseStep(
                ++currentStep,
                stateStack.toString(),
                getRemainingInput(),
                action.toString(),
                productionStr
            ));
            
            switch (action.getType()) {
                case SHIFT:
                    shift(action.getValue());
                    break;
                    
                case REDUCE:
                    reduce(action.getValue());
                    break;
                    
                case ACCEPT:
                    if (nodeStack.size() == 1) {
                        return nodeStack.peek();
                    } else {
                        throw new ParserException("Parser internal error: multiple nodes on stack", currentToken);
                    }
                    
                case ERROR:
                    handleError(currentToken, currentState);
                    break;
            }
        }
    }
    
    /**
     * Ejecuta una acción SHIFT.
     */
    private void shift(int nextState) {
        Token currentToken = getCurrentToken();
        
        // Crear nodo terminal y agregarlo a la pila
        ParseTreeNode terminalNode = new ParseTreeNode(currentToken);
        nodeStack.push(terminalNode);
        
        // Cambiar al siguiente estado
        stateStack.push(nextState);
        
        // Avanzar al siguiente token
        currentTokenIndex++;
    }
    
    /**
     * Ejecuta una acción REDUCE.
     */
    private void reduce(int productionId) throws ParserException {
        Grammar.Production production = Grammar.getProduction(productionId);
        if (production == null) {
            throw new ParserException("Invalid production ID: " + productionId, getCurrentToken());
        }
        
        // Crear nodo no terminal
        ParseTreeNode nonTerminalNode = new ParseTreeNode(production.getLeft());
        
        // Desapilar símbolos y estados según la longitud de la producción
        List<ParseTreeNode> children = new ArrayList<>();
        for (int i = 0; i < production.getLength(); i++) {
            if (!stateStack.isEmpty()) {
                stateStack.pop();
            }
            if (!nodeStack.isEmpty()) {
                children.add(0, nodeStack.pop()); // Insertar al inicio para mantener orden
            }
        }
        
        // Agregar hijos al nodo no terminal
        nonTerminalNode.addChildren(children);
        
        // Obtener el estado actual después de desapilar
        int currentState = stateStack.isEmpty() ? 0 : stateStack.peek();
        
        // Consultar tabla GOTO
        Integer gotoState = table.getGoto(currentState, production.getLeft());
        if (gotoState == null) {
            throw new ParserException("GOTO undefined for state " + currentState + 
                                   " and symbol " + production.getLeft(), getCurrentToken());
        }
        
        // Apilar el nuevo nodo y estado
        nodeStack.push(nonTerminalNode);
        stateStack.push(gotoState);
    }
    
    /**
     * Maneja errores sintácticos.
     */
    private void handleError(Token errorToken, int currentState) throws ParserException {
        StringBuilder expectedSymbols = new StringBuilder();
        
        // Encontrar símbolos esperados consultando la tabla ACTION
        for (String terminal : Grammar.getTerminals()) {
            SLR1Table.Action action = table.getAction(currentState, terminal);
            if (action.getType() != SLR1Table.ActionType.ERROR) {
                if (expectedSymbols.length() > 0) {
                    expectedSymbols.append(", ");
                }
                expectedSymbols.append(terminal);
            }
        }
        
        String message = String.format("Syntax error at line %d, column %d: unexpected token '%s'",
                                     errorToken.getLine(), errorToken.getColumn(), errorToken.getLexeme());
        
        throw new ParserException(message, errorToken, expectedSymbols.toString(), currentState);
    }
    
    /**
     * Obtiene el token actual.
     */
    private Token getCurrentToken() {
        if (currentTokenIndex >= tokens.size()) {
            // Retornar último token (debería ser EOF)
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(currentTokenIndex);
    }
    
    /**
     * Mapea un token a su representación terminal en la gramática.
     */
    private String mapTokenToTerminal(Token token) {
        return switch (token.getType()) {
            case INT ->
                "INT";
            case FLOAT ->
                "FLOAT";
            case IDENTIFIER ->
                "IDENTIFIER";
            case INTEGER ->
                "INTEGER";
            case ASSIGN ->
                "ASSIGN";
            case SEMICOLON ->
                "SEMICOLON";
            case PLUS ->
                "PLUS";
            case MINUS ->
                "MINUS";
            case MULTIPLY ->
                "MULTIPLY";
            case DIVIDE ->
                "DIVIDE";
            case LEFT_PAREN ->
                "LEFT_PAREN";
            case RIGHT_PAREN ->
                "RIGHT_PAREN";
            case STRING ->
                "STRING";
            case STRING_LITERAL ->
                "STRING_LITERAL";
            case VOID ->
                "VOID";
            case EOF ->
                "$";
            default ->
                token.getType().name();
        };
    }
    
    /**
     * Valida que los tokens sean compatibles con la gramática.
     */
    public List<String> validateTokens(List<Token> tokens) {
        List<String> warnings = new ArrayList<>();
        Set<String> validTerminals = Grammar.getTerminals();
        
        for (Token token : tokens) {
            String terminal = mapTokenToTerminal(token);
            if (!validTerminals.contains(terminal)) {
                warnings.add(String.format("Warning: Token '%s' at line %d, column %d not recognized by grammar",
                                         token.getLexeme(), token.getLine(), token.getColumn()));
            }
        }
        
        return warnings;
    }
    
    /**
     * Método auxiliar para obtener la entrada restante.
     */
    private String getRemainingInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = currentTokenIndex; i < tokens.size(); i++) {
            sb.append(mapTokenToTerminal(tokens.get(i))).append(" ");
        }
        return sb.toString().trim();
    }
    public List<ParseStep> getParseSteps() {
        return new ArrayList<>(parseSteps);
    }
    
    /**
     * Genera información de depuración del proceso de parsing.
     */
    public String getDebugInfo() {
        StringBuilder debug = new StringBuilder();
        debug.append("=== INFORMACIÓN DEL PARSER SLR(1) ===\n");
        debug.append("Estados en la pila: ").append(stateStack).append("\n");
        debug.append("Tokens procesados: ").append(currentTokenIndex).append("/").append(tokens.size()).append("\n");
        
        
        if (currentTokenIndex < tokens.size()) {
            Token current = tokens.get(currentTokenIndex);
            debug.append("Token actual: ").append(current).append("\n");
            debug.append("Terminal: ").append(mapTokenToTerminal(current)).append("\n");
        }
        
        return debug.toString();
    }
    
    /**
     * Imprime las tablas del parser (para depuración).
     */
    public void printTables() {
        table.printTables();
    }
}