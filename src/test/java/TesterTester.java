import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

import testingharness.Tester;


public class TesterTester {
    @Test
    public void testGetTestItemsJavaNormal()
    {
        String[] inputPaths = {"Aconfig1.xml", "Bfile1.java", "Zfile2.java", "Qfile3.java", "YConfig.xml"};
        Map<String, LinkedList<String>> inputMap = testCaseBuilder(inputPaths);
        
        String[] expectedOutputPaths = {"Bfile1.java", "Zfile2.java", "Qfile3.java"};
        Map<String, LinkedList<String>> expectedOutputMap = testCaseBuilder(expectedOutputPaths);
        
        Tester t = new Tester(inputMap, null);      
        
        assertEquals(expectedOutputMap, t.getDynamicTestItems(inputMap));
    }
    
    @Test
    public void testGetTestItemsJavaEmpty()
    {
        String[] inputPaths = {"Aconfig1.xml", "YConfig.xml"};
        Map<String, LinkedList<String>> inputMap = testCaseBuilder(inputPaths);
        
        String[] expectedOutputPaths = {};
        Map<String, LinkedList<String>> expectedOutputMap = testCaseBuilder(expectedOutputPaths);
        
        Tester t = new Tester(inputMap, null);      
        
        assertEquals(expectedOutputMap, t.getDynamicTestItems(inputMap));
    }
    
    @Test
    public void testGetTestItemsXMLNormal()
    {
        String[] inputPaths = {"Aconfig1.xml", "Bfile1.java", "Zfile2.java", "Qfile3.java", "YConfig.xml"};
        Map<String, LinkedList<String>> inputMap = testCaseBuilder(inputPaths);
        
        String[] expectedOutputPaths = {"Aconfig1.xml", "YConfig.xml"};
        Map<String, LinkedList<String>> expectedOutputMap = testCaseBuilder(expectedOutputPaths);
        
        Tester t = new Tester(inputMap, null);      
        
        assertEquals(expectedOutputMap, t.getStaticTestItems(inputMap));
    }
    
    @Test
    public void testGetTestItemsXMLEmpty()
    {
        String[] inputPaths = {"Bfile1.java", "Zfile2.java", "Qfile3.java"};
        Map<String, LinkedList<String>> inputMap = testCaseBuilder(inputPaths);
        
        String[] expectedOutputPaths = {};
        Map<String, LinkedList<String>> expectedOutputMap = testCaseBuilder(expectedOutputPaths);
        
        Tester t = new Tester(inputMap, null);      
        
        assertEquals(expectedOutputMap, t.getStaticTestItems(inputMap));
    }
    
    @Test
    public void testEmpty()
    {
        String[] inputPaths = {};
        Map<String, LinkedList<String>> inputMap = testCaseBuilder(inputPaths);
        
        String[] expectedOutputPaths = {};
        Map<String, LinkedList<String>> expectedOutputMap = testCaseBuilder(expectedOutputPaths);
        
        Tester t = new Tester(inputMap, null);      
        
        assertEquals(expectedOutputMap, t.getDynamicTestItems(inputMap));
        assertEquals(expectedOutputMap, t.getStaticTestItems(inputMap));
    }
    
    private HashMap<String, LinkedList<String>> testCaseBuilder(String[] paths)
    {
        //using a linked hash map for this ensures that the items in the map stay in their inserted order
        HashMap<String, LinkedList<String>> map = new HashMap<String, LinkedList<String>>();
        for (int i = 0; i < paths.length; i++)
        {
            map.put(paths[i], new LinkedList<String>());
        }
        return map;
    }
}
