 package badFiles;

import java.util.logging.Logger;
import badFiles.StringConstants;
import java.util.logging.Logger;

public class StringConstantsUser
{
    public static void main(String[] args)
    {         
        if (args[0]=="")
        {
            //do nothing
            Logger . getLogger("StringConstantsUserLogger") . log("Arg was empty");
        }
        else
        {
            try
            {
                System .out.println(new StringConstants().getCst(args[0]));
            }
            catch (Throwable e) 
            {
                
            }
        }
    }
}