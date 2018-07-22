package wareq.mongoqueue;

import org.springframework.data.annotation.Id;

/**
 * Unfortunately it can not be a interface: https://jira.spring.io/browse/DATAES-351
 */
public abstract class QueueElement {

    @Id
    private final String id;

    protected QueueElement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
