package ast.type;

public class ArrayType extends AbstractType {

    private int size;
    private Type ofType;

    public ArrayType(int size, Type ofType, int line, int column) {
        super(line, column);
        this.size = size;
        this.ofType = ofType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Type getOfType() {
        return ofType;
    }

    public void setOfType(Type ofType) {
        this.ofType = ofType;
    }

    @Override
    public String toString(){
        return "[" + size + "]" + ofType.toString();
    }
}