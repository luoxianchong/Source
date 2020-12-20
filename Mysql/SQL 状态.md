SQL 状态



maxActive   100     //连接池支持的最大连接数

initialSize= 5  //初始化连接数目           

maxIdle=40  //连接池中最多可空闲maxIdle个连接,表示即使没有数据库连接时依然可以保持40空闲的连接，而不被清除，随时处于待命状态。设 0为没有限制。            

minIdle= 5   //连接池中最少空闲minIdle个连接      

maxWait =30000  // 最大等待毫秒数, 单位为 ms, 超过时间会出错误信息，一般把maxActive设置成可能的并发量就行了

timeBetweenEvictionRunsMillis=30000 //毫秒内检查一次连接池中空闲的连接,把空闲时间超过minEvictableIdleTimeMillis毫秒的连接断开,直到连接池中的连接数到minIdle为止 

numTestsPerEvictionRun=5             

testOnBorrow= false         

validationQuery =select 1      

maxTotal =100           

minEvictableIdleTimeMillis=1800000 //连接池中连接可空闲的时间,毫秒

testWhileIdle= true      

removeAbandoned =true //是否清理removeAbandonedTimeout秒没有使用的活动连接,清理后并没有放回连接池  

removeAbandonedTimeout= 180000 //活动连接的最大空闲时间 



###### minEvictableIdleTimeMillis , removeAbandonedTimeout  这两个参数针对的连接对象不一样,

minEvictableIdleTimeMillis  针对连接池中的连接对象,

removeAbandonedTimeout   针对未被close的活动连接. 



#### mysql一个sql消耗的io次数

SET profiling=1;

DESCRIBE SELECT * FROM USER;

SHOW PROFILES;
SHOW PROFILE FOR QUERY 1;
SHOW PROFILE block io FOR QUERY 27;



show status variables like '%Last_query_cost %';