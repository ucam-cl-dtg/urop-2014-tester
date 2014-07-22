package report_elements;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProblemCategory {
    private String severity; //either "warning" or "error"
    private Map<String, List<FileItem>> fileDetails = new HashMap<>();

    public ProblemCategory(String severity) {
        this.severity = severity;
    }

    public void addDetail(String filename, int lineNumber, String detail) {
        if (!(fileDetails.containsKey(filename))) {
            fileDetails.put(filename, new LinkedList<FileItem>());
        }
        fileDetails.get(filename).add(new FileItem(lineNumber, detail));
    }
}
