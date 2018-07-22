# Mongo Queue  

Simple distributed persistence queue (based on MongoDB and Spring Data).

### Queueable element

Your class need to extend ```QueueElement```.  

```java
public class SomeQueueableElement extends QueueElement {

    private final String someUniqueField;
    private final Integer someNumber;

    public SomeQueueableElement(String someUniqueField, Integer someNumber) {
        super(someUniqueField);
        this.someUniqueField = someUniqueField;
        this.someNumber = someNumber;
    }
}
```

### Queue initialization

```java
MongoQueue<SomeQueueableElement> queue = new MongoQueue(
    mongoTemplate, 
    TEST_COLLECTION_NAME, 
    SomeQueueableElement.class
);
```

### Add new element to queue

```java
queue.add(someQueueableElement);
```

### Get element

```java
SomeQueueableElement lockedElement = queue.getAndLock()

... some processing ...

queue.remove(lockedElement)
```

### Other

Counting elements in queue
```java
queue.count()
```