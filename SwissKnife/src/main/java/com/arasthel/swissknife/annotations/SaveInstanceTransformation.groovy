package com.arasthel.swissknife.annotations

import android.os.Bundle
import com.arasthel.swissknife.utils.AnnotationUtils
import groovyjarjarasm.asm.Opcodes
import groovyjarjarasm.asm.commons.Method
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
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

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        AnnotationNode annotation = astNodes[0];
        FieldNode annotatedField = astNodes[1];

        ClassNode declaringClass = annotatedField.declaringClass

        Class annotatedFieldClass = annotatedField.getType().getTypeClass()

        String annotatedFieldName = annotatedField.name

        if(!canImplementSaveState(declaringClass, annotatedField)){
            throw new Exception("Annotated field must be able to be written to a Bundle object. Field: $annotatedFieldName .Type: $annotatedFieldClass.name");
        }


        def overrides = doesClassOverrideOnSave(declaringClass)

        if(overrides){

            //def methodList = declaringClass.getMethods("onSaveInstanceState")
            def methodList = declaringClass.getMethods("suma")
            println("***")
            methodList.each {
                println(it.parameters)
            }
        }












        MethodNode injectMethod = AnnotationUtils.getInjectViewsMethod(declaringClass);

        String id = null;

        if(annotation.members.size() > 0) {
            id = annotation.members.value.property.getValue();
        }

        if(id == null) {
            id = annotatedField.name;
        }

        Statement statement = createInjectStatement(annotatedField, id);

        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements();
        statementList.add(statement);

    }

    private Statement createInjectStatement(FieldNode field, String id) {

        BlockStatement statement =
                new AstBuilder().buildFromSpec {
                    block {
                        expression {
                            binary {
                                variable field.name
                                token "="
                                variable "v"
                            }
                        }
                    }
                }[0];

        statement.statements.add(0, AnnotationUtils.createInjectExpression(id));

        return statement;

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

                    if(!canImplement){
                        canImplement = doesClassImplementInterface(genericClass, "android.os.Parcelable") ||
                                doesClassImplementInterface(genericClass, "java.io.Serializable")
                    }


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