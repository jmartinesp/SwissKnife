package com.arasthel.swissknife.annotations

import android.view.View
import com.arasthel.swissknife.utils.AnnotationUtils
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
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
        FieldNode annotatedField = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedField.declaringClass;

        Class annotatedFieldClass = annotatedField.getType().getTypeClass();

        if(!AnnotationUtils.isSubtype(annotatedFieldClass, View.class)) {
            throw new Exception("Annotated field must extend View class. Type: $annotatedFieldClass.name");
        }

        MethodNode injectMethod = AnnotationUtils.getInjectViewsMethod(declaringClass);

        String id = null;

        if(annotation.members.size() > 0) {
            id = annotation.members.value.property.getValue();
        }

        if(id == null) {
            id = annotatedField.name;
        }

        Statement statement = createInjectStatement(annotatedField, id);

        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements();
        statementList.add(statement);

    }

    private Statement createInjectStatement(FieldNode field, String id) {

        BlockStatement statement =
                new AstBuilder().buildFromSpec {
                    block {
                        expression {
                            binary {
                                variable field.name
                                token "="
                                variable "v"
                            }
                        }
                    }
                }[0];

        statement.statements.add(0, AnnotationUtils.createInjectExpression(id));

        return statement;

    }
}
