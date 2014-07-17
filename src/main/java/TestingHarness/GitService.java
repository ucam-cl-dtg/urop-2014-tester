package TestingHarness;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.ws.rs.core.Response;

public class GitService implements WebInterface {

	public Response listRepositories() {
		// TODO Auto-generated method stub
		return null;
	}

	public LinkedList<String> listFiles(String repoName) throws IOException {
		LinkedList<String> files = new LinkedList<String>();
		//add files here!
		return files;
	}

	public Response getFile(String fileName, String repoName)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(/* add path here! */ fileName));
		String output = "";
		String line;
		while ((line = br.readLine()) != null) {
			output += line + "\n";
		}
		br.close();
		return Response.status(200).entity(output).build();
	}

}
