package elcom.com.neo4j.Schedule;

//import com.elcom.itscore.recognition.flink.clickhouse.service.RecognitionPlateClickHouseService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import elcom.com.neo4j.analysis.*;
import elcom.com.neo4j.clickhouse.model.Ais;
import elcom.com.neo4j.clickhouse.model.VsatMedia;
import elcom.com.neo4j.dto.*;
import elcom.com.neo4j.clickhouse.service.MetaCenMediaService;
import elcom.com.neo4j.redis.RedisRepository;
import elcom.com.neo4j.repositoryPostgre.CustomerRepository;
import elcom.com.neo4j.service.*;
import elcom.com.neo4j.service.impl.ObjectServiceImpl;
import org.apache.commons.io.IOUtils;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.util.Collector;
//import org.apache.spark.api.java.function.MapFunction;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
//import org.apache.spark.sql.catalyst.encoders.RowEncoder;
//import org.apache.spark.sql.types.DataTypes;
//import org.apache.spark.sql.types.StructField;
//import org.apache.spark.sql.types.StructType;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseDataSource;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author anhdv
 */
@Configuration
@Service
public class Schedulers {

    @Value("${jobs.enabled:true}")
    private boolean isEnabled;
    
    @Value("${aggregate.enabled:false}")
    private boolean aggregateEnabled;

    @Autowired
    private Environment environment;

    @Autowired
    private NodeImportantService nodeImportantService;

//    @Autowired
//    private SparkSession sparkSession;

    @Autowired
    private MetaCenMediaService vsatMediaService;

    @Autowired
    private ObjectServiceImpl objectService;

    @Autowired
    private RedisRepository redisRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedulers.class);

//    public String s02021 = "2021-06-08 00:00:00";
//    public String s02022 = "2022-01-01 21:00:00";

//        @Scheduled(cron = " 0 */15  * * * *")// Chạy vào 15
//    @Scheduled( fixedDelayString = "1000")
    public void analyticHour() throws Exception {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        LogFactory.release(contextLoader);
        ObjectMapper mapper = new ObjectMapper();
//        redisRepository.saveKeyTime("2022-03-09 09:00:00");\
        List<VsatMedia> vsatMedias= new ArrayList<>();
        Date nowCheck = new Date();
        Date now = new Date();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ClassLoader classLoader = getClass().getClassLoader();
        FileInputStream fis = new FileInputStream("config/ReportTime.txt");
        String s02022 = IOUtils.toString(fis, "UTF-8");
        LOGGER.info("load config s02022 {}",s02022);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dff.parse(s02022));
        cal.add(Calendar.MINUTE, 50);
        if (cal.getTime().getTime() < nowCheck.getTime()) {
            String endTime;
            cal.setTime(dff.parse(s02022));
            cal.add(Calendar.MINUTE, 62);
            if (cal.getTime().getTime() < nowCheck.getTime()) {
                cal.add(Calendar.MINUTE, -2);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(cal.getTime());
                cal2.add(Calendar.HOUR, 1);
                if (cal2.getTime().getTime() < nowCheck.getTime()) {

                    vsatMedias = vsatMediaService.findSearch2(s02022, dff.format(cal.getTime()));
                    processFlink(vsatMedias, s02022, dff.format(cal.getTime()));

                    vsatMedias = vsatMediaService.findSearch2(dff.format(cal.getTime()), dff.format(cal2.getTime()));
                    processFlink(vsatMedias,dff.format(cal.getTime()), dff.format(cal2.getTime()));
                    endTime = dff.format(cal2.getTime());
//                    process(vsatMedias, dff.format(cal.getTime()), endTime);
                } else {
                    vsatMedias = vsatMediaService.findSearch2(s02022, dff.format(cal.getTime()));
                    processFlink(vsatMedias, s02022, dff.format(cal.getTime()));
//                    vsatMedias = vsatMediaService.findSearch2(dff.format(cal.getTime()), dff.format(nowCheck.getTime()));
                    endTime = dff.format(cal.getTime());
//                    process(vsatMedias,dff.format(cal.getTime()),endTime);
                }
            } else {
                endTime = dff.format(nowCheck.getTime());
                vsatMedias = vsatMediaService.findSearch2(s02022, endTime);
                processFlink(vsatMedias, s02022, endTime);
//                    addAis(s02022, endTime);
            }

//        vsatMedias = vsatMediaService.findSearch2(s02022,endTime);
            BufferedWriter writer = new BufferedWriter(new FileWriter("config/ReportTime.txt", false));
            writer.write(endTime);
            writer.close();
            Date end = new Date();
            LOGGER.info("{} - {}",end,now);
            System.out.println("ádas");
        }else {
//                vsatMedias = vsatMediaService.findSearch2(s02022, dff.format(cal.getTime()));
//                process(vsatMedias, s02022, dff.format(cal.getTime()));
//                String endTime = dff.format(cal.getTime());
//                BufferedWriter writer = new BufferedWriter(new FileWriter("config/ReportTime.txt", false));
//                writer.write(endTime);
//                writer.close();
        }

    }

    private void processFlink(List<VsatMedia> vsatMedias,String startTime,String endTime) throws Exception {
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> resultMedia =null;
        List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> resultAis =null;
        String now = new Date().toString();
        Long count = 0L;
        String a = vsatMedias.toString();
        if (vsatMedias != null && !vsatMedias.isEmpty()) {
//            processMapToObjectMedia(vsatMedias,startTime,endTime);
            ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
            DataSet<List<VsatMedia>> text = env.fromElements(vsatMedias);
            DataSet<Tuple11<String,String, String, String, Long, Long, String,String, String, String, String>>  counts = ((DataSet) text).flatMap(new ReadValueClickHouse()).groupBy(new int[]{0,7,10}).reduceGroup(new DistinctSrcAndDest());
            List<Tuple11<String,String, String, String, Long, Long, String,String, String, String, String>> result = counts.collect();
            String b = result.toString();
            objectService.saveObjectUpdateTest(result, "metacenhour", startTime, endTime);
            count=Long.valueOf(result.size());
            System.out.println("oke meida ");
            resultMedia=processReportDayAndMonth(counts,startTime,endTime);
            env=null;
            text=null;
            counts=null;
        } else {
            System.out.println("no meida");
        }
        String out = "xu ly lưu Media :" + count + now + "-" + " den" +  new Date().toString();
        LOGGER.info(out);
        resultAis = addAis(startTime, endTime);
        reportNodeToNode(resultMedia,resultAis);

//        reportNodeToNode()


    }

    private List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> processReportDayAndMonth(DataSet<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> data,String startTime,String endTime) throws Exception {
        DataSet<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> countDay = ((DataSet) data).flatMap(new ReadValueToDay()).groupBy(new int[]{0,7,10}).reduceGroup(new DistinctSrcAndDest());
        List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> result = countDay.collect();
        objectService.insertDeleteUpdate(result,"dsf",startTime,endTime,1);
        List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> resultMonth = countDay.flatMap(new ReadValueToMonth()).groupBy(new int[]{0,7,10}).reduceGroup(new DistinctSrcAndDest()).collect();
        objectService.insertDeleteUpdate(resultMonth,"dsf",startTime,endTime,2);
        return result;
    }

    private void reportNodeToNode( List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> media,  List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> ais) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> datas = new ArrayList<>();
        if(media!=null&&!media.isEmpty()){
            datas.addAll(media);
        }
        if(ais!=null&&!ais.isEmpty()){
            datas.addAll(ais);
        }
        if(!datas.isEmpty()) {
            DataSet<List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>>> text = env.fromElements(datas);
            DataSet<Tuple4<String, String, String, Long>> counts = text.flatMap(new ReadValueNodeImportant()).distinct().groupBy(new int[]{0, 2}).sum(3);
            List<Tuple4<String, String, String, Long>> dataDay = counts.collect();
            saveDay(dataDay);
            List<Tuple4<String, String, String, Long>> dataMonth = counts.flatMap(new ReadValueNodeImportantMonth()).distinct().groupBy(new int[]{0, 2}).sum(3).collect();
            saveMonth(dataMonth);
        }
    }

    private void saveDay(List<Tuple4<String, String, String, Long>> data){
        List<elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeDay> dataSaves = new ArrayList<>();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeDay> finalDataSaves = dataSaves;
        data.stream().forEach((item)->{
            elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeDay nodeToNodeDay= new elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeDay();
            nodeToNodeDay.setId(UUID.randomUUID().toString());
            nodeToNodeDay.setNodeIds(item.f0);
            try {
                nodeToNodeDay.setStartTime(dff.parse(item.f2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            nodeToNodeDay.setNodeSize(item.f3);
            finalDataSaves.add(nodeToNodeDay);
        });
        while (dataSaves.size() > 1000) {
                    List<elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeDay> listSave = dataSaves.subList(0, 1000);
                    nodeImportantService.saveMultiDay(listSave);
                dataSaves = dataSaves.subList(1000, dataSaves.size());
        }
        nodeImportantService.saveMultiDay(dataSaves);

    }

    private void saveMonth(List<Tuple4<String, String, String, Long>> data){
        List<elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeMonth> dataSaves = new ArrayList<>();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeMonth> finalDataSaves = dataSaves;
        data.stream().forEach((item)->{
            elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeMonth nodeToNodeDay= new elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeMonth();
            nodeToNodeDay.setId(UUID.randomUUID().toString());
            nodeToNodeDay.setNodeIds(item.f0);
            try {
                nodeToNodeDay.setStartTime(dff.parse(item.f2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            nodeToNodeDay.setNodeSize(item.f3);
            finalDataSaves.add(nodeToNodeDay);
        });
        while (dataSaves.size() > 1000) {
            List<elcom.com.neo4j.clickhouse.model.TopologyNodeToNodeMonth> listSave = dataSaves.subList(0, 1000);
            nodeImportantService.saveMultiMonth(listSave);
            dataSaves = dataSaves.subList(1000, dataSaves.size());
        }
        nodeImportantService.saveMultiMonth(dataSaves);

    }

    private  List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> addAis(String startTime, String endTime) throws Exception {
        Date now= new Date();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(dff.parse(startTime));
        List<Ais> vsatMedias = vsatMediaService.findAis(startTime,endTime);
        Long count = 0L;
        if (vsatMedias != null && !vsatMedias.isEmpty()) {
//            processMapToObjectAis(vsatMedias, startTime, endTime);
            ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
            DataSet<List<Ais>> text = env.fromElements(vsatMedias);
            DataSet<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> counts = ((DataSet) text).flatMap(new ReadValueAis());
            List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> result = counts.collect();
            count=Long.valueOf(vsatMedias.size());
            objectService.saveObjectUpdateTest(result, "metacenhour", startTime,endTime);
            String out = "xu ly ais :" + count + now + "-" +" den"+ new Date().toString();
            result=processReportDayAndMonth(counts,startTime,endTime);
            LOGGER.info(out);
            text=null;
            counts=null;
            env=null;
            return result;
        }
        return null;
    }

    public class groupKey implements ReduceFunction<TestReduce> {


        @Override
        public TestReduce reduce(TestReduce stringLongLongTuple3, TestReduce t1) throws Exception {
            return t1;
        }
    }


    public static void saveListH(ClickHouseDataSource dataSource,List<Tuple2<String,Long>> values){
        try {
            ClickHouseConnection comn = dataSource.getConnection();
            String sql = "insert into report_h(id,key,total,start_time,source_id,object_type,brand,color,plate_color)"
                    + " VALUES " ;
            for (Tuple2<String,Long> value: values
            ) {
                sql = sql +"(?,?,?,?,?,?,?,?,?),";
            }
            sql = sql.substring(0,sql.length()-1);
            int index =1 ;
            PreparedStatement pstmt = comn.prepareStatement(sql);
            for (Tuple2<String,Long> value: values
            ) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DateFormat yyf = new SimpleDateFormat("yy");
                String data = value.f0;
                Long total = Long.valueOf(value.f1);
                String sourceId = data.substring(0,data.indexOf("_"));
                data= data.substring(data.indexOf("_")+1);
                String objectType = data.substring(0,data.indexOf("_"));
                data= data.substring(data.indexOf("_")+1);
                String color= data.substring(0,data.indexOf("_"));
                data= data.substring(data.indexOf("_")+1);
                String brand = data.substring(0,data.indexOf("_"));
                data= data.substring(data.indexOf("_")+1);
                String plateColor= data.substring(0,data.indexOf("_"));
                data= data.substring(data.indexOf("_")+1);
                String h= data.substring(0,data.indexOf("_"));
                data= data.substring(data.indexOf("_")+1);
                String key = value.f0.substring(0,value.f0.length()-11);
                String time = value.f0.substring(value.f0.length()-13);
                DateFormat dftmp = new SimpleDateFormat("HH_dd_MM_yyyy");

                pstmt.setString(index++,UUID.randomUUID().toString());
                pstmt.setString(index++,value.f0);
                pstmt.setLong(index++,total);
                pstmt.setString(index++,df.format(dftmp.parse(time)));
                pstmt.setString(index++,sourceId);
                pstmt.setString(index++,objectType);
                pstmt.setString(index++,brand);
                pstmt.setString(index++,color);
                pstmt.setString(index++,plateColor);
            }
            pstmt.execute();
        } catch (Exception e){
            e.printStackTrace();

        }

    }


    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        return scheduler;
    }
}
