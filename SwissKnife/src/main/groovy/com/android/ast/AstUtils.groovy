package com.android.ast

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

@CompileStatic
class AstUtils {
    /**
     * Find variable declaration position in block statment
     * @param blockStatement Block statment
     * @param variableName Variable name
     * @return Declaration position or -1 if not found
     */
    static int findVariableDeclarationPosition(BlockStatement blockStatement, String variableName) {
        int position = -1
        int index = 0
        for (Statement statement : blockStatement.statements) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = statement as ExpressionStatement
                if (expressionStatement.expression instanceof DeclarationExpression) {
                    DeclarationExpression declarationExpression = expressionStatement.expression as DeclarationExpression
                    if (declarationExpression.leftExpression instanceof VariableExpression) {
                        VariableExpression variableExpression = declarationExpression.leftExpression as VariableExpression
                        if (variableExpression.text == variableName) {
                            position = index
                            break
                        }
                    }
                }
            }
            index++
        }
        position
    }

    /** Look for method with a given object and name within a block statement.
     * @param blockStatement Block statement
     * @param object Method call object
     * @param methodName Method name
     * @return Found method position in block, -1 if not found
     */
    static int findMethodCallPosition(BlockStatement blockStatement, String object, String methodName) {
        int position = -1
        int index = 0
        for (Statement statement : blockStatement.statements) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = statement as ExpressionStatement
                if (expressionStatement.expression instanceof MethodCallExpression) {
                    MethodCallExpression methodCallExpression = expressionStatement.expression as MethodCallExpression
                    if (methodCallExpression.objectExpression.text == object && methodCallExpression.methodAsString == methodName) {
                        position = index
                        break
                    }
                }
            }
            index++
        }
        position
    }

    /**
     * Look for return statement position in a block statment
     * @param blockStatement Block statement
     * @return Found method position in block, -1 if not found
     */
    static int findReturnPosition(BlockStatement blockStatement) {
        int position = -1
        int index = 0
        for (Statement statement : blockStatement.statements) {
            if (statement instanceof ReturnStatement) {
                position = index
                break
            }
            index++
        }
        position
    }

    /**
     * Build a filed assignment expression.
     * @param fieldNode Field to assign
     * @param valueExpression Value to assign
     * @return An Expression
     */
    static Expression buildFieldAssignmentExpression(FieldNode fieldNode, Expression valueExpression) {
        new BinaryExpression(new VariableExpression(fieldNode),
                Token.newSymbol(Types.EQUAL, 0, 0),
                valueExpression)
    }

    /**
     * Check if a ClassNode inherits from a given class.
     * @param classNode ClassNode to test
     * @param clazz Ancestor class
     * @return true/false
     */
    static boolean inheritsFromClass(ClassNode classNode, Class clazz) {
        if(classNode == ClassHelper.makeWithoutCaching(clazz)) {
            return true
        }
        ClassNode parent = classNode.unresolvedSuperClass
        if (parent == null) {
            return false
        }
        while (parent.unresolvedSuperClass) {
            if (parent.text == clazz.name) {
                return true
            }
            parent = parent.unresolvedSuperClass
        }
        false
    }
}