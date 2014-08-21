package com.arasthel.swissknife.utils

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement;

/**
 * Created by Arasthel on 17/08/14.
 */
public class AnnotationUtils {

    public static MethodNode createInjectViewsMethod(ClassNode declaringClass) {
        Parameter[] parameters = [new Parameter(ClassHelper.make(Object.class), "view")];

        MethodNode injectMethod = declaringClass.getMethod("injectViews", parameters);
        if (injectMethod == null) {
            injectMethod = createInjectMethod();
            declaringClass.addMethod(injectMethod);
        }

        return injectMethod;
    }

    private static MethodNode createInjectMethod() {

        BlockStatement statement =
                new AstBuilder().buildFromSpec {
                    block {
                        expression {
                            declaration {
                                variable "v"
                                token "="
                                constant null
                            }
                        }
                    }
                }[0];

        Parameter[] parameters =  [new Parameter(ClassHelper.make(Object.class), "view")];

        MethodNode node = new MethodNode("injectViews",
                Opcodes.ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                parameters,
                null,
                statement);

        return node;
    }

    public static boolean isSubtype(Class original, Class compared) {
        while(original.name != compared.name) {
            original = original.getSuperclass();
            if(original == Object || original == null) {
                return false;
            }
        }
        return true;
    }
}
