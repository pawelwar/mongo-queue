package wareq.mongoqueue

class SomeQueueableElement extends QueueElement {

    String someUniqueField;
    Integer someNumber;

    SomeQueueableElement(String someUniqueField, Integer someNumber) {
        super(someUniqueField)
        this.someUniqueField = someUniqueField
        this.someNumber = someNumber
    }

}