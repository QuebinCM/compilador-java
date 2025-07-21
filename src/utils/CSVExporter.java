package utils;

import lexer.Token;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utilidad para exportar tokens a formato CSV.
 * Maneja la selección de archivo y escritura de datos.
 */
public class CSVExporter {
    
    /**
     * Exporta una lista de tokens a un archivo CSV.
     * 
     * @param tokens Lista de tokens a exportar
     * @param parent Ventana padre para el diálogo
     * @return true si la exportación fue exitosa, false en caso contrario
     */
    public static boolean exportTokensToCSV(List<Token> tokens, JFrame parent) {
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar tokens como CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        fileChooser.setSelectedFile(new java.io.File("tokens.csv"));
        
        int userSelection = fileChooser.showSaveDialog(parent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            
            // Asegurar extensión .csv
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new java.io.File(file.getAbsolutePath() + ".csv");
            }
            
            try (FileWriter writer = new FileWriter(file)) {
                // Escribir encabezados
                writer.write("TOKEN,TIPO,LINEA,COLUMNA\n");
                
                // Escribir tokens
                for (Token token : tokens) {
                    writer.write(String.format("\"%s\",\"%s\",%d,%d%n",
                                             escapeCSV(token.getLexeme()),
                                             token.getType().name(),
                                             token.getLine(),
                                             token.getColumn()));
                }
                
                return true;
                
            } catch (IOException e) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Escapa caracteres especiales en campos CSV.
     */
    private static String escapeCSV(String field) {
        if (field.contains("\"")) {
            field = field.replace("\"", "\"\"");
        }
        return field;
    }
}