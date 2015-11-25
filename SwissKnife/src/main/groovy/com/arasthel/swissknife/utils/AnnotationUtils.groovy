package com.arasthel.swissknife.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.arasthel.swissknife.annotations.Parcelable
import com.arasthel.swissknife.annotations.SaveInstanceTransformation
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement

import java.lang.reflect.Method

import static org.codehaus.groovy.ast.tools.GeneralUtils.*
import static org.codehaus.groovy.ast.tools.GeneralUtils.args

/**
 * Created by Arasthel on 17/08/14.
 */
public class AnnotationUtils {

    static final Class[] PARCELABLE_CLASSES = [String, int, byte, char, double, boolean, float,
                                               long, short, Integer, CharSequence, Bundle]

    public static MethodNode getSetExtrasMethod(ClassNode declaringClass, String methodName, String paramName) {
        Parameter[] parameters = [new Parameter(ClassHelper.make(Bundle), paramName)]

        MethodNode setExtrasMethod = declaringClass.getMethod(methodName, parameters)
        if(setExtrasMethod == null) {
            setExtrasMethod = createSetExtrasMethod(methodName, paramName)
            declaringClass.addMethod(setExtrasMethod)
        }
        return setExtrasMethod
    }

    private static MethodNode createSetExtrasMethod(String methodName, String paramName) {

        def activityParam = new Parameter(ClassHelper.make(Bundle), paramName)

        Parameter[] parameters = [activityParam]

        BlockStatement blockStatement = block()

        MethodNode node = new MethodNode(methodName,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                ClassHelper.VOID_TYPE,
                parameters,
                null,
                blockStatement)

        return node
    }

    public static MethodNode getInjectViewsMethod(ClassNode declaringClass) {
        Parameter[] parameters = [new Parameter(ClassHelper.make(Object.class), "view")]

        MethodNode injectMethod = declaringClass.getDeclaredMethod("injectViews", parameters)
        if (injectMethod == null) {
            injectMethod = createInjectMethod()
            // Some parent class has injectViews
            if (declaringClass.getMethod("injectViews", parameters)) {
                def block = injectMethod.getCode() as BlockStatement
                block.addStatement(stmt(callSuperX("injectViews", args(parameters))))
            }
            declaringClass.addMethod(injectMethod)
        }

        return injectMethod
    }

    private static MethodNode createInjectMethod() {

        BlockStatement blockStatement = block(
                declS(varX("currentClass", ClassHelper.CLASS_Type), callThisX("getClass")),
                declS(varX("superClass"), callX(callX(varX("currentClass"), "getClass"), "getSuperclass")),
                declS(varX("method", ClassHelper.make(MetaMethod)), callX(callX(varX("superClass"), "getMetaClass"), "pickMethod", args(constX("injectViews"), classX(Object)))),
                ifS(notNullX(varX("method")), callSuperX("injectViews", args(castX(ClassHelper.OBJECT_TYPE, varX("view"))))),
                declS(varX("v", ClassHelper.make(View)), constX(null))
        )

        Parameter[] parameters = [new Parameter(ClassHelper.make(Object.class), "view")]

        MethodNode node = new MethodNode("injectViews",
                Opcodes.ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                parameters,
                null,
                blockStatement)

        return node
    }

    public
    static ExpressionStatement createInjectExpression(Variable variable, Parameter viewParameter,
                                                      String id, String type = null) {

        return assignS(varX(variable), callX(ClassHelper.make(Finder), "findView",
                args(varX(viewParameter), constX(id), constX(type))))
    }

    public
    static ExpressionStatement createListInjectExpression(Variable variable,
                                                          Parameter viewParameter, String id,
                                                          String type) {

        return stmt(callX(varX(variable), "add", callX(ClassHelper.make(Finder), "findView",
                args(varX(viewParameter), constX(id), constX(type)))))
    }

    private static MethodNode createRestoreStateMethod(boolean overrideSuper) {
        def parameters = params(new Parameter(ClassHelper.make(Bundle.class), 'savedState'))
        BlockStatement blockStatement
        if (overrideSuper) {
            blockStatement = block(stmt(callSuperX('restoreSavedState', args(parameters))))
        } else {
            blockStatement = new BlockStatement()
        }
        blockStatement.addStatement(block(declS(varX("o"), constX(null))))
        MethodNode node = new MethodNode("restoreSavedState",
                Opcodes.ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                parameters,
                null,
                blockStatement)
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
        boolean overrideSuper = declaringClass.superClass.methods.find {
            it.name == 'restoreSavedState'
        } != null
        MethodNode restoreStateMethod = declaringClass.getMethod("restoreSavedState", parameters)
        if (restoreStateMethod == null) {
            restoreStateMethod = createRestoreStateMethod(overrideSuper)
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
        if (PARCELABLE_CLASSES.find {
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
        } || original == ClassHelper.make(implementable)

    }

    /*
     * Creates the Statement which will be used for restoring a variable's content in the
     * restoreState method
     */

    public static Statement createRestoreStatement(FieldNode annotatedField, Variable savedState,
                                             String id) {

        String bundleMethod = getBundleMethod(annotatedField)

        String getBundleMethod = "get$bundleMethod"

        return assignS(varX(annotatedField), callX(varX(savedState), getBundleMethod,
                args(constX(id))))

    }

    /*
     * Returns the corresponding method in order to add the content to the Bundle
     *
     * Example:
     * String -> StringArray
     * boolean[] -> BooleanArray
     * Parcelable -> Parcelable
     * Parcelable[] -> ParcelableArray
     * ArrayList<? extends Parcelable> -> ParcelableArrayList
     */

    public static String getBundleMethod(FieldNode annotatedField) {

        String method = null

        /*
         * We must first check if the annotated field is an array, in order to react accordingly
         */
        def isArray = annotatedField.getType().isArray()

        if (isArray) {
            method = processArray(annotatedField)
        }
        else {
            method = processCommonVariable(annotatedField)
        }

        method

    }

    /*
     * Returns the Bundle method for a variable declared as an array
     */

    private static String processArray(FieldNode annotatedField) {

        String method
        ClassNode type = annotatedField.getType().getComponentType()

        String typeName = ""

        if (ClassHelper.isPrimitiveType(type)) {
            typeName = type.nameWithoutPackage.capitalize()
        }
        else if (type == ClassHelper.STRING_TYPE) {
            typeName = "String"
        }
        else if (hasParcelableAnnotation(type)) {
            typeName = "Parcelable"
        }

        /*
         * As the variable is an array, we must append the "Array" suffix
         */

        method = typeName + "Array"

        method
    }

    /*
     * Returns the Bundle method for a variable that has not been declared as an array
     */

    private static String processCommonVariable(FieldNode annotatedField) {

        String method = ""

        ClassNode annotatedFieldClassNode = annotatedField.getType()

        ClassNode realClassNode = annotatedFieldClassNode

        if (annotatedFieldClassNode.isArray()) {
            method = "Array"
            realClassNode = annotatedFieldClassNode.getComponentType()
        }
        else if (annotatedFieldClassNode == ClassHelper.make(ArrayList)) {
            method = "ArrayList"
            realClassNode = annotatedFieldClassNode.getGenericsTypes()[0].type
        }

        /*
         * First we check if the variable is one of the Class objects declared at classes
         */
        PARCELABLE_CLASSES.find {
            if (ClassHelper.make(it) == realClassNode) {
                method = it.simpleName.capitalize() + method
                return true
            }
            return false
        }

        if (AnnotationUtils.doesClassImplementInterface(annotatedFieldClassNode,
                android.os.Parcelable) ||
                hasParcelableAnnotation(realClassNode))
            method = "Parcelable" + method
        else if (!method && AnnotationUtils.doesClassImplementInterface(annotatedFieldClassNode,
                java.io.Serializable))
            method = "Serializable"

        method

    }


}
