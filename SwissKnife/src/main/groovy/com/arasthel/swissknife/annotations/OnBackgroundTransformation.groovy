package com.arasthel.swissknife.annotations

import com.arasthel.swissknife.SwissKnife
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * Created by Arasthel on 16/08/14.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class OnBackgroundTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        MethodNode annotatedMethod = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedMethod.declaringClass;

        BlockStatement originalCode = annotatedMethod.getCode();
        declaringClass.addMethod(createNewMethod(annotatedMethod, originalCode));

        createRunnable(annotatedMethod);
    }

    private MethodNode createNewMethod(MethodNode annotatedMethod, BlockStatement originalCode) {
        return new MethodNode(
                annotatedMethod.name + "\$background",
                annotatedMethod.getModifiers(),
                ClassHelper.VOID_TYPE,
                annotatedMethod.parameters,
                null,
                originalCode);
    }

    private void createRunnable(MethodNode annotatedMethod) {
        ArgumentListExpression argumentListExpression = args(varX("this"),
                constX(annotatedMethod.name+"\$background"))

        for (Parameter parameter : annotatedMethod.parameters) {
            argumentListExpression.addExpression(varX(parameter));
        }

        BlockStatement blockStatement =
                block(
                        stmt(callX(ClassHelper.make(SwissKnife), "runOnBackground",
                                argumentListExpression))
                )

        annotatedMethod.setCode(blockStatement)
    }

}

