package semantic;

import lexer.Token;
import lexer.TokenType;
import parser.SLR1Parser;
import parser.SLR1Parser.ParseTreeNode;
import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable = new SymbolTable();
    private final List<SemanticError> errors = new ArrayList<>();
    private TokenType currentFunctionReturnType = null;

    public SemanticAnalysisResult analyze(ParseTreeNode root) {
        traverse(root);
        return new SemanticAnalysisResult(errors, symbolTable);
    }

    private void traverse(ParseTreeNode node) {
        try {
            switch (node.getSymbol()) {
                case "program":
                    processProgram(node);
                    break;
                case "decl_stmt":
                    processDeclaration(node);
                    break;
                case "function_decl":
                    processFunctionDeclaration(node);
                    break;
                case "expr":
                    processExpression(node);
                    break;
                case "block":
                    symbolTable.enterScope();
                    for (ParseTreeNode child : node.getChildren()) {
                        traverse(child);
                    }
                    symbolTable.exitScope();
                    break;
                default:
                    for (ParseTreeNode child : node.getChildren()) {
                        traverse(child);
                    }
            }
        } catch (Exception e) {
            errors.add(new SemanticError("Error durante el análisis semántico: " + e.getMessage(),
                    node.isTerminal() ? node.getToken() : null));
        }
    }

    private void processProgram(ParseTreeNode node) {
        for (ParseTreeNode child : node.getChildren()) {
            traverse(child);
        }
    }
    
    private TokenType getTokenTypeFromNode(ParseTreeNode node) {
        if (node == null || node.getSymbol() == null) {
            return null;
        }

        // Mapeo especial para tipos de datos
        if (node.getSymbol().equals("type")) {
            return getTypeFromTypeNode(node);
        }

        // Para nodos terminales (que tienen token)
        if (node.isTerminal() && node.getToken() != null) {
            return node.getToken().getType();
        }

        // Mapeo de símbolos no terminales a tipos
        switch (node.getSymbol()) {
            case "INTEGER":
                return TokenType.INT;
            case "FLOAT_NUMBER":
                return TokenType.FLOAT;
            case "STRING_LITERAL":
                return TokenType.STRING_LITERAL;
            case "CHAR_LITERAL":
                return TokenType.CHAR_LITERAL;
            case "TRUE":
            case "FALSE":
                return TokenType.BOOLEAN;
            default:
                return null;
        }
    }

    private TokenType getTypeFromTypeNode(ParseTreeNode typeNode) {
        // typeNode es un nodo "type" con hijos como "INT", "FLOAT", etc.
        if (typeNode.getChildren().isEmpty() || !typeNode.getChildren().get(0).isTerminal()) {
            return null;
        }

        return typeNode.getChildren().get(0).getToken().getType();
    }

    private void processDeclaration(ParseTreeNode node) {
        // Obtener el tipo
        TokenType type = getTokenTypeFromNode(node.getChildren().get(0));
        if (type == null) {
            errors.add(new SemanticError("Tipo no válido en declaración",
                    node.getChildren().get(0).getToken()));
            return;
        }

        String identifier = node.getChildren().get(1).getToken().getLexeme();

        // Verificar si la variable ya está declarada
        if (symbolTable.lookup(identifier) != null) {
            errors.add(new SemanticError("Variable '" + identifier + "' ya declarada",
                    node.getChildren().get(1).getToken()));
            return;
        }

        // Agregar a la tabla de símbolos
        Symbol symbol = new Symbol(identifier, type, null, symbolTable.getCurrentScope());
        if (!symbolTable.addSymbol(symbol)) {
            errors.add(new SemanticError("No se pudo agregar la variable '" + identifier + "' a la tabla de símbolos",
                    node.getChildren().get(1).getToken()));
        }

        // Verificación de tipo en asignación
        if (node.getChildren().size() > 3) {
            TokenType exprType = checkExpressionType(node.getChildren().get(3));
            if (exprType != null && !areTypesCompatible(type, exprType)) {
                errors.add(new SemanticError("Asignación incompatible: no se puede convertir de " + exprType + " a " + type,
                        node.getChildren().get(1).getToken()));
            }
        }
    }

    private void processFunctionDeclaration(ParseTreeNode node) {
        TokenType returnType = getTokenTypeFromNode(node.getChildren().get(0));
        if (returnType == null) {
            errors.add(new SemanticError("Tipo de retorno no válido",
                    node.getChildren().get(0).getToken()));
            return;
        }

        String functionName = node.getChildren().get(1).getToken().getLexeme();

        // Verificar si la función ya está declarada
        if (symbolTable.lookup(functionName) != null) {
            errors.add(new SemanticError("Función '" + functionName + "' ya declarada",
                    node.getChildren().get(1).getToken()));
            return;
        }

        // Procesar parámetros
        List<TokenType> paramTypes = new ArrayList<>();
        if (node.getChildren().size() > 3 && node.getChildren().get(3).getSymbol().equals("param_list")) {
            for (ParseTreeNode param : node.getChildren().get(3).getChildren()) {
                if (param.getSymbol().equals("param")) {
                    TokenType paramType = getTokenTypeFromNode(param.getChildren().get(0));
                    if (paramType != null) {
                        paramTypes.add(paramType);
                    }
                }
            }
        }

        // Agregar función a la tabla de símbolos
        //Symbol functionSymbol = new Symbol(functionName, TokenType.FUNCTION, null, 
                                         //symbolTable.getCurrentScope(), true, returnType, paramTypes);
        //symbolTable.addSymbol(functionSymbol);

        // Procesar el cuerpo de la función
        currentFunctionReturnType = returnType;
        symbolTable.enterScope();
        traverse(node.getChildren().get(node.getChildren().size() - 1)); // block
        symbolTable.exitScope();
        currentFunctionReturnType = null;
    }

    private TokenType checkExpressionType(ParseTreeNode exprNode) {
        if (exprNode == null) {
            return null;
        }

        // Para nodos terminales
        if (exprNode.isTerminal()) {
            return getTokenTypeFromNode(exprNode);
        }

        // Para expresiones más complejas
        switch (exprNode.getSymbol()) {
            case "expr":
            case "and_expr":
            case "eq_expr":
            case "rel_expr":
            case "add_expr":
            case "term":
                return checkExpressionType(exprNode.getChildren().get(0));

            case "factor":
                if (exprNode.getChildren().get(0).getSymbol().equals("LEFT_PAREN")) {
                    return checkExpressionType(exprNode.getChildren().get(1));
                }
                return checkExpressionType(exprNode.getChildren().get(0));

            case "postfix_expr":
                return checkExpressionType(exprNode.getChildren().get(0));

            case "unary_expr":
                if (exprNode.getChildren().size() == 1) {
                    return checkExpressionType(exprNode.getChildren().get(0));
                }
                return checkExpressionType(exprNode.getChildren().get(1));

            case "function_call":
                Symbol func = symbolTable.lookup(exprNode.getChildren().get(0).getToken().getLexeme());
                return func != null ? func.getReturnType() : null;

            default:
                return null;
        }
    }

    private boolean areTypesCompatible(TokenType target, TokenType source) {
        // Implementar reglas de compatibilidad de tipos
        // Esto es muy básico - deberías expandirlo
        return target == source;
    }

    private void processExpression(ParseTreeNode node) {
        // Verificar tipos en expresiones
        // Implementar según tus necesidades
        for (ParseTreeNode child : node.getChildren()) {
            traverse(child);
        }
    }
}