package com.android.ast.restable

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * This transformation does nothing,
 * I need help to create
 *
 * static {
 *     @see RestableBuilder#create(java.lang.Class)
 *     @see RestableValidationBuilder#create(java.lang.Class)
 * }
 *
 * in each class that is annotated as {@link RestableEntity }
 * @since 0.1
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RestableTransformation extends AbstractASTTransformation {
    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {

    }
}
