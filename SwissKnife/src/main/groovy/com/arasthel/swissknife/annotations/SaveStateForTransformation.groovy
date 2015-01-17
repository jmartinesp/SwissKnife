package com.arasthel.swissknife.annotations

import android.os.Bundle
import android.util.SparseArray
import com.arasthel.swissknife.utils.AnnotationUtils
import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * Adding injection for saving/restoring state
 *
 * @author Eugene Kamenev
 */

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class SaveStateForTransformation extends AbstractASTTransformation implements Opcodes {

    // Classes that can be written in Parcel
    private static final List<Class> PARCELABLE_CLASSES = [
            String, String[], List, Map, SparseArray, android.os.Parcelable, android.os.Parcelable[], Bundle, CharSequence, Serializable
    ]

    // Classes which need a ClassLoader as an argument for reading
    private static final List<Class> NEED_CLASSLOADER = [
            Bundle, List, Map, android.os.Parcelable, SparseArray
    ]

    static final ClassNode bundleClass = ClassHelper.make(Bundle).plainNodeReference

    List<FieldNode> saveStateVars = []
    ClassNode declaringClass

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        AnnotationNode annotation = (AnnotationNode) astNodes[0];
        ClassNode annotatedClass = (ClassNode) astNodes[1];
        declaringClass = annotatedClass
        // read fields for saving
        readSaveStateFields(annotation, annotatedClass)
        // create save instance method
        findOrCreateSaveInstanceMethod(annotatedClass)
        // create restore state method
        findOrCreateRestoreMethod(annotatedClass)
    }

    def readSaveStateFields(AnnotationNode annotationNode, ClassNode annotatedClass) {
        Expression excludesExpression = annotationNode.members.value as ClosureExpression
        if (excludesExpression) {
            (excludesExpression.getCode() as BlockStatement).getStatements().each {
                String fieldName = ((it as ExpressionStatement).expression as VariableExpression).accessedVariable.name
                FieldNode excluded = annotatedClass.getField(fieldName)
                if (excluded) {
                    saveStateVars.add(excluded)
                }
            }
        }
    }

    def findOrCreateSaveInstanceMethod(ClassNode classNode) {
        def method = classNode.methods.find {
            it.name == 'onSaveInstanceState' &&
                    it.parameters.size() == 1 &&
                    it.parameters.first().type.plainNodeReference == bundleClass
        }
        if (!method) {
            method = new MethodNode('onSaveInstanceState', ACC_PROTECTED, ClassHelper.VOID_TYPE, params(new Parameter(bundleClass, 'outState')), null, new BlockStatement())
            classNode.addMethod(method)
        }
        def methodCode = block(method.code)
        def outBundle = method.parameters.first()
        int superCallPos = findMethodCallPosition(methodCode, 'super', 'onSaveInstanceState')
        if (superCallPos == -1) {
            methodCode.statements.add(superCallPos + 1, (stmt(callSuperX('onSaveInstanceState', varX(outBundle)))))
        }
        saveStateVars.each {
            methodCode.addStatement(putToBundle(it, outBundle))
        }
        method.setCode(methodCode)
    }

    def findOrCreateRestoreMethod(ClassNode classNode) {
        def method = classNode.methods.find {
            it.name == 'restoreSavedState' &&
                    it.parameters.size() == 1 &&
                    it.parameters.first().type.plainNodeReference == bundleClass
        }
        if (!method) {
            method = new MethodNode('restoreSavedState', ACC_PUBLIC, ClassHelper.VOID_TYPE, params(new Parameter(bundleClass, 'outState')), null, new BlockStatement())
            classNode.addMethod(method)
        }
        def methodCode = block(method.code)
        def outBundle = method.parameters.first()
        saveStateVars.each {
            methodCode.addStatement(getFromBundle(it, outBundle))
        }
        method.setCode(methodCode)
    }

    Statement putToBundle(FieldNode field, Variable outState) {
        String id = "SWISSKNIFE_$field.name"
        String method = "put${getBundleMethod(field)}"
        stmt(callX(varX(outState), method, args(constX(id), varX(field))))
    }

    Statement getFromBundle(FieldNode field, Variable outState) {
        String id = "SWISSKNIFE_$field.name"
        String method = "get${getBundleMethod(field)}"
        stmt(assignX(varX(field), callX(varX(outState), method, args(constX(id)))))
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

    private String getBundleMethod(FieldNode annotatedField) {

        String method = null

        /*
         * We must first check if the annotated field is an array, in order to react accordingly
         */
        def isArray = annotatedField.getType().isArray()


        if (isArray) {
            method = processArray(annotatedField)
        } else {
            method = processCommonVariable(annotatedField)
        }

        method

    }

    /*
     * Returns the Bundle method for a variable declared as an array
     */

    private String processArray(FieldNode annotatedField) {

        String method
        String type = annotatedField.getType()

        /*
         * As String is a special case (it's not primitive type), we first check if it's that case
         */
        if (type.contains("String[]")) {
            method = "String"
        } else {

            /*
             * If it's not a String, then we will check if it's a Parcelable object, so we get the
             * array's Type Class and check if it implements Parcelable
             */
            ClassNode arrayTypeClass = annotatedField.originType.componentType

            if (AnnotationUtils.doesClassImplementInterface(arrayTypeClass, "android.os.Parcelable")) {
                type = "Parcelable"
            } else {

                /*
                 * If the current variable is neither a String[] or a Parcelable, we get the Type
                 * and simply capitalize the first character, because it will be a primitive Type
                 */
                type = type.substring(0, type.length() - 2)

                if (Character.isLowerCase(type.charAt(0))) {

                    char first = Character.toUpperCase(type.charAt(0))

                    type = "$first" + type.substring(1)
                }
            }
        }

        /*
         * As the variable is an array, we must append the "Array" suffix
         */
        method = type + "Array"

        method
    }

    /*
     * Given an annotated ArrayList, this function returns a String which contains:
     * ArrayList-generic + "ArrayList"
     */

    private String getGenericFromArrayList(FieldNode annotatedField) {

        String generic = "ArrayList"

        /*
         * First we retrieve the Generics Types found inside the ArrayList and iterate through them
         */
        GenericsType[] generics = declaringClass.getDeclaredField(annotatedField.name).type.genericsTypes


        generics.each { GenericsType it ->

            ClassNode genericClassNode = it.type

            // As we will modify the 'generic' variable, this ensures that it only will be modified once
            if (generic == "ArrayList") {

                /*
                 * If the Generic implements the Parcelable interface, the method will be ParcelableArrayList
                 */
                if (AnnotationUtils.doesClassImplementInterface(genericClassNode, "android.os.Parcelable")) {

                    generic = "Parcelable" + generic

                } else {

                    /*
                     * If the Generic is not a Parcelable, it must be one of the following classes
                     * in order to be able to be written to a Bundle object
                     */
                    switch (genericClassNode.name) {
                        case Integer.class.name:
                            generic = Integer.class.name + generic
                            break

                        case Boolean.class.name:
                            generic = Boolean.class.name + generic
                            break

                        case Byte.class.name:
                            generic = Byte.class.name + generic
                            break

                        case Character.class.name:
                            generic = Character.class.name + generic
                            break

                        case CharSequence.class.name:
                            generic = CharSequence.class.name + generic
                            break

                        case Double.class.name:
                            generic = Double.class.name + generic
                            break

                        case Float.class.name:
                            generic = Float.class.name + generic
                            break

                        case Long.class.name:
                            generic = Long.class.name + generic
                            break

                        case String.class.name:
                            generic = String.class.name + generic
                            break

                        case Short.class.name:
                            generic = Short.class.name + generic
                            break

                        default:
                            break
                    }
                }
            }
        }
        if (generic == 'ArrayList' && annotatedField.type.genericsTypes.find {
            it.type.annotations.find { it.classNode.nameWithoutPackage == 'Parcelable' }
        }) {
            generic = "Parcelable" + generic
        }

        /*
         * If a valid generic Type has not been found, we will set the variable again to null
         */
        if (generic == "ArrayList") generic = null

        generic
    }

    /*
     * Returns the Bundle method for a variable that has not been declared as an array
     */

    private String processCommonVariable(FieldNode annotatedField) {

        String method

        ClassNode annotatedFieldClassNode = annotatedField.getType()

        Class[] classes = [String.class, int.class, byte.class, char.class, double.class,
                           boolean.class, float.class, long.class, short.class, CharSequence.class,
                           Bundle.class]

        /*
         * First we check if the variable is one of the Class objects declared at classes
         */
        classes.each { Class it ->
            if (it.name == annotatedFieldClassNode.name && method == null) method = it.name
        }

        /*
         * If the variable's class was not in the declared Classs objects, it might be an ArrayList,
         * a Parcelable or a Serializable
         */
        if (method == null) {

            // We create a dummy ArrayList in order to check if is instance of the variable's class
            ArrayList dummyAL = new ArrayList()

            if (annotatedFieldClassNode.name == dummyAL.class.name) method = "ArrayList"

            /*
             * If the variable is an ArrayList, we start processing it in order to get it's Generic Type
             * If the generic wasn't a valid one, method would be null again
             */
            if (method == "ArrayList") {
                method = getGenericFromArrayList(annotatedField)
            }
        }

        /*
         * If the object was not one of the declared classes or a valid ArrayList, it might be a
         * Parcelable object or a Serializable one
         */
        if (method == null) {

            if (AnnotationUtils.doesClassImplementInterface(annotatedFieldClassNode, "android.os.Parcelable"))
                method = "Parcelable"
            else if (AnnotationUtils.doesClassImplementInterface(annotatedFieldClassNode, "java.io.Serializable"))
                method = "Serializable"
            else if (annotatedFieldClassNode.annotations.find {
                it.classNode.nameWithoutPackage == 'Parcelable'
            })
                method = 'Parcelable'

        }

        /*
         * If a valid type has been found, we must check that the first character is uppercase, in order
         * to add later the "put/get" prefix
         */
        if (method != null) {

            if (Character.isLowerCase(method.charAt(0))) {
                char first = Character.toUpperCase(method.charAt(0))
                method = "$first" + method.substring(1)
            }

            /*
             * If the Type name contains '.', we'll only need the last part
             */
            if (method.contains(".")) {
                String[] splits = method.split("\\.")
                method = splits[splits.length - 1]
            }

        }

        //Uncomment for debug
        //println(method)

        method

    }

    static int findMethodCallPosition(BlockStatement blockStatement, String object, String methodName) {
        int position = -1
        int index = 0
        for (Statement statement : blockStatement.statements) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = statement as ExpressionStatement
                if (expressionStatement.expression instanceof MethodCallExpression) {
                    MethodCallExpression methodCallExpression = expressionStatement.expression as MethodCallExpression
                    if (methodCallExpression.objectExpression.text == object && methodCallExpression.methodAsString == methodName) {
                        position = index
                        break
                    }
                }
            }
            index++
        }
        return position
    }
}