package ui;

import lexer.Token;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de tabla personalizado para mostrar tokens en un JTable.
 * Maneja la visualización de tokens con sus propiedades.
 */
public class TokenTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {"TOKEN", "TIPO", "LÍNEA", "COLUMNA"};
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, Integer.class, Integer.class};
    
    private List<Token> tokens;
    
    /**
     * Constructor que inicializa el modelo con una lista vacía.
     */
    public TokenTableModel() {
        this.tokens = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return tokens.size();
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
        if (rowIndex < 0 || rowIndex >= tokens.size()) {
            return null;
        }
        
        Token token = tokens.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> token.getLexeme();
            case 1 -> token.getType().name();
            case 2 -> token.getLine();
            case 3 -> token.getColumn();
            default -> null;
        };
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // La tabla es de solo lectura
    }
    
    /**
     * Actualiza la tabla con una nueva lista de tokens.
     * 
     * @param tokens Nueva lista de tokens a mostrar
     */
    public void setTokens(List<Token> tokens) {
        this.tokens = tokens != null ? new ArrayList<>(tokens) : new ArrayList<>();
        fireTableDataChanged();
    }
    
    /**
     * Limpia todos los tokens de la tabla.
     */
    public void clearTokens() {
        this.tokens.clear();
        fireTableDataChanged();
    }
    
    /**
     * Obtiene la lista actual de tokens.
     * 
     * @return Lista de tokens
     */
    public List<Token> getTokens() {
        return new ArrayList<>(tokens);
    }
    
    /**
     * Verifica si la tabla está vacía.
     * 
     * @return true si no hay tokens, false en caso contrario
     */
    public boolean isEmpty() {
        return tokens.isEmpty();
    }
}