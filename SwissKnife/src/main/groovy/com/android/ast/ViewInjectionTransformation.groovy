package com.android.ast
import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
/**
 * AST transformation for injecting Android Views
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class ViewInjectionTransformation extends AbstractASTTransformation {

    private final static ClassNode VIEWBYID = ClassHelper.make(ViewById)
    private final static ClassNode INJECTVIEWS = ClassHelper.make(InjectViews)
    private final static ClassNode BUNDLE_NODE = ClassHelper.make(Bundle)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (astNodes[1] instanceof ClassNode) {
            visitClass((ClassNode) astNodes[1])
        }
    }

    private void visitClass(ClassNode classNode) {
        final Expression valueExpr = classNode.getAnnotations(INJECTVIEWS)[0].getMember("value")
        if (AstUtils.inheritsFromClass(classNode, Activity)) {
            def method = classNode.methods.find { it.name == 'onCreate' }
            def statement = (BlockStatement) method.code
            int pos = AstUtils.findMethodCallPosition(statement, 'super', 'onCreate') + 1
            this.modifyActivityOnCreate(method, pos, valueExpr)
            this.injectViewComponents(classNode, method, statement, pos + 1)
        }
        if (AstUtils.inheritsFromClass(classNode, Fragment)) {
            def method = classNode.methods.find { it.name == 'onCreateView' }
            def statement = (BlockStatement) method.code
            int pos = AstUtils.findMethodCallPosition(statement, 'super', 'onCreateView') + 1
            this.modifyFragmentOnCreateView(method, valueExpr)
            this.injectViewComponents(classNode, method, statement, pos + 1)
        }
    }
    /**
     * {@inheritDoc }
     * @param classNode
     * @param method
     * @param body
     * @param position
     */
    private void injectViewComponents(ClassNode classNode, MethodNode method, BlockStatement body, int position) {
        /*BlockStatement toBeReloaded = new BlockStatement()
        BlockStatement toBeSaved = new BlockStatement()
        */
        boolean isToBeSaved = false
        def param = method.parameters.find { it.type == BUNDLE_NODE }
        def checkExpression = new BinaryExpression(new VariableExpression(param.name), Token.newSymbol('==', 0, 0), ConstantExpression.NULL)
        classNode.fields.each { field ->
            def ann = field.getAnnotations(VIEWBYID)
            ann.each {
                Expression idExpr = it.getMember('value')
                Expression save = it.getMember('save')
                def fvbid = new MethodCallExpression(new VariableExpression('this'), 'findViewById', idExpr)
                fvbid.implicitThis = true
                fvbid.sourcePosition = field
                def assign = new BinaryExpression(new VariableExpression(field),
                        Token.newSymbol(Types.EQUAL, -1, -1),
                        new CastExpression(field.getOriginType(), fvbid))
                assign.sourcePosition = field
                isToBeSaved = false
                if (save != null && save instanceof ConstantExpression) {
                    isToBeSaved = (Boolean) ((ConstantExpression) save).getValue()
                }
                body.statements.add(position++, new ExpressionStatement(assign))
                /*isToBeSaved ?
                        toBeSaved.statements.add(new ExpressionStatement(assign)) : toBeReloaded.statements.add(new ExpressionStatement(assign))*/

            }
        }
        /*if (toBeReloaded.statements.size() > 0 || toBeSaved.statements.size() > 0) {
            body.statements.add(position++, new IfStatement(new IfStatea(checkExpression), toBeSaved, toBeReloaded))
        }*/
    }

    private void modifyFragmentOnCreateView(MethodNode methodNode, Expression valueExpression) {
        if (methodNode.code instanceof BlockStatement) {
            BlockStatement blockStatement = methodNode.code as BlockStatement
            int returnPosition = AstUtils.findReturnPosition(blockStatement)
            // Remove existing return
            if (returnPosition >= 0) {
                blockStatement.statements.remove(returnPosition)
            }
            // Add return inflate in two parts : assignment (first expression) and return (last)
            blockStatement.statements.add(0, new ExpressionStatement(
                    new DeclarationExpression(new VariableExpression('groovyFragmentView'),
                            Token.newSymbol(Types.EQUAL, 0, 0), this.createFragmentInflateCall(methodNode, valueExpression)))
            )
            blockStatement.statements.add(new ReturnStatement(new VariableExpression('groovyFragmentView')))
        }
    }

    private Expression createFragmentInflateCall(MethodNode methodNode, Expression valueExpression) {
        Parameter[] params = methodNode.parameters
        String inflaterName = params[0].name
        String containerName = params[1].name
        return new MethodCallExpression(new VariableExpression(inflaterName, ClassHelper.make(LayoutInflater.class)), 'inflate',
                new ArgumentListExpression(valueExpression, new VariableExpression(containerName, ClassHelper.make(ViewGroup.class)), new ConstantExpression(false)))

    }

    private void modifyActivityOnCreate(MethodNode methodNode, int injectionPosition, Expression valueExpression) {
        if (methodNode.code instanceof BlockStatement) {
            BlockStatement blockStatement = (BlockStatement) methodNode.code
            blockStatement.statements.add(injectionPosition, new ExpressionStatement(this.createActivitySetContentViewCall(valueExpression)))
        }
    }

    private Expression createActivitySetContentViewCall(Expression valueExpression) {
        return new MethodCallExpression(new VariableExpression('this'), 'setContentView', valueExpression)
    }

}