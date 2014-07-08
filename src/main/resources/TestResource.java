public class TestResource {
	public void testFunction() {
		try {
			System.out.println("Hi");
			long x = 10L;
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
