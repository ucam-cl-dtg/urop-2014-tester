package TestingHarness;

import java.util.LinkedList;
import java.util.Map;

public class TesterFactory {
	public Tester createNewTester(Map<String, LinkedList<String>> arg0)
	{
		return new Tester(arg0);
	}
}
