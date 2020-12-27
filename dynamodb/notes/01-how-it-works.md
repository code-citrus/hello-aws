## How It Works
In DynamoDB you work with **items** in a specific **table** of a **database**. While items are mostly schemaless sets of **attributes**, every item must have a unique *primary-key* value. The primary-key is defined by the table, and consists of a *partition-key* that is hashed to determine the physical location of the item, and an optional *range-key* used to sort items with the same partition-key during storage.

When querying for items, you *must* specify an *exact* partition-key. You *may* provide additional conditional expressions for the *range-key*. To be able to query based on other fields you must enable *indexes* for that table.

There are two kinds of indexes. A *Global Secondary Index* creates an index that can have a different partition-key and sort-key. A *Local Secondary Index* must use the same partition-key. When creating indexes, you must specify what attributes to copy over into the new index. By default, the PK/RK of the *base-table* are automatically copied over. Querying an index is eventually consistent with its table (a strongly-consistent read is available for LSIs only).

Attributes are *typed* values. The follow types are available:
| Data Type  | Category | Description                  |
| ---------  | -------- | ---------------------------- |
| Number     | Scalar   | Up to 38 digits of precision |
| String     | Scalar   | UTF-8 Encoded.               |
| Binary     | Scalar   | Raw bytes                    |
| Boolean    | Scalar   | True/False                   |
| List       | Document | List of values (any type)    |
| Map        | Document | Key/Value set (any type)     |
| String-Set | Document | Set of unique strings        |
| Number-Set | Document | Set of unique numbers        |
| Binary-Set | Document | Set of unique binary vals    |

### Database Behavior
DynamoDB instances between AWS regions are independent and isolated. When you write to table, the data is replicated between AZs and is eventually-consistent within one second or less. By default, reads are also eventually-consistent, however, DDB does support strongly consistent reads that will return the most up-to-date data that reflects all prior successfull write operations. However, this comes at the cost of higher latency, lack of support on GSI, and higher cost.

### Provisioning Throughput (On-Demand Mode)
With on-demand provisioning, DDB will automatically provision your throughput to match demand. Pricing is based on *read/write request units*:<br/>
```
  1 RRU  = 1 strongly-consistent read up to 4KB/s.
         = 2 eventually-consistent read up to 4KB/s.
  2 RRU  = 1 transactional read up to 4KB/s.
  1 WRU  = 1 write up to 1KB/s
  2 WRU  = 1 transactional write up to 1KB/s
```
For example, you can do the following with 6 RRU and 6 WRU:
- Strongly-consistent reads upto 24KB/s (6 * 4KB)
- Eventually-consistent reads upto 48 KB/s
- Transactional reads upto 12 KB/s
- Write up to 6KB/s
- Read up to 3KB/s

### Scaling (On-Demand)
A newly created table starts out with 2000 WRU (8MB/s) and 6000 RRU (24 MB/s). On-Demand mode will *instantly* accomodate up to double this "previous peak." Thus, you can send up to 4000 write-requests
for small items (<1KB) without issue.

If you *sustain* more than 2000 WRU for some amount of time, DynamoDB will reset your "peak" to a higher value and allocation additional capacity. For example, suppose you sustain 3000 WRU. Then, your new peak will be 3000 that will allow bursts upt o 6K without throttling.

If you more than *double* the peak (ex 5K WRU) very quckly (in 30m),
DDB will still try to allocation additional capcaity but may encounter some throttling. For example, if you start out with 2K WRU but know you will need to ramp up to 5K, you will want to somehow "space out" the traffic so that you sustain like 4500 WRU
for ~30m (allowing DDB to scale up). Throttled requests fail with 400 status code. AWS SDKs have build-in support for retrying throttled requests.

NOTE: Unlike AutoScaling in Provisioned Mode, On-Demand mode has no upper limit.

### Scaling (Provisioned Mode)
In provisioned mode, you set the max WRU/RRU for a table/index explicitly. Exceeding this will result in throttled requests. You can, however, setup autocaling in response to CloudWatch alarms. Additionally, you can purchace *reserved capacity* in advance which allows for some cost savings.







