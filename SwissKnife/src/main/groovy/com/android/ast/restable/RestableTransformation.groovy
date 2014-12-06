package com.android.ast.restable
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class RestableTransformation extends AbstractASTTransformation {
    public static ClassNode linkedHashMap = ClassHelper.make(LinkedHashMap)
    public static ClassNode booleanNode = ClassHelper.make(boolean)
    public static ClassNode validator = ClassHelper.make(RestableValidation)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (astNodes[1] instanceof ClassNode) {
            visitClass((ClassNode) astNodes[1])
        }
    }

    private void visitClass(ClassNode node) {
        def constraints = node.fields.find { it.name == 'constraints' }
        if (constraints) {
            def closureExpression = constraints.getInitialExpression() as ClosureExpression
            def blockStatement = closureExpression.code as BlockStatement
            def fieldNode = new FieldNode('errors', ACC_PUBLIC, linkedHashMap.plainNodeReference, node, new ConstructorCallExpression(linkedHashMap, new TupleExpression()))
            node.addField(fieldNode)
            createValidateMethod(node, blockStatement.statements)
            blockStatement.statements.clear()
            node.fields.remove(constraints)
        }
    }

    private void createValidateMethod(ClassNode node, List<Statement> closureStatements) {
        def validateMethod = new MethodNode('validate', ACC_PUBLIC, booleanNode, [] as Parameter[], null, new BlockStatement())
        def code = validateMethod.code as BlockStatement
        def errors = new VariableExpression(node.getField('errors'))
        code.addStatement(new ExpressionStatement(new MethodCallExpression(errors, 'clear', new TupleExpression())))
        closureStatements.each {
            code.addStatement transformClosureExpression(node, it as ExpressionStatement)
        }
        def errorSizeExpression = new MethodCallExpression(errors, 'size', new TupleExpression())
        def checkExpression = new BinaryExpression(errorSizeExpression, Token.newSymbol('==', 0, 0), new ConstantExpression(0))
        code.addStatement(new ReturnStatement(new BooleanExpression(checkExpression)))
        node.addMethod(validateMethod)
    }

    BlockStatement transformClosureExpression(ClassNode node, ExpressionStatement statement) {
        def blockStatement = new BlockStatement()
        def methodCall = statement.expression as MethodCallExpression
        def errors = new VariableExpression(node.getField('errors'))
        def propertyName = (methodCall.method as ConstantExpression).getValue() as String
        def property = new VariableExpression(node.getField(propertyName))
        def expression = (methodCall.arguments as TupleExpression).expressions[0] as NamedArgumentListExpression
        expression.getMapEntryExpressions().each {
            createValidatorMethodCall(it.keyExpression.text, propertyName, property, it.valueExpression, blockStatement, errors)
        }
        return blockStatement
    }

    def createValidatorMethodCall(String method, String propertyName, Expression property, Expression validationExpression, BlockStatement code, VariableExpression errors) {
        def methodCall = new StaticMethodCallExpression(validator, method, new ArgumentListExpression([property, validationExpression, new ConstantExpression(propertyName), errors]))
        code.addStatement(new ExpressionStatement(methodCall))
    }
}
