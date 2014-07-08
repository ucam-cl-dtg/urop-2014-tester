import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class CheckStyleConfigTester {
	
	@Test
	public void testIndentation() throws IOException 
	{
		runTest("IndentationBadTab",        "indentation level", true,  "Should report badly indented code if indented too far with tab");
		runTest("IndentationBadSpace",      "indentation level", true,  "Should report badly indented code if indented too far with space");
		runTest("IndentationBadRightBrace", "indentation level", true,  "Should report badly indented close braces");
		runTest("IndentationBadLeftBrace",  "indentation level", true,  "Should report badly indented open braces");
		runTest("IndentationExpected",      "indentation level", false, "Should not report any indentation issues");
	}
	
	@Test
	public void testUnusedImport() throws IOException
	{
		runTest("RedundantImportsRepeated", "Duplicate import",   true,  "Should report presence of duplicate import");
		runTest("UnnecessaryImport",        "Unnecessary import", true,  "Should report presence of unncessary java.lang import");
		runTest("UnusedImportsFail",        "Unused import",      true,  "Should report presence of unused import");
		runTest("UnusedImportPass",         "import",             false, "Should not report presence of unused import");
	}
	
	@Test
	public void testTODOFIXME() throws IOException
	{
		runTest("FIXMEInShortImmediateComment", "TODO or FIXME found in comment", true,  "Should report presence of //fixme");
		runTest("TODOInShortComment",           "TODO or FIXME found in comment", true,  "Should report presence of // ... TODO");
		runTest("TODOInLongComment",            "TODO or FIXME found in comment", true,  "Should report presence of /* ... TODO");
		runTest("TODOInLongMultilineComment",   "TODO or FIXME found in comment", true,  "Should report presence of /* ... \n ... TODO");
		//TODO:
		//runTest("TODOInString",   "TODO or FIXME found in comment", false,  "Should not report presence of TODO in string");
		runTest("TODOInVarName",                "TODO or FIXME found in comment", false, "Should not report presence of TODO in a variable name");
	}
	
	@Test
	public void testEmptyBlocks() throws IOException
	{
		runTest("EmptyCatch",               "Empty", true,  "Should report empty catch block");
		runTest("EmptyBlockFollowingCatch", "Empty", true, "Should not report empty catch block");
		runTest("EmptyCommentedBlock",      "Empty", false, "Should not report an empty block");
		runTest("EmptyMethod",              "Empty", false, "Should not report empty function definition");
		runTest("EmptyException",           "Empty", false, "Should not report empty exception block");
		runTest("CatchInNameTest",          "Empty", false, "Should not report empty block");
	}
	
	@Test
	public void testWhitespaceAroundOperators() throws IOException
	{
		runTest("TooLittleWhitespaceBeforeEquals", "should be preceded with whitespace",                               true,  "Should report not enough whitespace before '='");
		runTest("TooLittleWhitespaceAfterEquals",  "should be followed by whitespace",                                 true,  "Should report not enough whitespace after '='");
		runTest("TooMuchWhitespaceBeforeEquals",   "Multiple spaces",                                                   true,  "Should report too much whitespace before '='");
		runTest("TooMuchWhitespaceAfterEquals",    "Multiple spaces",                                                  true,  "Should report too much whitespace after '='");
		runTest("TabBeforeEquals",                 "tabs should only be used as whitespace at the beginning of lines", true,  "Should report wrong whitespace before '='");
		runTest("NoWhitespacePlusPlus",            "should be preceded with whitespace",                               false, "Should not report not enough whitespace before ++");
		runTest("NoWhitespacePlusPlus",            "not should be followed by whitespace",                             false, "Should not report not enough whitespace after ++");
		runTest("ExpectedWhitespacePlusEquals",    "not should be preceded with whitespace",                           false, "Should not report error with +=");
	}
	
	@Test
	public void testNoWhitespaceAround() throws IOException
	{
		runTest("WhitespaceBeforeColon",         "Preceding whitespace",  true,  "Should report whitespace should not be used before a colon");
		runTest("WhitespaceFollowingLogicalNot", "Succeeding whitespace", true,  "Should report that logical not should not be followed by whitespace");
	}
	
	@Test
	public void testStringComparisons() throws IOException
	{
		runTest("StringEqualityEE",        "String comparison with", true,  "Should report strings should not be tested with ==");
		runTest("StringEqualityNE",        "String comparison with", true,  "Should report strings should not be tested with !=");
		runTest("StringEqualityDotEquals", "String comparison with", false, "Comparing strings with .equals() is fine, so shouldn't report a problem");
	}
	
	@Test
	public void testNamingConventions() throws Exception
	{
		
	}
	
	@Test
	public void testUpperCaseL() throws IOException
	{
		runTest("LongUpperL", "Lower case long assignment", false, "Should not report anything");
		runTest("LongLowerL", "Lower case long assignment", true,  "Should report that upper case L should be used");
	}
	
	//worker function for running tests
	public void runTest(String testCase, String desiredString, boolean desiredResponse, String message) throws IOException
	{
		//set up the arguments for the StaticAnalyser
		final String dir = System.getProperty("user.dir");
		String[] analyserArgs = new String[3];
		analyserArgs[0] = "-c" + dir + "\\src\\main\\resources\\CheckstyleFormat.xml"; //location of xml config file
		analyserArgs[1] = "-o" + dir + "\\src\\main\\resources\\CheckstyleOutput.txt"; //location of output file
		analyserArgs[2] = dir + "\\src\\test\\resources\\" + testCase + ".java"; 	   //location of test case
		
		//Run the static analyser
		StaticAnalyser.main(analyserArgs);
		
		//Read StaticAnasyser's output file line by line, searching for the desired string
		BufferedReader br = new BufferedReader(new FileReader(dir + "\\src\\main\\resources\\CheckstyleOutput.txt"));
		boolean expectedFound = false;		//was a string containing the substring we were looking for found?
		boolean unexpectedFound = false;	//was a string not containing the desired substring found? In which case, checkstyle is giving an unexpected error
		String line;
		while((line = br.readLine()) != null){
			if (line.equals("Starting audit...") || line.equals("Audit done."))
			{
				
			}
			else
			{
				System.out.println(line);
				if(line.contains(desiredString))
				{
					expectedFound = true;
				}
				else
				{
					unexpectedFound = true;
				}
			}
		}
	
		br.close();
		
		//Run JUnit assert
		if (!unexpectedFound)
		{
			assertEquals(message, desiredResponse, expectedFound);
		}
		else
		{
			assertEquals("Unexpected message found", false, true);
		}
	}
}

