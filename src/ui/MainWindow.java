package ui;

import lexer.*;
import parser.*;
import utils.CSVExporter;
import utils.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import semantic.SemanticAnalysisResult;
import semantic.SemanticAnalyzer;
import semantic.SemanticError;

/**
 * Ventana principal de la aplicación del analizador léxico y sintáctico.
 * Contiene todos los componentes de la interfaz y maneja las interacciones.
 */
public class MainWindow extends JFrame {
    // Componentes principales
    private JTextArea codeArea;
    private JTable tokenTable;
    private TokenTableModel tokenTableModel;
    private JTable parseStepsTable;
    private JTable symbolTable;
    private SymbolTableModel symbolTableModel;
    private DefaultTableModel parseStepsTableModel;
    private JTextArea parseTreeArea;
    private TerminalPanel terminalPanel;
    
    // Botones
    private JButton loadFileButton;
    private JButton analyzeLexicalButton;
    private JButton analyzeSyntaxButton;
    private JButton exportTokensButton;
    private JButton exportTreeButton;
    private JButton clearButton;
    
    // Datos del análisis
    private List<Token> currentTokens;
    private ParseResult currentParseResult;
    private List<ParseStep> parseSteps;
    
    /**
     * Representa un paso del análisis sintáctico para la tabla.
     */
    private static class ParseStep {
        private final int step;
        private final String stack;
        private final String input;
        private final String action;
        
        public ParseStep(int step, String stack, String input, String action) {
            this.step = step;
            this.stack = stack;
            this.input = input;
            this.action = action;
        }
        
        public int getStep() { return step; }
        public String getStack() { return stack; }
        public String getInput() { return input; }
        public String getAction() { return action; }
    }
    
    /**
     * Constructor que inicializa la ventana principal.
     */
    public MainWindow() {
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupWindow();
    }
    
    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void initializeComponents() {
        // Área de código
        codeArea = new JTextArea(12, 40);
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        codeArea.setLineWrap(false);
        codeArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Modelo y tabla de tokens
        tokenTableModel = new TokenTableModel();
        tokenTable = new JTable(tokenTableModel);
        setupTokenTable();
        
        // Tabla de pasos del análisis sintáctico
        parseStepsTableModel = new DefaultTableModel(
            new String[]{"Paso", "Pila", "Entrada", "Acción", "Producción"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        parseStepsTable = new JTable(parseStepsTableModel);
        setupParseStepsTable();
        
        // Área del árbol sintáctico
        parseTreeArea = new JTextArea(15, 30);
        parseTreeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        parseTreeArea.setEditable(false);
        parseTreeArea.setBackground(Color.WHITE);
        parseTreeArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Tabla de símbolos
        symbolTableModel = new SymbolTableModel();
        symbolTable = new JTable(symbolTableModel);
        setupSymbolTable();
        
        // Terminal
        terminalPanel = new TerminalPanel();
        
        // Botones
        loadFileButton = new JButton("Cargar Archivo");
        analyzeLexicalButton = new JButton("Análisis Léxico");
        analyzeSyntaxButton = new JButton("Análisis Sintáctico");
        exportTokensButton = new JButton("Exportar Tokens");
        exportTreeButton = new JButton("Exportar Árbol");
        clearButton = new JButton("Limpiar Todo");
        
        // Configurar botones
        setupButtons();
        
        // Inicializar datos
        currentTokens = new ArrayList<>();
        parseSteps = new ArrayList<>();
    }
    
    /**
     * Configura la tabla de tokens con formato apropiado.
     */
    private void setupTokenTable() {
        tokenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tokenTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Configurar anchos de columna
        tokenTable.getColumnModel().getColumn(0).setPreferredWidth(120); // TOKEN
        tokenTable.getColumnModel().getColumn(1).setPreferredWidth(100); // TIPO
        tokenTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // LÍNEA
        tokenTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // COLUMNA
        
        // Centrar contenido de columnas numéricas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tokenTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tokenTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        tokenTable.setRowHeight(22);
        tokenTable.setShowGrid(true);
        tokenTable.setGridColor(Color.LIGHT_GRAY);
    }
    
    /**
     * Configura la tabla de pasos del análisis sintáctico.
     */
    private void setupParseStepsTable() {
        parseStepsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        parseStepsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Configurar anchos de columna
        parseStepsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Paso
        parseStepsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Pila
        parseStepsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Entrada
        parseStepsTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Acción
        
        // Centrar columna de paso
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        parseStepsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        parseStepsTable.setRowHeight(22);
        parseStepsTable.setShowGrid(true);
        parseStepsTable.setGridColor(Color.LIGHT_GRAY);
        parseStepsTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }
    
    /**
     * Configura la tabla de símbolos.
     */
    private void setupSymbolTable() {
        symbolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        symbolTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Configurar anchos de columna
        symbolTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Nombre
        symbolTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Tipo
        symbolTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // Ámbito
        symbolTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Es Función
        symbolTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Tipo Retorno
        symbolTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Parámetros
        
        symbolTable.setRowHeight(22);
        symbolTable.setShowGrid(true);
        symbolTable.setGridColor(Color.LIGHT_GRAY);
    }
    
    /**
     * Configura los botones con tooltips y estado inicial.
     */
    private void setupButtons() {
        loadFileButton.setToolTipText("Cargar archivo de código fuente");
        analyzeLexicalButton.setToolTipText("Ejecutar análisis léxico del código");
        analyzeSyntaxButton.setToolTipText("Ejecutar análisis sintáctico (requiere tokens)");
        exportTokensButton.setToolTipText("Exportar tabla de tokens a CSV");
        exportTreeButton.setToolTipText("Exportar árbol sintáctico");
        clearButton.setToolTipText("Limpiar todo el contenido");
        
        // Estado inicial de botones
        analyzeSyntaxButton.setEnabled(false);
        exportTokensButton.setEnabled(false);
        exportTreeButton.setEnabled(false);
    }
    
    /**
     * Configura el layout principal de la ventana.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior con botones
        JPanel topPanel = createButtonPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Pestaña 1: Código y Tokens
        JSplitPane codeTokenSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        codeTokenSplit.setResizeWeight(0.6);
        codeTokenSplit.setLeftComponent(createCodePanel());
        codeTokenSplit.setRightComponent(createTokenPanel());
        tabbedPane.addTab("Análisis Léxico", codeTokenSplit);
        
        // Pestaña 2: Análisis Sintáctico
        JSplitPane syntaxSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        syntaxSplit.setResizeWeight(0.5);
        syntaxSplit.setLeftComponent(createParseStepsPanel());
        syntaxSplit.setRightComponent(createParseTreePanel());
        tabbedPane.addTab("Análisis Sintáctico", syntaxSplit);
        
        // Pestaña 3: Tabla de Símbolos (Análisis Semántico)
        JScrollPane symbolScrollPane = new JScrollPane(symbolTable);
        symbolScrollPane.setBorder(BorderFactory.createTitledBorder("Tabla de Símbolos"));
        tabbedPane.addTab("Análisis Semántico", symbolScrollPane);
        
        // Split principal vertical (pestañas arriba, terminal abajo)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setResizeWeight(0.75);
        mainSplit.setTopComponent(tabbedPane);
        mainSplit.setBottomComponent(terminalPanel);
        
        add(mainSplit, BorderLayout.CENTER);
    }
    
    /**
     * Crea el panel superior con botones.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        panel.add(loadFileButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(analyzeLexicalButton);
        panel.add(analyzeSyntaxButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(exportTokensButton);
        panel.add(exportTreeButton);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Crea el panel del área de código.
     */
    private JPanel createCodePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Código Fuente"));
        
        JScrollPane scrollPane = new JScrollPane(codeArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel con información
        JLabel infoLabel = new JLabel("Escriba o cargue código para analizar");
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.ITALIC));
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crea el panel de la tabla de tokens.
     */
    private JPanel createTokenPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tokens Generados"));
        
        JScrollPane scrollPane = new JScrollPane(tokenTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Etiqueta de estado
        JLabel statusLabel = new JLabel("0 tokens");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crea el panel de pasos del análisis sintáctico.
     */
    private JPanel createParseStepsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pasos del Análisis"));
        
        JScrollPane scrollPane = new JScrollPane(parseStepsTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Etiqueta de estado
        JLabel statusLabel = new JLabel("0 pasos");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crea el panel del árbol sintáctico.
     */
    private JPanel createParseTreePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Árbol Sintáctico"));
        
        JScrollPane scrollPane = new JScrollPane(parseTreeArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Configura los event listeners de los botones.
     */
    private void setupEventListeners() {
        loadFileButton.addActionListener(new LoadFileAction());
        analyzeLexicalButton.addActionListener(new AnalyzeLexicalAction());
        analyzeSyntaxButton.addActionListener(new AnalyzeSyntaxAction());
        exportTokensButton.addActionListener(new ExportTokensAction());
        exportTreeButton.addActionListener(new ExportTreeAction());
        clearButton.addActionListener(new ClearAction());
    }
    
    /**
     * Configura las propiedades de la ventana.
     */
    private void setupWindow() {
        setTitle("Analizador Léxico y Sintáctico - Compilador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));
    }
    
    /**
     * Actualiza la etiqueta de estado de tokens.
     */
    private void updateTokenStatus() {
        // Implementación para actualizar estado
        int tokenCount = tokenTableModel.getRowCount();
        // Buscar y actualizar el label de estado de tokens
    }
    
    /**
     * Actualiza la etiqueta de estado de pasos.
     */
    private void updateParseStepsStatus() {
        // Implementación para actualizar estado de pasos
        int stepCount = parseStepsTableModel.getRowCount();
        // Buscar y actualizar el label de estado de pasos
    }
    
    // Clases internas para manejar eventos
    
    /**
     * Acción para cargar archivo.
     */
    private class LoadFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            terminalPanel.addInfo("Abriendo diálogo de carga de archivo...");
            
            String content = FileUtils.loadTextFile(MainWindow.this);
            if (content != null) {
                codeArea.setText(content);
                terminalPanel.addSuccess("Archivo cargado exitosamente");
                terminalPanel.addInfo("Líneas cargadas: " + content.split("\\n").length);
            } else {
                terminalPanel.addWarning("Carga de archivo cancelada o falló");
            }
        }
    }
    
    /**
     * Acción para analizar léxicamente.
     */
    private class AnalyzeLexicalAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String code = codeArea.getText().trim();
            
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "No hay código para analizar. Escriba o cargue código primero.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
                );
                terminalPanel.addWarning("Intento de análisis sin código");
                return;
            }
            
            try {
                terminalPanel.addInfo("Iniciando análisis léxico...");
                
                Lexer lexer = new Lexer(code);
                currentTokens = lexer.tokenize();
                
                tokenTableModel.setTokens(currentTokens);
                updateTokenStatus();
                
                terminalPanel.addSuccess("Análisis léxico completado");
                terminalPanel.addInfo("Tokens generados: " + currentTokens.size());
                
                // Habilitar botones
                analyzeSyntaxButton.setEnabled(true);
                exportTokensButton.setEnabled(true);
                
            } catch (LexerException ex) {
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "Error léxico: " + ex.getMessage(),
                    "Error en el Análisis",
                    JOptionPane.ERROR_MESSAGE
                );
                
                terminalPanel.addError("Error léxico: " + ex.getMessage());
                terminalPanel.addError("Línea: " + ex.getLine() + ", Columna: " + ex.getColumn());
                
                // Deshabilitar botones
                analyzeSyntaxButton.setEnabled(false);
                exportTokensButton.setEnabled(false);
            }
        }
    }
    
    /**
     * Acción para analizar sintácticamente.
     */
    private class AnalyzeSyntaxAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentTokens == null || currentTokens.isEmpty()) {
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "No hay tokens para analizar. Ejecute el análisis léxico primero.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
                );
                terminalPanel.addWarning("Intento de análisis sintáctico sin tokens");
                return;
            }
            
            try {
                terminalPanel.addInfo("Iniciando análisis sintáctico...");
                
                long startTime = System.currentTimeMillis();
                
                // Crear parser y analizar
                SLR1Parser parser = new SLR1Parser();
                
                // Validar tokens antes del análisis
                List<String> warnings = parser.validateTokens(currentTokens);
                
                // Realizar análisis
                SLR1Parser.ParseTreeNode parseTree = parser.parse(currentTokens);
                
                long endTime = System.currentTimeMillis();
                long parseTime = endTime - startTime;
                
                // Crear resultado exitoso
                currentParseResult = new ParseResult(parseTree, warnings, parseTime);
                
                // Mostrar árbol sintáctico
                parseTreeArea.setText(parseTree.toTreeString());
                
                // Simular pasos del análisis (en una implementación real, 
                // el parser debería registrar estos pasos)
                simulateParseSteps();
                
                terminalPanel.addSuccess("Análisis sintáctico completado exitosamente");
                terminalPanel.addInfo("Tiempo de análisis: " + parseTime + " ms");
                
                if (!warnings.isEmpty()) {
                    terminalPanel.addWarning("Se encontraron " + warnings.size() + " advertencias");
                    for (String warning : warnings) {
                        terminalPanel.addWarning(warning);
                    }
                }
                
                parseStepsTableModel.setRowCount(0);
                List<SLR1Parser.ParseStep> steps = parser.getParseSteps();
                for (SLR1Parser.ParseStep step : steps) {
                    parseStepsTableModel.addRow(new Object[]{
                        step.getStep(),
                        step.getStack(),
                        step.getInput(),
                        step.getAction(),
                        step.getProduction()
                    });
                }
                
                //System.out.println("DEBUG:" + parser.getDebugInfo());
                //parser.printTables();

                // Habilitar exportación del árbol
                exportTreeButton.setEnabled(true);
                
                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
                SemanticAnalysisResult semanticResult = semanticAnalyzer.analyze(parseTree);
                
                // Mostrar resultados en terminal
                if (semanticResult.hasErrors()) {
                    terminalPanel.addError("\nErrores semánticos encontrados:");
                    for (SemanticError error : semanticResult.getErrors()) {
                        terminalPanel.addError(error.toString());
                    }
                } else {
                    terminalPanel.addSuccess("\nAnálisis semántico completado sin errores");
                }
                
                // Actualizar tabla de símbolos
                symbolTableModel.setSymbols(semanticResult.getSymbolTable().getSymbols());
                
            } catch (ParserException ex) {
                long endTime = System.currentTimeMillis();
                long parseTime = endTime - System.currentTimeMillis();
                
                // Crear resultado con error
                List<ParserException> errors = new ArrayList<>();
                errors.add(ex);
                currentParseResult = new ParseResult(errors, new ArrayList<>(), parseTime);
                
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "Error sintáctico: " + ex.getMessage(),
                    "Error en el Análisis Sintáctico",
                    JOptionPane.ERROR_MESSAGE
                );
                
                terminalPanel.addError("Error sintáctico: " + ex.getMessage());
                terminalPanel.addError(ex.getDetailedMessage());
                
                // Limpiar árbol sintáctico
                parseTreeArea.setText("Error en el análisis sintáctico:\n\n" + ex.getDetailedMessage());
                
                // Deshabilitar exportación del árbol
                exportTreeButton.setEnabled(false);
            }
        }
    }
    
    /**
     * Simula los pasos del análisis sintáctico.
     * En una implementación completa, el parser debería registrar estos pasos.
     */
    private void simulateParseSteps() {
        parseStepsTableModel.setRowCount(0);
        parseSteps.clear();
        
        // Ejemplo de pasos simulados
        parseSteps.add(new ParseStep(1, "[0]", "id = num + num ; $", "shift 5"));
        parseSteps.add(new ParseStep(2, "[0, 5]", "= num + num ; $", "shift 6"));
        parseSteps.add(new ParseStep(3, "[0, 5, 6]", "num + num ; $", "shift 8"));
        // ... más pasos simulados
        
        // Agregar pasos a la tabla
        for (ParseStep step : parseSteps) {
            parseStepsTableModel.addRow(new Object[]{
                step.getStep(),
                step.getStack(),
                step.getInput(),
                step.getAction()
            });
        }
        
        updateParseStepsStatus();
    }
    
    /**
     * Acción para exportar tokens a CSV.
     */
    private class ExportTokensAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (tokenTableModel.isEmpty()) {
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "No hay tokens para exportar. Ejecute el análisis léxico primero.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
                );
                terminalPanel.addWarning("Intento de exportación sin tokens");
                return;
            }
            
            terminalPanel.addInfo("Iniciando exportación de tokens a CSV...");
            
            boolean success = CSVExporter.exportTokensToCSV(tokenTableModel.getTokens(), MainWindow.this);
            
            if (success) {
                terminalPanel.addSuccess("Tokens exportados a CSV exitosamente");
            } else {
                terminalPanel.addError("Error al exportar tokens a CSV");
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "Error al exportar archivo CSV. Verifique los permisos y el espacio disponible.",
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Acción para exportar árbol sintáctico.
     */
    private class ExportTreeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentParseResult == null || !currentParseResult.isSuccess()) {
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "No hay árbol sintáctico para exportar. Ejecute el análisis sintáctico exitosamente primero.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
                );
                terminalPanel.addWarning("Intento de exportación sin árbol sintáctico válido");
                return;
            }
            
            // Diálogo para seleccionar formato de exportación
            String[] options = {"Texto (.txt)", "CSV (.csv)"};
            int choice = JOptionPane.showOptionDialog(
                MainWindow.this,
                "Seleccione el formato de exportación:",
                "Exportar Árbol Sintáctico",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            
            if (choice == -1) return; // Cancelado
            
            terminalPanel.addInfo("Iniciando exportación del árbol sintáctico...");
            
            boolean success = false;
            
            try {
                if (choice == 0 || choice == 2) {
                    // Exportar como texto
                    success = exportTreeAsText();
                }
                
                if (choice == 1 || choice == 2) {
                    // Exportar como CSV
                    success = exportTreeAsCSV() && success;
                }

                if (success) {
                    terminalPanel.addSuccess("Árbol sintáctico exportado exitosamente");
                } else {
                    terminalPanel.addError("Error al exportar árbol sintáctico");
                }
                
            } catch (Exception ex) {
                terminalPanel.addError("Error durante la exportación: " + ex.getMessage());
                JOptionPane.showMessageDialog(
                    MainWindow.this,
                    "Error al exportar: " + ex.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
        
        private boolean exportTreeAsText() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar Árbol Sintáctico como Texto");
            chooser.setSelectedFile(new File("arbol_sintactico.txt"));
            
            if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                    writer.write("ÁRBOL SINTÁCTICO\n");
                    writer.write("================\n\n");
                    writer.write(currentParseResult.getParseTree().toTreeString());
                    writer.write("\n\nRESUMEN DEL ANÁLISIS\n");
                    writer.write("====================\n");
                    writer.write(currentParseResult.getSummary());
                    
                    if (currentParseResult.hasWarnings()) {
                        writer.write("\n\n" + currentParseResult.getFormattedWarnings());
                    }
                    
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
            return false;
        }
        
        private boolean exportTreeAsCSV() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar Árbol Sintáctico como CSV");
            chooser.setSelectedFile(new File("arbol_sintactico.csv"));
            
            if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                    writer.write("Nivel,Nodo,Tipo,Valor\n");
                    exportNodeToCSV(currentParseResult.getParseTree(), 0, writer);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
            return false;
        }
        
        private void exportNodeToCSV(SLR1Parser.ParseTreeNode node, int level, FileWriter writer) throws IOException {
            String tipo = node.isTerminal() ? "Terminal" : "No Terminal";
            String valor = node.getValue() != null ? node.getValue() : "";
            
            writer.write(String.format("%d,\"%s\",\"%s\",\"%s\"\n", 
                level, node.getSymbol(), tipo, valor));
            
            for (SLR1Parser.ParseTreeNode child : node.getChildren()) {
                exportNodeToCSV(child, level + 1, writer);
            }
        }
    }
    
    /**
     * Acción para limpiar todo.
     */
    private class ClearAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(
                MainWindow.this,
                "¿Está seguro de que desea limpiar todo el contenido?",
                "Confirmar Limpieza",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (option == JOptionPane.YES_OPTION) {
                // Limpiar datos
                codeArea.setText("");
                tokenTableModel.clearTokens();
                parseStepsTableModel.setRowCount(0);
                parseTreeArea.setText("");
                currentTokens = new ArrayList<>();
                currentParseResult = null;
                parseSteps.clear();
                
                // Deshabilitar botones
                analyzeSyntaxButton.setEnabled(false);
                exportTokensButton.setEnabled(false);
                exportTreeButton.setEnabled(false);
                
                // Limpiar tabla de símbolos
                symbolTableModel.setSymbols(new HashMap<>());
                
                // Limpiar terminal
                terminalPanel.clearTerminal();
                terminalPanel.addInfo("Contenido limpiado completamente");
            }
        }
    }
    
    /**
     * Método main para ejecutar la aplicación.
     */
    public static void main(String[] args) {
        
        // Ejecutar en el EDT
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}