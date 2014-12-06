package com.android.ast.restable

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * AST transformation for injecting json methods
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@CompileStatic
class ToJsonTransformation extends AbstractASTTransformation {

    private final static ClassNode toJSONAnnotation = ClassHelper.make(ToJson)
    private final static ClassNode closureNode = ClassHelper.make(Closure, false)
    private final static ClassNode mapNode = ClassHelper.make(Map, false)
    private final static ClassNode hashMap = ClassHelper.make(HashMap, false)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (astNodes[1] instanceof ClassNode) {
            visitClass((ClassNode) astNodes[1])
        }
    }

    private void visitClass(ClassNode classNode) {
        classNode.getAnnotations(toJSONAnnotation).each {
            createJsonMethod(classNode, it)
        }
    }

    private void createJsonMethod(ClassNode classNode, AnnotationNode annotationNode) {
        String name = (getExpressionFromAnnotation(annotationNode, 'value', null) as ConstantExpression)?.text
        name = name ?: 'default'
        ListExpression includes = getExpressionFromAnnotation(annotationNode, 'includes', null) as ListExpression
        ListExpression excludes = getExpressionFromAnnotation(annotationNode, 'excludes', null) as ListExpression
        ListExpression rename = getExpressionFromAnnotation(annotationNode, 'rename', null) as ListExpression
        def methodName = "as${name.capitalize()}"
        println "adding $methodName"
        createAsJsonMethod(classNode, methodName, includes)
        createFromJsonMethod(classNode, excludes)
    }

    def createAsJsonMethod(ClassNode node, String name, ListExpression includes) {
        def methodNode = new MethodNode(name, ACC_PUBLIC, hashMap.plainNodeReference, [new Parameter(closureNode.plainNodeReference, 'closure')] as Parameter[], null, new BlockStatement())
        processInclude(methodNode, includes)
        node.addMethod(methodNode)
        def callMethod = new ExpressionStatement(new MethodCallExpression(new VariableExpression('this'), name, ConstantExpression.NULL))
        def emptyMethodNode = new MethodNode(name, ACC_PUBLIC, hashMap.plainNodeReference, [] as Parameter[], null, callMethod)
        node.addMethod(emptyMethodNode)
    }

    def createFromJsonMethod(ClassNode node, ListExpression excludes) {
        def methodNode = new MethodNode('fromJSON', ACC_PUBLIC | ACC_STATIC, node.plainNodeReference, [new Parameter(mapNode.plainNodeReference, 'json'), new Parameter(closureNode.plainNodeReference, 'closure')] as Parameter[], null, new BlockStatement())
        processExcludes(node, methodNode, excludes)
        node.addMethod(methodNode)
        def emptyMethodNode = new MethodNode('fromJSON', ACC_PUBLIC | ACC_STATIC, node.plainNodeReference, [new Parameter(mapNode.plainNodeReference, 'json')] as Parameter[], null, new BlockStatement())
        def callMethod = new ExpressionStatement(new StaticMethodCallExpression(node, 'fromJSON', new ArgumentListExpression([new VariableExpression(emptyMethodNode.parameters[0]), ConstantExpression.NULL])))
        emptyMethodNode.code = callMethod
        node.addMethod(emptyMethodNode)
    }

    def processInclude(MethodNode methodNode, ListExpression includes) {
        def blockStatement = (BlockStatement) methodNode.code
        blockStatement = blockStatement ?: new BlockStatement()
        def mapExpression = new VariableExpression("map", hashMap.plainNodeReference)
        def constructorCallExpression = new ConstructorCallExpression(hashMap.plainNodeReference, new TupleExpression())
        def declaration = new DeclarationExpression(mapExpression, Token.newSymbol(Types.EQUAL, 0, 0), constructorCallExpression)
        blockStatement.addStatement(new ExpressionStatement(declaration))
        List<Statement> statements = new ArrayList<>()
        includes.expressions.each {
            String property = (it as ConstantExpression).getValue() as String
            def getterMethodCall = new MethodCallExpression(new VariableExpression('this'), "get${property.capitalize()}", new TupleExpression())
            getterMethodCall.implicitThis = true
            def putExpression = new ArgumentListExpression([it, getterMethodCall])
            statements.add(new ExpressionStatement(new MethodCallExpression(mapExpression, 'put', putExpression)))
        }
        def checkExpression = new BinaryExpression(new VariableExpression('closure'), Token.newSymbol('!=', 0, 0), ConstantExpression.NULL)
        def closureCallExpression = new ExpressionStatement(new MethodCallExpression(new VariableExpression(methodNode.getParameters()[0]), 'call', new ArgumentListExpression(new VariableExpression('this'), mapExpression)))
        def ifStatement = new IfStatement(new BooleanExpression(checkExpression), closureCallExpression, new BlockStatement())
        statements.add(ifStatement)
        statements.add(new ReturnStatement(new VariableExpression('map')))
        blockStatement.addStatements(statements)
        methodNode.code = blockStatement
    }

    def processExcludes(ClassNode node, MethodNode methodNode, ListExpression excludes) {
        def blockStatement = (BlockStatement) methodNode.code
        blockStatement = blockStatement ?: new BlockStatement()
        def entityVar = new VariableExpression("entity", node)
        def jsonVar = new VariableExpression(methodNode.parameters[0])
        def constructorCallExpression = new ConstructorCallExpression(node, new TupleExpression())
        def declaration = new DeclarationExpression(entityVar, Token.newSymbol(Types.EQUAL, 0, 0), constructorCallExpression)
        blockStatement.addStatement(new ExpressionStatement(declaration))
        List<Statement> statements = new ArrayList<>()
        node.plainNodeReference.fields.each { field ->
            def mapGetterMethodNode = jsonVar.originType.plainNodeReference.methods.find { it.name == 'get' }
            def mapGetter = new MethodCallExpression(jsonVar, 'get', new ConstantExpression(field.name))
            mapGetter.setMethodTarget(mapGetterMethodNode)
            def methodCall = new MethodCallExpression(entityVar, "set${field.name.capitalize()}", mapGetter)
            statements.add(new ExpressionStatement(methodCall))
        }
        if (excludes) {
            excludes.expressions.each {
                String property = (it as ConstantExpression).getValue() as String
                def setNullMethodCall = new MethodCallExpression(new VariableExpression('entity'), "set${property.capitalize()}", ConstantExpression.NULL)
                statements.add(new ExpressionStatement(setNullMethodCall))
            }
        }
        def checkExpression = new BinaryExpression(new VariableExpression('closure'), Token.newSymbol('!=', 0, 0), ConstantExpression.NULL)
        def closureCallExpression = new ExpressionStatement(new MethodCallExpression(new VariableExpression(methodNode.getParameters()[1]), 'call', new ArgumentListExpression(entityVar, jsonVar)))
        def ifStatement = new IfStatement(new BooleanExpression(checkExpression), closureCallExpression, new BlockStatement())
        statements.add(ifStatement)
        statements.add(new ReturnStatement(entityVar))
        blockStatement.addStatements(statements)
        methodNode.code = blockStatement
    }

    def getExpressionFromAnnotation(AnnotationNode node, String property, def defaultValue) {
        def expression = node.members[property]
        expression = expression ?: defaultValue
        expression
    }
}