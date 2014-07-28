package publicinterfaces;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/*
 * class stores details about where instances of the problem being looked for in the code appears (if anywhere)
 */

public class ProblemDetails {
	//hypothetical severity of the problem in case it is found in the code
    private Severity severity;
    //stores files where the corresponding problem was found (the string)  with the details describing where it is (the FileItem)
    //note this map is empty if all given java files pass the test for the problem being checked for
    private Map<String, List<FileItem>> fileDetails = new HashMap<>();

    protected ProblemDetails(Severity severity) {
        this.severity = severity;
    }

    /**
     * Adds a new occurrence of a detail in this problem category
     * @param file      name of file in which occurrence was found
     * @param lineNumber    line in that file where occurrence was found
     * @param detail        More specific problem description e.g. java.io.StreamReader, expected
     *                      12 spaces, found 16
     */
    protected void addDetail(String file, Integer lineNumber, String detail) {
    	//TODO: at the moment, .java extension is removed here because filename is the key of a map. This could cause
        //TODO: problems later, so something needs to be done about this...
    	String filename = getName(file);
        if (!(fileDetails.containsKey(filename))) {
            fileDetails.put(filename, new LinkedList<FileItem>());
        }
        fileDetails.get(filename).add(new FileItem(lineNumber, detail));
    }

    /**
     * If the severity is error and this category contains some problems then it should cause a failure
     * @return  true if the contents of this object mean the report should be classed as a FAIL, false otherwise
     */
    @JsonIgnore
    protected boolean causesFail() {
        return severity == Severity.ERROR && fileDetails.size() > 1;
    }

    public ProblemDetails() {}

    public Severity getSeverity() {
        return severity;
    }

    public Map<String, List<FileItem>> getFileDetails() {
        return fileDetails;
    }

    @JsonIgnore
    public String getName(String file) {
    	return file.substring(0,file.lastIndexOf(".") - 1);
    }
}
