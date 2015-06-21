package com.arasthel.swissknife.annotations

import com.arasthel.swissknife.utils.AnnotationUtils
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Created by Arasthel on 05/04/15.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class FragmentArgTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        FieldNode annotatedField = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedField.declaringClass;

        MethodNode setParamsMethod = AnnotationUtils.getSetExtrasMethod(declaringClass, "setFragmentArgs", "args");

        String name = null;

        if (annotation.members.size() > 0) {
            name = annotation.members.value.property.getValue();
        }

        if (name == null) {
            name = annotatedField.name;
        }

        List<Statement> statementList = ((BlockStatement) setParamsMethod.getCode()).getStatements();

        Variable argsParam = setParamsMethod.getParameters()[0]

        Statement statement = getSetExtraStatement(annotatedField, argsParam, name)

        statementList.add(statement);
    }

    private Statement getSetExtraStatement(FieldNode property, Variable extras, String extraName) {
        return AnnotationUtils.createRestoreStatement(property, varX(extras), extraName)
    }
}
