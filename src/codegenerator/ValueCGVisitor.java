package codegenerator;


import ast.expression.ArrayAccess;
import ast.expression.FieldAccess;
import ast.expression.binary.Arithmetic;
import ast.expression.binary.Comparison;
import ast.expression.binary.Logical;
import ast.expression.unary.Cast;
import ast.expression.unary.Negation;
import ast.expression.unary.UnaryMinus;
import ast.expression.value.CharLiteral;
import ast.expression.value.DoubleLiteral;
import ast.expression.value.IntLiteral;
import ast.expression.value.Variable;
import ast.type.*;

public class ValueCGVisitor extends AbstractCGVisitor{

    private AddressCGVisitor addressCGVisitor;

    public ValueCGVisitor(CodeGenerator codeGenerator) {
        super(codeGenerator);
        this.addressCGVisitor = new AddressCGVisitor(codeGenerator, this);
    }

    /**
     * value[[Arithmetic : expression1 -> expression2 operator=('+'|'-'|'*'|'/'|'%') expression3 ]]() =
     *      value[[expression2]]()
     *      expression1.type.promote(expression2)
     *      value[[expression3]]()
     *      expression1.type.promote(expression3)
     *
     *      switch operator:
     *          case '+':
     *              <add>expression1.type.suffix()
     *          case '-':
     *              <sub>expression1.type.suffix()
     *          case '*':
     *              <mul>expression1.type.suffix()
     *          case '/':
     *              <div>expression1.type.suffix()
     *          case '%':
     *              <mod>expression1.type.suffix()
     */
    @Override
    public Void visit (Arithmetic arithmetic, Void param){
        arithmetic.getLeftExpression().accept(this, null);
        arithmetic.getType().promote(arithmetic.getLeftExpression(), codeGenerator);
        arithmetic.getRightExpression().accept(this, null);
        arithmetic.getType().promote(arithmetic.getRightExpression(), codeGenerator);

        switch (arithmetic.getOperator()){
            case "+":
                codeGenerator.add(arithmetic.getType());
                break;
            case "-":
                codeGenerator.sub(arithmetic.getType());
                break;
            case "*":
                codeGenerator.mul(arithmetic.getType());
                break;
            case "/":
                codeGenerator.div(arithmetic.getType());
                break;
            case "%":
                codeGenerator.mod(arithmetic.getType());
                break;
        }

        return null;
    }

    /**
     * value[[Logical : expression1 -> expression2 operator=('&&'|'||') expression3 ]]() =
     *      value[[expression2]]()
     *      value[[expression3]]()
     *
     *      switch operator:
     *          case '&&':
     *              <and>
     *          case '||':
     *              <or>
     */
    @Override
    public Void visit (Logical logical, Void param){
        logical.getLeftExpression().accept(this,null);
        logical.getLeftExpression().accept(this,null);

        switch (logical.getOperator()){
            case "&&":
                codeGenerator.and();
                break;
            case "||":
                codeGenerator.or();
                break;
        }

        return null;
    }

    /**
     * value[[Conditional : expression1 -> expression2 operator=('=='|'>'|'>='|'<'|'<='|'!=') expression3 ]]() =
     *      Type comparisonType = expression3.type;
     *
     *      if(!expression3.type.promotesTo(expression2.type, expression1) instanceof ErrorType)
     *          comparisonType = expression2.type;
     *
     *      value[[expression2]]()
     *      comparisonType.promote(expression2, codeGenerator);
     *
     *      value[[expression3]]()
     *      comparisonType.promote(expression3, codeGenerator);
     *
     *      switch operator:
     *          case '>':
     *              <gt>comparisonType.suffix()
     *          case '<':
     *              <lt>comparisonType.suffix()
     *          case '>=':
     *              <ge>comparisonType.suffix()
     *          case '<=':
     *              <le>comparisonType.suffix()
     *          case '==':
     *              <eq>comparisonType.suffix()
     *          case '!=':
     *              <ne>comparisonType.suffix()
     */
    @Override
    public Void visit (Comparison comparison, Void param){
        Type comparisonType = comparison.getRightExpression().getType();

        // Left operand type has preference
        if(!(comparison.getRightExpression().getType().promotesTo(comparison.getLeftExpression().getType(), comparison)
                instanceof ErrorType)){
            comparisonType = comparison.getLeftExpression().getType();
        }

        comparison.getLeftExpression().accept(this,null);
        comparisonType.promote(comparison.getRightExpression(), codeGenerator);

        comparison.getRightExpression().accept(this,null);
        comparisonType.promote(comparison.getLeftExpression(), codeGenerator);

        switch (comparison.getOperator()){
            case ">":
                codeGenerator.gt(comparisonType);
                break;
            case "<":
                codeGenerator.lt(comparisonType);
                break;
            case ">=":
                codeGenerator.ge(comparisonType);
                break;
            case "<=":
                codeGenerator.le(comparisonType);
                break;
            case "==":
                codeGenerator.eq(comparisonType);
                break;
            case "!=":
                codeGenerator.ne(comparisonType);
                break;
        }

        return null;
    }

    /**
     *  value[[Cast : expression1 -> type expression2 ]]() =
     *       value[[expression2]]()
     *       type.promote(expression2, codeGenerator)
     */
    @Override
    public Void visit (Cast cast, Void param){
        cast.getExpression().accept(this,null);
        cast.getCastType().promote(cast.getExpression(), codeGenerator);
        return null;
    }

    /**
     * value[[Negation : expression1 -> expression2]]() =
     *      value[[expression2]]()
     *      <not>
     */
    @Override
    public Void visit (Negation negation, Void param){
        negation.getExpression().accept(this, null);
        codeGenerator.not();
        return null;
    }

    /**
     * value[[UnaryMinus : expression1 -> expression2]]() =
     *      <pushi 0>
     *      value[[expression2]]()
     *      <sub> expression1.type.suffix()
     */
    @Override
    public Void visit (UnaryMinus unaryMinus, Void param){
        codeGenerator.push(0);
        unaryMinus.getExpression().accept(this, null);
        codeGenerator.sub(unaryMinus.getType());

        return null;
    }

    /**
     * value[[Variable : expression1 -> ID]]() =
     *      address[[expression1]]()
     *      <load>expression1.definition.type.suffix()
     */
    @Override
    public Void visit (Variable variable, Void param){
        variable.accept(this.addressCGVisitor, null);
        codeGenerator.load(variable.getDefinition().getType());
        return null;
    }

    /**
     * value[[IntLiteral : expression1 -> INT_CONSTANT]]() =
     *      <pushi> INT_CONSTANT
     */
    @Override
    public Void visit (IntLiteral intLiteral, Void param){
        codeGenerator.push(intLiteral.getValue());
        return null;
    }

    /**
     * value[[DoubleLiteral : expression1 -> DOUBLE_CONSTANT]]() =
     *      <pushf> DOUBLE_CONSTANT
     */
    @Override
    public Void visit (DoubleLiteral doubleLiteral, Void param){
        codeGenerator.push(doubleLiteral.getValue());
        return null;
    }

    /**
     * value[[CharLiteral : expression1 -> CHAR_CONSTANT]]() =
     *      <pushb> CHAR_CONSTANT
     */
    @Override
    public Void visit (CharLiteral charLiteral, Void param){
        codeGenerator.push(charLiteral.getValue());
        return null;
    }

    /**
     * value[[ArrayAccess : expression1 -> expression2 expression3 ]]() =
     *      address[[expression1]]()
     *      <load>expression2.type.suffix()
     */
    @Override
    public Void visit (ArrayAccess arrayAccess, Void param){
        arrayAccess.accept(this.addressCGVisitor, null);

        codeGenerator.load(((ArrayType)arrayAccess.getLeftExpression().getType()).getOfType());

        return null;
    }

    /**
     * value[[FieldAccess : expression1 -> expression2 ID ]]() =
     *      address[[expression1]]()
     *
     *      for (RecordField field : expression2.type.fields)
     *          if (field.name.equals(ID))
     *              <load>field.type.suffix()
     */
    @Override
    public Void visit (FieldAccess fieldAccess, Void param){
        fieldAccess.accept(this.addressCGVisitor, null);

        for (RecordField recordField : ((RecordType)fieldAccess.getExpression().getType()).getFields())
            if (recordField.getName().equals(fieldAccess.getFieldName()))
                codeGenerator.load(recordField.getType());

        return null;
    }

}
