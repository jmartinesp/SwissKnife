package com.arasthel.swissknife.annotations

import android.os.Bundle
import android.view.View
import com.arasthel.swissknife.utils.AnnotationUtils
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovyjarjarasm.asm.Opcodes
import groovyjarjarasm.asm.commons.Method
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

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

        Class annotatedFieldClass = annotatedField.getType().getTypeClass()

        String annotatedFieldName = annotatedField.name

        if(!canImplementSaveState(declaringClass, annotatedField)){
            throw new Exception("Annotated field must be able to be written to a Bundle object. Field: $annotatedFieldName .Type: $annotatedFieldClass.name");
        }

        String id = null

        if(annotation.members.size() > 0){
            id = annotation.members.value.text
        } else {
            id = "SWISSKNIFE_$annotatedFieldName"
        }


        MethodNode onSaveInstanceState = AnnotationUtils.getSaveStateMethod(declaringClass)

        String bundleName = onSaveInstanceState.parameters[0].name

        Statement insertStatement = null;

        if(AnnotationUtils.isSubtype(annotatedFieldClass, View.class)) {
            insertStatement = createViewSaveStateExpression(bundleName, id, annotatedFieldName);
        } else {
            String bundleMethod = getBundleMethod(annotatedField)

            insertStatement = createSaveStateExpression(bundleName, bundleMethod, id, annotatedFieldName);
        }

        List<Statement> statementsList = ((BlockStatement)onSaveInstanceState.getCode()).getStatements()
        statementsList.add(insertStatement)


        MethodNode restoreMethod = AnnotationUtils.getRestoreStateMethod(declaringClass)

        Statement statement = null;

        if(AnnotationUtils.isSubtype(annotatedFieldClass, View.class)) {
            statement = createViewRestoreStatement(annotatedField, id);
        } else {
            statement = createRestoreStatement(annotatedField, id);
        }

        List<Statement> statementList = ((BlockStatement) restoreMethod.getCode()).getStatements();
        statementList.add(statement)
    }

    private Statement createSaveStateExpression(String bundleName, String bundleMethod, String id, String annotatedFieldName){

        String method = "put$bundleMethod"


        ExpressionStatement statement = new AstBuilder().buildFromSpec {
            expression {
                methodCall {
                    variable bundleName
                    constant method
                    argumentList {
                        constant id
                        variable annotatedFieldName
                    }
                }
            }
        }[0]

    }

    private Statement createViewSaveStateExpression(String bundleName, String id, String annotatedFieldName){

        String method = "onSaveInstanceState"

        BlockStatement statement = new AstBuilder().buildFromSpec {
            block {
                expression {
                    declaration {
                        variable id
                        token "="
                        constant null
                    }
                }
                expression {
                    binary {
                        variable id
                        token "="
                        methodCall {
                            variable annotatedFieldName
                            constant method
                            argumentList {}
                        }
                    }
                }
                expression {
                    methodCall {
                        variable bundleName
                        constant "putParcelable"
                        argumentList {
                            constant id
                            variable id
                        }
                    }
                }
            }
        }[0]

        statement

    }

    private Statement createViewRestoreStatement(FieldNode annotatedField, String id) {

        BlockStatement statement = new AstBuilder().buildFromSpec {
            block {
                expression {
                    declaration {
                        variable id
                        token "="
                        constant null
                    }
                }
                expression {
                    binary {
                        variable id
                        token "="
                        methodCall {
                            variable "savedState"
                            constant "getParcelable"
                            argumentList {
                                constant id
                            }
                        }
                    }
                }
                expression {
                    methodCall {
                        variable annotatedField.name
                        constant "onRestoreInstanceState"
                        argumentList {
                            variable id
                        }
                    }
                }
            }
        }[0]

        statement

    }


    private Statement createRestoreStatement(FieldNode annotatedField, String id) {

        String bundleMethod = getBundleMethod(annotatedField)

        String getBundleMethod = "get$bundleMethod"

        BlockStatement statement =
                new AstBuilder().buildFromSpec {
                    block {
                        expression {
                            binary {
                                variable annotatedField.name
                                token "="
                                methodCall {
                                    variable "savedState"
                                    constant getBundleMethod
                                    argumentList {
                                        constant id
                                    }
                                }
                            }
                        }
                    }
                }[0]

        statement

    }

    private String getBundleMethod(FieldNode annotatedField){

        String method = null



        Class annotatedFieldClass = annotatedField.getType().getTypeClass()

        def isArray = annotatedField.getType().isArray()



        Class[] classes = [String.class, int.class, byte.class, char.class, double.class,
                           boolean.class, float.class, long.class, short.class, CharSequence.class,
                           Bundle.class]



        classes.each {
            if (it == annotatedFieldClass && method == null) method = it.name
        }


        if(method == null){

            ArrayList dummyAL = new ArrayList()

            if(annotatedFieldClass.isInstance(dummyAL)) method = "ArrayList"

            if(method == "ArrayList"){
                GenericsType[] generics = declaringClass.getDeclaredField(annotatedField.name).type.genericsTypes


                generics.each {
                    ClassNode genericClassNode = it.type

                    Class genericClass = genericClassNode.typeClass

                    if(method == "ArrayList"){

                        if(doesClassImplementInterface(genericClass, "android.os.Parcelable")) {

                            method = "Parcelable" + method

                        } else {

                            switch(genericClass.name){
                                case Integer.class.name:
                                    method = Integer.class.name+method
                                    break

                                case Boolean.class.name:
                                    method = Boolean.class.name+method
                                    break

                                case Byte.class.name:
                                    method = Byte.class.name+method
                                    break

                                case Character.class.name:
                                    method = Character.class.name+method
                                    break

                                case CharSequence.class.name:
                                    method = CharSequence.class.name+method
                                    break

                                case Double.class.name:
                                    method = Double.class.name+method
                                    break

                                case Float.class.name:
                                    method = Float.class.name+method
                                    break

                                case Long.class.name:
                                    method = Long.class.name+method
                                    break

                                case String.class.name:
                                    method = String.class.name+method
                                    break

                                case Short.class.name:
                                    method = Short.class.name+method
                                    break

                                default:
                                    break
                            }
                        }
                    }
                }
            }
        }


        if(method == null){

          if(doesClassImplementInterface(annotatedFieldClass, "android.os.Parcelable"))
              method = "Parcelable"
          else if (doesClassImplementInterface(annotatedFieldClass, "java.io.Serializable") && !isArray)
              method = "Serializable"

        }


        if(method != null) {

            if (Character.isLowerCase(method.charAt(0))) {
                char first = Character.toUpperCase(method.charAt(0))
                method = "$first" + method.substring(1)
            }


            if (method.contains(".")) {
                String[] splits = method.split("\\.")
                method = splits[splits.length - 1]
            }


            if (isArray && method != "Parcelable" && method != "Serializable") {
                if (method == "Int") {

                    method = "Integer"
                }
                method = method + "Array"
            }
        }

        if(method == null && isArray){
            String type = annotatedField.getType()
            type = type.substring(0, type.length()-2)
            if(type == "int")type = "Integer"
            if (Character.isLowerCase(type.charAt(0))) {

                char first = Character.toUpperCase(type.charAt(0))

                type = "$first" + type.substring(1)
            }
            method = type+"Array"
        }

        println(method)

        method

    }

    private boolean doesClassOverrideOnSave(ClassNode declaringClass){

        def methods = declaringClass.methods

        def overrides = false

        methods.each{
            if(!overrides) overrides = it.name.equalsIgnoreCase("onSaveInstanceState")
        }

        overrides

    }

    private boolean canImplementSaveState(ClassNode declaringClass, FieldNode annotatedField){

        def canImplement = false

        Class[] classes = [String.class, int.class, byte.class, char.class, double.class, boolean.class,
                           float.class, long.class, short.class, Integer.class, CharSequence.class,
                           Bundle.class]


        Class original = annotatedField.getType().getTypeClass();

        classes.each {
            if (it == original) canImplement = true
        }


        if(!canImplement){

            def containsGenerics = false

            ArrayList dummyAL = new ArrayList()

            if(original.isInstance(dummyAL)) containsGenerics = true

            if(containsGenerics){
                GenericsType[] generics = declaringClass.getDeclaredField(annotatedField.name).type.genericsTypes

                generics.each {
                    ClassNode genericClassNode = it.type

                    Class genericClass = genericClassNode.typeClass

                    // Here we don't check Serializable because Bundle does not support putSerializableArrayList
                    if(!canImplement) canImplement = doesClassImplementInterface(genericClass, "android.os.Parcelable")



                    if(!canImplement){

                        switch(genericClass.name){

                            case [Integer.class.name, Boolean.class.name, Byte.class.name,
                                  Character.class.name, CharSequence.class.name, Double.class.name,
                                  Float.class.name, Long.class.name, String.class.name,
                                  Short.class.name]:
                                canImplement = true
                                break
                            default:
                                canImplement = false
                                break

                        }
                    }
                }
            }

        }

        if(!canImplement) canImplement = doesClassImplementInterface(original, "android.os.Parcelable") ||
                doesClassImplementInterface(original, "java.io.Serializable")

        if(!canImplement) canImplement = AnnotationUtils.isSubtype(original, View.class)

        canImplement

    }

    private boolean doesClassImplementInterface(Class original, String desiredInterface){

        def interfaces = original.getInterfaces()

        def implementsInterface = false

        interfaces.each {
            if(it.getName().equalsIgnoreCase(desiredInterface)) implementsInterface = true
        }

        implementsInterface

    }


}