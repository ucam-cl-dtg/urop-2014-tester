import java.io;
import java.awt;

public class TestResource {
	public void testFunction() {
		try {
			System.out.println("Hi");
			long x = 10l;
		} 
		catch (IOException e) {
			//comment!
		}
		try {
			System.out.println("Hi");
		} 
		catch (IOException e) {
			//comment!
		}
	}
}
