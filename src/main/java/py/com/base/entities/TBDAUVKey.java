package py.com.base.entities;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class TBDAUVKey implements Serializable {

    private String op_rrnbepsa;
    private String op_fech_trans;
    private String op_audfcht;

    // Constructores, getters, setters, equals() y hashCode()

    public TBDAUVKey() {}

    public TBDAUVKey(String op_rrnbepsa, String op_fech_trans, String op_audfcht) {
        this.op_rrnbepsa = op_rrnbepsa;
        this.op_fech_trans = op_fech_trans;
        this.op_audfcht = op_audfcht;
    }

    public String getOp_rrnbepsa() {
        return op_rrnbepsa;
    }

    public void setOp_rrnbepsa(String op_rrnbepsa) {
        this.op_rrnbepsa = op_rrnbepsa;
    }

    public String getOp_fech_trans() {
        return op_fech_trans;
    }

    public void setOp_fech_trans(String op_fech_trans) {
        this.op_fech_trans = op_fech_trans;
    }

    public String getOp_audfcht() {
        return op_audfcht;
    }

    public void setOp_audfcht(String op_audfcht) {
        this.op_audfcht = op_audfcht;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TBDAUVKey that = (TBDAUVKey) o;
        return Objects.equals(op_rrnbepsa, that.op_rrnbepsa) &&
               Objects.equals(op_fech_trans, that.op_fech_trans) &&
               Objects.equals(op_audfcht, that.op_audfcht);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op_rrnbepsa, op_fech_trans, op_audfcht);
    }
}
