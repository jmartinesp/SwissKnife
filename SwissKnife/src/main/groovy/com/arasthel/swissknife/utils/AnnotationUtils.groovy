package com.arasthel.swissknife.utils

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.arasthel.swissknife.annotations.Parcelable
import com.arasthel.swissknife.annotations.SaveInstanceTransformation
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * Created by Arasthel on 17/08/14.
 */
public class AnnotationUtils {

    public static MethodNode getInjectViewsMethod(ClassNode declaringClass) {
        Parameter[] parameters = [new Parameter(ClassHelper.make(Object.class), "view")];

        MethodNode injectMethod = declaringClass.getMethod("injectViews", parameters);
        if (injectMethod == null) {
            injectMethod = createInjectMethod();
            declaringClass.addMethod(injectMethod);
        }

        return injectMethod
    }

    private static MethodNode createInjectMethod() {

        BlockStatement blockStatement = block(
                declS(varX("v", ClassHelper.make(View)), constX(null))
        )

        Parameter[] parameters = [new Parameter(ClassHelper.make(Object.class), "view")]

        MethodNode node = new MethodNode("injectViews",
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                ClassHelper.VOID_TYPE,
                parameters,
                null,
                blockStatement)

        return node
    }

    public
    static ExpressionStatement createInjectExpression(Variable variable, Parameter viewParameter,
                                                      String id) {

        return assignS(varX(variable), callX(ClassHelper.make(Finder), "findView",
                args(varX(viewParameter), constX(id))))
    }

    public
    static ExpressionStatement createListInjectExpression(Variable variable,
                                                          Parameter viewParameter, String id) {

        return stmt(callX(varX(variable), "add", callX(ClassHelper.make(Finder), "findView",
                args(varX(viewParameter), constX(id)))))
    }

    private static MethodNode createRestoreStateMethod() {

        Parameter[] parameters = [new Parameter(ClassHelper.make(Bundle.class), "savedState")];

        BlockStatement blockStatement =
                block(
                        declS(varX("o"), constX(null))
                )

        MethodNode node = new MethodNode("restoreSavedState",
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                ClassHelper.VOID_TYPE,
                parameters,
                null,
                blockStatement);

        return node;
    }

    private static MethodNode createSaveStateMethod() {

        Parameter savedState = new Parameter(ClassHelper.make(Bundle.class), "savedState");

        Parameter[] parameters = [savedState];

        BlockStatement blockStatement =
                block(
                        stmt(callSuperX("onSaveInstanceState", args(savedState)))
                )

        AnnotationNode overrideAnnotation = new AnnotationNode(ClassHelper.make(Override.class))

        MethodNode node = new MethodNode("onSaveInstanceState",
                Opcodes.ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                parameters,
                null,
                blockStatement);

        node.addAnnotation(overrideAnnotation)

        return node;

    }

    public static boolean isSubtype(ClassNode original, Class compared) {
        ClassNode comparedClassNode = ClassHelper.make(compared);
        return original.isDerivedFrom(comparedClassNode);
    }

    public static MethodNode getSaveStateMethod(ClassNode declaringClass) {
        Parameter[] parameters = [new Parameter(ClassHelper.make(Bundle.class), "outState")]
        MethodNode saveStateMethod = declaringClass.getDeclaredMethod("onSaveInstanceState",
                parameters)
        if (saveStateMethod == null) {
            saveStateMethod = createSaveStateMethod()
            declaringClass.addMethod(saveStateMethod)
        }

        saveStateMethod

    }

    public static MethodNode getRestoreStateMethod(ClassNode declaringClass) {
        Parameter[] parameters = [new Parameter(ClassHelper.make(Bundle.class), "savedState")]

        MethodNode restoreStateMethod = declaringClass.getMethod("restoreSavedState", parameters)
        if (restoreStateMethod == null) {
            restoreStateMethod = createRestoreStateMethod()
            declaringClass.addMethod(restoreStateMethod)
        }
        restoreStateMethod
    }

    public static boolean canImplementSaveState(FieldNode annotatedField) {

        ClassNode originalClassNode = annotatedField.getType()

        ClassNode realClassNode = originalClassNode

        // We look for the real ClassNode
        if (originalClassNode.isArray()) {
            realClassNode = originalClassNode.getComponentType()
        }
        else if (originalClassNode == ClassHelper.make(ArrayList)) {
            realClassNode = originalClassNode.getGenericsTypes()[0].type
        }

        // If it's a parcelable class, it's parcelable
        if (SaveInstanceTransformation.PARCELABLE_CLASSES.find {
            ClassHelper.make(it) == realClassNode
        }) {
            return true
        }

        // If it implements Parcelable or Serializable, it's parcelable
        if (doesClassImplementInterface(originalClassNode, android.os.Parcelable) ||
                doesClassImplementInterface(originalClassNode, Serializable)) {
            return true
        }

        // If uses the @Parcelable annotation, it's parcelable
        return hasParcelableAnnotation(realClassNode)
    }

    public static boolean hasParcelableAnnotation(ClassNode node) {
        node.annotations.find { it.classNode == ClassHelper.make(Parcelable) }
    }


    public static boolean doesClassImplementInterface(ClassNode original, Class implementable) {

        return original.getInterfaces().find {
            it == ClassHelper.make(implementable)
        }

    }


}
