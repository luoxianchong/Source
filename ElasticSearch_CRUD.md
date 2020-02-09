## ElasticSearch CRUD(增删改查)

#### 概述

Elasticsearch是一个开源的分布式、RESTful 风格的搜索和数据分析引擎，它的底层是开源库Apache Lucene。

##### 索引（Index）

  Elasticsearch 数据管理的顶层单位就叫做 Index（索引），每个Index的名字必须是小写，相当于关系型数据库里的数据库的概念。

##### 文档（Document）

  Index里面单条的记录称为 Document（文档）。许多条 Document 构成了一个 Index。Document 使用 JSON 格式表示。同一个 Index 里面的 Document，不要求有相同的结构（scheme），但是最好保持相同，这样有利于提高搜索效率。

##### 类型（Type）

  Document 可以分组，比如employee这个 Index 里面，可以按部门分组，也可以按职级分组。这种分组就叫做 Type，它是虚拟的逻辑分组，用来过滤 Document，类似关系型数据库中的数据表。
  不同的 Type 应该有相似的结构（Schema），性质完全不同的数据（比如 products 和 logs）应该存成两个 Index，而不是一个 Index 里面的两个 Type（虽然可以做到）。

##### 文档元数据（Document metadata）

  文档元数据为_index, _type, _id, 这三者可以唯一表示一个文档，_index表示文档在哪存放，_type表示文档的对象类别，_id为文档的唯一标识。

##### 字段（Fields）

  每个Document都类似一个JSON结构，它包含了许多字段，每个字段都有其对应的值，多个字段组成了一个 Document，可以类比关系型数据库数据表中的字段。
  在 Elasticsearch 中，文档（Document）归属于一种类型（Type），而这些类型存在于索引（Index）中，下图展示了Elasticsearch与传统关系型数据库的类比：

​	relational DB       databases     tables     rows                   columns

​	elasticSearch       indices           types      documents      fields



#### CRUL

curl**是一个利用URL语法在命令行下工作的文件传输工具

-X 指定http 请求方式，比如：GET、PUT、POST、DELETE、HEAD等

-d 传输的数据内容

-H 请求头信息



ElasticSearch 服务的基本信息：

http://localhost:9200/_cat

**1. 查看集群的健康状况**

http://127.0.0.1:9200/_cat/health?v

```说明：v是用来要求在结果中返回表头```

状态值说明
Green - everything is good (cluster is fully functional)，即最佳状态
Yellow - all data is available but some replicas are not yet allocated (cluster is fully functional)，即数据和集群可用，但是集群的备份有的是坏的
Red - some data is not available for whatever reason (cluster is partially functional)，即数据和集群都不可用



**2. 查看所有索引**
http://localhost:9200/_cat/indices?v



#### PUT 和 POST

POST不用加具体的id，它是作用在一个集合资源之上的（/uri），而PUT操作是作用在一个具体资源之上的（/uri/xxx）,PUT是幂等的。

##### 创建一个索引

```js
curl  -H 'Content-type:application/json;charset=utf-8'  -XPUT 'http://ing.org:9200/order?pretty' 
```

```说明：'?pretty' =》 美化输出的json格式```

创建一个指定属性的索引

```js
curl  -H 'Content-type:application/json;charset=utf-8'  -XPUT 'http://ing.org:9200/task?pretty'  -d 
'{
    "settings" : {
        "index" : {
            "number_of_shards" : 3,
            "number_of_replicas" : 2
        }
    }
}'
```

说明：

设置索引的分片数为3，备份数为2。

默认的分片数是5到1024
默认的备份数是1
索引的名称必须是小写的，不可重名



##### 创建一个文档（document）

```js
curl -H 'Content-type:application/json;charset=utf-8' -XPOST "http://ing.org:9200/order/primary/1?pretty" -d  '{"order_code" : "XD78946521", "payment" : 28,"buyer":"张三","recevier":"李四","address":"xxxxxxxxxxx"}'
```



##### 创建mapping映射

注意：在ES中创建一个mapping映射类似于在数据库中定义表结构，即表里面有哪些字段、字段是什么类型、字段的默认值等；也类似于solr里面的模式schema的定义

> curl -H 'Content-type:application/json;charset=utf-8' -XPUT "http://ing.org:9200/twitter/primary/1?pretty" -d 
>
> '{
>   "settings" : {
>     "index" : {
>       "number_of_shards" : 1,
>       "number_of_replicas" : 1
>     }
>   },
>   "mappings" : {
>     "properties" : {
>       "article_title" : {
>         "field1" : { "type" : "text" }
>       }
>     }
>   }
> }'



#####  **Get Index 查看索引的定义信息**

​	GET /twitter，可以一次获取多个索引（以逗号间隔） 获取所有索引 _all 或 用通配符*

​	GET /twitter/_settings

​	GET /twitter/_mapping



##### 判断索引是否存在

HEAD twitter
HTTP status code 表示结果 404 不存在 ， 200 存在





### 搜索

#### 空搜索

直接使用

```
GET /_search
```

响应内容中有 `{took : times , hits : { total : n , hits : [ … ] }}`
Took表示本次请求花费的毫秒，hits表示匹配到的文档。文档中包含一个_score评分

```
{    	"hits" : {		"total" :14,		"hits" : [				{					"_index":"us",					"_type":"tweet",					"_id":"7",					"_score":1,					"_source": {								"date":	"2014-09-17",								"name":	"John Smith",									"tweet":"The Query DSL is really powerful and flexible",								"user_id": 2								}        				},				... 9 RESULTS REMOVED ...       		],       		"max_score" :   1   	},    	"took" :4,    	"_shards" : {       		"failed" :0,       		"successful" :10,       		"total" :10   	},	"timed_out" :false}
```

评分为relevance score，在提供了查询条件的情况下，文档按照相关性评分降序排列。
_shards表示参数查询的分片数。
time_out表示是否超时，如果要求响应速度，可以限制time_out，这样ES会在请求超时前返回收集到的结果

```
GET /_search?timeout=10ms
```

#### 限定搜索

```
/index[,index]/_search 限制在几个索引间搜索数据
```

也可以使用通配符搜索。/g*/_search，以g开头的index中搜索。

```
/index[,index]/type[,type]/_search 在index索引中，搜索type中的文档。
```

#### 分页

```
GET /_search?size=5&from=10
```

#### 简单搜索

```
GET /index/type/search?q=key:value
```

表示搜索key字段包含value的文档

```
+k1:(v1 v3) -k2:v2 +date:>2014-09-10
```

表示必须含有k1:v1,v3和必须没有k2:v2，时间大于给定时间的文档

```
GET /_search?q=value
```

表示含有value的文档。因为ES会将所有的字段拼接起来，形成一个_all字段，最终被ES索引。

一般搜索结构：

```
{	QUERY_NAME: {		FIELD_NAME: {			ARGUMENT: VALUE,			ARGUMENT: VALUE,			...		}	} }
```



#### 过滤

- term : `"term":{ k:v }` 主要用于精确匹配，包括bool，日期，数字，未经分析的字符串。

- terms : `"terms":{ k:[v1,v2] }` 可以用于精确匹配多个值。

- range : `"range": { k:{"lte":20,"gte":30} }` 可以用于指定范围查找数据，范围操作符包含:gt,gte,lt,lte

- exists|missing : `"exists":{ "field":"k" }` 这两个过滤语句仅用于已经找到文档后的过滤

- bool过滤 :

  ```
  "bool": { 	"must": { 		"term": { "folder": "inbox" }		},	"should": [{ … },{ … }] }
  ```

  可以用来合并多个过滤条件查询结果的布尔逻辑。
  must表示多个结果的完全匹配，must_not表示多个结果的完全匹配的否定，should表示至少有一个查询条件匹配

#### 查询

- match_all : `"match_all": {}` 表示匹配所有文档

- match : match是一个标准查询

- multi_match : `"multi_match":{ k:v, k:[v,v] }` 允许一次查询多个字段

- bool查询 :

  ```
  "bool": { 	"must": { 		"match": { "title": "how to make millions" }	} }
  ```

  bool查询相比bool过滤多了一部查询_score的步骤

#### 组合查询

过滤和查询需要放置在对应的context当中，过滤对应filter，查询对应query
由于search API中只能使用query语句，所以多重查询中需要使用filtered来包含query和其他过滤语句。
例如同时使用match查询和term过滤。

```
"query": { 	"filtered": { 		"query": { "match": { k:v } }, 		"filter": { "term": { k:v } } 	}}
```



如果想要匹配所有的文档，可以忽略query语句的match查询，这样filtered会默认补充一个match_all的查询。以下两句效果相同。

```
"query": { "filtered": { "query": { "match_all": { } } , "filter": { "term": { k:v } } }"query": { "filtered": { "filter": { "term": { k:v } } }
```

另外，过滤语句中可以使用query方式代替bool过滤子句。
例如 `"must_not": { "query" : { ... } }` 但是这种方式使用比较少。

#### 查询检测 - validate API

validate API可以用于检测查询语句是否合法。

```
GET /index/type/_validate/query <BODY>
```

如果需要查询具体错误信息，可以加上explain参数,query?explain
如果查询语句合法的情况下，explain会针对每一个index返回不同的描述。因为不同的index有不同的映射关系和分析器，例如tweet:powerful这样的查询语句，在一个分析器里可能查询powerful单词，在另外一个使用english分析器的index里就是查询power单词。



### 排序

默认排序： _score

一般情况下得到的文档都以_score降序排列，相关性高的排在前面。过滤语句不影响_score，如果使用了match_all或者隐式使用了match_all，那么所有的文档的得分都是1.

字段值排序

使用sort对字段值进行排序。

```
{ "query" : { … }, "sort": { "date": { "order": "desc" } } }
```

如果使用了sort排序，那么在没有显式指定track_scores为true的情况下，每一个文档的_score和查询的max_score都不会被计算。
因为相关性的计算比较消耗性能，如果指定了排序规则，就没有必要计算了。另外假如排序是date的情况下，date会被转成timestamp用于计算。

如果需要顺序排列时，可以使用简写。 `"sort": "key"`
如果需要多级排序，可以使用： `"sort": [ k1:{"order": "desc" }, k2:{"order": "desc" } ]`
如果需要排列的字段是一个数组，那么可以使用min, max, avg 或 sum这些模式来排序。`"sort": { k1:{"order": "desc","mode": "min" } }`

如果对于针对全文搜索而使用了analyzer的字段上进行排序，很难得到正确的结果。因此针对这些值，需要重新指定类型。
默认：

```json
"tweet": {	"type":"string",	"analyzer": "english"}
```

修改后：

```json
"tweet": {   
    "type":     "string",
    "analyzer": "english",   
    "fields": {       
              "raw": {             
                  "type":  "string",            
                  "index": "not_analyzed"        
              }
    }
}
```



tweet 字段用于全文本的 analyzed 索引方式不变。新增的 tweet.raw 子字段索引方式是 not_analyzed。
后面可以使用 `"sort": "tweet.raw"` 来对这个字段进行排序。