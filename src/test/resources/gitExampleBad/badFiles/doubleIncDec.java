package badFiles;

import java.util.LinkedHashMap;

public class doubleIncDec {
    private double x;
     public doubleIncDec(double x) {
        if(x==0) {
            x = 1l;
        }else{
        this. x =x;
        }
    }
    
    public void INC_X() {
        x ++;
    }
    
    public void dec_X() {
        //TODO
    }
    
    public void divBy(double k) throws Exception {
        if (! (k==0)) {
            x/=k ;
        }
        else {
            throw new Exception("Div by zero!");
        }
    }
 }