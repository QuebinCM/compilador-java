package semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private final Map<String, Symbol> symbols = new HashMap<>();
    private final Stack<Integer> scopeStack = new Stack<>();
    private int currentScope = 0;

    public SymbolTable() {
        scopeStack.push(currentScope);
    }

    public void enterScope() {
        currentScope++;
        scopeStack.push(currentScope);
    }

    public void exitScope() {
        scopeStack.pop();
        currentScope = scopeStack.isEmpty() ? 0 : scopeStack.peek();
    }
    
    public int getCurrentScope() {
        return currentScope;
    }

    public boolean addSymbol(Symbol symbol) {
        String scopedName = symbol.getName() + "@" + currentScope;
        if (symbols.containsKey(scopedName)) {
            return false;
        }
        symbols.put(scopedName, symbol);
        return true;
    }

    public Symbol lookup(String name) {
        // Buscar desde el ámbito más interno al más externo
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            int scope = scopeStack.get(i);
            String scopedName = name + "@" + scope;
            if (symbols.containsKey(scopedName)) {
                return symbols.get(scopedName);
            }
        }
        return null;
    }

    public Map<String, Symbol> getSymbols() {
        return new HashMap<>(symbols);
    }
}