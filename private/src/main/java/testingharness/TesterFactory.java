package testingharness;

import java.util.LinkedList;
import java.util.Map;

/** Used to generate Tester objects.
 * Enables mocking of Tester in TestService.runNewTest()
 * @author as2388
 */
public class TesterFactory {
    public Tester createNewTester(Map<XMLTestSettings, LinkedList<String>> testingQueue, String repoName)
    {
        return new Tester(testingQueue, repoName);
    }
}
