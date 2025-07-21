package parser;

import java.util.*;

/**
 * Construcción de las tablas ACTION y GOTO para el parser SLR(1).
 */
public class SLR1Table {
    
    /**
     * Tipos de acciones en la tabla ACTION.
     */
    public enum ActionType {
        SHIFT, REDUCE, ACCEPT, ERROR
    }
    
    /**
     * Representa una acción en la tabla ACTION.
     */
    public static class Action {
        private final ActionType type;
        private final int value; // Estado para SHIFT, producción para REDUCE
        
        public Action(ActionType type, int value) {
            this.type = type;
            this.value = value;
        }
        
        public Action(ActionType type) {
            this(type, -1);
        }
        
        public ActionType getType() { return type; }
        public int getValue() { return value; }
        
        @Override
        public String toString() {
            return switch (type) {
                case SHIFT -> "s" + value;
                case REDUCE -> "r" + value;
                case ACCEPT -> "acc";
                case ERROR -> "error";
            };
        }
    }
    
    private final Map<Integer, Map<String, Action>> actionTable;
    private final Map<Integer, Map<String, Integer>> gotoTable;
    private final LR0Automaton automaton;
    private final FirstFollowSets firstFollow;
    
    public SLR1Table(LR0Automaton automaton, FirstFollowSets firstFollow) {
        this.automaton = automaton;
        this.firstFollow = firstFollow;
        this.actionTable = new HashMap<>();
        this.gotoTable = new HashMap<>();
        buildTables();
    }
    
    /**
     * Construye las tablas ACTION y GOTO.
     */
    private void buildTables() {
        // Inicializar tablas
        for (LR0Automaton.State state : automaton.getStates()) {
            actionTable.put(state.getId(), new HashMap<>());
            gotoTable.put(state.getId(), new HashMap<>());
        }
        
        // Llenar tablas según las reglas SLR(1)
        for (LR0Automaton.State state : automaton.getStates()) {
            fillTableForState(state);
        }
    }
    
    /**
     * Llena las tablas para un estado específico.
     */
    private void fillTableForState(LR0Automaton.State state) {
        Map<String, Action> stateActions = actionTable.get(state.getId());
        Map<String, Integer> stateGotos = gotoTable.get(state.getId());
        
        for (Grammar.Item item : state.getItems()) {
            if (item.isComplete()) {
                // Reglas de reducción
                if (item.getProduction().getId() == 0) {
                    // S' -> program •, acción ACCEPT
                    stateActions.put("$", new Action(ActionType.ACCEPT));
                } else {
                    // A -> α •, reducir por producción
                    String leftSymbol = item.getProduction().getLeft();
                    Set<String> followSet = firstFollow.getFollow(leftSymbol);
                    
                    for (String terminal : followSet) {
                        Action existingAction = stateActions.get(terminal);
                        if (existingAction != null) {
                            if (terminal.equals("ELSE")) {
                                stateActions.put(terminal, new Action(ActionType.SHIFT, item.getProduction().getId()));
                                continue;
                            }
                            System.err.println("CONFLICT: Reduce-Reduce en estado " + 
                                             state.getId() + " terminal " + terminal);
                        }
                        stateActions.put(terminal, new Action(ActionType.REDUCE, 
                                                            item.getProduction().getId()));
                    }
                }
            } else {
                // Reglas de desplazamiento y goto
                String nextSymbol = item.getNextSymbol();
                Integer nextState = automaton.getTransition(state.getId(), nextSymbol);
                
                if (nextState != null) {
                    if (Grammar.isTerminal(nextSymbol)) {
                        // Acción SHIFT
                        Action existingAction = stateActions.get(nextSymbol);
                        if (existingAction != null && existingAction.getType() == ActionType.REDUCE) {
                            if (nextSymbol.equals("ELSE")) {
                                stateActions.put(nextSymbol, new Action(ActionType.SHIFT, nextState));
                                continue;
                            }
                            System.err.println("CONFLICT: Shift-Reduce en estado " + 
                                             state.getId() + " terminal " + nextSymbol);
                        }
                        stateActions.put(nextSymbol, new Action(ActionType.SHIFT, nextState));
                    } else {
                        // Acción GOTO
                        stateGotos.put(nextSymbol, nextState);
                    }
                }
            }
        }
    }
    
    public Action getAction(int state, String terminal) {
        return actionTable.getOrDefault(state, Map.of())
                         .getOrDefault(terminal, new Action(ActionType.ERROR));
    }
    
    public Integer getGoto(int state, String nonTerminal) {
        return gotoTable.getOrDefault(state, Map.of()).get(nonTerminal);
    }
    
    public void printTables() {
        System.out.println("=== TABLA ACTION ===");
        System.out.printf("%-8s", "Estado");
        for (String terminal : Grammar.getTerminals()) {
            System.out.printf("%-10s", terminal);
        }
        System.out.println();
        
        for (int i = 0; i < automaton.getStates().size(); i++) {
            System.out.printf("%-8d", i);
            for (String terminal : Grammar.getTerminals()) {
                Action action = getAction(i, terminal);
                System.out.printf("%-10s", action.getType() == ActionType.ERROR ? "" : action);
            }
            System.out.println();
        }
        
        System.out.println("\n=== TABLA GOTO ===");
        System.out.printf("%-8s", "Estado");
        for (String nonTerminal : Grammar.getNonTerminals()) {
            if (!nonTerminal.equals("S'")) {
                System.out.printf("%-12s", nonTerminal);
            }
        }
        System.out.println();
        
        for (int i = 0; i < automaton.getStates().size(); i++) {
            System.out.printf("%-8d", i);
            for (String nonTerminal : Grammar.getNonTerminals()) {
                if (!nonTerminal.equals("S'")) {
                    Integer gotoState = getGoto(i, nonTerminal);
                    System.out.printf("%-12s", gotoState != null ? gotoState : "");
                }
            }
            System.out.println();
        }
        System.out.println("=== AUTOMATA ===");
        automaton.printAutomaton();
        System.out.println("=== SETS ===");
        firstFollow.printSets();
    }
}