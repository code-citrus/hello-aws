# Programming Interfaces
Every language specific AWS SDK provides a low-level interface for Amazon DynamoDB which wraps the HTTP REST API. A higher-level *Document Interface* is available that automatically infers data-types. Finally, an even higher *Object Persistence Interface* is available in Java/.NET.

## Error Handling
A 400 response indicates some problem with the request:
| Exception                      | R | Description                       |
| ------------------------------ | - | --------------------------------- |
| AccessDenied                   | N | incorrect request signining       |
| ConditionalCheckFailed         | N | ex. update expr. was false        |
| IncompleteSignature            | N | missing signature components      |
| ItemCollectionSizeLimitExcee.. | Y | 10GB limit on tables w. LSI       |
| LimitExceeded                  | Y | max 50 parallel control plane ops |
| MissingAuthnToken              | N | missing/malformed authz header    |
| ProvisionedThroughputExceede.. | A | exceeded provisioned throughput   |
| RequestLimitExceeded           | Y | TPS exceeded                      |
| ResourceInUse                  | N | ex. recreating existing table     |
| ResourceNotFound               | N | ex. table dne (or CREATING)       |
| Throttling                     | Y | request rate too high             |
| UnrecognizedClient             | Y | Invalid token / secret-key        |
| Validation                     | N | Some validation failed            |
In Java, you can use the `ClientConfiguration` class to configure retry logic for the SDK. By default, the SDK supports simple retries, exponential backoff with or without jitter (randomized delay).

NOTE: When using *BatchGetItem* and *BatchWriteItem* API support batch read/write operations. Under the hood, these simply call *GetItem* and *PutItem* once for each item. If some of these failed, the corres. tables/keys are returned in the `UnprocessedKeys` or `UnprocessedItems` fields.

## DynamoDB Mapper API

### save(obj, [DynamoDBMapperConfig config])
Upserts an item to the table. For updates, only annotated attributes are updated and other attributes of the item are left unaffected. However, if you specify `SaveBehavior.CLOBBER`, you can force the item to be completely over-written.

### load(obj, [DynamoDBMapperConfig config])
Retrieve item. The `obj` must at least have the primary-key filled in. To make a strongly-consistent read, use the `CONSISTENT` flag on config.

### query(clazz, [expression])
Query a table that uses a *composite-key*. You must provide a PK and a query filter applied to the sort key. Returns a *lazy-loaded* collection. Initially loads a single *page* of results and makes additionally API requests as necessary during iteration.

Example:
```
DynamoDBQueryExpression<MusicItem> query = new DynamoDBQueryExpression<>()
    .withKeyConditionExpression("id = :v1 and song = :v2)
    .withExpressionAttributeValues(/* Map<String,AttributeValue> */);
List<MusicItem> items = mapper.query(MusicItem.class, query);
```

You can also query a GSI, but your class must be annotated accordingly. The following will allow you to query items of a *specific* genre within some time range.
```
@DynamodDBTable(tableName="Music") // base-table
public class MusicItem {
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "artist")
    public String genre;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "ByYear-Index")
    public int year;
}
```

### queryPage
Returns only a single page of results (1MB).

### scan / scanPage
Scans an entire table or index. By default, returns a lazy-loaded collection as well.

### batchSave/batchLoad/batchDelete/batchWrite
Multiple items from one or more tables using as many *BatchWriteItems* or *BatchGetItems*. NOTE: Since these APIs don't support *update* operations, this method ignores the *SaveBehavior* and always behaves like `SaveBehavior.CLOBBER`.

## transactionWrite
Saves/Deletes multiple objects from one or more tables using a single call to *transactWriteItem*. 


## Optimistic Locking w. DynamoDBMapper
With optimistic locking, each item has an attribute that acts as a verion number. This attribute is marked with `@DynamoDbVersionAttribute` annotation in the corresponding POJO. If you retrieve an item from the table, update it, and and try to save it again, DynamoDBMapper will verify the version number on the server has not changed. If it has, it means someone else has modified the item before you did and the update fails. Otherwise, the AWS SDK will save the item with an incremented version number. Under the hood, this is implemented with conditional updates. NOTE: Transactional write operations do not support `@DynamoDBVersionAttribute` (exception is thrown).
