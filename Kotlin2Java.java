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

class KotlinWalker extends KotlinBaseVisitor {
    @Override public String visitProg(KotlinParser.ProgContext ctx) { return (String)visitChildren(ctx); }

    @Override
    public String visitPackageDeclaration(KotlinParser.PackageDeclarationContext ctx) {
        return "package " + this.visitPackageName(ctx.packageName()) + ";\n";
    }

    @Override
    public String visitPackageName(KotlinParser.PackageNameContext ctx) {
        return ctx.getText();
    }

    @Override public String visitPackageSubName(KotlinParser.PackageSubNameContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitImportList(KotlinParser.ImportListContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitImportDeclaration(KotlinParser.ImportDeclarationContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitImportName(KotlinParser.ImportNameContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitImportSubName(KotlinParser.ImportSubNameContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitTopLevelBody(KotlinParser.TopLevelBodyContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitTopLevelBodyElement(KotlinParser.TopLevelBodyElementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitBody(KotlinParser.BodyContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitBodyElement(KotlinParser.BodyElementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitFunctionDeclaration(KotlinParser.FunctionDeclarationContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitFunctionDeclarationParameterList(KotlinParser.FunctionDeclarationParameterListContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitFunctionCallStatement(KotlinParser.FunctionCallStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitFunctionCallArgumentList(KotlinParser.FunctionCallArgumentListContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitClassDeclaration(KotlinParser.ClassDeclarationContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitClassDeclarationArgumentList(KotlinParser.ClassDeclarationArgumentListContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitClassInheritance(KotlinParser.ClassInheritanceContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitInterfaceDeclaration(KotlinParser.InterfaceDeclarationContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitStatementBody(KotlinParser.StatementBodyContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitCompoundStatement(KotlinParser.CompoundStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitReturnStatement(KotlinParser.ReturnStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitIfStatement(KotlinParser.IfStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitIfExpression(KotlinParser.IfExpressionContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitForStatement(KotlinParser.ForStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitWhileStatement(KotlinParser.WhileStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitWhenStatement(KotlinParser.WhenStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitWhenList(KotlinParser.WhenListContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitWhenCondition(KotlinParser.WhenConditionContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitType(KotlinParser.TypeContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitStringLiteral(KotlinParser.StringLiteralContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitStringReference(KotlinParser.StringReferenceContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitStringExpression(KotlinParser.StringExpressionContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitStringText(KotlinParser.StringTextContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitRange(KotlinParser.RangeContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitVariable(KotlinParser.VariableContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitAssignStatement(KotlinParser.AssignStatementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitAssignmentExpression(KotlinParser.AssignmentExpressionContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitCondition(KotlinParser.ConditionContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitLogicalOperator(KotlinParser.LogicalOperatorContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitConditionOperator(KotlinParser.ConditionOperatorContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitExpression(KotlinParser.ExpressionContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitExpressionForCondition(KotlinParser.ExpressionForConditionContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitExpressionForRange(KotlinParser.ExpressionForRangeContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitExpressionTerm(KotlinParser.ExpressionTermContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitExpressionFactor(KotlinParser.ExpressionFactorContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitExpressionElement(KotlinParser.ExpressionElementContext ctx) { return (String)visitChildren(ctx); }

    @Override public String visitUnsignedNumericLiteral(KotlinParser.UnsignedNumericLiteralContext ctx) { return (String)visitChildren(ctx); }
}