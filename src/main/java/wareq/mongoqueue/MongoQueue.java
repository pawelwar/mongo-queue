package wareq.mongoqueue;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.lang.Nullable;

import java.time.Clock;
import java.time.Duration;

public class MongoQueue<T extends QueueElement> {

    private final static String LOCK_FIELD_NAME = "lockedUntil";
    private final static Duration DEFAULT_LOCK_DURATION = Duration.ofMinutes(5);

    private final MongoTemplate mongoTemplate;
    private final String collectionName;
    private final Class<T> clazz;
    private final Clock clock;

    MongoQueue(MongoTemplate mongoTemplate, String collectionName, Class<T> clazz, Clock clock) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.clazz = clazz;
        this.clock = clock;
    }

    public MongoQueue(MongoTemplate mongoTemplate, String collectionName, Class<T> clazz) {
        this(mongoTemplate, collectionName, clazz, Clock.systemUTC());
    }

    public void add(T element) {
        try {
            mongoTemplate.insert(element, collectionName);
        } catch (DuplicateKeyException dke) {
            throw new ElementAlreadyExists(element.getId());
        }
    }

    @Nullable
    public T get() {
        return this.mongoTemplate.findOne(notLocked(), clazz, collectionName);
    }

    @Nullable
    public T getAndLock() {
        return getAndLock(DEFAULT_LOCK_DURATION);
    }

    @Nullable
    public T getAndLock(Duration lockDuration) {
        Update update = Update.update(LOCK_FIELD_NAME, clock.instant().plus(lockDuration));
        return this.mongoTemplate.findAndModify(notLocked(), update, clazz, collectionName);
    }

    public long count() {
        return mongoTemplate.count(new Query(), collectionName);
    }

    public void remove(T element) {
        this.mongoTemplate.remove(element, collectionName);
    }

    private Query notLocked() {
        Criteria withoutLock = Criteria.where(LOCK_FIELD_NAME).exists(false);
        Criteria lockExpired = Criteria.where(LOCK_FIELD_NAME).lte(clock.instant());
        return Query.query(new Criteria().orOperator(withoutLock, lockExpired));
    }
}