//test case for exception being caught
public class SwitchDefaultNotLast
{
    public void someFun()
    {
    	int x;
        switch (x)
        {
            default:
                x++;
                break;
            case 0:
            	x--;
            	break;
        }
    }
}