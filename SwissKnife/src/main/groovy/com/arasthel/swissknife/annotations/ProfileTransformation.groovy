package com.arasthel.swissknife.annotations

import android.util.Log
import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * This transformation handles {@link Profile}
 *
 * @author Eugene Kamenev @eugenekamenev
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
class ProfileTransformation extends AbstractASTTransformation implements Opcodes {
    /**
     * Used classes
     */
    static ClassNode logNode = ClassHelper.make(Log).plainNodeReference
    static ClassNode systemNode = ClassHelper.make(System).plainNodeReference
    static ClassNode longNode = ClassHelper.make(long).plainNodeReference
    static ClassNode gStringNode = ClassHelper.make(GString).plainNodeReference
    static ClassNode voidNode = ClassHelper.make(void)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        def annotatedMethod = (MethodNode) astNodes[1]
        def annotation = (AnnotationNode) astNodes[0]
        def declaringClass = annotatedMethod.declaringClass
        def originalCode = (BlockStatement) annotatedMethod.getCode()
        def logTag = annotationValue(annotation.members.tag, 'PROFILE') as String
        int logLevel = annotationValue(annotation.members.level, 3) as int
        def excludes = annotationValue(annotation.members.excludes, []) as List<String>
        def includes = annotationValue(annotation.members.includes, []) as List<String>
        def profileTime = annotationValue(annotation.members.time, true) as Boolean
        def logValues = annotationValue(annotation.members.values, true) as Boolean
        def logResult = annotationValue(annotation.members.logResult, true) as Boolean
        def parameters = annotatedMethod.parameters
        addInstructions(originalCode, annotatedMethod, parameters, logTag, logLevel, excludes, includes, profileTime, logValues, logResult)
    }

    /**
     * Get annotation value or default value
     * @param constant
     * @param defaultValue
     * @return
     */
    private static def annotationValue(constant, defaultValue) {
        if (!constant) {
            return defaultValue
        }
        if (constant instanceof ArrayExpression) {
            return constant?.expressions?.collect { (it as ConstantExpression).value }
        }
        if (constant instanceof ConstantExpression) {
            return constant?.value
        }
    }
    /**
     * @todo I am not sure this will work fine for methods with try catch blocks
     *
     * @param code
     * @param methodNode
     * @param params
     * @param logTag
     * @param logLevel
     * @param excludes
     * @param includes
     * @param profileTime
     * @param logValues
     * @param logResult
     */
    void addInstructions(BlockStatement code, MethodNode methodNode, Parameter[] params,
                         String logTag, int logLevel, List excludes, List includes, boolean profileTime,
                         boolean logValues, boolean logResult) {
        // create new block statement
        def newBlockStatement = block()
        Statement returnStatement = null
        // generate profile statements
        def startStatements = createTimeProfileStatements(Log.DEBUG, "${methodNode.declaringClass.nameWithoutPackage}.${methodNode.name}", logTag)
        // log method params
        if (logValues && params.length > 0) {
            newBlockStatement.addStatement(createProfileMethodParams(includes, excludes, logTag, logLevel, params, methodNode))
        }
        // add profile start statement
        if (profileTime) {
            newBlockStatement.addStatement(startStatements.start)
        }
        // check if method has return type
        if (!methodNode.voidMethod) {
            if (logResult) {
                (methodNode.code as BlockStatement).statements.add(createReturnProfile(logTag, logLevel, methodNode))
            }
            // should be
            // returnStatement = code.statements.find { it instanceof ReturnStatement }
            // but cant workaround for now
            returnStatement = code.statements.last()
            code.statements.remove(returnStatement)
        }
        // add all method statements
        newBlockStatement.addStatements(code.statements)
        // add profile end statements
        if (profileTime) {
            newBlockStatement.addStatement(startStatements.end)
        }
        // if method has return type, inject return statement last
        if (!methodNode.voidMethod) {
            newBlockStatement.addStatement(returnStatement)
        }
        // set new code block
        methodNode.code = newBlockStatement
    }

    /**
     * @param logLevel @see {@link Log#DEBUG}
     * @param logTag @see {@link Log#i(java.lang.String, java.lang.String)}
     * @return
     */
    static Map<String, Statement> createTimeProfileStatements(int logLevel, String methodName, String logTag) {
        if (!logTag) {
            logTag = 'PROFILE'
        }
        def startTimeVarName = random()
        def totalTimeVarName = random()
        def logMessageVarName = random()
        def timeStartVar = varX(startTimeVarName, longNode)
        def startStatement = declS(timeStartVar, callX(classX(systemNode), 'currentTimeMillis'))
        def totalTime = varX(totalTimeVarName, longNode)
        def endTime = declS(totalTime, new BinaryExpression(callX(classX(systemNode), 'currentTimeMillis'), Token.newSymbol(Types.MINUS, -1, -1), timeStartVar))
        def messageVar = varX(logMessageVarName, gStringNode)
        def messageConstants = [constX('⇛⇛⇛ Method call ' + methodName + ' completed in '),
                                constX(''), constX(' ms')] as List<ConstantExpression>
        def logMessage = declS(varX(messageVar), new GStringExpression('', messageConstants, [totalTime] as List<Expression>))
        def endStatement = stmt(callX(classX(logNode), logMethodName(logLevel), args(constX(logTag), callX(varX(messageVar), 'toString'))))
        [start: startStatement, end: block(endTime, logMessage, endStatement)]
    }
    /**
     * Create profile statements for method parameters
     * @param includes
     * @param excludes
     * @param tag
     * @param logLevel
     * @param params
     * @param method
     * @return
     */
    Statement createProfileMethodParams(List<String> includes, List<String> excludes, String tag, int logLevel, Parameter[] params, MethodNode method) {
        // if nothing defined return empty block
        if (!params) {
            return block()
        }
        def logMessageVarName = random()
        def messageVar = varX(logMessageVarName, gStringNode)
        // collect parameters names
        def parametersNames = params.findAll { Parameter p -> !(p.name in excludes) }.collect {
            it.name
        }
        if (!includes) {
            includes = parametersNames
        }
        List<ConstantExpression> emptyValues = []
        List<Expression> values = []
        for (param in includes) {
            if (param in parametersNames) {
                def p = params.find { it.name == param }
                emptyValues << constX('')
                emptyValues << constX('')
                emptyValues << constX('')
                values << constX(p.name + '=')
                values << varX(p)
                values << constX(',')
            }
        }
        def constants = [constX('⇛⇛⇛ Method ' + method.name + ' params: ')] + emptyValues
        def logMessage = declS(varX(messageVar), new GStringExpression('', constants, values))
        return block(logMessage, stmt(callX(classX(logNode), logMethodName(logLevel), args(constX(tag), callX(messageVar, 'toString')))))
    }

    /**
     * Create profile return statement
     * @param tag
     * @param logLevel
     * @param method
     * @return
     */
    Statement createReturnProfile(String tag, int logLevel, MethodNode method) {
        def returnStmt = (method.code as BlockStatement).getStatements().last()
        (method.code as BlockStatement).statements.remove(returnStmt)
        def returnVarName = random()
        def logMessageVarName = random()
        def returnVar = varX(returnVarName, method.returnType)
        Expression expression
        if (returnStmt instanceof ReturnStatement) {
            expression = returnStmt.getExpression()
        }
        if (returnStmt instanceof ExpressionStatement) {
            expression = returnStmt.getExpression()
        }
        assert expression != null
        def returnVarDecl = declS(varX(returnVar), expression)
        def messageVar = varX(logMessageVarName, gStringNode)
        def logMessage = declS(varX(messageVar), new GStringExpression('',[constX(''), constX('')] as List<ConstantExpression>,[constX('⇛⇛⇛ Method ' + method.name + ' returned: '), varX(returnVar)] as List<Expression>))
        def logStatement = stmt(callX(classX(logNode), logMethodName(logLevel), args(constX(tag), callX(varX(messageVar), 'toString'))))
        block(returnVarDecl, logMessage, logStatement, returnS(castX(method.returnType, varX(returnVar))))
    }

    /**
     * Get a method name to call Log instance
     * @param logLevel
     * @return
     */
    private static String logMethodName(int logLevel) {
        String logLevelMethod
        switch (logLevel) {
            case Log.DEBUG: logLevelMethod = 'd'; break;
            case Log.ERROR: logLevelMethod = 'e'; break;
            case Log.WARN: logLevelMethod = 'w'; break;
            default: logLevelMethod = 'i'; break;
        }
        logLevelMethod
    }

    public static String random() {
        String r = 's' + new Random().nextInt(1000000)+1
        println "generated name $r"
        return r
    }
}
