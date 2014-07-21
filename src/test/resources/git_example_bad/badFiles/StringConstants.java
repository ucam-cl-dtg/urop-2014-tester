package badFiles;

import java.lang.String;

public abstract class StringConstants extends String {
    private static final String cst0 = "Hello World!";
    private static final String cst1 = "Test Constant 1";
    private static final String cst2 = "Test Constant 2";
    
    public getCst(int index) throws Throwable {
       switch (index) {
            default:
                throw new Throwable();
            case 0:
                return cst0;
            case 1:
                return cst1;
            case 2:
                return cst2;
       }
    }
}