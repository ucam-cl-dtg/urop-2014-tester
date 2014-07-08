//test case for an empty catch block
public class EmptyBlockFollowingCatch
{
    public void someFunction()
    {
        try
        {
            String x;
        }
        catch (IOException ioe) 
        {
        	//shouldn't run
        }
        
        finally {
        	
        }
    }
}