package parser;

import java.util.*;

/**
 * Calculadora de conjuntos FIRST y FOLLOW para la gramática.
 */
public class FirstFollowSets {
    private final Map<String, Set<String>> firstSets;
    private final Map<String, Set<String>> followSets;
    
    public FirstFollowSets() {
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
        calculateFirstSets();
        calculateFollowSets();
    }
    
    /**
     * Calcula los conjuntos FIRST para todos los símbolos.
     */
    private void calculateFirstSets() {
        // 1. Inicialización segura
        Map<String, Set<String>> tempFirst = new HashMap<>();

        // Inicializar todos los terminales
        for (String terminal : Grammar.getTerminals()) {
            Set<String> set = new HashSet<>();
            set.add(terminal);
            tempFirst.put(terminal, set);
        }

        // Inicializar todos los no terminales
        for (String nonTerminal : Grammar.getNonTerminals()) {
            tempFirst.put(nonTerminal, new HashSet<>());
        }

        // 2. Algoritmo de punto fijo mejorado
        boolean changed;
        int iterations = 0;
        final int MAX_ITERATIONS = 100; // Prevención de bucles infinitos

        do {
            changed = false;
            iterations++;

            for (Grammar.Production production : Grammar.getProductions()) {
                String left = production.getLeft();
                List<String> right = production.getRight();

                Set<String> firstLeft = tempFirst.get(left);
                int originalSize = firstLeft.size();

                if (right.isEmpty() || right.get(0).equals("ε")) {
                    firstLeft.add("ε");
                } else {
                    boolean allNullable = true;

                    for (String symbol : right) {
                        Set<String> firstSymbol = tempFirst.get(symbol);
                        if (firstSymbol == null) {
                            throw new IllegalStateException("Símbolo no encontrado: " + symbol);
                        }

                        // Agregar todos los elementos excepto ε
                        for (String s : firstSymbol) {
                            if (!s.equals("ε")) {
                                firstLeft.add(s);
                            }
                        }

                        if (!firstSymbol.contains("ε")) {
                            allNullable = false;
                            break;
                        }
                    }

                    if (allNullable) {
                        firstLeft.add("ε");
                    }
                }

                if (firstLeft.size() > originalSize) {
                    changed = true;
                }
            }

            if (iterations > MAX_ITERATIONS) {
                throw new IllegalStateException("Demasiadas iteraciones en calculateFirstSets()");
            }
        } while (changed);

        // 3. Asignar los resultados calculados
        this.firstSets.putAll(tempFirst);
    }
    
    /**
     * Calcula los conjuntos FOLLOW para todos los no terminales.
     */
    private void calculateFollowSets() {
        // Inicializar FOLLOW para todos los no terminales
        for (String nonTerminal : Grammar.getNonTerminals()) {
            followSets.put(nonTerminal, new HashSet<>());
        }

        // Regla 1: $ está en FOLLOW(S')
        followSets.get("S'").add("$");

        boolean changed;
        do {
            changed = false;
            for (Grammar.Production production : Grammar.getProductions()) {
                List<String> right = production.getRight();

                for (int i = 0; i < right.size(); i++) {
                    String current = right.get(i);
                    if (Grammar.isTerminal(current)) {
                        continue;
                    }

                    Set<String> followCurrent = followSets.get(current);
                    int initialSize = followCurrent.size();

                    if (i < right.size() - 1) {
                        // Regla 2: Agregar FIRST(Yi+1) - {ε} a FOLLOW(Yi)
                        String next = right.get(i + 1);
                        Set<String> firstNext = new HashSet<>(firstSets.get(next));
                        firstNext.remove("ε");
                        followCurrent.addAll(firstNext);

                        // Regla 3: Si Yi+1 es nullable, agregar FOLLOW(X) a FOLLOW(Yi)
                        if (isNullable(next)) {
                            followCurrent.addAll(followSets.get(production.getLeft()));
                        }
                    } else {
                        // Regla 3: Si Yi es el último símbolo, agregar FOLLOW(X) a FOLLOW(Yi)
                        followCurrent.addAll(followSets.get(production.getLeft()));
                    }

                    if (followCurrent.size() > initialSize) {
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    private boolean isNullable(String symbol) {
        // Caso base para terminales
        if (Grammar.isTerminal(symbol)) {
            return symbol.equals("ε");
        }

        // Para no terminales, verificar producciones
        for (Grammar.Production production : Grammar.getProductions()) {
            if (production.getLeft().equals(symbol)) {
                // Producción vacía o que deriva en ε
                if (production.getRight().isEmpty()
                        || (production.getRight().size() == 1 && production.getRight().get(0).equals("ε"))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Set<String> getFirst(String symbol) {
        return new HashSet<>(firstSets.getOrDefault(symbol, Set.of()));
    }
    
    public Set<String> getFollow(String symbol) {
        return new HashSet<>(followSets.getOrDefault(symbol, Set.of()));
    }
    
    public void printSets() {
        System.out.println("=== CONJUNTOS FIRST ===");
        for (String symbol : Grammar.getNonTerminals()) {
            System.out.println("FIRST(" + symbol + ") = " + firstSets.get(symbol));
        }
        
        System.out.println("\n=== CONJUNTOS FOLLOW ===");
        for (String symbol : Grammar.getNonTerminals()) {
            System.out.println("FOLLOW(" + symbol + ") = " + followSets.get(symbol));
        }
    }
}