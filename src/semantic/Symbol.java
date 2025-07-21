// Symbol.java
package semantic;

import java.util.List;
import lexer.TokenType;

public class Symbol {
    private final String name;
    private final TokenType type;
    private final Object value;
    private final int scopeLevel;
    private final boolean isFunction;
    private final TokenType returnType; // Para funciones
    private final List<TokenType> parameters; // Para funciones

    public Symbol(String name, TokenType type, Object value, int scopeLevel) {
        this(name, type, value, scopeLevel, false, null, null);
    }

    public Symbol(String name, TokenType type, Object value, int scopeLevel, 
                 boolean isFunction, TokenType returnType, List<TokenType> parameters) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.scopeLevel = scopeLevel;
        this.isFunction = isFunction;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    // Getters
    public String getName() { return name; }
    public TokenType getType() { return type; }
    public Object getValue() { return value; }
    public int getScopeLevel() { return scopeLevel; }
    public boolean isFunction() { return isFunction; }
    public TokenType getReturnType() { return returnType; }
    public List<TokenType> getParameters() { return parameters; }
}