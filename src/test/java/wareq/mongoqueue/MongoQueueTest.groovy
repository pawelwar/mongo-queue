package wareq.mongoqueue

import com.github.fakemongo.Fongo
import com.mongodb.MongoClient
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

import java.time.Duration

class MongoQueueTest extends Specification {

    def TEST_COLLECTION_NAME = 'testcollection'
    SomeQueueableElement SOME_TEST_ELEMENT = new SomeQueueableElement('argh', 123)

    MongoClient mongoClient = new Fongo('mongo server').mongo
    MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, 'testdb')
    SettableFixClock clock = new SettableFixClock()

    MongoQueue<SomeQueueableElement> mongoTaskQueue = new MongoQueue(mongoTemplate, TEST_COLLECTION_NAME, SomeQueueableElement.class, clock)

    def 'should add new element'() {
        when:
        mongoTaskQueue.add(SOME_TEST_ELEMENT)

        then: 'queue size should be one'
        mongoTaskQueue.count()

        and: 'we can peek this element '
        mongoTaskQueue.get().someUniqueField == SOME_TEST_ELEMENT.someUniqueField
    }

    def 'should not allow adding the same element two times'() {
        given:
        mongoTaskQueue.add(SOME_TEST_ELEMENT)

        when:
        mongoTaskQueue.add(SOME_TEST_ELEMENT)

        then: 'queue size should be one'
        ElementAlreadyExists exception = thrown(ElementAlreadyExists)
        exception.getId() == SOME_TEST_ELEMENT.someUniqueField
    }

    def 'should not take locked element'() {
        given:
        mongoTaskQueue.add(SOME_TEST_ELEMENT)
        mongoTaskQueue.getAndLock()

        expect:
        mongoTaskQueue.get() == null
        mongoTaskQueue.getAndLock() == null
    }

    def 'when lock expires document should be re-taken'() {
        given: 'there is single element in queue'
        mongoTaskQueue.add(SOME_TEST_ELEMENT)

        and: 'this element is locked for 5 minutes'
        mongoTaskQueue.getAndLock(Duration.ofMinutes(5))

        when: '4 minutes and 59 seconds has passed'
        clock.moveForward(Duration.ofMinutes(4).plusSeconds(59))

        then: 'element should still be locked'
        mongoTaskQueue.get() == null
        mongoTaskQueue.getAndLock() == null

        when: '1 second later lock should expire'
        clock.moveForward(Duration.ofSeconds(1))

        then: 'we should re-take element'
        mongoTaskQueue.get() != null
        mongoTaskQueue.getAndLock() != null
    }

    def 'removed element should not be available any more'() {
        given:
        mongoTaskQueue.add(SOME_TEST_ELEMENT)

        when:
        SomeQueueableElement lockedElementInQueue = mongoTaskQueue.getAndLock()
        mongoTaskQueue.remove(lockedElementInQueue)

        then:
        mongoTaskQueue.count() == 0
    }

    def cleanup() {
        mongoTemplate.dropCollection(TEST_COLLECTION_NAME)
    }
}
