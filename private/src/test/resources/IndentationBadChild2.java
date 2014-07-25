//tests for poor indentation of the child of a block e.g. in this case, an if statemant,
//the child of the function, is poorly indented
public class IndentationBadChild2 {
    public void wellIndentedFunction() 
    {
        if (true) {
           int x = 4;
        }
    }
}