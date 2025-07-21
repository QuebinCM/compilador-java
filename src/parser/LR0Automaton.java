package parser;

import java.util.*;

/**
 * Construcción del autómata LR(0) con estados, transiciones y cierres.
 */
public class LR0Automaton {
    
    /**
     * Representa un estado del autómata LR(0).
     */
    public static class State {
        private final int id;
        private final Set<Grammar.Item> items;
        
        public State(int id, Set<Grammar.Item> items) {
            this.id = id;
            this.items = new HashSet<>(items);
        }
        
        public int getId() { return id; }
        public Set<Grammar.Item> getItems() { return new HashSet<>(items); }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof State)) return false;
            State state = (State) obj;
            return items.equals(state.items);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(items);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("I").append(id).append(":\n");
            for (Grammar.Item item : items) {
                sb.append("  ").append(item).append("\n");
            }
            return sb.toString();
        }
    }
    
    /**
     * Representa una transición entre estados.
     */
    public static class Transition {
        private final int fromState;
        private final String symbol;
        private final int toState;
        
        public Transition(int fromState, String symbol, int toState) {
            this.fromState = fromState;
            this.symbol = symbol;
            this.toState = toState;
        }
        
        public int getFromState() { return fromState; }
        public String getSymbol() { return symbol; }
        public int getToState() { return toState; }
        
        @Override
        public String toString() {
            return "I" + fromState + " --" + symbol + "--> I" + toState;
        }
    }
    
    private final List<State> states;
    private final List<Transition> transitions;
    private final Map<Integer, Map<String, Integer>> transitionTable;
    
    public LR0Automaton() {
        this.states = new ArrayList<>();
        this.transitions = new ArrayList<>();
        this.transitionTable = new HashMap<>();
        buildAutomaton();
    }
    
    /**
     * Construye el autómata LR(0) completo.
     */
    private void buildAutomaton() {
        // Estado inicial I0 con S' -> •program
        Grammar.Item initialItem = new Grammar.Item(Grammar.getProduction(0), 0);
        Set<Grammar.Item> initialClosure = closure(Set.of(initialItem));
        State initialState = new State(0, initialClosure);
        states.add(initialState);
        
        Queue<State> workQueue = new LinkedList<>();
        workQueue.add(initialState);
        
        while (!workQueue.isEmpty()) {
            State currentState = workQueue.poll();
            processState(currentState, workQueue);
        }
    }
    
    /**
     * Procesa un estado generando nuevos estados y transiciones.
     */
    private void processState(State state, Queue<State> workQueue) {
        Map<String, Set<Grammar.Item>> transitions = new HashMap<>();
        
        // Agrupar ítems por símbolo después del punto
        for (Grammar.Item item : state.getItems()) {
            String nextSymbol = item.getNextSymbol();
            if (nextSymbol != null) {
                transitions.computeIfAbsent(nextSymbol, k -> new HashSet<>())
                          .add(item.advance());
            }
        }
        
        // Crear nuevos estados y transiciones
        for (Map.Entry<String, Set<Grammar.Item>> entry : transitions.entrySet()) {
            String symbol = entry.getKey();
            Set<Grammar.Item> kernelItems = entry.getValue();
            Set<Grammar.Item> newStateClosure = closure(kernelItems);
            
            // Buscar si ya existe un estado con estos ítems
            State existingState = findExistingState(newStateClosure);
            
            if (existingState == null) {
                // Crear nuevo estado
                State newState = new State(states.size(), newStateClosure);
                states.add(newState);
                workQueue.add(newState);
                addTransition(state.getId(), symbol, newState.getId());
            } else {
                // Usar estado existente
                addTransition(state.getId(), symbol, existingState.getId());
            }
        }
    }
    
    /**
     * Calcula el cierre de un conjunto de ítems.
     */
    private Set<Grammar.Item> closure(Set<Grammar.Item> items) {
        Set<Grammar.Item> closure = new HashSet<>(items);
        Queue<Grammar.Item> workQueue = new LinkedList<>(items);
        
        while (!workQueue.isEmpty()) {
            Grammar.Item item = workQueue.poll();
            String nextSymbol = item.getNextSymbol();
            
            if (nextSymbol != null && Grammar.isNonTerminal(nextSymbol)) {
                // Agregar todos los ítems A -> •α para producciones A -> α
                for (Grammar.Production prod : Grammar.getProductions()) {
                    if (prod.getLeft().equals(nextSymbol)) {
                        Grammar.Item newItem = new Grammar.Item(prod, 0);
                        if (!closure.contains(newItem)) {
                            closure.add(newItem);
                            workQueue.add(newItem);
                        }
                    }
                }
            }
        }
        
        return closure;
    }
    
    /**
     * Busca un estado existente con los mismos ítems.
     */
    private State findExistingState(Set<Grammar.Item> items) {
        for (State state : states) {
            if (state.getItems().equals(items)) {
                return state;
            }
        }
        return null;
    }
    
    /**
     * Añade una transición al autómata.
     */
    private void addTransition(int fromState, String symbol, int toState) {
        transitions.add(new Transition(fromState, symbol, toState));
        transitionTable.computeIfAbsent(fromState, k -> new HashMap<>())
                      .put(symbol, toState);
    }
    
    public List<State> getStates() { return new ArrayList<>(states); }
    public List<Transition> getTransitions() { return new ArrayList<>(transitions); }
    
    /**
     * Obtiene el estado destino de una transición.
     */
    public Integer getTransition(int state, String symbol) {
        return transitionTable.getOrDefault(state, Map.of()).get(symbol);
    }
    
    public void printAutomaton() {
        System.out.println("=== AUTÓMATA LR(0) ===");
        for (State state : states) {
            System.out.println(state);
        }
        
        System.out.println("=== TRANSICIONES ===");
        for (Transition transition : transitions) {
            System.out.println(transition);
        }
    }
}
