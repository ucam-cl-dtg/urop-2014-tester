package gitapidependencies;

public class RepositoryNotFoundException extends Exception {
    public RepositoryNotFoundException(String message) {
        super(message);
    }

    public RepositoryNotFoundException() {
        super();
    }
}
