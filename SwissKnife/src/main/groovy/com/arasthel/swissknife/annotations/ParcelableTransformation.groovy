package com.arasthel.swissknife.annotations

import android.os.Bundle
import android.os.Parcel
import android.util.SparseArray
import com.arasthel.swissknife.utils.AnnotationUtils
import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.ASTHelper
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * Transforming entity into Parcelable
 *
 * @author Jorge Martín Espinosa
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@CompileStatic
public class ParcelableTransformation extends AbstractASTTransformation implements Opcodes {

    // Classes that can be written in Parcel
    private static final List<Class> PARCELABLE_CLASSES = [
            String, String[], List, Map, SparseArray, android.os.Parcelable,
            android.os.Parcelable[], Bundle, CharSequence, Serializable
    ]

    List<FieldNode> excludedFields = []

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        AnnotationNode annotation = (AnnotationNode) astNodes[0];
        ClassNode annotatedClass = (ClassNode) astNodes[1];
        readExcludedFields(annotation, annotatedClass)
        // We implement the interface
        annotatedClass.addInterface(ClassHelper.make(android.os.Parcelable))
        // We add the describeContents method
        MethodNode describeContentsMethod = createDescribeContentsMethod()
        annotatedClass.addMethod(describeContentsMethod)
        // We add the writeToPacel method
        MethodNode writeToParcelMethod = createWriteToParcelMethod(annotatedClass)
        annotatedClass.addMethod(writeToParcelMethod)
        // We add the CREATOR field
        createCREATORField(annotatedClass, sourceUnit)
        // We add an empty constructor
        annotatedClass.addConstructor(createEmptyConstructor());
        // We add a constructor which takes only one Parcel argument
        annotatedClass.addConstructor(createParcelConstructor(annotatedClass));
        println "Implemented Parcelable class for $annotatedClass"
    }

    def checkIfParentIsParcelable(ClassNode parentClass) {
        boolean isParcelable = false

        if (!parentClass) {
            return isParcelable
        }

        if (parentClass.superClass) {
            isParcelable = checkIfParentIsParcelable(parentClass.superClass) || checkIfClassIsParcelable(parentClass)
        } else {
            isParcelable = checkIfClassIsParcelable(parentClass)
        }
        return isParcelable
    }

    def checkIfClassIsParcelable(ClassNode classNode) {
        boolean hasAnnotation = classNode.annotations.find {
            return it.classNode.equals(ClassHelper.make(Parcelable))
        }
        boolean isParcelable = classNode.implementsInterface(ClassHelper.make(android.os.Parcelable)) || hasAnnotation
        return isParcelable
    }

    def readExcludedFields(AnnotationNode annotationNode, ClassNode annotatedClass) {
        Expression excludesExpression = annotationNode.members.exclude as ClosureExpression
        if (excludesExpression) {
            (excludesExpression.getCode() as BlockStatement).getStatements().each {
                String fieldName = ((it as ExpressionStatement).expression as VariableExpression)
                        .accessedVariable.name
                FieldNode excluded = annotatedClass.getField(fieldName)
                if (excluded) {
                    excludedFields.add(excluded)
                }
            }
        }
    }

    List<FieldNode> getParcelableFields(ClassNode declaringClass) {
        def parcelableFields = []
        declaringClass.getFields().each { FieldNode field ->
            if (!(field in excludedFields)) {
                // We don't want to parcel static fields
                if (!field.isStatic()) {
                    ClassNode fieldClass = field.getType()
                    // If it's a primitive, it can be parceled
                    if (ClassHelper.isPrimitiveType(fieldClass)) {
                        parcelableFields << field
                    }
                    else if (fieldClass.isArray()) {
                        // If it's an array of primitives, too
                        if (ClassHelper.isPrimitiveType(fieldClass.getComponentType())) {
                            parcelableFields << field
                        }
                        else {
                            // If it's an array of objects, find if it's one of the parcelable
                            // classes
                            PARCELABLE_CLASSES.find {
                                if (fieldClass.isDerivedFrom(ClassHelper.make(it))
                                        || fieldClass.implementsInterface(ClassHelper.make(it))
                                        || checkIfClassIsParcelable(ClassHelper.make(it))) {
                                    parcelableFields << field
                                    return true
                                }
                                return false
                            }
                        }
                    }
                    else {
                        // If it's an object, check if it's parcelable
                        PARCELABLE_CLASSES.find {
                            if (fieldClass.isDerivedFrom(ClassHelper.make(it))
                                    || fieldClass.implementsInterface(ClassHelper.make(it))
                                    || checkIfClassIsParcelable(ClassHelper.make(it))) {
                                parcelableFields << field
                                return true
                            }
                            return false
                        }
                    }

                }
            }
        }

        return parcelableFields
    }

    ConstructorNode createParcelConstructor(ClassNode annotatedClass) {
        Parameter parcelParameter = new Parameter(ClassHelper.make(Parcel).plainNodeReference,
                'parcel')
        Statement code = readFromParcelCode(annotatedClass, parcelParameter)
        return new ConstructorNode(ACC_PUBLIC, params(parcelParameter), ClassNode.EMPTY_ARRAY, code)
    }

    ConstructorNode createEmptyConstructor() {
        return new ConstructorNode(ACC_PUBLIC, new BlockStatement())
    }

    MethodNode createDescribeContentsMethod() {
        ReturnStatement returnStatement = new ReturnStatement(new ConstantExpression(0))
        MethodNode methodNode = new MethodNode("describeContents", ACC_PUBLIC,
                ClassHelper.int_TYPE, [] as Parameter[], [] as ClassNode[], returnStatement)
        return methodNode
    }

    MethodNode createWriteToParcelMethod(ClassNode annotatedClass) {
        Parameter[] parameters = [new Parameter(ClassHelper.make(Parcel), "dest"),
                                  new Parameter(ClassHelper.int_TYPE, "flags")]
        Statement statement = writeToParcelCode(annotatedClass, parameters)
        MethodNode methodNode = new MethodNode("writeToParcel", ACC_PUBLIC,
                ClassHelper.VOID_TYPE, parameters, [] as ClassNode[], statement)
        return methodNode
    }

    Statement writeToParcelCode(ClassNode annotatedClass, Parameter[] parameters) {
        BlockStatement statement = new BlockStatement()
        List<FieldNode> fields = getParcelableFields(annotatedClass)

        println("Processing $annotatedClass")

        // Call super.writeToParcel(...) if needed
        if (checkIfParentIsParcelable(annotatedClass.superClass)) {
            statement.addStatement(stmt(callSuperX("writeToParcel", args(parameters))))
        }

        Variable parcelVar = parameters.first()

        fields.each { FieldNode field ->
            // Is Primitive (int, char...)
            if (ClassHelper.isPrimitiveType(field.getType())) {
                statement.addStatement(getWritePrimitiveStatement(field, parcelVar))
            } else {
                // writeValue(field)
                statement.addStatement(stmt(callX(varX(parcelVar), "writeValue", args(fieldX(field)))))
            }
        }
        return statement
    }

    private Statement getWritePrimitiveStatement(FieldNode field, Variable parcelVar) {
        // write___(field) -> writeChar, writeInt...
        return stmt(callX(
                        varX(parcelVar),
                        "write${field.getType().name.capitalize()}",
                        args(fieldX(field))))
    }

    Statement readFromParcelCode(ClassNode annotatedClass, Parameter parcelParam) {
        List<Statement> statements = []
        List<FieldNode> fields = getParcelableFields(annotatedClass)
        def parcelVar = varX(parcelParam)

        if (checkIfParentIsParcelable(annotatedClass.superClass)) {
            statements.add(
                    ctorSuperS(args([parcelParam] as Parameter[]))
            )
        }

        fields.each { FieldNode field ->
            // Every method will be read____
            if (ClassHelper.isPrimitiveType(field.getType())) {
                statements << getReadPrimitiveStatement(field, parcelVar)
            } else {
                Expression classLoader = constX(null)

                if (field.getType().isArray()) {
                    classLoader = callX(field.getType().getComponentType(), "getClassLoader")
                } else if (AnnotationUtils.doesClassImplementInterface(field.getType(), List)) {
                    classLoader = callX(field.getType().getGenericsTypes().first().getType(), "getClassLoader")
                }

                // field = readValue(DeclaringClass.getClassLoader())
                statements << assignS(fieldX(field), callX(parcelVar, "readValue", args(classLoader)))
            }
        }
        block(statements as Statement[])
    }

    private Statement getReadPrimitiveStatement(FieldNode field, Variable parcelVar) {
        // field = read___() -> readChar, readInt...
        return assignS(fieldX(field),
                        callX(
                            varX(parcelVar),
                            "read${field.getType().name.capitalize()}",
                            args(Parameter.EMPTY_ARRAY)))
    }

    void createCREATORField(ClassNode ownerClass, SourceUnit sourceUnit) {
        // We take a Creator<MyClass>
        ClassNode creatorInterfaceClassNode = ClassHelper.make(android.os.Parcelable.Creator)
        creatorInterfaceClassNode.genericsTypes = [new GenericsType(ownerClass)] as GenericsType[]
        // Create an inner class that implements that Creator<MyClass>
        String name = ownerClass.getNameWithoutPackage() + '$Creator';
        String fullName = ASTHelper.dot(ownerClass.getPackageName(), name);
        InnerClassNode customCreatorClassNode = new InnerClassNode(ownerClass, fullName,
                ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE)
        customCreatorClassNode.addInterface(creatorInterfaceClassNode.getPlainNodeReference())
        // This line is needed to add the inner class to the original class
        ownerClass.module.addClass(customCreatorClassNode)
        // Add createFromParcel method to inner class
        customCreatorClassNode.addMethod(createFromParcelMethod(ownerClass, customCreatorClassNode))
        // Add newArray method to inner class
        customCreatorClassNode.addMethod(newArrayMethod(ownerClass, customCreatorClassNode))
        // public static CREATOR = new MyClass.Creator()
        FieldNode creatorField = new FieldNode("CREATOR",
                ACC_PUBLIC | ACC_STATIC,
                creatorInterfaceClassNode.plainNodeReference,
                ownerClass,
                new ConstructorCallExpression(customCreatorClassNode, new ArgumentListExpression()))
        ownerClass.addField(creatorField)
        // This line is needed to add the inner class to the original class
        ownerClass.module.addClass(customCreatorClassNode)
    }

    MethodNode createFromParcelMethod(ClassNode outerClass, InnerClassNode creatorClassNode) {
        // Just call new MyClass(parcel) and return it
        Statement cfpCode =
                returnS(
                        ctorX(outerClass.getPlainNodeReference(),
                                args(varX("source", ClassHelper.make(Parcel)))
                        )
                )

        MethodNode createFromParcelMethod = new MethodNode("createFromParcel",
                ACC_PUBLIC,
                outerClass.getPlainNodeReference(),
                [new Parameter(ClassHelper.make(Parcel), "source")] as Parameter[],
                [] as ClassNode[],
                cfpCode)
        return createFromParcelMethod
    }

    MethodNode newArrayMethod(ClassNode outerClass, InnerClassNode creatorClassNode) {
        // Return an array of MyClass of the given size
        ClassNode arrayNode = ClassHelper.OBJECT_TYPE.makeArray()
        Statement naCode =
                returnS(new ArrayExpression(outerClass, null,
                        [varX("size", ClassHelper.int_TYPE)] as List<Expression>))
        MethodNode createNewArrayNode = new MethodNode("newArray",
                ACC_PUBLIC,
                arrayNode,
                [new Parameter(ClassHelper.int_TYPE, "size")] as Parameter[],
                [] as ClassNode[],
                naCode)
        return createNewArrayNode
    }
}