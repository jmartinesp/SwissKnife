package com.arasthel.swissknife.annotations.resources

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import com.arasthel.swissknife.utils.AnnotationUtils
import com.arasthel.swissknife.utils.Finder
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static com.arasthel.swissknife.utils.AnnotationUtils.isSubtype
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Created by MrBIMC on 19/05/15.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ResTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        FieldNode annotatedField = astNodes[1]
        AnnotationNode annotation = astNodes[0]

        switch (annotation.classNode.nameWithoutPackage) {
            case "StringRes":
                if (!isSubtype(annotatedField.type, String))
                    throw new WrongTypeException("String", annotatedField.type.name)
                break
            case "DimenRes":
                if (!isSubtype(annotatedField.type, float.class) &&
                        !isSubtype(annotatedField.getType(), Float))
                    throw new WrongTypeException("Float(or float)", annotatedField.type.name)
                break
            case "ColorRes":
                if (!isSubtype(annotatedField.type, int.class) &&
                        !isSubtype(annotatedField.type, Integer))
                    throw new WrongTypeException("Integer(or int)", annotatedField.type.name)
                break
            case "BooleanRes":
                if (!isSubtype(annotatedField.type, Boolean) &&
                        !isSubtype(annotatedField.type, boolean.class))
                    throw new WrongTypeException("Boolean(or boolean)", annotatedField.type.name)
                break
            case "IntegerRes":
                if (!isSubtype(annotatedField.type, int.class) &&
                        !isSubtype(annotatedField.type, Integer))
                    throw new WrongTypeException("Integer(or int)", annotatedField.type.name)
                break
            case "IntegerArrayRes":
                if (!isSubtype(annotatedField.type, int[].class) &&
                        !isSubtype(annotatedField.type, Integer[].class))
                    throw new WrongTypeException("int[]", annotatedField.type.name)
                break
            case "StringArrayRes":
                if (!isSubtype(annotatedField.type, String[].class))
                    throw new WrongTypeException("Animation", annotatedField.type.name)
                break
            case "DrawableRes":
                if (!isSubtype(annotatedField.type, Drawable))
                    throw new WrongTypeException("Drawable", annotatedField.type.name)
                break
            case "AnimationRes":
                if (!isSubtype(annotatedField.type, Animation))
                    throw new WrongTypeException("Animation", annotatedField.type.name)
                break
            case "ColorStateListRes":
                if (!isSubtype(annotatedField.type, ColorStateList))
                    throw new WrongTypeException("ColorStateList", annotatedField.type.name)
                break
        }

        String method = "get" + annotation.classNode.nameWithoutPackage.replace("Res", "")

        String id = annotation.members.size() ?
                annotation.members.value.property.getValue() : annotatedField.name


        ClassNode declaringClass = annotatedField.declaringClass
        MethodNode injectMethod = AnnotationUtils.getInjectViewsMethod(declaringClass)
        Variable viewParameter = injectMethod.parameters.first()

        Statement statement = assignS(varX(annotatedField), callX(ClassHelper.make(Finder),
                method, args(varX(viewParameter), constX(id))))

        ((BlockStatement) injectMethod.getCode()).getStatements().add(statement)
    }

    private class WrongTypeException extends Exception {
        WrongTypeException(String expectedType, String actualType) {
            super("Annotated field must be $expectedType. Type: $actualType")
        }
    }
}
