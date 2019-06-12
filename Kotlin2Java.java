import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Kotlin2Java {
    public static void main(String[] args) throws Exception {
        // Input
        CharStream charStream;
        Boolean fromConsole = true;
        String fileName = "converted.java";

        if(args.length == 0) {
            charStream = CharStreams.fromStream(System.in);
        } else {
            File file = new File(args[0]);
            if(!file.exists()) {
                System.err.println("File not exist. Input from console.");
                charStream = CharStreams.fromStream(System.in);
            } else {
                if(args.length >= 2) {
                    fileName = args[1];
                } else {
                    fileName = args[0].substring(0, args[0].lastIndexOf(".kt")) + ".java";
                }
                charStream = CharStreams.fromFileName(args[0]);
                fromConsole = false;
            }
        }

        // Process
        KotlinLexer lexer = new KotlinLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        KotlinParser parser = new KotlinParser(tokens);
        ParseTreeVisitor visitor = new KotlinWalker();

        String result = (String)visitor.visit(parser.prog());

        // Output
        fromConsole = true; // For debugging
        if(fromConsole) {
            System.out.println(result);
        } else {
            FileWriter fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(result);
            printWriter.close();
            fileWriter.close();
        }
    }
}

enum Type {
    Int, Integer, Double, String, Long, Object, Boolean, None, List, Void;

    Type subType;

    @Override
    public String toString() {
        switch(this) {
            case Int: return "int";
            case Integer: return "Integer";
            case Double: return "Double";
            case String: return "String";
            case Long: return "long";
            case Object: return "Object";
            case Boolean: return "bool";
            case Void: return "void";
            case List: return "List<" + subType.toString() + ">";
            default: return "None";
        }
    }
}

class Expression {
    String content;
    Type type;

    Expression(String _content, Type _type) {
        content = _content;
        type = _type;
    }

    @Override
    public String toString() {
        return content;
    }

    public Type getType() {
        return type;
    }
}

class KotlinWalker extends KotlinBaseVisitor {
    int typePriority(Type type) {
        return 0; //TODO
    }

    Type typeInference(Type... types) {
        Type type = types[0];

        for (Type t: types) {
            if(typePriority(type) < typePriority(t)) {
                type = t;
            }
        }

        return type;
    }
    
    String nullSafe(Object string) {
        if(string == null) {
            return "";
        } else if(string instanceof String) {
            return (String)string;
        } else {
            return string.toString();
        }
    }

    String visitChildren(RuleNode node, String seperator, int startIndex, int endIndex) {
        StringBuilder result = new StringBuilder();

        if(endIndex > startIndex) {
            if(node.getChild(startIndex) instanceof TerminalNode) {
                if(!node.getChild(startIndex).getText().equals("<EOF>")) {
                    result.append(nullSafe(node.getChild(startIndex).getText()));
                }
            } else {
                result.append(nullSafe(node.getChild(startIndex).accept(this)));
            }
        }
        for(int i = startIndex + 1; i < endIndex; i++) {
            if(node.getChild(i) instanceof TerminalNode) {
                if(!node.getChild(i).getText().equals("<EOF>")) {
                    result.append(seperator + nullSafe(node.getChild(i).getText()));
                }
            } else {
                result.append(seperator + nullSafe(node.getChild(i).accept(this)));
            }
        }

        return result.toString();
    }

    String visitChildren(RuleNode node, String seperator) {
        return visitChildren(node, seperator, 0, node.getChildCount());
    }

    @Override
    public String visitChildren(RuleNode node) {
        return visitChildren(node, "", 0, node.getChildCount());
    }

    @Override
    public String visitProg(KotlinParser.ProgContext ctx) {
        return visitChildren(ctx, "\n");
    }

    @Override
    public String visitPackageDeclaration(KotlinParser.PackageDeclarationContext ctx) {
        return "package " + this.visitPackageName(ctx.packageName()) + ";\n";
    }

    @Override
    public String visitPackageName(KotlinParser.PackageNameContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitImportList(KotlinParser.ImportListContext ctx) {
        return "import java.util.*;\n" + visitChildren(ctx, "\n") + "\n";
    }

    @Override
    public String visitImportDeclaration(KotlinParser.ImportDeclarationContext ctx) {
        return "import " + this.visitImportName(ctx.importName()) + ";";
    }

    @Override
    public String visitImportName(KotlinParser.ImportNameContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitTopLevelBody(KotlinParser.TopLevelBodyContext ctx) {
        return "class Main {\n" + visitChildren(ctx, "\n") + "\n}";
    }

    @Override
    public String visitTopLevelBodyElement(KotlinParser.TopLevelBodyElementContext ctx) {
        return visitChildren(ctx, "\n");
    }

    @Override public
    String visitBody(KotlinParser.BodyContext ctx) {
        return visitChildren(ctx, "\n");
    }

    @Override
    public String visitBodyElement(KotlinParser.BodyElementContext ctx) {
        if(ctx.functionCallStatement() != null || ctx.returnStatement() != null || ctx.expression() != null) {
            return this.visit(ctx.getChild(0)).toString() + ";";
        } else {
            return this.visit(ctx.getChild(0)).toString();
        }

    }

    @Override
    public String visitFunctionDeclaration(KotlinParser.FunctionDeclarationContext ctx) {
        String name;
        Type type = Type.Void;

        if((name = ctx.Id().getText()).equals("main")) {
            if(ctx.expression() != null) {
                return "public static void main(String[] args)" + this.visitExpression(ctx.expression());
            } else {
                return "public static void main(String[] args)" + this.visitCompoundStatement(ctx.compoundStatement());
            }
        } else {
            StringBuilder content = new StringBuilder();

            content.append("static ");

            if(ctx.type() != null) {
                content.append(this.visitType(ctx.type()));
            } else {
                content.append(type.toString());
            }

            content.append(" " + name + "(");

            if(ctx.functionDeclarationParameterList() != null) {
                content.append(this.visitFunctionDeclarationParameterList(ctx.functionDeclarationParameterList()));
            }

            content.append(")");

            if(ctx.expression() != null) {
                content.append(this.visitExpression(ctx.expression()));
            } else if (ctx.compoundStatement() != null) {
                content.append(this.visitCompoundStatement(ctx.compoundStatement()));
            }

            return content.toString();
        }
    }

    @Override
    public String visitFunctionDeclarationParameterList(KotlinParser.FunctionDeclarationParameterListContext ctx) {
        String list = "";

        if(ctx.functionDeclarationParameterList() != null) {
            list = this.visitFunctionDeclarationParameterList(ctx.functionDeclarationParameterList()) + ", ";
        }

        return list + this.visitType(ctx.type()) + " " + ctx.Id().getText();
    }

    @Override
    public Expression visitFunctionCallStatement(KotlinParser.FunctionCallStatementContext ctx) {
        StringBuilder content = new StringBuilder();
        boolean list = false;
        Type argumentType = Type.None;

        if(ctx.Id() != null) {
            if(ctx.Id().getText().equals("println")) {
                content.append("System.out.println");
            } else if(ctx.Id().getText().equals("print")) {
                content.append("System.out.print");
            } else if(ctx.Id().getText().equals("listOf")) {
                //content.append("List.of"); //TODO
                content.append("Arrays.asList");
                list = true;
            } else {
                content.append(ctx.Id().getText());
            }
        } else {
            content.append(this.visitType(ctx.type()));
        }

        if(ctx.compoundStatement() != null) {
            content.append(this.visitCompoundStatement(ctx.compoundStatement()));
        } else {
            content.append("(");
            if(ctx.functionCallArgumentList() != null) {
                Expression argument = this.visitFunctionCallArgumentList(ctx.functionCallArgumentList());
                argumentType = argument.getType();
                content.append(argument.toString());
            }
            content.append(")");
        }

        if(ctx.functionCallStatement() != null) {
            Expression expression = this.visitFunctionCallStatement(ctx.functionCallStatement());
            return new Expression(content.toString() + "." + expression.toString(), expression.getType());
        } else if(ctx.variable() != null) {
            Expression expression = this.visitVariable(ctx.variable());
            return new Expression(content.toString() + "." + expression.toString(), expression.getType());
        } else {
            if(list) {
                Type type = Type.List;
                type.subType = argumentType;

                return new Expression(content.toString(), type);
            } else {
                return new Expression(content.toString(), Type.None); //TODO: Get function return
            }
        }
    }

    @Override
    public Expression visitFunctionCallArgumentList(KotlinParser.FunctionCallArgumentListContext ctx) {
        if(ctx.functionCallArgumentList() != null) {
            Expression list = this.visitFunctionCallArgumentList(ctx.functionCallArgumentList());
            Expression expression = this.visitExpression(ctx.expression());
            Type type = typeInference(list.getType(), expression.getType());

            return new Expression( list.toString() + ", " + expression.toString(), type);
        } else {
            return this.visitExpression(ctx.expression());
        }
    }

    @Override public String visitClassDeclaration(KotlinParser.ClassDeclarationContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitClassDeclarationArgumentList(KotlinParser.ClassDeclarationArgumentListContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitClassInheritance(KotlinParser.ClassInheritanceContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitInterfaceDeclaration(KotlinParser.InterfaceDeclarationContext ctx) { return (String)visitChildren(ctx); }

    @Override
    public String visitStatementBody(KotlinParser.StatementBodyContext ctx) {
        if(ctx.bodyElement() != null) {
            return "\n" + this.visitBodyElement(ctx.bodyElement());
        } else {
            return this.visitCompoundStatement(ctx.compoundStatement());
        }
    }

    @Override
    public String visitCompoundStatement(KotlinParser.CompoundStatementContext ctx) {
        return "{\n" + this.visitBody(ctx.body()) + "\n}";
    }

    @Override
    public String visitReturnStatement(KotlinParser.ReturnStatementContext ctx) {
        if(ctx.expression() != null) {
            return "return " + this.visitExpression(ctx.expression()).toString();
        } else {
            return "return";
        }
    }

    @Override
    public String visitIfStatement(KotlinParser.IfStatementContext ctx) {
        if(ctx.ifExpression() != null) {
            return this.visitIfExpression(ctx.ifExpression()).toString();
        } else {
            return "if(" + this.visitCondition(ctx.condition()) + ")" + this.visitStatementBody(ctx.statementBody());
        }
    }

    @Override
    public Expression visitIfExpression(KotlinParser.IfExpressionContext ctx) { //TODO
        String statementBody1 = this.visitStatementBody(ctx.statementBody(0));
        String statementBody2 = this.visitStatementBody(ctx.statementBody(1));
        Type type = Type.None;

        return new Expression("if(" + this.visitCondition(ctx.condition()) + ")" + statementBody1.toString() + " else " + statementBody2.toString(), type);
    }

    @Override
    public String visitForStatement(KotlinParser.ForStatementContext ctx) {
        if(ctx.range() != null) {
            return "for(" + visitForRange(ctx.range(), this.visitVariable(ctx.variable(0)).toString()) + ")" + this.visitStatementBody(ctx.statementBody());
        } else {
            Expression variable = this.visitVariable(ctx.variable(0));
            Expression variableTarget = this.visitVariable(ctx.variable(1));
            variableTarget.type.subType = Type.None; //TODO
            return "for(" + variableTarget.type.subType.toString() + " " + variable.toString() + ": " + variableTarget.toString() + ")" + this.visitStatementBody(ctx.statementBody());
        }
    }

    @Override
    public String visitWhileStatement(KotlinParser.WhileStatementContext ctx) {
        return "while(" + this.visitCondition(ctx.condition()) + ")" + this.visitStatementBody(ctx.statementBody());
    }

    @Override
    public String visitWhenStatement(KotlinParser.WhenStatementContext ctx) {
        if(ctx.expression() != null) {
            return "switch(" + this.visitExpression(ctx.expression()).toString() + ") {\n" + this.visitWhenList(ctx.whenList()) + "}";
        } else {
            return "switch {\n" + this.visitWhenList(ctx.whenList()) + "\n}";
        }
    }

    @Override
    public String visitWhenList(KotlinParser.WhenListContext ctx) {
        StringBuilder content = new StringBuilder();

        for(int i = 0; i < ctx.getChildCount() / 3; i++) {
            content.append(this.visitWhenCondition(ctx.whenCondition(i)) + ": " + this.visitStatementBody(ctx.statementBody(i)) + "\n");
        }
        return content.toString();
    }

    @Override
    public String visitWhenCondition(KotlinParser.WhenConditionContext ctx) {
        if(ctx.Else() != null) {
            return "default";
        } else if(ctx.Is() != null) {
            return "case " + ""; //TODO
        } else if(ctx.In() != null) {
            return "case " + ""; //TODO
        } else {
            return "case " + this.visitExpression(ctx.expression()).toString();
        }
    }

    @Override
    public String visitType(KotlinParser.TypeContext ctx) {
        if(ctx.Type() != null) {
            if(ctx.getText().equals("Int")) {
                return "int";
            } else if(ctx.getText().equals("Int?")) {
                return "Integer";
            } else if(ctx.getText().equals("String") || ctx.getText().equals("String?")) {
                return "String";
            } else if(ctx.getText().equals("Double") || ctx.getText().equals("Double?")) {
                return "Double";
            } else if(ctx.getText().equals("Any") || ctx.getText().equals("Any?")) {
                return "Object";
            } else {
                return null;
            }
        } else {
            return "List<" + this.visitType(ctx.type()) + ">";
        }
    }

    @Override
    public Expression visitStringLiteral(KotlinParser.StringLiteralContext ctx) {
        return new Expression(visitChildren(ctx, " + ", 1, ctx.getChildCount() - 1), Type.String);
    }

    @Override
    public String visitStringReference(KotlinParser.StringReferenceContext ctx) {
        return ctx.Id().getText();
    }

    @Override
    public String visitStringExpression(KotlinParser.StringExpressionContext ctx) {
        return this.visitExpression(ctx.expression()).toString();
    }

    @Override
    public String visitStringText(KotlinParser.StringTextContext ctx) {
        return "\"" + this.visitChildren(ctx, " ") + "\"";
    }

    @Override
    public String visitRange(KotlinParser.RangeContext ctx) {
        return visitChildren(ctx); //TODO
    }

    public String visitForRange(KotlinParser.RangeContext ctx, String variable) {
        Expression startExpression = this.visitExpressionForRange(ctx.expressionForRange(0));
        Expression endExpression = this.visitExpressionForRange(ctx.expressionForRange(1));
        Expression stepExpression = null;
        Type type;

        StringBuilder content = new StringBuilder();

        if(ctx.Step() != null) {
            stepExpression = this.visitExpressionForRange(ctx.expressionForRange(2));
            type = typeInference(startExpression.getType(), endExpression.getType(), stepExpression.getType());
        } else {
            type = typeInference(startExpression.getType(), endExpression.getType());
        }

        content.append(type.toString() + " " + variable + " = " + startExpression.toString() + "; ");

        if(ctx.To() != null) {
            content.append(variable + " <= " + endExpression.toString() + "; ");
            if(ctx.Step() != null) {
                content.append(variable + " += " + stepExpression.toString());
            } else {
                content.append(variable + " ++");
            }
        } else {
            content.append(variable + " >= " + endExpression.toString() + "; ");
            if(ctx.Step() != null) {
                content.append(variable + " -= " + stepExpression.toString());
            } else {
                content.append(variable + " --");
            }
        }

        return content.toString();
    }

    @Override
    public Expression visitVariable(KotlinParser.VariableContext ctx) {
        StringBuilder content = new StringBuilder();

        content.append(ctx.Id().getText());

        if(ctx.expression() != null) {
            content.append("[" + this.visitExpression(ctx.expression()).toString() + "]");
        }

        if(ctx.functionCallStatement() != null) {
            Expression expression = this.visitFunctionCallStatement(ctx.functionCallStatement());
            return new Expression(content.toString() + "." + expression.toString(), expression.getType());
        } else if(ctx.variable() != null) {
            Expression expression = this.visitVariable(ctx.variable());
            return new Expression(content.toString() + "." + expression.toString(), expression.getType());
        } else {
            return new Expression(content.toString(), Type.None); //TODO: Get variable type from initialization
        }
    }

    @Override
    public String visitAssignStatement(KotlinParser.AssignStatementContext ctx) {
        StringBuilder content = new StringBuilder();
        String mode = ctx.Mode().getText();

        if(mode.equals("val")) {
            content.append("final ");
        }

        if(ctx.type() != null) {
            content.append(this.visitType(ctx.type()) + " " + ctx.Id().getText());

            if(ctx.expression() != null) {
                content.append(" = " + this.visitExpression(ctx.expression()).toString());
            }
        } else {
            if(ctx.expression() != null) {
                Expression expression = this.visitExpression(ctx.expression());
                content.append(expression.getType().toString() + " " + ctx.Id().getText() + " = " + expression.toString());
            }
        }

        return content.toString() + ";";
    }

    @Override
    public Expression visitAssignmentExpression(KotlinParser.AssignmentExpressionContext ctx) {
        Expression variable = this.visitVariable(ctx.variable());
        Expression expression = this.visitExpression(ctx.expression());

        return new Expression(variable.toString() + " = " + expression.toString(), expression.getType());
    }

    @Override
    public String visitCondition(KotlinParser.ConditionContext ctx) {
        if(ctx.logicalOperator() != null) {
            return this.visitCondition(ctx.condition(0)) + " " + this.visitLogicalOperator(ctx.logicalOperator()) + " " + this.visitCondition(ctx.condition(1));
        } else {
            String operand1 = this.visit(ctx.getChild(0)).toString();
            String operand2 = this.visit(ctx.getChild(2)).toString();
            String operator = this.visitConditionOperator(ctx.conditionOperator());

            return operand1 + " " + operator + " " + operand2;
        }
    }

    @Override
    public String visitLogicalOperator(KotlinParser.LogicalOperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitConditionOperator(KotlinParser.ConditionOperatorContext ctx) {
        String operator = ctx.getText();
        if(ctx.Is() != null) {
            return operator.replace("is", "instanceof");
        } else {
            return operator;
        }
    }

    @Override
    public Expression visitExpression(KotlinParser.ExpressionContext ctx) {
        if(ctx.whenStatement() != null) {
            String expression = this.visitWhenStatement(ctx.whenStatement()); //TODO
            return new Expression(expression.toString(), Type.None); //TODO
        } else if(ctx.stringLiteral() != null) {
            Expression expression = this.visitStringLiteral(ctx.stringLiteral());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.assignmentExpression() != null) {
            Expression expression = this.visitAssignmentExpression(ctx.assignmentExpression());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.ifExpression() != null) {
            Expression expression = this.visitIfExpression(ctx.ifExpression());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.compoundStatement() != null) {
            String expression = this.visitCompoundStatement(ctx.compoundStatement()); //TODO
            return new Expression(expression, Type.None); //TODO
        } else if(ctx.expression(0) != null) {
            Expression expression1 = this.visitExpression(ctx.expression(0));
            Expression expression2 = this.visitExpression(ctx.expression(1));
            return new Expression(expression1.toString() + " ?: " + expression2.toString(), Type.None); //TODO
        } else if(ctx.expressionTerm() != null && ctx.getChildCount() == 3) {
            Expression expression = this.visitExpressionTerm(ctx.expressionTerm());
            return new Expression("(" + expression.toString() + ")", expression.getType());
        } else if(ctx.expressionTerm() != null) {
            Expression expression = this.visitExpressionTerm(ctx.expressionTerm());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.functionCallStatement() != null) {
            Expression expression = this.visitFunctionCallStatement(ctx.functionCallStatement());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.condition() != null) {
            String expression = this.visitCondition(ctx.condition());
            return new Expression( expression, Type.Boolean);
        } else if(ctx.range() != null) {
            String expression = this.visitRange(ctx.range());
            return new Expression(expression, Type.None); //TODO
        } else if(ctx.variable() != null) {
            Expression expression = this.visitVariable(ctx.variable());
            return new Expression(expression.toString(), expression.getType());
        } else {
            return new Expression(visitChildren(ctx), Type.None);
        }
    }

    @Override
    public Expression visitExpressionForCondition(KotlinParser.ExpressionForConditionContext ctx) {
        if(ctx.stringLiteral() != null) {
            Expression expression = this.visitStringLiteral(ctx.stringLiteral());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.ifExpression() != null) {
            Expression expression = this.visitIfExpression(ctx.ifExpression());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.expressionTerm() != null && ctx.getChildCount() == 3) {
            Expression expression = this.visitExpressionTerm(ctx.expressionTerm());
            return new Expression("(" + expression.toString() + ")", expression.getType());
        } else if(ctx.expressionTerm() != null) {
            Expression expression = this.visitExpressionTerm(ctx.expressionTerm());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.functionCallStatement() != null) {
            Expression expression = this.visitFunctionCallStatement(ctx.functionCallStatement());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.range() != null) {
            String expression = this.visitRange(ctx.range());
            return new Expression(expression, Type.None); //TODO
        } else if(ctx.variable() != null) {
            Expression expression = this.visitVariable(ctx.variable());
            return new Expression(expression.toString(), expression.getType());
        } else {
            return new Expression(visitChildren(ctx), Type.None);
        }
    }

    @Override
    public Expression visitExpressionForRange(KotlinParser.ExpressionForRangeContext ctx) {
        if(ctx.expressionTerm() != null && ctx.getChildCount() == 3) {
            Expression expression = this.visitExpressionTerm(ctx.expressionTerm());
            return new Expression("(" + expression.toString() + ")", expression.getType());
        } else if(ctx.expressionTerm() != null) {
            Expression expression = this.visitExpressionTerm(ctx.expressionTerm());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.functionCallStatement() != null) {
            Expression expression = this.visitFunctionCallStatement(ctx.functionCallStatement());
            return new Expression(expression.toString(), expression.getType());
        } else if(ctx.variable() != null) {
            Expression expression = this.visitVariable(ctx.variable());
            return new Expression(expression.toString(), expression.getType());
        } else {
            return new Expression(visitChildren(ctx), Type.None);
        }
    }

    @Override
    public Expression visitExpressionTerm(KotlinParser.ExpressionTermContext ctx) {
        if(ctx.expressionFactor() != null) {
            return this.visitExpressionFactor(ctx.expressionFactor());
        } else {
            Expression expressionTerm1 = this.visitExpressionTerm(ctx.expressionTerm(0));
            Expression expressionTerm2 = this.visitExpressionTerm(ctx.expressionTerm(1));

            String content = expressionTerm1.toString() + " " + ctx.getChild(1).getText() + " " + expressionTerm2.toString();
            Type type = typeInference(expressionTerm1.getType(), expressionTerm2.getType());

            return new Expression(content, type);
        }
    }

    @Override
    public Expression visitExpressionFactor(KotlinParser.ExpressionFactorContext ctx) {
        if(ctx.expressionElement() != null) {
            return this.visitExpressionElement(ctx.expressionElement());
        } else {
            Expression expressionFactor1 = this.visitExpressionFactor(ctx.expressionFactor(0));
            Expression expressionFactor2 = this.visitExpressionFactor(ctx.expressionFactor(1));

            String content = expressionFactor1.toString() + " " + ctx.getChild(1).getText() + " " + expressionFactor2.toString();
            Type type = typeInference(expressionFactor1.getType(), expressionFactor2.getType());

            return new Expression(content, type);
        }
    }

    @Override
    public Expression visitExpressionElement(KotlinParser.ExpressionElementContext ctx) {
        String sign = "";

        if(ctx.getChild(0).getText().equals("+") || ctx.getChild(0).getText().equals("-")) {
            sign = ctx.getChild(0).getText();
        }

        if(ctx.functionCallStatement() != null) {
            Expression expression = this.visitFunctionCallStatement(ctx.functionCallStatement());
            return new Expression(sign + expression.toString(), expression.getType());
        } else if(ctx.unsignedNumericLiteral() != null) {
            Expression expression = this.visitUnsignedNumericLiteral(ctx.unsignedNumericLiteral());
            return new Expression(sign + expression.toString(), expression.getType());
        } else if(ctx.expression() != null) {
            Expression expression = this.visitExpression(ctx.expression());
            return new Expression(sign + "(" + expression.toString() + ")", expression.getType());
        } else {
            Expression expression = this.visitVariable(ctx.variable());

            if(ctx.getChildCount() == 3) {
                if(ctx.getChild(2).getText().equals("++") || ctx.getChild(2).getText().equals("--")) {
                    return new Expression(sign + expression.toString() + ctx.getChild(2).getText(), expression.getType());
                } else {
                    return new Expression(sign + ctx.getChild(1).getText() + expression.toString(), expression.getType());
                }
            } else if(ctx.getChildCount() == 2) {
                if(ctx.getChild(1).getText().equals("++") || ctx.getChild(1).getText().equals("--")) {
                    return new Expression(expression.toString() + ctx.getChild(1).getText(), expression.getType());
                } else if(ctx.getChild(0).getText().equals("++") || ctx.getChild(0).getText().equals("--"))  {
                    return new Expression(ctx.getChild(0).getText() + expression.toString(), expression.getType());
                } else {
                    return new Expression(sign + expression.toString(), expression.getType());
                }
            } else {
                return new Expression(expression.toString(), expression.getType());
            }
        }
    }

    @Override
    public Expression visitUnsignedNumericLiteral(KotlinParser.UnsignedNumericLiteralContext ctx) {
        if(ctx.UnsignedDecimalLiteral() != null) {
            if(ctx.getChildCount() == 2) {
                return new Expression(ctx.getText(), Type.Long);
            } else {
                return new Expression(ctx.getText(), Type.Int);
            }
        } else {
            return new Expression(ctx.getText(), Type.Double);
        }
    }
}