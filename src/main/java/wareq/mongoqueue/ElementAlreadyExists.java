package wareq.mongoqueue;

public class ElementAlreadyExists extends RuntimeException {

    private final String id;

    public ElementAlreadyExists(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
