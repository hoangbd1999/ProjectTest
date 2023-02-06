package elcom.com.neo4j.repositoryPostgre;

import elcom.com.neo4j.model.MmsiIp;
import elcom.com.neo4j.utils.DateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Repository
public class CustomerRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerRepository.class);

    private final SessionFactory sessionFactory;

    @Autowired
    public CustomerRepository(EntityManagerFactory factory) {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
    }

    private Session openSession() {
        Session session = this.sessionFactory.openSession();
        return session;
    }

    private void closeSession(Session session) {
        if (session.isOpen()) {
            session.disconnect();
            session.close();
        }
    }

    public List<String> listNodeImportant(String startTime,String endTime, Integer size, Integer page) {
        Session session = openSession();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(df.parse(startTime));
            calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calFrom.set(Calendar.HOUR_OF_DAY,0);
            calFrom.clear(Calendar.MINUTE);
            calFrom.clear(Calendar.SECOND);
            calFrom.clear(Calendar.MILLISECOND);
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(df.parse(endTime));
            calTo.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calTo.set(Calendar.HOUR_OF_DAY,0);
            calTo.clear(Calendar.MINUTE);
            calTo.clear(Calendar.SECOND);
            calTo.clear(Calendar.MILLISECOND);
            StringBuilder stringBuilder = new StringBuilder("");
            Date start = df.parse(startTime);
            Date end = df.parse(endTime);
            String queryYear = "";
            if(end.getYear()-start.getYear()>=2){
                queryYear = "select  y.nodeIds, SUM(nodeSize) as count  from vsat_local.topology_node_to_node_month y where y.start_time>= '"+
                        df.format(calFrom.getTime()) +"' and y.start_time<= '"+df.format(calTo.getTime())+"'  group by (nodeIds,startTime)";
            }

            String queryDay = "";
            if(end.getYear()==start.getYear()&&end.getMonth()==start.getMonth()){
                queryDay = "select  nodeIds,SUM(nodeSize) as count from vsat_local.topology_node_to_node_day y where y.start_time>= '"+
                        df.format(calFrom.getTime()) +"' and y.start_time< '"+df.format(calTo.getTime())+"' group by (nodeIds,startTime)  ";
            } else {
                queryDay = "select  nodeIds,SUM(nodeSize) as count  from from vsat_local.topology_node_to_node_month y where  ( y.start_time>= '"+
                        df.format(calFrom.getTime()) +"'";
                calFrom.set(Calendar.DAY_OF_MONTH,1);
                calFrom.add(Calendar.MONTH,1);
                queryDay += " and y.start_time< '" +df.format(calFrom.getTime())+"' ) or (";
                queryDay += "y.start_time<= '"+df.format(calTo.getTime())+"' and ";
                calTo.set(Calendar.DAY_OF_MONTH,1);
                queryDay += "y.start_time>= '"+df.format(calTo.getTime())+"') group by (nodeIds,startTime) ";
            }

            String queryMonth = "";
            if(end.getYear()==start.getYear()){
                calFrom.set(Calendar.DAY_OF_MONTH,1);
                calFrom.add(Calendar.MONTH,1);
                calTo.set(Calendar.DAY_OF_MONTH,1);
                calTo.add(Calendar.MONTH,-1);
                queryMonth = "select  nodeIds,SUM(nodeSize) as count  from vsat_local.topology_node_to_node_month y where y.start_time>= '"+
                        df.format(calFrom.getTime()) +"' and y.start_time<= '"+df.format(calTo.getTime())+"' group by (nodeIds,startTime) ";
            }else {
                calFrom.setTime(df.parse(startTime));
                calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                calFrom.set(Calendar.HOUR_OF_DAY,0);
                calFrom.clear(Calendar.MINUTE);
                calFrom.clear(Calendar.SECOND);
                calFrom.clear(Calendar.MILLISECOND);
                calFrom.set(Calendar.DAY_OF_MONTH,1);
                calFrom.add(Calendar.MONTH,1);
                calTo.setTime(df.parse(endTime));
                calTo.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                calTo.set(Calendar.HOUR_OF_DAY,0);
                calTo.clear(Calendar.MINUTE);
                calTo.clear(Calendar.SECOND);
                calTo.clear(Calendar.MILLISECOND);
                queryMonth = "select  nodeIds,SUM(nodeSize) as count  from vsat_local.topology_node_to_node_month y where  ( y.start_time>= '"+
                        df.format(calFrom.getTime()) +"'";
                calFrom.set(Calendar.DAY_OF_MONTH,1);
                calFrom.add(Calendar.MONTH,-1);
                calFrom.set(Calendar.MONTH,11);
                queryMonth += " and y.start_time<= '" +df.format(calFrom.getTime())+"' ) or (";
                calTo.set(Calendar.DAY_OF_MONTH,1);
                calTo.add(Calendar.MONTH,-1);
                queryMonth += "y.start_time<= '"+df.format(calTo.getTime())+"' and ";
                calTo.add(Calendar.MONTH,1);
                calTo.set(Calendar.MONTH,0);
                queryMonth += "y.start_time>= '"+df.format(calTo.getTime())+"') group by (nodeIds,startTime) ";
            }

            String query = "select a.nodeIds, max(a.count) as count  from( ";
            if(queryDay.length()>1){
                query+=queryDay;
            }
            if(queryMonth.length()>1){
                query+= " union  " +queryMonth;
            }
            if(queryYear.length()>1){
                query+= " union  " +queryYear;
            }
            query+= ") a group by a.nodeIds order by  count desc";
            stringBuilder.append(query);
            stringBuilder.append(" LIMIT ").append(size).append(" OFFSET ").append(size * page);


            List<Object[]> result = session.createNativeQuery(stringBuilder.toString()).getResultList();
            List<String> tmp = result.stream().map(item -> item[0].toString()).collect(Collectors.toList());

            return tmp;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }

    public void addMonthlyPartition(String[] tables) {
        Session session = this.openSession();

        try {
            String fromPartitonValue = DateUtil.getPartitionValueOfCurrentMonth();
            String toPartitonValue = DateUtil.getPartitionValueOfNextMonth();
            Transaction tx = session.beginTransaction();
            String partitionName = null;
            String[] var9 = tables;
            int var10 = tables.length;

            for(int var11 = 0; var11 < var10; ++var11) {
                String table = var9[var11];
                partitionName = DateUtil.getPartitionNameOfNextMonth(table);
                String strSql = " CREATE TABLE " + partitionName + " PARTITION OF " + table + " FOR VALUES FROM (TO_TIMESTAMP('" + fromPartitonValue + "', 'YYYY-MM-DD HH24:MI:SS'))  TO (TO_TIMESTAMP('" + toPartitonValue + "', 'YYYY-MM-DD HH24:MI:SS')) ";
                Query query = session.createNativeQuery(strSql);
                query.executeUpdate();
            }

            tx.commit();
        } catch (Exception var16) {
            LOGGER.error(var16.toString());
        } finally {
            this.closeSession(session);
        }

    }
    public void addYearlyPartition(String[] tables) {
        Session session = this.openSession();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date now = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.MONTH,0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            cal.add(Calendar.YEAR, 1);
            String fromPartitonValue = dff.format(cal.getTime());
            cal.add(Calendar.YEAR, 1);
            String toPartitonValue = dff.format(cal.getTime());
            Transaction tx = session.beginTransaction();
            String partitionName = null;
            String[] var9 = tables;
            int var10 = tables.length;

            for(int var11 = 0; var11 < var10; ++var11) {
                String table = var9[var11];
                partitionName = DateUtil.getPartitionNameOfNextYear(table);
                String strSql = " CREATE TABLE " + partitionName + " PARTITION OF " + table + " FOR VALUES FROM ('" + fromPartitonValue + "')  TO ('" + toPartitonValue + "') ";
                Query query = session.createNativeQuery(strSql);
                query.executeUpdate();
            }

            tx.commit();
        } catch (Exception var16) {
            LOGGER.error(var16.toString());
        } finally {
            this.closeSession(session);
        }

    }

    public long countKey(String startDate, String endDate) {
        Session session = openSession();
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            //List
            //List
            stringBuilder.append("select count(DISTINCT(parent_id)) as count"
                    + " FROM event e WHERE 1 = 1 ");
            if (startDate != null && endDate != null) {
                stringBuilder.append(" AND start_time >='").append(startDate).append("' ");
                stringBuilder.append(" AND start_time <='").append(endDate).append("' ");
            }
            // Nếu ko truyền sortBy thì mặc định sort theo startTime DESC
            Object countResult = session.createNativeQuery(stringBuilder.toString()).getSingleResult();
            Long count = 0L;
            if (countResult != null) {
                count = Long.parseLong(countResult.toString());
            }
            return count;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return 0L;
    }
}
