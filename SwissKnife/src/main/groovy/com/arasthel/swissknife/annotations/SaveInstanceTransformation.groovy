package com.arasthel.swissknife.annotations

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.arasthel.swissknife.utils.AnnotationUtils
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * Created by Dexafree on 02/10/14.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class SaveInstanceTransformation implements ASTTransformation, Opcodes {

    private ClassNode declaringClass

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        AnnotationNode annotation = astNodes[0];
        FieldNode annotatedField = astNodes[1];

        declaringClass = annotatedField.declaringClass

        ClassNode annotatedFieldClassNode = annotatedField.getType()

        String annotatedFieldName = annotatedField.name

        boolean isView = AnnotationUtils.isSubtype(annotatedFieldClassNode, View.class)

        /*
         * First of all, we must check that the annotated field is a View, and if it's not,
         * it must be able to be written to a Bundle object
         * If it's not, we throw an Exception showing the field and its type
         */
        if (!isView && !AnnotationUtils.canImplementSaveState(annotatedField)) {
            throw new Exception("Annotated field must be able to be written to a Bundle object. " +
                    "Field: $annotatedFieldName .Type: $annotatedFieldClassNode.name");
        }

        /*
         * Here we check if the user has passed any specific name to the SaveInstance annotation
         * If he has, the size will be 1, so we recover the passed String
         * Else, in order to avoid possible collisions with his own defined saveInstanceState
         * defined
         * constants, we will add the "SWISSKNIFE_" prefix, and name the variable after the
         * annotated field
         */
        String id = null

        if (annotation.members.size() > 0) {
            id = annotation.members.value.text
        }
        else {
            id = "SWISSKNIFE_$annotatedFieldName"
        }

        /*
         * ****************************************
         * * BEGIN OF ONSAVEINSTANCESTATE SECTION *
         * ****************************************
         */

        /*
         * We get the onSaveInstanceState method
         * If it hasn't been explicitly declared at the class, we must first generate one
         * If it has already been declared at the class, we get it in order to make further
         * modifications
         */
        MethodNode onSaveInstanceState = AnnotationUtils.getSaveStateMethod(declaringClass)

        Statement insertStatement = null;

        Variable bundleVariable = onSaveInstanceState.parameters.first()
        /*
         * Depending on the annotated method's class, we must get the correct statement in order
         * to add it to the onSaveInstanceState method
         */
        if (isView) {
            insertStatement = createViewSaveStateExpression(bundleVariable, id, annotatedField);
        }
        else {
            String bundleMethod = AnnotationUtils.getBundleMethod(annotatedField)

            insertStatement = createSaveStateExpression(onSaveInstanceState.parameters.first(),
                    bundleMethod, id, annotatedField);
        }

        /*
         * After getting the correct method, we add it to the class
         */
        List<Statement> statementsList = ((BlockStatement) onSaveInstanceState.getCode())
                .getStatements()
        statementsList.add(insertStatement)

        /*
         * **************************************
         * * END OF ONSAVEINSTANCESTATE SECTION *
         * **************************************
         */

        /*
         * *********************************
         * * BEGIN OF RESTORESTATE SECTION *
         * *********************************
         */

        /*
         * We get the restoreState method
         * If it hasn't been created on a previous iteration, we must first generate one
         * If it has already been declared at the class, we get it in order to make further
         * modifications
         */
        MethodNode restoreMethod = AnnotationUtils.getRestoreStateMethod(declaringClass)

        Statement statement = null;

        bundleVariable = restoreMethod.parameters.first()

        /*
         * Depending on the annotated method's class, we must get the correct statement in order
         * to add it to the onSaveInstanceState method
         */
        if (isView) {
            statement = createViewRestoreStatement(annotatedField, bundleVariable, id);
        }
        else {
            statement = AnnotationUtils.createRestoreStatement(annotatedField, bundleVariable, id);
        }

        /*
         * After getting the correct method, we add it to the class
         */
        List<Statement> statementList = ((BlockStatement) restoreMethod.getCode()).getStatements();
        statementList.add(statement)
    }

    /*
     * Creates the Statement which will be used for saving a variable's content in the
     * onSaveInstanceState method
     */

    private Statement createSaveStateExpression(Variable bundleVariable, String bundleMethod,
                                                String id, Variable fieldVariable) {

        String method = "put$bundleMethod"

        ExpressionStatement statement =
                stmt(
                        callX(varX(bundleVariable), method, args(constX(id), varX(fieldVariable)))
                )

        statement

    }

    /*
     * Creates the Statement which will be used for saving a View state in the
     * onSaveInstanceState method
     */

    private Statement createViewSaveStateExpression(Variable bundleVariable, String id,
                                                    FieldNode annotatedField) {

        String method = "onSaveInstanceState"

        Statement freezeTextStatement = null;

        // If view extends TextView is needed to set "freezesText" to true
        if (AnnotationUtils.isSubtype(annotatedField.getType(), TextView.class)) {
            freezeTextStatement =
                    stmt(
                            callX(varX(annotatedField), "setFreezesText", args(constX(true)))
                    )
        }

        BlockStatement statement =
                block(
                        stmt(callX(varX(bundleVariable), "putParcelable", args(constX(id),
                                callX(varX(annotatedField), method, args()))))
                )

        if (freezeTextStatement) {
            statement.getStatements().add(0, freezeTextStatement)
        }

        statement

    }

    /*
     * Creates the Statement which will be used for restoring a View state in the restoreState
     * method
     */

    private Statement createViewRestoreStatement(FieldNode annotatedField, Parameter savedState,
                                                 String id) {

        return stmt(callX(varX(annotatedField), "onRestoreInstanceState", callX(varX(savedState), "getParcelable",
                args(constX(id)))))

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
        GenericsType[] generics = declaringClass.getDeclaredField(annotatedField.name).type
                .genericsTypes


        generics.each { GenericsType it ->

            ClassNode genericClassNode = it.type

            // As we will modify the 'generic' variable, this ensures that it only will be
            // modified once
            if (generic == "ArrayList") {

                /*
                 * If the Generic implements the Parcelable interface,
                 * the method will be ParcelableArrayList
                 */
                if (AnnotationUtils.doesClassImplementInterface(genericClassNode,
                        android.os.Parcelable)) {

                    generic = "Parcelable" + generic

                }
                else {

                    /*
                     * If the Generic is not a Parcelable, it must be one of the following classes
                     * in order to be able to be written to a Bundle object
                     */

                    def parcelableObjects = [Integer, Boolean, Byte, Character, CharSequence,
                                             Double,
                                             Float, Long, String, Short]

                    if (parcelableObjects.find { ClassHelper.make(it) == genericClassNode }) {
                        generic = genericClassNode.nameWithoutPackage + generic
                    }
                }
            }
        }
        if (generic == 'ArrayList' && annotatedField.type.genericsTypes.find {
            hasParcelableAnnotation(it.type)
        }) {
            generic = "Parcelable" + generic
        }

        /*
         * If a valid generic Type has not been found, we will set the variable again to null
         */
        if (generic == "ArrayList") generic = null

        generic
    }

    private boolean hasParcelableAnnotation(ClassNode node) {
        node.annotations.find {
            it.classNode == ClassHelper.make(Parcelable)
        }
    }


}