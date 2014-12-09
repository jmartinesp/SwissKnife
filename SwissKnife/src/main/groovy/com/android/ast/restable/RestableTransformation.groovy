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
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RestableTransformation extends AbstractASTTransformation {
    public static ClassNode linkedHashMap = ClassHelper.make(LinkedHashMap)
    public static ClassNode map = ClassHelper.make(Map)
    public static ClassNode booleanNode = ClassHelper.make(boolean)
    public static ClassNode validator = ClassHelper.make(RestableValidation)
    public static ClassNode closure = ClassHelper.make(Closure)

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
        def jsons = node.fields.find { it.name == 'toJSON' }
        if (jsons) {
            def closureExpression = jsons.getInitialExpression() as ClosureExpression
            def blockStatement = closureExpression.code as BlockStatement
            createToJsonsMethods(node, blockStatement.statements)
            blockStatement.statements.clear()
            node.fields.remove(jsons)
        }
        cleanNode(node)
    }

    private void cleanNode(ClassNode classNode) {
        classNode.methods.remove(classNode.methods.find { it.name.endsWith('Constraints') })
        classNode.methods.remove(classNode.methods.find { it.name.endsWith('toJSON') })
    }

    private void createToJsonsMethods(ClassNode node, List<Statement> closureStatements) {
        println 'Entering closure'
        closureStatements.each {
            transformJsonsClosureExpression(node, it as ExpressionStatement)
        }
    }

    private void transformJsonsClosureExpression(ClassNode node, ExpressionStatement expressionStatement) {
        try {
            println 'Processing expression ' + expressionStatement
            def methodCallExpression = expressionStatement.expression as MethodCallExpression
            //def methodNode = new MethodNode((methodCallExpression.method as ConstantExpression).getValue() as String, ACC_PUBLIC, linkedHashMap.plainNodeReference, [new Parameter(closure.plainNodeReference, 'closure')] as Parameter[], null, new BlockStatement())
            //def code = methodNode.code as BlockStatement
            // node.addMethod(methodNode)
            def emptyMethodNode = new MethodNode((methodCallExpression.method as ConstantExpression).getValue() as String, ACC_PUBLIC, ClassHelper.make(Map).plainNodeReference, [] as Parameter[], null, new BlockStatement())
            def code = emptyMethodNode.code as BlockStatement
            node.addMethod(emptyMethodNode)
            if (!code) {
                code = new BlockStatement()
            }
            def args = methodCallExpression.arguments as ArgumentListExpression
            args.expressions.each {
                if (it instanceof ClosureExpression) {
                    code.addStatement(it.code)
                    /*List<Statement> statements = []
                    (it.code as BlockStatement).statements.each {
                        code.addStatement(it)
                        def expression = (it as ExpressionStatement).expression
                        if (expression instanceof BinaryExpression) {
                            println 'Binary expression found: ' + it
                        } else if (expression instanceof MapExpression) {
                            transformMapExpression(expression as MapExpression, statements)
                            println 'Map expression found: ' + it
                        } else {
                            println 'Another expression found: ' + it
                        }
                    }*/
                }
            }
            // methodNode.code = code
            emptyMethodNode.code = code
        } catch (Exception e) {
            addError(e.message, expressionStatement)
            println e.message
        }
    }

    private void transformMapExpression(MapExpression expression, List<Statement> statements) {
        // nothing here yet
    }

    private void transformBinaryExpression(BinaryExpression expression, List<Statement> statements) {
        // nothing here yet
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

    def stringFromConstantExpression(ConstantExpression expression) {
        expression.getValue() as String
    }
}
