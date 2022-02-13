package ast.type;

public class CharType extends AbstractType{

    private static CharType instance;

    private CharType(){
        super(0,0);
    }

    public static CharType getInstance(){
        if(instance == null){
            instance = new CharType();
        }
        return instance;
    }
}