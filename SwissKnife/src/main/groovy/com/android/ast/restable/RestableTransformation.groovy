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
            removeProperty(node, constraints.name)
        }
        def jsons = node.fields.find { it.name == 'toJSON' }
        if (jsons) {
            def closureExpression = jsons.initialExpression as ClosureExpression
            def blockStatement = closureExpression.code as BlockStatement
            createToJsonsMethods(node, blockStatement.statements)
            blockStatement.statements.clear()
            removeProperty(node, jsons.name)
        }
        def fromJsons = node.fields.find { it.name == 'fromJSON' }
        if (fromJsons) {
            def closureExpression = fromJsons.initialExpression as ClosureExpression
            def blockStatement = closureExpression.code as BlockStatement
            createFromJsonMethods(node, blockStatement.statements)
            blockStatement.statements.clear()
            removeProperty(node, fromJsons.name)
        }

    }

    private void createFromJsonMethods(ClassNode node, List<Statement> closureStatements) {
        closureStatements.each {
            transformFromJsonClosureExpression(node, it as ExpressionStatement)
        }
    }

    private void createToJsonsMethods(ClassNode node, List<Statement> closureStatements) {
        closureStatements.each {
            transformJsonsClosureExpression(node, it as ExpressionStatement)
        }
    }

    private void transformFromJsonClosureExpression(ClassNode node, ExpressionStatement statement) {
        try {
            def methodCallExpression = statement.expression as MethodCallExpression
            def emptyMethodNode = new MethodNode((methodCallExpression.method as ConstantExpression).getValue() as String, ACC_PUBLIC | ACC_STATIC, node.plainNodeReference, [new Parameter(map.plainNodeReference, 'map')] as Parameter[], null, new BlockStatement())
            def code = emptyMethodNode.code as BlockStatement
            node.addMethod(emptyMethodNode)
            if (!code) {
                code = new BlockStatement()
            }
            def args = methodCallExpression.arguments as ArgumentListExpression
            args.expressions.each {
                if (it instanceof ClosureExpression) {
                    code.addStatement(it.code)
                }
            }
            emptyMethodNode.code = code
        } catch (Exception e) {
            addError(e.message, statement)
            println e.message
        }
    }

    private void transformJsonsClosureExpression(ClassNode node, ExpressionStatement expressionStatement) {
        try {
            def methodCallExpression = expressionStatement.expression as MethodCallExpression
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
                }
            }
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

    private removeProperty(ClassNode classNode, String propertyName) {
        for (int i = 0; i < classNode.fields.size(); i++) {
            if (classNode.fields[i].name == propertyName) {
                classNode.fields.remove(i)
                break
            }
        }
        for (int i = 0; i < classNode.properties.size(); i++) {
            if (classNode.properties[i].name == propertyName) {
                classNode.properties.remove(i)
                break
            }
        }
    }
}
