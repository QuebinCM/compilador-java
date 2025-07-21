package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Panel que simula una terminal para mostrar mensajes del sistema.
 * Permite mostrar diferentes tipos de mensajes con formato.
 */
public class TerminalPanel extends JPanel {
    private final JTextArea terminalArea;
    private final JScrollPane scrollPane;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * Constructor que inicializa el panel terminal.
     */
    public TerminalPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Terminal de Mensajes"));
        
        // Configurar área de texto
        terminalArea = new JTextArea(8, 50);
        terminalArea.setEditable(false);
        terminalArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        terminalArea.setCaretColor(Color.GREEN);
        
        // Scroll pane
        scrollPane = new JScrollPane(terminalArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Botón para limpiar terminal
        JButton clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> clearTerminal());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Mensaje inicial
        addMessage("Terminal iniciada - Lista para mostrar mensajes", MessageType.INFO);
    }
    
    /**
     * Tipos de mensajes soportados por el terminal.
     */
    public enum MessageType {
        INFO("[INFO]", Color.GREEN),
        WARNING("[WARN]", Color.YELLOW),
        ERROR("[ERROR]", Color.RED),
        SUCCESS("[OK]", Color.CYAN);
        
        private final String prefix;
        private final Color color;
        
        MessageType(String prefix, Color color) {
            this.prefix = prefix;
            this.color = color;
        }
        
        public String getPrefix() { return prefix; }
        public Color getColor() { return color; }
    }
    
    /**
     * Añade un mensaje al terminal con timestamp.
     * 
     * @param message Mensaje a mostrar
     * @param type Tipo de mensaje
     */
    public void addMessage(String message, MessageType type) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(TIME_FORMAT);
            String formattedMessage = String.format("[%s] %s %s%n", 
                                                   timestamp, type.getPrefix(), message);
            
            terminalArea.append(formattedMessage);
            terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
        });
    }
    
    /**
     * Añade un mensaje de información.
     */
    public void addInfo(String message) {
        addMessage(message, MessageType.INFO);
    }
    
    /**
     * Añade un mensaje de advertencia.
     */
    public void addWarning(String message) {
        addMessage(message, MessageType.WARNING);
    }
    
    /**
     * Añade un mensaje de error.
     */
    public void addError(String message) {
        addMessage(message, MessageType.ERROR);
    }
    
    /**
     * Añade un mensaje de éxito.
     */
    public void addSuccess(String message) {
        addMessage(message, MessageType.SUCCESS);
    }
    
    /**
     * Muestra el error semantico.
     */
    public void addSemanticError(String message, int line, int column) {
        String formattedMessage = String.format("Línea %d, Columna %d: %s", line, column, message);
        addMessage(formattedMessage, MessageType.ERROR);
    }
    
    /**
     * Limpia el contenido del terminal.
     */
    public void clearTerminal() {
        terminalArea.setText("");
        addInfo("Terminal limpiada");
    }
}