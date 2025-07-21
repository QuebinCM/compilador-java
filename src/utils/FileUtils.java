package utils;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utilidades para manejo de archivos.
 * Proporciona métodos para cargar archivos de texto.
 */
public class FileUtils {
    
    /**
     * Muestra un diálogo para seleccionar y cargar un archivo de texto.
     * 
     * @param parent Ventana padre para el diálogo
     * @return Contenido del archivo o null si se cancela o hay error
     */
    public static String loadTextFile(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar archivo de código");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Archivos de código (*.java, *.txt)", "java", "txt"));
        
        int userSelection = fileChooser.showOpenDialog(parent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                return Files.readString(Paths.get(file.getAbsolutePath()));
            } catch (IOException e) {
                return null;
            }
        }
        
        return null;
    }
}
