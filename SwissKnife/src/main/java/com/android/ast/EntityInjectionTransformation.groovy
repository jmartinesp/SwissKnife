package com.android.ast

import android.view.View
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.Verifier
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class EntityInjectionTransformation extends AbstractASTTransformation {
    private static final ClassNode INJECT_ENTITY = ClassHelper.make(InjectEntity)
    private static final ClassNode VIEW_CLASS = ClassHelper.make(View)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (astNodes[1] instanceof ClassNode) {
            visitClass((ClassNode) astNodes[1])
        }
    }

    private void visitClass(ClassNode classNode) {
        ClassExpression clazz = (ClassExpression) classNode.getAnnotations(INJECT_ENTITY)[0].getMember('value')
        ConstantExpression fieldName = (ConstantExpression) classNode.getAnnotations(INJECT_ENTITY)[0].getMember('fieldName')
        ClassNode entityClass = clazz.type
        String entityFieldName = (String) fieldName.value
        if (!entityFieldName) {
            entityFieldName = clazz.toString().toLowerCase()
        }
        FieldNode entityFieldNode = new FieldNode(entityFieldName, ACC_PRIVATE, entityClass, classNode, ConstantExpression.NULL)
        classNode.addField(entityFieldNode)
        List<Map<String, FieldNode>> injectionMap = (List<Map<String, FieldNode>>) classNode.fields.collect { field ->
            println "field detected: $field.name"
            def fieldNameList = entityClass.fields*.name
            println "$fieldNameList"
            if (field.name in fieldNameList) {
                return [(field.name): field]
            }
            return null
        }.findAll { it != null }
        MethodNode method = classNode.methods.find { it.name == 'onCreate' }
        injectionMap.each {
            List<Statement> statement = injectEntityFields(it, entityFieldNode)
            if (method) {
                println 'injecting methods'
                (method.code as BlockStatement).addStatements(statement)
            }

        }

    }


    private List<Statement> injectEntityFields(Map<String, FieldNode> fields, FieldNode entityField) {
        List<ExpressionStatement> statements = new ArrayList()
        fields.each { k, v ->
            def setterMethod = findSetValueMethod(v)
            if (setterMethod) {
                def getterName = "get" + Verifier.capitalize(k)
                def getterExpression = new MethodCallExpression(new VariableExpression(entityField), getterName, MethodCallExpression.NO_ARGUMENTS)
                def expr = new MethodCallExpression(new VariableExpression(v), setterMethod?.name, getterExpression)
                expr.implicitThis = true
                expr.sourcePosition = v
                statements.add(new ExpressionStatement(expr))
            }
        }
        statements
    }

    private MethodNode findSetValueMethod(FieldNode node) {
        if (AstUtils.inheritsFromClass(node.originType, View)) {
            return node.type.methods.find { it.name == 'setText' }
        }
        return null
    }

}
