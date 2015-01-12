package com.arasthel.swissknife.annotations

import com.arasthel.swissknife.utils.AnnotationUtils
import com.arasthel.swissknife.utils.Finder
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Created by Arasthel on 16/08/14.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class InjectViewsTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        FieldNode annotatedField = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedField.declaringClass;

        MethodNode injectMethod = AnnotationUtils.getInjectViewsMethod(declaringClass);

        def ids = [];

        ClassNode fieldClass = annotatedField.getType();

        if(!AnnotationUtils.isSubtype(fieldClass, List.class)) {
            throw new Exception("The annotated field must extend List. Type: $fieldClass.name");
        }

        if(annotation.members.size() > 0) {
            if(annotation.members.value instanceof ListExpression) {
                annotation.members.value.getExpressions().each {
                    ids << (String) it.property.getValue();
                };
            } else {
                throw new Exception("InjectViews must have a list of ids");
            }
        } else {
            throw new Exception("InjectViews must have a list of ids");
        }


        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements();

        statementList.add(createViewListStatement(annotatedField));

        ids.each { statementList.add(createInjectViewStatement(annotatedField, it)); }

    }

    private Statement createViewListStatement(FieldNode field) {
        return new AstBuilder().buildFromSpec {
            expression{
                binary {
                    property {
                        variable "this"
                        constant field.name
                    }
                    token "="
                    constructorCall(ArrayList.class) {
                        argumentList {}
                    }
                }
            }
        }[0];
    }

    private Statement createInjectViewStatement(FieldNode field, String id) {

        ExpressionStatement injectStatement = AnnotationUtils.createInjectExpression(id)

        BlockStatement blockStatement = new BlockStatement()

        ExpressionStatement addToListExpression =
                new AstBuilder().buildFromSpec {
                        expression {
                            methodCall {
                                property {
                                    variable "this"
                                    constant field.name
                                }
                                constant "add"
                                argumentList {
                                    variable "v"
                                }
                            }
                        }
                }[0];

        blockStatement.addStatement(injectStatement)
        blockStatement.addStatement(addToListExpression)

        return blockStatement;
    }
}
