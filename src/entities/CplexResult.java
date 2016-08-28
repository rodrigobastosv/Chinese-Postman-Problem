package entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class CplexResult implements Serializable {
    private int objectiveFunctionValue;
    private List<CplexResultVariable> variables;

    public CplexResult(int objectiveFunctionValue, List<CplexResultVariable> variables) {
        this.objectiveFunctionValue = objectiveFunctionValue;
        this.variables = variables;
    }

    public int getObjectiveFunctionValue() {
        return objectiveFunctionValue;
    }

    public List<CplexResultVariable> getVariables() {
        return variables;
    }

    public void setObjectiveFunctionValue(int objectiveFunctionValue) {
        this.objectiveFunctionValue = objectiveFunctionValue;
    }

    public void setVariables(List<CplexResultVariable> variables) {
        this.variables = variables;
    }
    
    public Object clone() {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bout);
            out.writeObject(this);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            in = new ObjectInputStream(bin);
            Object copy = in.readObject();
            in.close();
            return copy;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignore) {
            }
        }
        return null;
    }
}