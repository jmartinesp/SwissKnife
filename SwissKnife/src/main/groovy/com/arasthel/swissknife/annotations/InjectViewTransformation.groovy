package com.arasthel.swissknife.annotations

import android.view.View
import com.arasthel.swissknife.utils.AnnotationUtils
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Created by Arasthel on 16/08/14.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class InjectViewTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        FieldNode annotatedField = astNodes[1]
        AnnotationNode annotation = astNodes[0]
        ClassNode declaringClass = annotatedField.declaringClass

        if (!AnnotationUtils.isSubtype(annotatedField.getType(), View.class)) {
            throw new Exception("Annotated field must extend View class. Type: " +
                    "${annotatedField.type.name}")
        }

        MethodNode injectMethod = AnnotationUtils.getInjectViewsMethod(declaringClass)

        Variable viewParameter = injectMethod.parameters.first()

        String id = null;
        String type = null;

        if (annotation.members.size() > 0) {
            id = annotation.members.value.property.getValue()
            type = (annotation.members.value as PropertyExpression).objectExpression.type.name
        }

        if (id == null) {
            id = annotatedField.name;
        }

        Statement statement = AnnotationUtils.createInjectExpression(annotatedField,
                viewParameter, id, type)

        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements()
        statementList.add(statement)

    }
}
