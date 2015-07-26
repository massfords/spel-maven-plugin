package com.massfords.maven.spel;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.MethodReference;
import org.springframework.expression.spel.ast.Operator;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;


/**
 * Validator for the expressions. Will eventually do more than just parse the
 * expression, but for now, that's all it does.
 *
 * Additional behavior in the works:
 * - validate method calls
 * - validate function calls
 *
 * @author markford
 */
public class ExpressionValidator {
    private final ExpressionParser parser = new SpelExpressionParser();

    public void validate(String expression, Class<?> expressionRoot) throws ExpressionValidationException, ParseException {
        SpelExpression exp = (SpelExpression) parser.parseExpression(expression);
        if (expressionRoot != null) {
            SpelNode node = exp.getAST();
            handle(node, expressionRoot);
        }
    }

    private void handle(SpelNode node, Class<?> expressionRoot) throws ExpressionValidationException{
        if (node instanceof MethodReference) {
            verify((MethodReference)node, expressionRoot);
        } else if (node instanceof Operator) {
            Operator operator = (Operator) node;
            handle(operator.getLeftOperand(), expressionRoot);
            handle(operator.getRightOperand(), expressionRoot);
        } else if (node != null) {
            for(int i=0; i<node.getChildCount(); i++) {
                SpelNode child = node.getChild(i);
                handle(child, expressionRoot);
            }
        }
    }

    private void verify(MethodReference node, Class<?> expressionRoot) throws ExpressionValidationException {
        String methodName = node.getName();
        int args = node.getChildCount();
        Method[] methods = expressionRoot.getDeclaredMethods();
        for(Method m : methods) {
            if (m.getName().equals(methodName) && args == m.getParameterCount()) {
                return;
            }
        }
        // if we get here, then we were unable to match the method call
        String pattern = "Unable to match method %s with %d params";
        throw new ExpressionValidationException(String.format(pattern, methodName, args));
    }
}
