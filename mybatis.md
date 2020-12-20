# mybatis-schema

### 四个基础组件：Executor、ParameterHandler、StatementHandler、ResultSetHandler



#### Executor：MyBatis的执行器，用于执行增删改查操作,用它来调度StatementdHandler,ParameterHandler,ResultHandler等来执行对应的SQL.

```java
// 所有的Executor 都是通过Configuration中newExecutor创建的。 org.apache.ibatis.session.Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)

public interface Executor {

  ResultHandler NO_RESULT_HANDLER = null;

  int update(MappedStatement ms, Object parameter) throws SQLException;

  <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, 
                    CacheKey cacheKey, BoundSql boundSql) throws SQLException;

  <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

  <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

  List<BatchResult> flushStatements() throws SQLException;

  void commit(boolean required) throws SQLException;

  void rollback(boolean required) throws SQLException;

  CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

  boolean isCached(MappedStatement ms, CacheKey key);

  void clearLocalCache();

  void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

  Transaction getTransaction();

  void close(boolean forceRollback);

  boolean isClosed();

  void setExecutorWrapper(Executor executor);

}

```

![](E:\201320180110\source\image\mybatis\mybatis-Executor.png)

##### ExecutorType.SIMPLE, REUSE, BATCH  Executor类型有三种。

**BaseExecutor** 是基础的执行器类，提供了大部分公共的方法实现也包含了connection和datasource、本地缓存三个重要属性

BaseExecutor的四个抽象方法，待子类实现：

> doUpdate
> doFlushStatements
> doQuery
> doQueryCursor

**CachingExecutor ** 是二级缓存的装饰者Executor。

**SimpleExecutor**  是一个简单的

**BatchExecutor** 批量执行，更新时批量操作

**ReuseExecutor** 执行器会重用预处理语句（PreparedStatements）







#### ParameterHandler：处理SQL的参数对象  

```java
// 所有的ParameterHandler 都是通过Configuration中newParameterHandler创建的。 org.apache.ibatis.session.Configuration#newParameterHandler


```



#### StatementHandler：数据库的处理对象，用于执行SQL语句      

```java
// 所有的StatementHandler 都是通过Configuration中newStatementHandler创建的。  org.apache.ibatis.session.Configuration#newStatementHandler
public interface StatementHandler {
  Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException;
  void parameterize(Statement statement) throws SQLException;
  void batch(Statement statement) throws SQLException;
  int update(Statement statement) throws SQLException;
  <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;
  <E> Cursor<E> queryCursor(Statement statement) throws SQLException;
  BoundSql getBoundSql();
  ParameterHandler getParameterHandler();
}

```

**BaseStatementHandler**：基础的抽象的Handler。其中包含了parameterHandler和ResultSetHandler的创建。

```java
大致结构图
statementHandler{
    Configuration
    Executor
    MappedStatement
    boundSql
    
    ResultSetHandler
    ParameterHandler
}



public abstract class BaseStatementHandler implements StatementHandler {

  protected final Configuration configuration;
  protected final ObjectFactory objectFactory;
  protected final TypeHandlerRegistry typeHandlerRegistry;
  protected final ResultSetHandler resultSetHandler;
  protected final ParameterHandler parameterHandler;

  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final RowBounds rowBounds;

  protected BoundSql boundSql;

  protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, 
                                 RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    this.configuration = mappedStatement.getConfiguration();
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;

    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();

    if (boundSql == null) {  generateKeys(parameterObject); boundSql = mappedStatement.getBoundSql(parameterObject); }

    this.boundSql = boundSql;

    //创建 parameterHandler
    this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    //创建 resultSetHandler
    this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
  }

  //主要获取statement，给statement设置超时时间和获取行数大小。此方法在Executor中执行，
  @Override
  public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
    Statement statement  = instantiateStatement(connection);
    setStatementTimeout(statement, transactionTimeout);
    setFetchSize(statement);
    return statement;
  }

  //待子类实现真正的statement,实例化statement
  protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

  //从配置中获取查询超时时间，最终指定的事务超时时间和配置的查询超时时间（不为空）二者取最小值，省略代码
  protected void setStatementTimeout(Statement stmt, Integer transactionTimeout) throws SQLException;

  //从配置中获取fetchsize,并付给statement,省略代码
  protected void setFetchSize(Statement stmt) throws SQLException;

}
```



**RoutingStatementHandler** : 路由、区分、获取其他三个真实的Handler，一下是其主要功能。创建一个StatementHandler 是通过configuration.newStatementHandler来创建的，而此方法中new的就是RoutingStatementHandler ,通过此RoutingStatementHandler来路由、判断获取PreparedStatementHandler、SimpleStatementHandler、CallableStatementHandler其中之一。

```
public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

    switch (ms.getStatementType()) {
      case STATEMENT:
        delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      case PREPARED:
        delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      case CALLABLE:
        delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        break;
      default:
        throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
    }

  }
```



**PreparedStatementHandler**

**SimpleStatementHandler**



**CallableStatementHandler** 存储过程的statement



**MappedStatement**   这是一个Statement 的映射，因为它包含的信息是构成以及获取Statement的实例。而connection.getPreparedStatement(sql)中的sql来自MappedStatement中。connection每一次获取statement都需要一次网络io。



#### ResultSetHandler：处理SQL的返回结果集

```java
// 所有的ResultSetHandler 都是通过Configuration中newResultSetHandler创建的。  org.apache.ibatis.session.Configuration#newResultSetHandler



```





=======================================================================================================================================



### Configuration(贯穿始终)

```java
  protected Environment environment;//所使用的datasource以及transactionManager封装类
  protected boolean safeRowBoundsEnabled;
  protected boolean safeResultHandlerEnabled = true;
  protected boolean mapUnderscoreToCamelCase;
  protected boolean aggressiveLazyLoading;
  protected boolean multipleResultSetsEnabled = true;
  protected boolean useGeneratedKeys;
  protected boolean useColumnLabel = true;
  protected boolean cacheEnabled = true;
  protected boolean callSettersOnNulls;
  protected boolean useActualParamName = true;
  protected boolean returnInstanceForEmptyRow;

  protected String logPrefix;
  protected Class<? extends Log> logImpl;
  protected Class<? extends VFS> vfsImpl;
  protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;
  protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
  protected Set<String> lazyLoadTriggerMethods = new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString"));
  protected Integer defaultStatementTimeout;
  protected Integer defaultFetchSize;
  protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;
  protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
  protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;

  protected Properties variables = new Properties();
  protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
  protected ObjectFactory objectFactory = new DefaultObjectFactory();
  protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

  protected boolean lazyLoadingEnabled = false;
  protected ProxyFactory proxyFactory = new JavassistProxyFactory(); // #224 Using internal Javassist instead of OGNL

  protected String databaseId;
  /**
   * Configuration factory class.
   * Used to create Configuration for loading deserialized unread properties. 
   */
  protected Class<?> configurationFactory;

  protected final MapperRegistry mapperRegistry = new MapperRegistry(this);//Mapper接口注册类，包含了所有Mapper文件。
  protected final InterceptorChain interceptorChain = new InterceptorChain();
  protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
  protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
  protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

  protected final Map<String, MappedStatement> mappedStatements = 
      new StrictMap<MappedStatement>("Mapped Statements collection").conflictMessageProducer((savedValue, targetValue) ->
          ". please check " + savedValue.getResource() + " and " + targetValue.getResource());

  protected final Map<String, Cache> caches = new StrictMap<>("Caches collection");
  protected final Map<String, ResultMap> resultMaps = new StrictMap<>("Result Maps collection");
  protected final Map<String, ParameterMap> parameterMaps = new StrictMap<>("Parameter Maps collection");
  protected final Map<String, KeyGenerator> keyGenerators = new StrictMap<>("Key Generators collection");

  protected final Set<String> loadedResources = new HashSet<>();
  protected final Map<String, XNode> sqlFragments = new StrictMap<>("XML fragments parsed from previous mappers");

  protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<>();
  protected final Collection<CacheRefResolver> incompleteCacheRefs = new LinkedList<>();
  protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();
  protected final Collection<MethodResolver> incompleteMethods = new LinkedList<>();
  protected final Map<String, String> cacheRefMap = new HashMap<>();

  public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);

    typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

    typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
    typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
    typeAliasRegistry.registerAlias("LRU", LruCache.class);
    typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
    typeAliasRegistry.registerAlias("WEAK", WeakCache.class);

    typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);

    typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
    typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);

    typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
    typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
    typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
    typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
    typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
    typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
    typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

    typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
    typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

    languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    languageRegistry.register(RawLanguageDriver.class);
  }

```



### 加载配置

```java
创建 SqlSessionFactoryBuilder.build(configFile)
    创建 XMLConfigBuilder   //可以通过Reader或InputStream的构造器创建，XMLConfigBuilder 继承 BaseBuilder
		创建 Configuration  //一个Builder 一个configuration
		创建 XPathParser  //xml真实解析器
    
    XMLConfigBuilder.parse()
    	XMLConfigBuilder.parseConfiguration(root) //解析后的结果保存再configuration中。
    	//解析配置文件...
    
    
    
    SqlSessionFactoryBuilder.build(configuration)
    创建 SqlSessionFactory //factory中保存了configuration  一个factory 一个config
    
```



### SqlSessionFactoryBuilder(三个主要方法)

```java
build(Reader reader, String environment, Properties properties)
build(InputStream inputStream, String environment, Properties properties)
build(Configuration config)//最终都是通过此方法返回SqlSessionFactory
```



### BaseBuilder  建造者基础类（子类包含：XMLConfigBuilder、XMLMapperBuilder、XMLStatementBuilder、XMLScriptBuilder、SqlSourceBuilder、MapperBuilderAssistant等）

```

三个属性
protected final Configuration configuration;
protected final TypeAliasRegistry typeAliasRegistry;
protected final TypeHandlerRegistry typeHandlerRegistry;

以及JdbcType、ResultSetType、TypeHandler  等工具解析方法，而且修饰符是protected 说明这些方法是给子类定义使用的。
```



### SqlSessionFactory

```java
openSession() //开启session,获取一个session,openSession 实现给了默认实现类 org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource

//org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromConnection
SqlSession openSession(ExecutorType execType, Connection connection);

Configuration getConfiguration(); //获取配置
```



### Environment

```java
private final String id;//实例id 换而言之 config配置文件中的environment标签中的id
private final TransactionFactory transactionFactory;//事务工厂
private final DataSource dataSource;//数据源,来自于配置文件

environment 是对 txFactory和dataSource的一个封装。


```

![1600500541030](E:\201320180110\source\image\mybatis\mybatis-environments.png)



### Transaction

```java
Connection getConnection() throws SQLException; //最终connection是来自datasource
void commit() throws SQLException; 
void rollback() throws SQLException; 
void close() throws SQLException; 
Integer getTimeout() throws SQLException;
```







### SqlSession

```java
//sqlSession 结构

DefaultSqlSession{
	Configuration{
		Environment{
            id;
            transactionFactory;
            datasource;
        }
	}
	Executor{
		Transaction{
			connection;//来自于datasource或构造tx时指定
			dataSource;
			TransactionIsolationLevel;
			boolean autoCommit;
		}
	}
}


//以下是sqlsessionFactory主要的两个方法，获取sqlSession

//从该方法中可以看个sqlsession 全貌。org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource

private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;
    try {
      final Environment environment = configuration.getEnvironment();
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);//从environment中获取tx工厂
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);//获取一个新的事务而已
      final Executor executor = configuration.newExecutor(tx, execType);//根据类型获取Executor，其中Executor包含了tx；
      return new DefaultSqlSession(configuration, executor, autoCommit);//sqlSession 包含了Executor。
    } catch (Exception e) {
      closeTransaction(tx); // may have fetched a connection so lets call close()
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
}


//根据执行器类型获取执行器，再组合connection获取sqlsession
private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
    try {
      boolean autoCommit;
      try {
        autoCommit = connection.getAutoCommit();
      } catch (SQLException e) {
        autoCommit = true;
      }
      final Environment environment = configuration.getEnvironment();
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      final Transaction tx = transactionFactory.newTransaction(connection);//指定connection，获取tx；
      final Executor executor = configuration.newExecutor(tx, execType);
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
}


  //Sqlsession 接口所有方法
  <T> T selectOne(String statement);
  <T> T selectOne(String statement, Object parameter);
  <E> List<E> selectList(String statement);
  <E> List<E> selectList(String statement, Object parameter);
  <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);
  <K, V> Map<K, V> selectMap(String statement, String mapKey);
  <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey); 
  <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds); 
  <T> Cursor<T> selectCursor(String statement); 
  <T> Cursor<T> selectCursor(String statement, Object parameter); 
  <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds); 
  void select(String statement, Object parameter, ResultHandler handler); 
  void select(String statement, ResultHandler handler); 
  void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler); 
  int insert(String statement); 
  int insert(String statement, Object parameter); 
  int update(String statement); 
  int update(String statement, Object parameter);
  int delete(String statement); 
  int delete(String statement, Object parameter); 
  void commit(); 
  void commit(boolean force); 
  void rollback(); 
  void rollback(boolean force); 
  List<BatchResult> flushStatements(); 
  @Override
  void close(); 
  void clearCache(); 
  Configuration getConfiguration(); 
  <T> T getMapper(Class<T> type); 
  Connection getConnection();


```



#### MappedStatement  statement的映射其中包含了configuration

```java

MappedStatement 大致结构

MappedStatement{
	configuration
	statementID
	type
	timeout
	SqlSource
	
	resultMap
	...等配置（cache）

}

//一个简单的select 的sql配置。
 <select id="getUserByid"
            parameterType="int"
            resultType="hashMap"
            resultMap="UserMap"
            flushCache="false"
            useCache="true"
            timeout="10000"
            fetchSize="256"
            statementType="PREPARED"
            resultSetType="FORWARD_ONLY">
    </select>



public final class MappedStatement {

  private String resource;//mapper配置文件名，如：UserMapper.xml
  private Configuration configuration;
  private String id;//在命名空间中唯一的标识符，可以被用来引用这条配置信息。
  private Integer fetchSize;//用于设置JDBC中Statement对象的fetchSize属性，该属性用于指定SQL执行后返回的最大行数。
  private Integer timeout;//驱动程序等待数据库返回请求结果的秒数，超时将会抛出异常。
  private StatementType statementType;//参数可选值为STATEMENT、PREPARED或CALLABLE，这会让MyBatis分别使用Statement、PreparedStatement或CallableStatement与数据库交互，默认值为PREPARED。
  private ResultSetType resultSetType;//参数可选值为FORWARD_ONLY、SCROLL_SENSITIVE或SCROLL_INSENSITIVE，用于设置ResultSet对象的特征
  private SqlSource sqlSource;
  private Cache cache;
  private ParameterMap parameterMap;
  private List<ResultMap> resultMaps;
  private boolean flushCacheRequired;//用于控制是否刷新缓存。如果将其设置为true，则任何时候只要语句被调用，都会导致本地缓存和二级缓存被清空，默认值为false。
  private boolean useCache;//是否使用二级缓存。如果将其设置为true，则会导致本条语句的结果被缓存在MyBatis的二级缓存中，对应<select>标签，该属性的默认值为true。
  private boolean resultOrdered;
  private SqlCommandType sqlCommandType;
  private KeyGenerator keyGenerator;
  private String[] keyProperties;
  private String[] keyColumns;
  private boolean hasNestedResultMaps;
  private String databaseId;//如果配置了databaseIdProvider，MyBatis会加载所有不带databaseId或匹配当前databaseId的语句。
  private Log statementLog;
  private LanguageDriver lang;
  private String[] resultSets;

  MappedStatement() {
    // constructor disabled
  }

  public static class Builder {
      
    private MappedStatement mappedStatement = new MappedStatement();

    public Builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
      mappedStatement.configuration = configuration;
      mappedStatement.id = id;
      mappedStatement.sqlSource = sqlSource;
      mappedStatement.statementType = StatementType.PREPARED;
      mappedStatement.resultSetType = ResultSetType.DEFAULT;
      mappedStatement.parameterMap = new ParameterMap.Builder(configuration, "defaultParameterMap", null, new ArrayList<>()).build();
      mappedStatement.resultMaps = new ArrayList<>();
      mappedStatement.sqlCommandType = sqlCommandType;
      mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
      String logId = id;
      if (configuration.getLogPrefix() != null) {
        logId = configuration.getLogPrefix() + id;
      }
      mappedStatement.statementLog = LogFactory.getLog(logId);
      mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
    }

    public MappedStatement build() {
      assert mappedStatement.configuration != null;
      assert mappedStatement.id != null;
      assert mappedStatement.sqlSource != null;
      assert mappedStatement.lang != null;
      mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
      return mappedStatement;
    }
  } 

  public BoundSql getBoundSql(Object parameterObject) {
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);//创建sql约束
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings == null || parameterMappings.isEmpty()) {
      boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
    }
 
    for (ParameterMapping pm : boundSql.getParameterMappings()) {
      String rmId = pm.getResultMapId();
      if (rmId != null) {
        ResultMap rm = configuration.getResultMap(rmId);
        if (rm != null) {
          hasNestedResultMaps |= rm.hasNestedResultMaps();//是否有resultmap
        }
      }
    }

    return boundSql;
  }

}

```