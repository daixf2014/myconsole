package app.util.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import app.action.TableController;
import app.util.PropertyHelper;
import app.util.StringHelper;
import app.util.math.MathHelper;

/**
 * 数据库操作类
 * @author xuzh
 * @createDate 2014-02-11
 * @description
 */
public class DBHelper extends JdbcDaoSupport {
    private static Logger logger = Logger.getLogger(DBHelper.class);
    public static TransactionTemplate transactionTemplate = new TransactionTemplate();
    public final static int DEFAULT_FETCHSIZE = 32; //默认的fetchsize
    public LobHandler lobHandler;
    public StringHelper str = new StringHelper();
    private static DBHelper db = null;

    public LobHandler getLobHandler() {
        return lobHandler;
    }

    public void setLobHandler(LobHandler lobHandler){
        this.lobHandler = lobHandler;
    }

    public static BasicDataSource createDataSource() throws Exception {
        Properties properties = new Properties();
        //解密数据库连接
        properties.setProperty("username", PropertyHelper.getPropertiesFileValue("username"));
        properties.setProperty("password", PropertyHelper.getPropertiesFileValue("password"));
        properties.setProperty("url", PropertyHelper.getPropertiesFileValue("url"));
        properties.setProperty("driverClassName", PropertyHelper.getPropertiesFileValue("driverClassName"));
        properties.setProperty("maxActive", PropertyHelper.getPropertiesFileValue("maxActive"));
        properties.setProperty("validationQuery", PropertyHelper.getPropertiesFileValue("validationQuery"));

        BasicDataSource ds = (BasicDataSource) BasicDataSourceFactory.createDataSource(properties);
        return ds;
    }

    public synchronized static DBHelper getInstance() {
        if(db != null) {
            return db;
        }

        try {
            DataSource ds = DBHelper.createDataSource();
            db = new DBHelper();
            db.setDataSource(ds);
            DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
            transactionManager.setDataSource(ds);
            transactionTemplate.setTransactionManager(transactionManager);
            db.setTransactionTemplate(transactionTemplate);
            logger.debug("创建数据操作对象成功！");

        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("创建数据操作对象失败！");
        }
        return db;
    }

    public synchronized static DBHelper getNewInstance() {
        DBHelper newDb = new DBHelper();
        try {
            DataSource ds = DBHelper.createDataSource();
            newDb.setDataSource(ds);
            DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
            transactionManager.setDataSource(ds);
            transactionTemplate.setTransactionManager(transactionManager);
            newDb.setTransactionTemplate(transactionTemplate);
            logger.debug("创建数据操作对象成功！");
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("创建数据操作对象失败！");
        }
        return newDb;
    }

    public synchronized static Connection getDBConnection(String userName, String url) {
        Connection conn = null;
        DBHelper db = DBHelper.getInstance();
        try {
            conn = db.getConnection();
        }
        catch (Exception e) {
            logger.error("获取数据库链接失败！" + e.getMessage());
        }
        return conn;
    }

	/**
     * 返回一条记录
     * @param sql 传入的sql语句: select *from table where user_id=?
     * @param objects
     * @return
     */
	@SuppressWarnings("unchecked")
	public Map<String, Object> queryForMap(String sql, Object[] objects) {
		Map<String, Object> map = null;
		try {
			map = this.getJdbcTemplate().queryForMap(sql, objects);
		} catch (EmptyResultDataAccessException e) {

		} catch (Exception e) {
			logger.error(e);
			logger.error(StringHelper.getSql(sql, objects));
		}
		if (map == null)
			map = new HashMap<String, Object>();
		return map;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> queryForMap(String sql) {
		Map<String, Object> map = null;
		try {
			map = this.getJdbcTemplate().queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {

		} catch (Exception e) {
			logger.error(e);
			logger.error(sql);
		}
		if (map == null)
			map = new HashMap<String, Object>();
		return map;
	}

	/**
	 * 获取某个字段的值
	 * @param sql
	 * @param args
	 * @return
	 */
	public String queryForString(String sql, Object[] args) {
		try {
			return StringHelper.notEmpty(this.getJdbcTemplate().queryForObject(sql, args, String.class));
		} catch (Exception ex) {
			logger.error(StringHelper.getSql(sql, args));
			return "";
		}
	}

	public String queryForString(String sql) {
		try {
			return StringHelper.notEmpty(this.getJdbcTemplate().queryForObject(sql, null, String.class));
		} catch (Exception ex) {
			logger.error(sql);
			return "";
		}
	}

	public String queryForString(String sql,List<String> list) {
		try {
			return StringHelper.notEmpty(this.getJdbcTemplate().queryForObject(sql, list.toArray(), String.class));
		} catch (Exception ex) {
			logger.error(sql);
			return "";
		}
	}

	/**
	 * 返回一条记录剔除null值
	 * @param sql 传入的sql语句: select *from table where user_id=?
	 * @param objects
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> queryForMapNotNull(String sql, Object[] objects) {
		Map<String, Object> map = null;
		Map<String, Object> temp = new HashMap<String, Object>();
		try {
			map = this.getJdbcTemplate().queryForMap(sql, objects);
			if (map != null) {
				Set<String> s = map.keySet();
				for (Iterator<String> iter = s.iterator(); iter.hasNext();) {
					String key = StringHelper.notEmpty(iter.next()).toString();
					String value = StringHelper.notEmpty(map.get(key)).toString();
					temp.put(key, value);
				}
				map.clear();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return temp;
	}

	/**
	 * 返回相应sequence的下一个值
	 * @param sequenceName sequence名称
	 * @return 下个值
	 */
	@SuppressWarnings("unchecked")
	public String getNextSequenceValue(String sequenceName) {
		Map<String, Object> map = null;
		String nextVal = "";
		try {
			map = this.getJdbcTemplate().queryForMap("select " + sequenceName + ".NEXTVAL SEQ from dual");
			nextVal = StringHelper.get(map, "SEQ");
		} catch (Exception e) {
			logger.error(e);
		}
		return nextVal;
	}

	/**
	 * 获取数据库当前时间
	 * @param time_formate 日期格式
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getSysdate(String time_formate) {
		Map<String, Object> map = null;
		String sysdate = "";
		try {
			map = this.getJdbcTemplate().queryForMap("select to_char(sysdate,'" + time_formate + "') date_time from dual");
			sysdate = StringHelper.get(map, "DATE_TIME");
		} catch (Exception e) {
			logger.error(e);
		}
		return sysdate;
	}

	/**
	 * 返回数据集
	 * @param sql 传入的sql语句: select *from table where user_id=?
	 * @param objects
	 * @return
	 */
	public List<Map<String, Object>> queryForList(String sql, Object[] objects)
	{
		return this.queryForList(sql, objects, DEFAULT_FETCHSIZE);
	}

	/**
	 * 返回数据集
	 * 查询时条件不确定，将条件放入一个List<String>中
	 * @param sql
	 * @param list
	 * @return
	 */
	public List<Map<String, Object>> queryForList(String sql, List<String> list) {
		return this.queryForList(sql, list.toArray(), DEFAULT_FETCHSIZE);
	}

	/**
	 * 查询条件不确定时返回数据集
	 * @param sql sql_where拼接 sql="select * from table where name='"+v_name+"'";
	 * @return
	 */
	public List<Map<String, Object>> queryForList(String sql) {
		return this.queryForList(sql, DEFAULT_FETCHSIZE);
	}

	/**
	 * 查询条件不确定时返回数据集
	 * @param sql sql_where拼接 sql="select * from table where name='"+v_name+"'";
	 * @param fetchSize 一次获取的数据条数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForList(String sql, int fetchSize) {
		JdbcTemplate jdbc = this.getJdbcTemplate();
		jdbc.setFetchSize(fetchSize);

		List<Map<String, Object>> list = null;
		try {
			list = jdbc.queryForList(sql);
		} catch (Exception e) {
			logger.error(e);
			logger.error(sql);
		}
		if (list == null)
			list = new ArrayList<Map<String, Object>>();
		return list;
	}

	/**
	 * 返回数据集
	 * @param sql 传入的sql语句: select *from table where user_id=?
	 * @param objects
	 * @param fetchSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForList(String sql, Object[] objects, int fetchSize) {
		JdbcTemplate jdbc = this.getJdbcTemplate();
		jdbc.setFetchSize(fetchSize);

		List<Map<String, Object>> list = null;
		try {
			list = jdbc.queryForList(sql, objects);
		} catch (Exception e) {
			logger.error(e);
			logger.error(sql);
		}
		if (list == null)
			list = new ArrayList<Map<String, Object>>();
		return list;
	}

	/**
	 * 返回数据集
	 * 查询时条件不确定，将条件放入一个List<String>中
	 * @param sql
	 * @param list
	 * @param fetchSize
	 * @return
	 */
	public List<Map<String, Object>> queryForList(String sql, List<String> list, int fetchSize) {
		return this.queryForList(sql, list.toArray(), fetchSize);
	}

	/**
	 * displayTag 数据库分页，支持占位符
	 * @param sql
	 * @param list
	 * @param pageSize
	 * @param request
	 * @return
	 */
	public List<Map<String, Object>> getForList(TableController tbc, String sql, List<String> paramList, int pageSize, int curentPage) {

		/*********************下面一段是替换sql语句中的order by，并将查询字段替换成count(1)*************************/
		List<String> cntSqlList = new ArrayList<String>();
		cntSqlList.addAll(paramList);
		String orderSql = this.replaceSqlOrder(sql);
		//需要查看替换掉order by之后占位符是否减少
		int cnt = this.getCharCnt(orderSql);
		if (cnt != cntSqlList.size()) {//如果替换order by之后占位符少了，将list从后去除cntSqlList.size()-cnt个参数
			int removeCnt = 0;
			int rCnt = cntSqlList.size()-cnt;//需要剔除的参数个数
			Iterator<String> iterator = cntSqlList.iterator();
	        while(iterator.hasNext()) {
	        	iterator.next();
	        	iterator.remove();
	        	removeCnt ++;
	        	if (removeCnt == rCnt) {
					break;
				}
	        }
		}
		String cntSql = this.replaceSqlCount(orderSql);
		//需要查看替换成count(1)之后占位符是否减少
		cnt = this.getCharCnt(cntSql);
		if (cnt != cntSqlList.size()) {//如果替换count(1)之后占位符少了，将list从前去除list.size()-cnt个参数
			int removeCnt = 0;
			int rCnt = cntSqlList.size()-cnt;//需要剔除的参数个数
			Iterator<String> iterator = cntSqlList.iterator();
	        while(iterator.hasNext()) {
	        	iterator.next();
	        	iterator.remove();
	        	removeCnt ++;
	        	if (removeCnt == rCnt) {
					break;
				}
	        }
		}
		/***********************sql语句替换结束**************************/

		// beginIndex与size在displayTag显示时调用，beginIndex为当前开始序列，size为总数,displayTag根据size与pageSize自动计算总页码
		int size = this.queryForInt(cntSql, cntSqlList);
		tbc.lb_total_size.setText(String.valueOf(size));
		//最大页数
		int totalPage = MathHelper.ceilAfterDivide(size, pageSize);
		tbc.lb_total_page.setText(String.valueOf(totalPage));

		if(curentPage >= totalPage) {
			curentPage = totalPage;
			tbc.hl_next_page.setVisited(true);
			tbc.hl_next_page.setDisable(true);
		}
		else {
			tbc.hl_next_page.setVisited(false);
			tbc.hl_next_page.setDisable(false);
		}

		if(curentPage <= 1) {
			curentPage = 1;
			tbc.hl_prev_page.setVisited(true);
			tbc.hl_prev_page.setDisable(true);
		}
		else {
			tbc.hl_prev_page.setVisited(false);
			tbc.hl_prev_page.setDisable(false);
		}

		tbc.lb_current_page.setText(String.valueOf(curentPage));

		//起始编号
        int first = (curentPage-1) * pageSize;

        //查询语句
		sql = "select AA.*, (@rownum:=@rownum+1)+"+pageSize+"*("+curentPage+"-1) row_num from (select BB.*, @rownum:=0 rownum from ("+sql+") BB) AA limit "+first+", "+pageSize;
		return this.queryForList(sql, paramList);
	}

	/**
	 * 替换掉sql语句中的order
	 * 只替换掉最外面的order by
	 * @param sql
	 * @return
	 */
	public String replaceSqlOrder(String sql){
		String oldSql = sql;
	    sql = sql.toLowerCase();
		String newSql = "";
		if (sql.contains(" order ")) {
			String orderSql = oldSql.substring(sql.lastIndexOf(" order "));
			if (orderSql.contains(")")) {
				newSql = oldSql;
			} else {
				newSql = oldSql.substring(0, sql.lastIndexOf(" order "));
			}
		} else {
			newSql = oldSql;
		}
        return newSql;
    }

	/**
	 * 替换查询字段为count(1)
	 * 如：select * from xxxx 替换为select count(1) from xxxx
	 * @param sql
	 * @return
	 */
	public String replaceSqlCount(String sql){
	    String oldSql = sql;
	    String newSql = "";

		sql = sql.toLowerCase();
		if (sql.contains("group ") || sql.contains("distinct ")) {//如果语句最后有group by 不需要替换，嵌套一层
		    /*String groupSql = oldSql.substring(sql.lastIndexOf("group "));
            if (!groupSql.contains(")")) {
                return "select count(1) from (" + sql + ")";
            }*/
            return "select count(1) from (" + oldSql + ")";
		}

		//以小写字母匹配所有的select和from
		Pattern p = Pattern.compile("[\\s(]select\\s|\\sfrom\\s", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(" "+sql);
		int iSelect = 0;
		int iFrom = 0;
		//找到最外面的select对应的的是第几个from
		while(m.find()) {
			if (m.group().trim().toLowerCase().endsWith("select")) {
				iSelect ++;
			}
			if (m.group().trim().toLowerCase().equals("from")) {
				iFrom ++;
			}
			if (iSelect == iFrom) {
				break;
			}
		}

		int iSearch = 0;
		int fromIndex = 0;//记录最外面select对应的from的位置
		//找到最外面的select对应的from的位置
		while(iSearch < iFrom){
			if ((sql.toLowerCase()).indexOf(" from ")>0) {
				fromIndex = (sql.toLowerCase()).indexOf(" from ");
				sql = sql.replaceFirst(" from ", " #### ");
				iSearch ++;
			}
		}
		newSql = " select count(1) from "+oldSql.substring(fromIndex+5);
		return newSql;
	}

	/**
	 * 获取sql语句中占位符的个数
	 * @param sql
	 * @return
	 */
	public int getCharCnt(String sql){
		int cnt = 0;
		Pattern p = Pattern.compile("\\?", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		while(m.find()){
			cnt ++;
		}
		return cnt;
	}

	/**
	 * insert,update,delete 操作
	 * @param sql 传入的语句 sql="insert into tables values(?,?)";
	 * @param objects
	 * @return 0:失败 1:成功
	 */
	public int update(String sql, Object[] objects) {
		int exc = 1;
		try {
			this.getJdbcTemplate().update(sql, objects);
		} catch (Exception e) {
			exc = 0;
			logger.error(e);
			logger.error(StringHelper.getSql(sql, objects));
		}
		return exc;
	}

	public int update(String sql) {
		int exc = 1;
		try {
			this.getJdbcTemplate().update(sql);
		} catch (Exception e) {
			exc = 0;
			logger.error(sql);
			logger.error(e);
		}
		return exc;
	}

	/**
	 * 返还记录数
	 * @param sql 传入的sql语句 select count(*) from table where name=?
	 * @param objects 参数值
	 * @return -1:数据库异常
	 */
	public int queryForInt(String sql, Object[] objects) {
		int exc = -1;
		try {
			exc = this.getJdbcTemplate().queryForInt(sql, objects);
		} catch (Exception e) {
			exc = -1;
			logger.error(e);
		}
		return exc;
	}

	/**
	 * 返还记录数
	 * @param sql 传入的sql语句 select count(*) from table where name=?
	 * @param list 参数值
	 * @return -1:数据库异常
	 */
	public int queryForInt(String sql, List<String> list) {
		return this.queryForInt(sql, list.toArray());
	}

	/**
	 * 返还记录数
	 * @param sql 传入的sql语句直接拼接好
	 * @return
	 */
	public int queryForInt(String sql) {
		return this.getJdbcTemplate().queryForInt(sql);
	}

	/**
	 * 返还记录数--返回记录数超出int范围
	 * @param sql 传入的sql语句直接拼接好 sql="select count(*) from table where name='"+mike+"'"
	 * @return
	 */
	public Long queryForLong(String sql) {
		return this.getJdbcTemplate().queryForLong(sql);
	}

	/**
	 * 事务处理
	 * @param batchSqls
	 * @return
	 */
	public int doInTransaction(final BatchSql batchSql) {
		int exc = 1;
		if (batchSql == null) {
			exc = 0;
		}
		try {
			TransactionCallbackWithoutResult t = new TransactionCallbackWithoutResult() {
				@SuppressWarnings("rawtypes")
				public void doInTransactionWithoutResult(
						TransactionStatus status) {
					List sqlList = batchSql.getSqlList();
					for (int i = 0; i < sqlList.size(); i++) {
						Map sqlMap = (Map) sqlList.get(i);
						String sql = (String) sqlMap.get("sql");
						Object[] objects = (Object[]) sqlMap.get("objects");
						getJdbcTemplate().update(sql, objects);
					}
				}
			};
			transactionTemplate.execute(t);
		} catch (Exception e) {
			exc = 0;
			e.printStackTrace();
		}
		return exc;
	}

	/**
     * 处理sql语句中in的占位符
     * @param inParams in的参数
     * @param paramList 参数list
     * @param splitChart inParams的分隔符
     * @return
     */
    public String rebuildInSql(String inParams, List<String> paramList, String splitChart){
        String[] paramArray = inParams.split(splitChart);
        String inSql = "";
        for (int i = 0; i < paramArray.length; i++) {
            inSql += ",?";
            paramList.add(paramArray[i]);
        }
        if (!inSql.equals("")) {
            inSql = inSql.substring(1);
        }
        return inSql;
    }

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	@SuppressWarnings("static-access")
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}
}