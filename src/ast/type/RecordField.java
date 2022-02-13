package ast.type;

import ast.definition.VariableDefinition;

public class RecordField {

    private Type type;
    private String name;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
