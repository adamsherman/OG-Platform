/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testng.annotations.*;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ZipUtils;
import com.opengamma.util.db.*;
import com.opengamma.util.test.DbTool.TableCreationCallback;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Base DB test.
 */
public abstract class DbTest implements TableCreationCallback {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbTest.class);

  private static Map<String, String> s_databaseTypeVersion = new HashMap<String, String>();
  private static final Map<String, DbDialect> s_dbDialects = new HashMap<String, DbDialect>();
  private static final File SCRIPT_INSTALL_DIR = new File(DbTool.getWorkingDirectory(), "temp/" + DbTest.class.getSimpleName());

  static {
    DateUtils.initTimeZone();
    addDbDialect("hsqldb", new HSQLDbDialect());
    addDbDialect("postgres", new PostgresDbDialect());
    addDbDialect("sqlserver2008", new SqlServer2008DbDialect());
  }

  static {
    try {
      extractSQLScript();
    } catch (IOException ex) {
      s_logger.warn("problem with unzip publish sql script to {} rethrowing", SCRIPT_INSTALL_DIR, ex);
      throw new OpenGammaRuntimeException("problem with unzip publish sql script to " + SCRIPT_INSTALL_DIR, ex);
    }
  }

  private final String _databaseType;
  private final String _databaseVersion;
  private final DbTool _dbtool;

  protected DbTest(String databaseType, String databaseVersion) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    _databaseType = databaseType;
    _dbtool = TestProperties.getDbTool(databaseType);
    _dbtool.setJdbcUrl(getDbTool().getTestDatabaseUrl());
    _databaseVersion = databaseVersion;
    if (isScriptPublished()) {
      _dbtool.addDbScriptDirectory(SCRIPT_INSTALL_DIR.getAbsolutePath());
    } else {
      _dbtool.addDbScriptDirectory(DbTool.getWorkingDirectory());
    }
  }

  private boolean skip = false;

  /**
   * Initialise the database to the required version. This tracks the last initialised version
   * in a static map to avoid duplicate DB operations on bigger test classes. This might not be
   * such a good idea.
   */
  @BeforeMethod
  public void setUp() throws Exception {
    String prevVersion = s_databaseTypeVersion.get(getDatabaseType());
    if ((prevVersion == null) || !prevVersion.equals(getDatabaseVersion())) {
      s_databaseTypeVersion.put(getDatabaseType(), getDatabaseVersion());
      _dbtool.setCreateVersion(getDatabaseVersion());
      _dbtool.dropTestSchema();
      _dbtool.createTestSchema();
      _dbtool.createTestTables(this);
    }
    _dbtool.clearTestTables();
  }

  private static void extractSQLScript() throws IOException {
    if (isScriptPublished()) {
      cleanUp();
      unzipSQLScripts();
    }
  }

  private static String getZipPath() {
    return TestProperties.getTestProperties().getProperty("test.sqlzip.path");
  }

  private static void unzipSQLScripts() throws IOException {
    File zipScriptPath = new File(DbTool.getWorkingDirectory(), getZipPath());
    for (File file : (Collection<File>) FileUtils.listFiles(zipScriptPath, new String[]{"zip"}, false)) {
      ZipUtils.unzipArchive(file, SCRIPT_INSTALL_DIR);
    }
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _dbtool.resetTestCatalog(); // avoids locking issues with Derby
  }

  @AfterClass
  public void tearDownClass() throws Exception {
    _dbtool.close();
  }

  @AfterSuite
  public static void cleanUp() {
    FileUtils.deleteQuietly(SCRIPT_INSTALL_DIR);
  }

  //-------------------------------------------------------------------------
  public static Collection<Object[]> getParameters() {
    int previousVersionCount = getPreviousVersionCount();
    return getParameters(previousVersionCount);
  }

  protected static Collection<Object[]> getParameters(final int previousVersionCount) {
    String databaseType = System.getProperty("test.database.type");
    if (databaseType == null) {
      databaseType = "all";
    }
    Collection<String> databaseTypes = TestProperties.getDatabaseTypes(databaseType);
    ArrayList<Object[]> returnValue = new ArrayList<Object[]>();
    for (String dbType : databaseTypes) {
      for (int i = previousVersionCount; i >= 0; i--) {
        returnValue.add(new Object[]{dbType, "" + i});
      }
    }
    return returnValue;
  }

  protected static Collection<Object[]> getParameters(final String databaseType, final int previousVersionCount) {
    ArrayList<Object[]> returnValue = new ArrayList<Object[]>();
    for (String db : TestProperties.getDatabaseTypes(databaseType)) {
      final DbTool dbTool = TestProperties.getDbTool(db);
      if (isScriptPublished()) {
        dbTool.addDbScriptDirectory(SCRIPT_INSTALL_DIR.getAbsolutePath());
      } else {
        dbTool.addDbScriptDirectory(DbTool.getWorkingDirectory());
      }
      final String[] versions = dbTool.getDatabaseCreatableVersions();
      for (int i = 0; i < versions.length; i++) {
        returnValue.add(new Object[]{db, versions[i]});
        if (i >= previousVersionCount) {
          break;
        }
      }
    }
    return returnValue;
  }

  private static boolean isScriptPublished() {
    String zipPath = getZipPath();
    if (zipPath == null) {
      throw new OpenGammaRuntimeException("missing test.sqlZip.path property in test properties file");
    }
    File zipScriptPath = new File(DbTool.getWorkingDirectory(), zipPath);
    boolean result = false;
    if (zipScriptPath.exists()) {
      @SuppressWarnings("rawtypes")
      Collection zipfiles = FileUtils.listFiles(zipScriptPath, new String[]{"zip"}, false);
      result = !zipfiles.isEmpty();
    }
    return result;
  }

  //-------------------------------------------------------------------------  
  private static boolean checkScripts(String databaseType, int versionsBack) {
    DbTool dbtool = TestProperties.getDbTool(databaseType);
    dbtool.setJdbcUrl(dbtool.getTestDatabaseUrl());
    if (isScriptPublished()) {
      dbtool.addDbScriptDirectory(SCRIPT_INSTALL_DIR.getAbsolutePath());
    } else {
      dbtool.addDbScriptDirectory(DbTool.getWorkingDirectory());
    }
   
    int minVersionDifference = Integer.MAX_VALUE;
    for (Map<Integer, Pair<File, File>> versions : dbtool.getScriptDirs().values()) {
      int min = Integer.MAX_VALUE;
      int max = Integer.MIN_VALUE;
      for (Integer v : versions.keySet()) {
        min = v < min ? v : min;
        max = v > max ? v : max;
      }
      minVersionDifference = minVersionDifference > (max - min) ? (max - min) : minVersionDifference;
    }
    return minVersionDifference >= versionsBack;
  }

  private static Object[][] checkScripts(Collection<Object[]> parameters) {
    Collection<Object[]> filteredParameters = newArrayList();
    for (Object[] parameter : parameters) {
      String databaseType = (String) parameter[0];
      int versionsBack = Integer.parseInt((String) parameter[1]);
      if (checkScripts(databaseType, versionsBack)) {
        filteredParameters.add(parameter);
      }
    }
    Object[][] array = new Object[filteredParameters.size()][];
    filteredParameters.toArray(array);
    return array;
  }

  @DataProvider(name = "localDatabase")
  public static Object[][] data_localDatabase() {
    Collection<Object[]> parameters = getParameters("hsqldb", 0);
    return checkScripts(parameters);
  }

  @DataProvider(name = "databases")
  public static Object[][] data_databases() {
    Collection<Object[]> parameters = getParameters(3);
    return checkScripts(parameters);
  }

  @DataProvider(name = "databasesMoreVersions")
  public static Object[][] data_databasesMoreVersions() {
    Collection<Object[]> parameters = getParameters(3);
    return checkScripts(parameters);
  }

  protected static int getPreviousVersionCount() {
    String previousVersionCountString = System.getProperty("test.database.previousVersions");
    int previousVersionCount;
    if (previousVersionCountString == null) {
      previousVersionCount = 0; // If you run from Eclipse, use current version only
    } else {
      previousVersionCount = Integer.parseInt(previousVersionCountString);
    }
    return previousVersionCount;
  }

  public DbTool getDbTool() {
    return _dbtool;
  }

  public String getDatabaseType() {
    return _databaseType;
  }

  public String getDatabaseVersion() {
    return _databaseVersion;
  }

  public DataSourceTransactionManager getTransactionManager() {
    return new DataSourceTransactionManager(getDbTool().getDataSource());
  }

  public DbConnector getDbConnector() {
    DbDialect dbDialect = s_dbDialects.get(getDatabaseType());
    if (dbDialect == null) {
      throw new OpenGammaRuntimeException("config error - no DBHelper setup for " + getDatabaseType());
    }
    DbConnectorFactoryBean factory = new DbConnectorFactoryBean();
    factory.setName("DbTest");
    factory.setDialect(dbDialect);
    factory.setDataSource(getDbTool().getDataSource());
    factory.setTransactionIsolationLevelName("ISOLATION_READ_COMMITTED");
    factory.setTransactionPropagationBehaviorName("PROPAGATION_REQUIRED");
    return factory.createObject();
  }

  /**
   * Adds a dialect to the map of known.
   *
   * @param dbType  the database type, not null
   * @param dialect  the dialect, not null
   */
  public static void addDbDialect(String dbType, DbDialect dialect) {
    s_dbDialects.put(dbType, dialect);
  }

  /**
   * Override this if you wish to do something with the database while it is in its "upgrading" state - e.g. populate with test data
   * at a particular version to test the data transformations on the next version upgrades.
   */
  public void tablesCreatedOrUpgraded(final String version, final String prefix) {
    // No action 
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getDatabaseType() + ":" + getDatabaseVersion();
  }

}
