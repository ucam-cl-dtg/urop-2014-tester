package TestingHarness;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

/**
 * Used to sort a collection of strings ending with .java and .xml
 * so that .java files come first
 * @author as2388
 */
public class FileTypeComparator implements Comparator<String> {    
   Map<String, LinkedList<String>> base;
   public FileTypeComparator(Map<String, LinkedList<String>> base)
   {
       this.base = base;
   }
    
    @Override
    public int compare(String a, String b)
    {
        String typeA = getFileExt(a);
        String typeB = getFileExt(b);
        
        if (typeA.equals("java") && typeB.equals("xml"))
        {
            return 1;
        }
        else if (typeA.equals("xml") && typeB.equals("java"))
        {
            return -1;
        }
        
        //keys are equal so return 1
        //(returning 0 would merge keys)
        return 1;
    }
    
    public String getFileExt(String fileName)
    {
        return FilenameUtils.getExtension(fileName);
    }
}