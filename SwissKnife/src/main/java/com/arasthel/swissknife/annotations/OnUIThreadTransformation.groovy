package com.arasthel.swissknife.annotations

import com.arasthel.swissknife.utils.AstNodeToScriptVisitor
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Created by Arasthel on 16/08/14.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class OnUIThreadTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        MethodNode annotatedMethod = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedMethod.declaringClass;

        BlockStatement originalCode = annotatedMethod.getCode();

        ((BlockStatement) annotatedMethod.setCode(createRunnable(originalCode)));

    }

    private BlockStatement createRunnable(BlockStatement originalCode) {
        String source = "";
        new StringWriter().with { writer ->
            originalCode.visit new AstNodeToScriptVisitor(writer);
            source = "$writer";
        }

        println source
        def statement = new AstBuilder().buildFromString(
            """static clos = {
                    $source
               };
               SwissKnife.runOnBackground(clos);
            return;"""
        )[0];

        println "Generated";

        return statement;
    }

}
