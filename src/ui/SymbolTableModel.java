// SymbolTableModel.java
package ui;

import semantic.Symbol;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lexer.TokenType;

public class SymbolTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {"Nombre", "Tipo", "Ámbito", "Es Función", "Tipo Retorno", "Parámetros"};
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, Integer.class, Boolean.class, String.class, String.class};
    
    private List<Symbol> symbols;
    
    public SymbolTableModel() {
        this.symbols = new ArrayList<>();
    }
    
    public void setSymbols(Map<String, Symbol> symbolMap) {
        this.symbols = new ArrayList<>(symbolMap.values());
        fireTableDataChanged();
    }
    
    @Override
    public int getRowCount() {
        return symbols.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Symbol symbol = symbols.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> symbol.getName();
            case 1 -> symbol.getType().name();
            case 2 -> symbol.getScopeLevel();
            case 3 -> symbol.isFunction();
            case 4 -> symbol.isFunction() ? symbol.getReturnType().name() : "N/A";
            case 5 -> symbol.isFunction() ? formatParameters(symbol.getParameters()) : "N/A";
            default -> null;
        };
    }
    
    private String formatParameters(List<TokenType> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "Sin parámetros";
        }
        StringBuilder sb = new StringBuilder();
        for (TokenType type : parameters) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(type.name());
        }
        return sb.toString();
    }
}