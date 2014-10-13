package edu.berkeley.path.lprm.lp.problem;

/**
 * Created by gomes on 10/9/2014.
 */
public class Constraint extends Linear {

    public enum State {violated,active,inactive};
    public enum Type {bound,other}

    protected Type type;
    private Relation relation = null;
    private double rhs = Double.NaN;

    public Constraint(){
        this.type = Type.other;
    }

    // set / add .............................................
    public void set_relation(Relation rel){
        this.relation = rel;
    }

    public void set_rhs(double rhs){
        this.rhs = rhs;
    }

    public void set_type(Type type){
        this.type = type;
    }

    // get  ..................................................

    public double get_rhs(){
        return rhs;
    }

    public Relation get_relation(){
        return relation;
    }

    public Double evaluate_lhs_minus_rhs(PointValue P){
        return evaluate_linear(P)-rhs;
    }

    public Constraint.State evaluate_state(PointValue P, double epsilon){
        double diff = evaluate_lhs_minus_rhs(P);
        if(Math.abs(diff)<epsilon)
            return Constraint.State.active;
        switch(relation){
            case EQ:
                return Constraint.State.violated;
            case GEQ:
                return diff>epsilon ? Constraint.State.inactive : Constraint.State.violated;
            case LEQ:
                return diff<-epsilon ? Constraint.State.inactive : Constraint.State.violated;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        String str = super.toString();
        if(relation!=null){
            switch(relation){
                case LEQ:
                    str += " <= ";
                    break;
                case EQ:
                    str += " = ";
                    break;
                case GEQ:
                    str += " >= ";
                    break;
            }

            if(!Double.isNaN(rhs))
                str += String.format("%f",rhs);
        }
        return str;
    }

}
