import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import org.junit.Test;

import TestingHarness.FileTypeComparator;
import TestingHarness.Tester;

public class FileTypeComparatorValidatorTester {
    
    //TEST SORTER
    @Test
    public void testComparatorJavaFirst()
    {
        assertEquals(1, new FileTypeComparator(null).compare("path/file.java", "path/config.xml"));
        assertEquals(1, new FileTypeComparator(null).compare("path1/file1.java", "path2/file2.java"));
        assertEquals(1, new FileTypeComparator(null).compare("path1/file1.xml", "path2/file2.xml"));
        assertEquals(-1, new FileTypeComparator(null).compare("path/config.xml", "path/file.java"));
    }   
    
    //TEST SORTED ASSERTER
    //(kind of redundant now that testComparatorJavaFirst() has been added, but just in case may as well
    //  just leave it in)
    @Test
    public void testValidatorNormal()
    {
        String[] paths = {"Bfile1.java", "Zfile2.java", "Qfile3.java", "Aconfig1.xml", "YConfig.xml"};
        runTest(true, paths);
    }
    
    @Test
    public void testValidatorOnlyJava()
    {
        String[] paths = {"Bfile1.java", "Zfile2.java", "Qfile3.java"};
        runTest(true, paths);
    }
    
    @Test
    public void testValidatorOnlyXML()
    {
        String[] paths = {"Aconfig1.xml", "YConfig.xml"};
        runTest(true, paths);
    }
    
    @Test
    public void testValidatorInvalid()
    {
        String[] paths = {"Aconfig1.xml", "Bfile1.java", "Zfile2.java", "Qfile3.java", "YConfig.xml"};
        runTest(false, paths);
    }
    
    private void runTest(boolean expected, String[] paths)
    {
        assertEquals(expected, new Tester(new TreeMap<String, LinkedList<String>>(), null).validateTestingQueueIsSorted(testCaseBuilder(paths)));
    }
    
    private LinkedHashMap<String, LinkedList<String>> testCaseBuilder(String[] paths)
    {
        //using a linked hash map for this ensures that the items in the map stay in their inserted order
        LinkedHashMap<String, LinkedList<String>> map = new LinkedHashMap<String, LinkedList<String>>();
        for (int i = 0; i < paths.length; i++)
        {
            map.put(paths[i], new LinkedList<String>());
        }
        return map;
    }
}
