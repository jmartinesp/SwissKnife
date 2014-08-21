package com.arasthel.swissknife.annotations

import android.view.View
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.utils.AnnotationUtils
import com.arasthel.swissknife.utils.Finder
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ListExpression
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
public class OnTouchTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        MethodNode annotatedMethod = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedMethod.declaringClass;

        MethodNode injectMethod = AnnotationUtils.createInjectViewsMethod(declaringClass);

        def ids = [];

        if(annotation.members.size() > 0) {
            if(annotation.members.value instanceof ListExpression) {
                annotation.members.value.getExpressions().each {
                    ids << (String) it.property.getValue();
                };
            } else {
                ids << (String) annotation.members.value.property.getValue();
            }
        } else {
            throw new Exception("OnTouch must have an id");
        }

        if(annotatedMethod.getReturnType().name != "boolean" && annotatedMethod.getReturnType().name != "java.lang.Boolean") {
            throw new Exception("OnTouch must return a boolean value");
        }

        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements();

        ids.each { String id ->
            Statement statement = createInjectStatement(id, annotatedMethod);
            statementList.add(statement);
        }

    }

    private Statement createInjectStatement(String id, MethodNode method) {

        def statement =
                new AstBuilder().buildFromSpec {
                    block {
                        expression {
                            binary {
                                variable "v"
                                token "="
                                staticMethodCall(Finder.class, "findView") {
                                    argumentList {
                                        variable "this"
                                        constant id
                                    }
                                }
                            }
                        }
                        expression {
                            staticMethodCall(SwissKnife.class, "setOnTouch") {
                                argumentList {
                                    cast(View.class) {
                                        variable "v"
                                    }
                                    variable "this"
                                    constant method.name
                                }
                            }
                        }
                    }
                }[0];

        return statement;

    }
}
