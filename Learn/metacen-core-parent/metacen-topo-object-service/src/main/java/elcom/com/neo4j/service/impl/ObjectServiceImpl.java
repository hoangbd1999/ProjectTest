package elcom.com.neo4j.service.impl;

import com.opencsv.CSVWriter;
import elcom.com.neo4j.Schedule.Schedulers;
import elcom.com.neo4j.dto.AisValueSpark;
import elcom.com.neo4j.dto.DataNeo4j;
import elcom.com.neo4j.dto.ResponseDto;
import elcom.com.neo4j.node.ValueReport;
import elcom.com.neo4j.rabbitmq.RabbitMQProperties;
import elcom.com.neo4j.redis.RedisRepository;
import org.apache.flink.api.java.tuple.Tuple11;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.types.Row;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.internal.value.NullValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author Michael Hunger
 * @author Mark Angrish
 * @author Jennifer Reif
 * @author Michael J. Simons
 */
@Service
public class ObjectServiceImpl {

	@Value("${upload.url}")
	public String UPLOAD;

	private final ObjectRepository movieRepository;

	private final Neo4jClient neo4jClient;

	private final Driver driver;

	private final DatabaseSelectionProvider databaseSelectionProvider;

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectServiceImpl.class);

	@Autowired
	private RedisRepository redisRepository;

	@Autowired
	private SaveMutil saveMutil;

	ObjectServiceImpl(ObjectRepository movieRepository,
					  Neo4jClient neo4jClient,
					  Driver driver,
					  DatabaseSelectionProvider databaseSelectionProvider) {

		this.movieRepository = movieRepository;
		this.neo4jClient = neo4jClient;
		this.driver = driver;
		this.databaseSelectionProvider = databaseSelectionProvider;
	}

//	public MovieDetailsDto fetchDetailsByTitle(String title) {
//		return this.neo4jClient
//				.query("" +
//						"MATCH (movie:Movie {title: $title}) " +
//						"OPTIONAL MATCH (person:Person)-[r]->(movie) " +
//						"WITH movie, COLLECT({ name: person.name, job: REPLACE(TOLOWER(TYPE(r)), '_in', ''), role: HEAD(r.roles) }) as cast " +
//						"RETURN movie { .title, cast: cast }"
//				)
//				.in(database())
//				.bindAll(Map.of("title", title))
//				.fetchAs(MovieDetailsDto.class)
//				.mappedBy(this::toMovieDetails)
//				.one()
//				.orElse(null);
//	}


	public int saveObjectUpdate(List<Tuple11<String,String,String,String,Long,Long,Integer,String,String,String,String>> datas, String key, String timeStart, String timeEnd) {
//		String createDate = timeStart;
//		key = "metatest";
//		List<String> keyNode = new ArrayList<>();
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
////		dbquery="";
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//		Calendar cal = Calendar.getInstance();
//
//		for (Tuple11<String,String,String,String,Long,Long,Integer,String,String,String,String> data: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = data.f0;
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = data.f7;
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//
//			String querySrc = "(" + srcId + ":Object {name: '" + data.f2+ "',ids: '" + data.f0
//					+ "',ips: '" + data.f1 + "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + data.f9+ "',ids: '" + data.f7
//					+ "',ips: '" + data.f8 + "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + data.f0
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + data.f7
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			setMatch.add(matchQueryDest);
//			setMatch.add(matchQuerySrc);
//
//			relation += "(" + srcId + ") - [" + uuid + ": MEDIA { ";
//			int check = 0;
//			for (Row tmp : data.getMedias()
//			) {
//				if (check == 0) {
//					check++;
//				} else {
//					relation += ",";
//				}
//				relation += tmp.getAs("typeName") + "Count:" + tmp.getAs("typeSize");
//				relation += "," + tmp.getAs("typeName") + "FileSize:" + tmp.getAs("fileSize");
//				Long count1 = tmp.getAs("typeSize");
//				count += count1;
//				timeStart = tmp.getAs("eventTime");
//				try {
//					cal.setTime(df.parse(timeStart));
//					cal.add(Calendar.HOUR,1);
//					timeEnd= df.format(cal.getTime());
//				}catch (Exception ex){
//
//				}
//
//			}
//			relation += ",count:" + count;
//			relation += ",createDate:'" + createDate+"'";
//			relation += ",startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//			relation += ",src:'" + row.getAs("id")+"',dest:'"+row.getAs("destId")+"'";
//			relation += "}] -> ("
//					+ destId + "),";
//		}
//		List<String> nodeCreated = redisRepository.findNode(keyNode);
//		List<String> nodeCreate = set.stream().filter(c-> nodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				dbquery = " USE fabric."+"vsat2022";
//				for (String tmp : nodeCreate
//				) {
//					dbquery =" USE fabric."+"vsat2022"+ " MERGE " + tmp ;
//					try {
//						this.neo4jClient
//								.query(dbquery)
//								.in(database())
//								.run()
//								.counters()
//								.propertiesSet();
//					}catch (Exception exx){
//						String ids = tmp.substring(tmp.indexOf("ids")+4,tmp.indexOf("ips")-1);
//						String query = " USE fabric."+"vsat2022" +" MATCH (m:Object)  where m.ids ="+ids;
//						query += " set m +=";
//						String node = tmp.substring(tmp.indexOf("{"),tmp.length()-1);
//						query += node;
//						this.neo4jClient
//								.query(query)
//								.in(database())
//								.run()
//								.counters()
//								.propertiesSet();
//					}
//
//				}
//
////				dbquery = dbquery.substring(0, dbquery.length() - 1);
//
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				redisRepository.saveNode(keyToCreateNote.get(tmp), tmp);
//			}
//			dbquery = " USE fabric." + key;
////			dbquery="";
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//
//		dbquery +=relation;
//
//		this.neo4jClient
//				.query( dbquery )
//				.in( database() )
//				.run()
//				.counters()
//				.propertiesSet();
		return 1;

	}
	public void deleteRelation(String query,String database){
		this.neo4jClient
				.query(query)
				.in(database)
				.run()
				.counters()
				.propertiesSet();
	}

	public int saveObjectUpdateTest(List<Tuple11<String,String, String, String, Long, Long, String,String, String, String, String>> datas, String database, String startTime, String endTime) {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String path = "import"+ df.format(now)+".csv";
		File file = new File(path);
		DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			FileWriter outputfile = new FileWriter(file);

			// create CSVWriter with '|' as separator
			CSVWriter writer = new CSVWriter(outputfile, '|',
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END);

			// create a List which contains String array
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "id","srcIp","name","typeName","count","fileSize","dataSource","destId","destIp","destName","startTime","endTime","createDate" });
			datas.stream().forEach((item)->{
				List<String> tmp = new ArrayList<>();
				tmp.add(item.f0);
				tmp.add(item.f1);
				tmp.add(item.f2);
				tmp.add(item.f3);
				tmp.add(item.f4.toString());
				tmp.add(item.f5.toString());
				tmp.add(item.f6);
				tmp.add(item.f7);
				tmp.add(item.f8);
				tmp.add(item.f9);
				tmp.add(item.f10);
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(dff.parse(item.f10));
					cal.add(Calendar.HOUR,1);
					String timeEndTmp= dff.format(cal.getTime());
					tmp.add(timeEndTmp);
				} catch (ParseException e) {
					tmp.add(endTime);
					e.printStackTrace();
				}
				tmp.add(startTime);
				String[] dataTmp = tmp.toArray(new String[13]);
				data.add(dataTmp);

			});
			writer.writeAll(data);
			writer.close();
			String url =uploadFile(file);
			String a ="USING PERIODIC COMMIT 1000 LOAD CSV WITH HEADERS FROM \""+url+"\" AS row FIELDTERMINATOR '|'\n" +
					"MERGE(a:Object{name:row.name,mmsi:row.id})\n" +
					"MERGE(b:Object{name:row.destName,mmsi:row.destId})\n" +
					"WITH a, b, row\n" +
					"CALL apoc.create.relationship(a, row.typeName, {startTime:row.startTime,endTime:row.endTime,createDate:row.createDate,count:row.count,fileSize:row.fileSize,src:row.id,dest:row.destId,srcIp:row.srcIp,destIp:row.destIp,dataSource:row.dataSource},b) YIELD rel\n" +
					"RETURN rel;";
				this.neo4jClient
						.query(a)
						.in(database)
						.run()
						.counters()
						.propertiesSet();
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return 1;

	}

	private String uploadFile(File path) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(path));
		body.add("keepFileName", true);
		body.add("localUpload ", true);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<ResponseDto> response = restTemplate.postForEntity(UPLOAD, requestEntity, ResponseDto.class);
		if (response != null && response.getBody() != null && response.getBody().getData() != null) {
            path.delete();
			return response.getBody().getData().getFileDownloadUri();
		}
		return null;
	}

	public int deleteRelationMedia( String timeStart, String timeEnd, String key) {
		List<String> keyNode = new ArrayList<>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dbquery = " USE fabric."+key;
		dbquery += " Match (a:Object) -[r:MEDIA]- (b:Object) where r.startTime >='"+timeStart+"' and r.startTime < '" +timeEnd+ "' delete r";

		this.neo4jClient
				.query( dbquery )
				.in( database() )
				.run()
				.counters()
				.propertiesSet();
		return 1;

	}
	public int deleteRelationAIS( String timeStart, String timeEnd, String key) {
		List<String> keyNode = new ArrayList<>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dbquery = " USE fabric."+key;
		dbquery += " Match (a:Object) -[r:AIS]- (b:Object) where r.startTime >='"+timeStart+"' and r.startTime < '" +timeEnd+ "' delete r";

		this.neo4jClient
				.query( dbquery )
				.in( database() )
				.run()
				.counters()
				.propertiesSet();
		return 1;

	}

	public int saveReport(List<Row> datas, String key, String timeStart, String timeEnd, Integer type) {
//		List<String> keyNode = new ArrayList<>();
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//
//		for (Row row: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = row.getAs("idsSrc");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("idsDest");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("nameSrc")+ "',ids: '" + row.getAs("idsSrc")
//					+ "',ips: '" + row.getAs("ipsSrc") + "',longitude: '" + row.getAs("longitudeSrc") + "',latitude: '" + row.getAs("latitudeSrc")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("nameDest") +"',ids: '" + row.getAs("idsDest")
//					+ "',ips: '" + row.getAs("ipsDest") + "',longitude: '" + row.getAs("longitudeDest") + "',latitude: '" + row.getAs("latitudeDest")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("idsSrc")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("idsDest")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			setMatch.add(matchQueryDest);
//			setMatch.add(matchQuerySrc);
//
//			relation += "(" + srcId + ") - [" + uuid + ": MEDIA { ";
//			relation += "count:" + row.getAs("count");
//			Object a = row.getAs("WebCount");
//			Long value = row.getAs("WebCount");
//			if(value>0){
//				relation += ",WebCount" + ":" + value;
//				relation += ",WebFileSize" + ":" + row.getAs("WebFileSize");
//			}
//
//			value = row.getAs("VoiceCount");
//			if(value>0){
//				relation += ",VoiceCount" + ":" + value;
//				relation += ",VoiceFileSize" + ":" + row.getAs("VoiceFileSize");
//			}
//			value = row.getAs("TransferFileCount");
//			if(value>0){
//				relation += ",TransferFileCount" + ":" + value;
//				relation += ",TransferFileFileSize" + ":" + row.getAs("TransferFileFileSize");
//			}
//			value = row.getAs("VideoCount");
//			if(value>0){
//				relation += ",VideoCount" + ":" + value;
//				relation += ",VideoFileSize" + ":" + row.getAs("VideoFileSize");
//			}
//			value = row.getAs("EmailCount");
//			if(value>0){
//				relation += ",EmailCount" + ":" + value;
//				relation += ",EmailFileSize" + ":" + row.getAs("EmailFileSize");
//			}
//			relation += ",startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//			relation += ",src:'" + row.getAs("idsSrc")+"',dest:'"+row.getAs("idsDest")+"'";
//			relation += "}] -> ("
//					+ destId + "),";
//		}
//		List<String> nodeCreated = new ArrayList<>();
//		if(type==1){
//			nodeCreated = redisRepository.findNodeDay(keyNode);
//		} else if(type==2){
//			nodeCreated = redisRepository.findNodeMonth(keyNode);
//		} else if(type==3){
//			nodeCreated = redisRepository.findNodeYear(keyNode);
//		} else if(type==4){
//			nodeCreated = redisRepository.findNodeWeek(keyNode);
//		}
//		List<String> finalNodeCreated = nodeCreated;
//		List<String> nodeCreate = set.stream().filter(c-> finalNodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				dbquery = " ";
//				for (String tmp : nodeCreate
//				) {
//					dbquery += " MERGE " + tmp ;
//				}
////				dbquery = dbquery.substring(0, dbquery.length() - 1);
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				if(type==1){
//					redisRepository.saveNodeDay(keyToCreateNote.get(tmp), tmp);
//				} else if(type==2){
//					redisRepository.saveNodeMonth(keyToCreateNote.get(tmp), tmp);
//				} else if(type==3){
//					redisRepository.saveNodeYear(keyToCreateNote.get(tmp), tmp);
//				} else if(type==4){
//					redisRepository.saveNodeWeek(keyToCreateNote.get(tmp), tmp);
//				}
//
//			}
//			dbquery = " USE fabric." + key;
////			dbquery="";
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//
//		dbquery +=relation;
//
//		this.neo4jClient
//				.query( dbquery )
//				.in( database() )
//				.run()
//				.counters()
//				.propertiesSet();
		return 1;

	}

//	public int saveReportCheck(List<Row> datas, String key, String timeStart, String timeEnd, Integer type) {
//		List<String> keyNode = new ArrayList<>();
//		String createTime = timeStart;
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//		Calendar cal = Calendar.getInstance();
//
//		for (Row row: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = row.getAs("idsSrc");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("idsDest");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("nameSrc")+ "',ids: '" + row.getAs("idsSrc")
//					+ "',ips: '" + row.getAs("ipsSrc") + "',longitude: '" + row.getAs("longitudeSrc") + "',latitude: '" + row.getAs("latitudeSrc")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("nameDest") +"',ids: '" + row.getAs("idsDest")
//					+ "',ips: '" + row.getAs("ipsDest") + "',longitude: '" + row.getAs("longitudeDest") + "',latitude: '" + row.getAs("latitudeDest")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("idsSrc")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("idsDest")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			timeStart = row.getAs("dateTime");
//			try {
//				cal.setTime(df.parse(timeStart));
//				if(type==1) {
//					cal.add(Calendar.DAY_OF_MONTH, 1);
//				} else if(type==2){
//					cal.add(Calendar.MONTH, 1);
//				} else if(type==3){
//					cal.add(Calendar.YEAR, 1);
//				} else if(type==4){
//					cal.add(Calendar.WEEK_OF_YEAR, 1);
//				}
//				timeEnd= df.format(cal.getTime());
//			}catch (Exception ex){
//
//			}
//			List<ValueReport> valueReports = checkDataMedia(type,timeStart,timeEnd,row.getAs("idsSrc"),row.getAs("idsDest"));
//			if(valueReports==null || valueReports.isEmpty()) {
//				setMatch.add(matchQueryDest);
//				setMatch.add(matchQuerySrc);
//
//				relation += "(" + srcId + ") - [" + uuid + ": MEDIA { ";
//				relation += "count:" + row.getAs("count");
//				Object a = row.getAs("WebCount");
//				Long value = row.getAs("WebCount");
//				if (value > 0) {
//					relation += ",WebCount" + ":" + value;
//					relation += ",WebFileSize" + ":" + row.getAs("WebFileSize");
//				}
//
//				value = row.getAs("VoiceCount");
//				if (value > 0) {
//					relation += ",VoiceCount" + ":" + value;
//					relation += ",VoiceFileSize" + ":" + row.getAs("VoiceFileSize");
//				}
//				value = row.getAs("TransferFileCount");
//				if (value > 0) {
//					relation += ",TransferFileCount" + ":" + value;
//					relation += ",TransferFileFileSize" + ":" + row.getAs("TransferFileFileSize");
//				}
//				value = row.getAs("VideoCount");
//				if (value > 0) {
//					relation += ",VideoCount" + ":" + value;
//					relation += ",VideoFileSize" + ":" + row.getAs("VideoFileSize");
//				}
//				value = row.getAs("EmailCount");
//				if (value > 0) {
//					relation += ",EmailCount" + ":" + value;
//					relation += ",EmailFileSize" + ":" + row.getAs("EmailFileSize");
//				}
//				relation += ",createDate:'" + createTime;
//				relation += "',startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//				relation += ",src:'" + row.getAs("idsSrc") + "',dest:'" + row.getAs("idsDest") + "'";
//				relation += "}] -> ("
//						+ destId + "),";
//			} else {
//				saveData(valueReports,timeStart,timeEnd,row,key);
//			}
//		}
//		List<String> nodeCreated = new ArrayList<>();
//		if(type==1){
//			nodeCreated = redisRepository.findNodeDay(keyNode);
//		} else if(type==2){
//			nodeCreated = redisRepository.findNodeMonth(keyNode);
//		} else if(type==3){
//			nodeCreated = redisRepository.findNodeYear(keyNode);
//		} else if(type==4){
//			nodeCreated = redisRepository.findNodeWeek(keyNode);
//		}
//		List<String> finalNodeCreated = nodeCreated;
//		List<String> nodeCreate = set.stream().filter(c-> finalNodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				dbquery = " ";
//				for (String tmp : nodeCreate
//				) {
//					dbquery += " MERGE " + tmp ;
//				}
////				dbquery = dbquery.substring(0, dbquery.length() - 1);
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				if(type==1){
//					redisRepository.saveNodeDay(keyToCreateNote.get(tmp), tmp);
//				} else if(type==2){
//					redisRepository.saveNodeMonth(keyToCreateNote.get(tmp), tmp);
//				} else if(type==3){
//					redisRepository.saveNodeYear(keyToCreateNote.get(tmp), tmp);
//				} else if(type==4){
//					redisRepository.saveNodeWeek(keyToCreateNote.get(tmp), tmp);
//				}
//
//			}
//			dbquery = " USE fabric." + key;
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//		if(relation.contains("MEDIA")) {
//
//			dbquery += relation;
//
//			this.neo4jClient
//					.query(dbquery)
//					.in(database())
//					.run()
//					.counters()
//					.propertiesSet();
//		}
//		return 1;
//
//	}
	public int saveReportAisCheck(List<Row> datas, String key, String timeStart, String timeEnd, Integer type) {
//		List<String> keyNode = new ArrayList<>();
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
//		String createTime = timeStart;
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//		Calendar cal = Calendar.getInstance();
//		for (Row row: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = row.getAs("idsSrc");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("idsDest");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("nameSrc")+ "',ids: '" + row.getAs("idsSrc")
//					+ "',ips: '" + row.getAs("ipsSrc") + "',longitude: '" + row.getAs("longitudeSrc") + "',latitude: '" + row.getAs("latitudeSrc")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("nameDest") +"',ids: '" + row.getAs("idsDest")
//					+ "',ips: '" + row.getAs("ipsDest") + "',longitude: '" + row.getAs("longitudeDest") + "',latitude: '" + row.getAs("latitudeDest")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("idsSrc")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("idsDest")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			timeStart = row.getAs("dateTime");
//			try {
//				cal.setTime(df.parse(timeStart));
//				if(type==1) {
//					cal.add(Calendar.DAY_OF_MONTH, 1);
//				} else if(type==2){
//					cal.add(Calendar.MONTH, 1);
//				} else if(type==3){
//					cal.add(Calendar.YEAR, 1);
//				} else if(type==4){
//					cal.add(Calendar.WEEK_OF_YEAR, 1);
//				}
//				timeEnd= df.format(cal.getTime());
//			}catch (Exception ex){
//
//			}
//			List<ValueReport> valueReports = checkDataAIS(type,timeStart,timeEnd,row.getAs("idsSrc"),row.getAs("idsDest"));
//			if(valueReports==null || valueReports.isEmpty()) {
//				setMatch.add(matchQueryDest);
//				setMatch.add(matchQuerySrc);
//
//				relation += "(" + srcId + ") - [" + uuid + ": AIS { ";
//				relation += "count:" + row.getAs("count");
//				relation += ",createDate:'" + createTime;
//				relation += "',startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//				relation += ",src:'" + row.getAs("idsSrc") + "',dest:'" + row.getAs("idsDest") + "'";
//				relation += "}] -> ("
//						+ destId + "),";
//			} else {
//				saveDataAis(valueReports,timeStart,timeEnd,row,key);
//			}
//		}
//		List<String> nodeCreated = new ArrayList<>();
//		if(type==1){
//			nodeCreated = redisRepository.findNodeDay(keyNode);
//		} else if(type==2){
//			nodeCreated = redisRepository.findNodeMonth(keyNode);
//		} else if(type==3){
//			nodeCreated = redisRepository.findNodeYear(keyNode);
//		} else if(type==4){
//			nodeCreated = redisRepository.findNodeWeek(keyNode);
//		}
//		List<String> finalNodeCreated = nodeCreated;
//		List<String> nodeCreate = set.stream().filter(c-> finalNodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				dbquery = " ";
//				for (String tmp : nodeCreate
//				) {
//					dbquery += " MERGE " + tmp ;
//				}
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				if(type==1){
//					redisRepository.saveNodeDay(keyToCreateNote.get(tmp), tmp);
//				} else if(type==2){
//					redisRepository.saveNodeMonth(keyToCreateNote.get(tmp), tmp);
//				} else if(type==3){
//					redisRepository.saveNodeYear(keyToCreateNote.get(tmp), tmp);
//				} else if(type==4){
//					redisRepository.saveNodeWeek(keyToCreateNote.get(tmp), tmp);
//				}
//
//			}
//			dbquery = " USE fabric." + key;
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//		if(relation.contains("AIS")) {
//
//			dbquery += relation;
//
//			this.neo4jClient
//					.query(dbquery)
//					.in(database())
//					.run()
//					.counters()
//					.propertiesSet();
//		}
		return 1;

	}

	public int saveReportAisCheckAsyn(List<Row> datas, String key, String timeStart, String timeEnd, Integer type) throws ExecutionException, InterruptedException {
//		List<String> keyNode = new ArrayList<>();
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
//		String createTime = timeStart;
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//		Calendar cal = Calendar.getInstance();
//		List<CompletableFuture<ValueReport>> listResult = new ArrayList<>();
//		for (Row row: datas
//		) {
//			timeStart = row.getAs("dateTime");
//			try {
//				cal.setTime(df.parse(timeStart));
//				if(type==1) {
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
////					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.DAY_OF_MONTH, 1);
//				} else if(type==2){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.MONTH, 1);
//				} else if(type==3){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.MONTH, 0);
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.YEAR, 1);
//				} else if(type==4){
//					cal.add(Calendar.WEEK_OF_YEAR, 1);
//				}
//				timeEnd= df.format(cal.getTime());
//			}catch (Exception ex){
//
//			}
//			CompletableFuture<ValueReport> bridge = saveMutil.checkData(type,timeStart,timeEnd,row.getAs("idsSrc"),row.getAs("idsDest"));
//			listResult.add(bridge);
//		}
//
//		CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
//				.thenApply(v -> listResult.stream()
//						.map(CompletableFuture::join)
//						.collect(Collectors.toList())
//				);
//		Map<String,ValueReport> mapValue = new HashMap<>();
//		for (CompletableFuture<ValueReport> result : listResult) {
//			ValueReport valueReport = result.get();
//			if(valueReport!=null)
//				mapValue.put(valueReport.getIdsSrc()+valueReport.getIdsDest()+valueReport.getDateTime(),valueReport);
//		}
//		for (Row row: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = row.getAs("idsSrc");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("idsDest");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("nameSrc")+ "',ids: '" + row.getAs("idsSrc")
//					+ "',ips: '" + row.getAs("ipsSrc") + "',longitude: '" + row.getAs("longitudeSrc") + "',latitude: '" + row.getAs("latitudeSrc")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("nameDest") +"',ids: '" + row.getAs("idsDest")
//					+ "',ips: '" + row.getAs("ipsDest") + "',longitude: '" + row.getAs("longitudeDest") + "',latitude: '" + row.getAs("latitudeDest")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("idsSrc")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("idsDest")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			timeStart = row.getAs("dateTime");
//			try {
//				cal.setTime(df.parse(timeStart));
//				if(type==1) {
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
////					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.DAY_OF_MONTH, 1);
//				} else if(type==2){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.MONTH, 1);
//				} else if(type==3){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.MONTH, 0);
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.YEAR, 1);
//				} else if(type==4){
//					cal.add(Calendar.WEEK_OF_YEAR, 1);
//				}
//				timeEnd= df.format(cal.getTime());
//			}catch (Exception ex){
//
//			}
//			if(mapValue.get(row.getAs("idsSrc").toString()+row.getAs("idsDest").toString()+timeStart)==null) {
//				setMatch.add(matchQueryDest);
//				setMatch.add(matchQuerySrc);
//
//				relation += "(" + srcId + ") - [" + uuid + ": AIS { ";
//				relation += "count:" + row.getAs("count");
//				relation += ",createDate:'" + createTime;
//				relation += "',startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//				relation += ",src:'" + row.getAs("idsSrc") + "',dest:'" + row.getAs("idsDest") + "'";
//				relation += "}] -> ("
//						+ destId + "),";
//			} else {
//				List<ValueReport> valueReports = new ArrayList<>();
//				ValueReport valueReport = mapValue.get(row.getAs("idsSrc").toString()+row.getAs("idsDest").toString()+timeStart);
//				valueReports.add(valueReport);
//				saveDataAis(valueReports,timeStart,timeEnd,row,key);
//			}
//		}
//		List<String> nodeCreated = new ArrayList<>();
//		if(type==1){
//			nodeCreated = redisRepository.findNodeDay(keyNode);
//		} else if(type==2){
//			nodeCreated = redisRepository.findNodeMonth(keyNode);
//		} else if(type==3){
//			nodeCreated = redisRepository.findNodeYear(keyNode);
//		} else if(type==4){
//			nodeCreated = redisRepository.findNodeWeek(keyNode);
//		}
//		List<String> finalNodeCreated = nodeCreated;
//		List<String> nodeCreate = set.stream().filter(c-> finalNodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				for (String tmp : nodeCreate
//				) {
//					dbquery +=" USE fabric."+key+ " MERGE " + tmp ;
//					try {
//						this.neo4jClient
//								.query(dbquery)
//								.in(database())
//								.run()
//								.counters()
//								.propertiesSet();
//					}catch (Exception exx){
//						String ids = tmp.substring(tmp.indexOf("ids")+4,tmp.indexOf("ips")-1);
//						String query = " USE fabric."+key +" MATCH (m:Object)  where m.ids ="+ids;
//						query += " set m +=";
//						String node = tmp.substring(tmp.indexOf("{"),tmp.length()-1);
//						query += node;
//						this.neo4jClient
//								.query(query)
//								.in(database())
//								.run()
//								.counters()
//								.propertiesSet();
//					}
//
//				}
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				if(type==1){
//					redisRepository.saveNodeDay(keyToCreateNote.get(tmp), tmp);
//				} else if(type==2){
//					redisRepository.saveNodeMonth(keyToCreateNote.get(tmp), tmp);
//				} else if(type==3){
//					redisRepository.saveNodeYear(keyToCreateNote.get(tmp), tmp);
//				} else if(type==4){
//					redisRepository.saveNodeWeek(keyToCreateNote.get(tmp), tmp);
//				}
//
//			}
//			dbquery = " USE fabric." + key;
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//		if(relation.contains("AIS")) {
//
//			dbquery += relation;
////			saveMutil.saveNeo4j(dbquery);
//
//			this.neo4jClient
//					.query(dbquery)
//					.in(database())
//					.run()
//					.counters()
//					.propertiesSet();
//		}
		return 1;

	}

	public int saveReportCheck(List<Row> datas, String key, String timeStart, String timeEnd, Integer type) {
//		List<String> keyNode = new ArrayList<>();
//		String createTime = timeStart;
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//		Calendar cal = Calendar.getInstance();
//
//		for (Row row: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = row.getAs("idsSrc");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("idsDest");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("nameSrc")+ "',ids: '" + row.getAs("idsSrc")
//					+ "',ips: '" + row.getAs("ipsSrc") + "',longitude: '" + row.getAs("longitudeSrc") + "',latitude: '" + row.getAs("latitudeSrc")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("nameDest") +"',ids: '" + row.getAs("idsDest")
//					+ "',ips: '" + row.getAs("ipsDest") + "',longitude: '" + row.getAs("longitudeDest") + "',latitude: '" + row.getAs("latitudeDest")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("idsSrc")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("idsDest")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			timeStart = row.getAs("dateTime");
//			try {
//				cal.setTime(df.parse(timeStart));
//				if(type==1) {
//					cal.add(Calendar.DAY_OF_MONTH, 1);
//				} else if(type==2){
//					cal.add(Calendar.MONTH, 1);
//				} else if(type==3){
//					cal.add(Calendar.YEAR, 1);
//				} else if(type==4){
//					cal.add(Calendar.WEEK_OF_YEAR, 1);
//				}
//				timeEnd= df.format(cal.getTime());
//			}catch (Exception ex){
//
//			}
//			List<ValueReport> valueReports = checkDataMedia(type,timeStart,timeEnd,row.getAs("idsSrc"),row.getAs("idsDest"));
//			if(valueReports==null || valueReports.isEmpty()) {
//				setMatch.add(matchQueryDest);
//				setMatch.add(matchQuerySrc);
//
//				relation += "(" + srcId + ") - [" + uuid + ": MEDIA { ";
//				relation += "count:" + row.getAs("count");
//				Object a = row.getAs("WebCount");
//				Long value = row.getAs("WebCount");
//				if (value > 0) {
//					relation += ",WebCount" + ":" + value;
//					relation += ",WebFileSize" + ":" + row.getAs("WebFileSize");
//				}
//
//				value = row.getAs("VoiceCount");
//				if (value > 0) {
//					relation += ",VoiceCount" + ":" + value;
//					relation += ",VoiceFileSize" + ":" + row.getAs("VoiceFileSize");
//				}
//				value = row.getAs("TransferFileCount");
//				if (value > 0) {
//					relation += ",TransferFileCount" + ":" + value;
//					relation += ",TransferFileFileSize" + ":" + row.getAs("TransferFileFileSize");
//				}
//				value = row.getAs("VideoCount");
//				if (value > 0) {
//					relation += ",VideoCount" + ":" + value;
//					relation += ",VideoFileSize" + ":" + row.getAs("VideoFileSize");
//				}
//				value = row.getAs("EmailCount");
//				if (value > 0) {
//					relation += ",EmailCount" + ":" + value;
//					relation += ",EmailFileSize" + ":" + row.getAs("EmailFileSize");
//				}
//				relation += ",createDate:'" + createTime;
//				relation += "',startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//				relation += ",src:'" + row.getAs("idsSrc") + "',dest:'" + row.getAs("idsDest") + "'";
//				relation += "}] -> ("
//						+ destId + "),";
//			} else {
//				saveData(valueReports,timeStart,timeEnd,row,key);
//			}
//		}
//		List<String> nodeCreated = new ArrayList<>();
//		if(type==1){
//			nodeCreated = redisRepository.findNodeDay(keyNode);
//		} else if(type==2){
//			nodeCreated = redisRepository.findNodeMonth(keyNode);
//		} else if(type==3){
//			nodeCreated = redisRepository.findNodeYear(keyNode);
//		} else if(type==4){
//			nodeCreated = redisRepository.findNodeWeek(keyNode);
//		}
//		List<String> finalNodeCreated = nodeCreated;
//		List<String> nodeCreate = set.stream().filter(c-> finalNodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				dbquery = " ";
//				for (String tmp : nodeCreate
//				) {
//					dbquery += " MERGE " + tmp ;
//				}
////				dbquery = dbquery.substring(0, dbquery.length() - 1);
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				if(type==1){
//					redisRepository.saveNodeDay(keyToCreateNote.get(tmp), tmp);
//				} else if(type==2){
//					redisRepository.saveNodeMonth(keyToCreateNote.get(tmp), tmp);
//				} else if(type==3){
//					redisRepository.saveNodeYear(keyToCreateNote.get(tmp), tmp);
//				} else if(type==4){
//					redisRepository.saveNodeWeek(keyToCreateNote.get(tmp), tmp);
//				}
//
//			}
//			dbquery = " USE fabric." + key;
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//		if(relation.contains("MEDIA")) {
//
//			dbquery += relation;
//
//			this.neo4jClient
//					.query(dbquery)
//					.in(database())
//					.run()
//					.counters()
//					.propertiesSet();
//		}
		return 1;

	}

	public int insertUpdate(List<Tuple11<String,String,String,String,Long,Long,Integer,String,String,String,String>> datas, String key, String startTime, String endTime, Integer type) throws ExecutionException, InterruptedException {
		String database ="";
		if(type==1){
			database="metacenday";
		}else if(type==2){
			database="metacenmonth";
		}
		System.out.println(new Date().toString());
		Map<String,Integer> mapKeyToIndex = new HashMap<>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		List<CompletableFuture<ValueReport>> listResult = new ArrayList<>();
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "id", "ips", "name","typeName","count","fileSize","destId","destIps","destName","startTime","endTime","createDate" });
		for (Tuple11<String,String,String,String,Long,Long,Integer,String,String,String,String> row: datas
		) {
			startTime = row.f10;
			try {
				cal.setTime(df.parse(startTime));
				if(type==1) {
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());

					cal.add(Calendar.DAY_OF_MONTH, 1);
				} else if(type==2){
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());
					cal.add(Calendar.MONTH, 1);
				} else if(type==3){
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.MONTH, 0);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());
					cal.add(Calendar.YEAR, 1);
				} else if(type==4){
					cal.add(Calendar.WEEK_OF_YEAR, 1);
				}
				endTime= df.format(cal.getTime());
			}catch (Exception ex){

			}


			CompletableFuture<ValueReport> bridge = saveMutil.checkData(type,startTime,endTime,row.f0,row.f7,row.f3,null,1);
			listResult.add(bridge);

			List<String> tmp = new ArrayList<>();
			tmp.add(row.f0);
			tmp.add(row.f1);
			tmp.add(row.f2);
			tmp.add(row.f3);
			tmp.add(row.f4.toString());
			tmp.add(row.f5.toString());
			tmp.add(row.f7);
			tmp.add(row.f8);
			tmp.add(row.f9);
			tmp.add(row.f10);
			tmp.add(endTime);
			tmp.add(startTime);
			String[] dataTmp = tmp.toArray(new String[12]);
			data.add(dataTmp);
			mapKeyToIndex.put(row.f0+row.f7+row.f10+row.f3,data.size()-1);
		}

		CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
				.thenApply(v -> listResult.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toList())
				);
		LOGGER.info("count relatio: {}",data.size());
		List<String[]> dataRemove = new ArrayList<>();
		for (CompletableFuture<ValueReport> result : listResult) {
			ValueReport valueReport = result.get();
			if(valueReport!=null) {
				Integer a =mapKeyToIndex.get(valueReport.getIdsSrc() + valueReport.getIdsDest() + valueReport.getDateTime() + valueReport.getMediaType());
				String[] rowUpdate = data.get(a);
				dataRemove.add(rowUpdate);
//				saveMutil.saveData(valueReports,timeStart,timeEnd,row,key);
				saveMutil.updateRelation(valueReport,rowUpdate,database);

			}
		}
		data.removeAll(dataRemove);
		LOGGER.info("count relation insert sau update: {}", data.size());
		if(!data.isEmpty()&&data.size()>1) {
			Date now = new Date();
			df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String path = "import" + df.format(now) + ".csv";
			File file = new File(path);
			DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				FileWriter outputfile = new FileWriter(file);

				// create CSVWriter with '|' as separator
				CSVWriter writer = new CSVWriter(outputfile, '|',
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);

				writer.writeAll(data);
				writer.close();
				String url = uploadFile(file);
				String a = "USING PERIODIC COMMIT 1000 LOAD CSV WITH HEADERS FROM \"" + url + "\" AS row FIELDTERMINATOR '|'\n" +
						"MERGE(a:Object{id:row.id,ips:row.ips,name:row.name})\n" +
						"MERGE(b:Object{id:row.destId,ips:row.destIps,name:row.destName})\n" +
						"WITH a, b, row\n" +
						"CALL apoc.create.relationship(a, row.typeName, {startTime:row.startTime,endTime:row.endTime,createDate:row.createDate,count:row.count,fileSize:row.fileSize},b) YIELD rel\n" +
						"RETURN rel;";
				this.neo4jClient
						.query(a)
						.in(database)
						.run()
						.counters()
						.propertiesSet();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return 1;

	}

	public int insertDelete(List<Tuple11<String,String,String,String,Long,Long,Integer,String,String,String,String>> datas, String key, String startTime, String endTime, Integer type) throws ExecutionException, InterruptedException {
		String database ="";
		if(type==1){
			database="metacenday";
		}else if(type==2){
			database="metacenmonth";
		}
		Long start =System.currentTimeMillis();
		Long end =System.currentTimeMillis();
		System.out.println(new Date().toString());
		Map<String,Integer> mapKeyToIndex = new HashMap<>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		List<CompletableFuture<ValueReport>> listResult = new ArrayList<>();
		List<CompletableFuture<ValueReport>> listResultDelete = new ArrayList<>();
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "id", "ips", "name","typeName","count","fileSize","destId","destIps","destName","startTime","endTime","createDate","key" });
		List<String> listKey = new ArrayList<>();
		for (Tuple11<String,String,String,String,Long,Long,Integer,String,String,String,String> row: datas
		) {
			startTime = row.f10;
			try {
				cal.setTime(df.parse(startTime));
				if(type==1) {
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());

					cal.add(Calendar.DAY_OF_MONTH, 1);
				} else if(type==2){
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());
					cal.add(Calendar.MONTH, 1);
				} else if(type==3){
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.MONTH, 0);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());
					cal.add(Calendar.YEAR, 1);
				} else if(type==4){
					cal.add(Calendar.WEEK_OF_YEAR, 1);
				}
				endTime= df.format(cal.getTime());
			}catch (Exception ex){

			}
			List<String> tmp = new ArrayList<>();
			tmp.add(row.f0);
			tmp.add(row.f1);
			tmp.add(row.f2);
			tmp.add(row.f3);
			tmp.add(row.f4.toString());
			tmp.add(row.f5.toString());
			tmp.add(row.f7);
			tmp.add(row.f8);
			tmp.add(row.f9);
			tmp.add(row.f10);
			tmp.add(endTime);
			tmp.add(startTime);
			String keyCheck = row.f0+row.f7+row.f10+row.f3;
			tmp.add(keyCheck);
			String[] dataTmp = tmp.toArray(new String[13]);
			data.add(dataTmp);
			listKey.add(keyCheck);
			CompletableFuture<ValueReport> bridge = saveMutil.checkData(type,startTime,endTime,row.f0,row.f7,row.f3,dataTmp,data.size()-1);
			listResult.add(bridge);
			mapKeyToIndex.put(row.f0+row.f7+row.f10+row.f3,data.size()-1);
		}

		CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
				.thenApply(v -> listResult.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toList())
				);
		LOGGER.info("count relatio: {}",data.size());
		List<Long> idDelete = new ArrayList<>();
		end =System.currentTimeMillis();
		LOGGER.info("thoi gian xoa {}",end-start);
		start=System.currentTimeMillis();
		List<Long> finalIdDelete = idDelete;
		listResult.stream().forEach((result)->{
			try {
				ValueReport valueReport = result.get();
				if(valueReport!=null) {
					Integer a =mapKeyToIndex.get(valueReport.getIdsSrc() + valueReport.getIdsDest() + valueReport.getDateTime() + valueReport.getMediaType());
					String[] rowUpdate = data.get(a);
					Long count = Long.valueOf(rowUpdate[4])+valueReport.getCount();
					Long fileSize = Long.valueOf(rowUpdate[5])+valueReport.getFileSize();
					rowUpdate[4]=count.toString();
					rowUpdate[5]=fileSize.toString();
					data.set(a,rowUpdate);
					finalIdDelete.add(valueReport.getId());

				}
			}catch (Exception ex){

			}

		});
//		for (CompletableFuture<ValueReport> result : listResult) {
//
//		}
		end =System.currentTimeMillis();
		LOGGER.info("thoi gian chay for {}",end-start);
		start=System.currentTimeMillis();
		idDelete=finalIdDelete;
		if(!idDelete.isEmpty()){
			while (idDelete.size()>2000) {
				List<Long> listSave = idDelete.subList(0,2000);
				delete(listSave, database);
//				CompletableFuture<ValueReport> result = saveMutil.delete(listSave, database);
				idDelete = idDelete.subList(2000,idDelete.size());
//				listResultDelete.add(result);

			}
			delete(idDelete, database);
//			CompletableFuture<ValueReport> result=saveMutil.delete(idDelete, database);
//			listResultDelete.add(result);
		}
//		CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
//				.thenApply(v -> listResultDelete.stream()
//						.map(CompletableFuture::join)
//						.collect(Collectors.toList())
//				);

		end =System.currentTimeMillis();
		LOGGER.info("thoi gian check {}",end-start);
		start=System.currentTimeMillis();
		LOGGER.info("count relation insert sau update: {}", data.size());
		if(!data.isEmpty()&&data.size()>1) {
			Date now = new Date();
			df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String path = "import" + df.format(now) + ".csv";
			File file = new File(path);
			DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				FileWriter outputfile = new FileWriter(file);

				// create CSVWriter with '|' as separator
				CSVWriter writer = new CSVWriter(outputfile, '|',
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);

				writer.writeAll(data);
				writer.close();
				String url = uploadFile(file);
				String a = "USING PERIODIC COMMIT 1000 LOAD CSV WITH HEADERS FROM \"" + url + "\" AS row FIELDTERMINATOR '|'\n" +
						"MERGE(a:Object{id:row.id,ips:row.ips,name:row.name})\n" +
						"MERGE(b:Object{id:row.destId,ips:row.destIps,name:row.destName})\n" +
						"WITH a, b, row\n" +
						"CALL apoc.create.relationship(a, row.typeName, {startTime:row.startTime,endTime:row.endTime,createDate:row.createDate,count:row.count,fileSize:row.fileSize,key:row.key},b) YIELD rel\n" +
						"RETURN rel;";
				this.neo4jClient
						.query(a)
						.in(database)
						.run()
						.counters()
						.propertiesSet();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		end =System.currentTimeMillis();
		LOGGER.info("thoi gian check {}",end-start);
		return 1;

	}

	public void delete(List<Long> ids, String database){
		String id = ids.toString();
		String query = " Match (a:Object) -[r]->(b:Object) where id(r) in "+id+ " CALL { WITH r \n" +
				"DELETE r \n" +
				"} IN TRANSACTIONS OF 1000 ROWS ";
		this.neo4jClient
				.query(query)
				.in(database)
				.run()
				.counters()
				.propertiesSet();
	}

	public int insertDeleteUpdate(List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> datas, String key, String createDate, String endTime, Integer type) throws ExecutionException, InterruptedException {
		String database ="";
		if(type==1){
			database="metacenday";
		}else if(type==2){
			database="metacenmonth";
		}
		Long start =System.currentTimeMillis();
		Long end =System.currentTimeMillis();
		System.out.println(new Date().toString());
		Map<String,Integer> mapKeyToIndex = new HashMap<>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		List<CompletableFuture<ValueReport>> listResult = new ArrayList<>();
		List<CompletableFuture<ValueReport>> listResultDelete = new ArrayList<>();
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "id","srcIp", "name","typeName","count","fileSize","dataSource","destId","destIp","destName","startTime","endTime","createDate","key" });
		List<String> listKey = new ArrayList<>();
		String startTime="";
		for (Tuple11<String,String,String,String,Long,Long,String,String,String,String,String> row: datas
		) {
			startTime = row.f10;
			try {
				cal.setTime(df.parse(startTime));
				if(type==1) {
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());

					cal.add(Calendar.DAY_OF_MONTH, 1);
				} else if(type==2){
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());
					cal.add(Calendar.MONTH, 1);
				} else if(type==3){
					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
					cal.set(Calendar.DAY_OF_MONTH, 1);
					cal.set(Calendar.MONTH, 0);
					cal.set(Calendar.HOUR_OF_DAY,0);
					cal.clear(Calendar.MINUTE);
					cal.clear(Calendar.SECOND);
					cal.clear(Calendar.MILLISECOND);
					startTime = df.format(cal.getTime());
					cal.add(Calendar.YEAR, 1);
				} else if(type==4){
					cal.add(Calendar.WEEK_OF_YEAR, 1);
				}
				endTime= df.format(cal.getTime());
			}catch (Exception ex){

			}
			List<String> tmp = new ArrayList<>();
			tmp.add(row.f0);
			tmp.add(row.f1);
			tmp.add(row.f2);
			tmp.add(row.f3);
			tmp.add(row.f4.toString());
			tmp.add(row.f5.toString());
			tmp.add(row.f6);
			tmp.add(row.f7);
			tmp.add(row.f8);
			tmp.add(row.f9);
			tmp.add(row.f10);
			tmp.add(endTime);
			tmp.add(createDate);
			String keyCheck = row.f0+row.f7+row.f10+row.f3;
			tmp.add(keyCheck);
			String[] dataTmp = tmp.toArray(new String[14]);
			data.add(dataTmp);
			listKey.add("'"+keyCheck+"'");
			mapKeyToIndex.put(row.f0+row.f7+row.f10+row.f3,data.size()-1);
		}
		List<Long> finalIdDelete = new ArrayList<>();
		List<ValueReport> listCheck = saveMutil.checkData(listKey,database);
		if(listCheck!=null&& !listCheck.isEmpty()){
			listCheck.stream().forEach((item) ->{
					Integer a =mapKeyToIndex.get(item.getKey());
					String[] rowUpdate = data.get(a);
					Long count = Long.valueOf(rowUpdate[4])+item.getCount();
					Long fileSize = Long.valueOf(rowUpdate[5])+item.getFileSize();
					rowUpdate[4]=count.toString();
					rowUpdate[5]=fileSize.toString();
					data.set(a,rowUpdate);
					finalIdDelete.add(item.getId());
			});
		}

//		CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
//				.thenApply(v -> listResult.stream()
//						.map(CompletableFuture::join)
//						.collect(Collectors.toList())
//				);
		LOGGER.info("count relatio: {}",data.size());
		List<Long> idDelete = finalIdDelete;
		end =System.currentTimeMillis();
		LOGGER.info("thoi gian check {}",end-start);
		start=System.currentTimeMillis();
		if(!idDelete.isEmpty()){
			while (idDelete.size()>10000) {
				List<Long> listSave = idDelete.subList(0,10000);
				delete(listSave, database);
				idDelete = idDelete.subList(10000,idDelete.size());

			}
			delete(idDelete, database);
		}
		end =System.currentTimeMillis();
		LOGGER.info("thoi gian xoa {}",end-start);
		start=System.currentTimeMillis();
		LOGGER.info("count relation insert sau update: {}", data.size());
		if(!data.isEmpty()&&data.size()>1) {
			Date now = new Date();
			df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String path = "import" + df.format(now) + ".csv";
			File file = new File(path);
			DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				FileWriter outputfile = new FileWriter(file);

				// create CSVWriter with '|' as separator
				CSVWriter writer = new CSVWriter(outputfile, '|',
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);

				writer.writeAll(data);
				writer.close();
				String url = uploadFile(file);
				String a = "USING PERIODIC COMMIT 1000 LOAD CSV WITH HEADERS FROM \"" + url + "\" AS row FIELDTERMINATOR '|'\n" +
						"MERGE(a:Object{name:row.name,mmsi:row.id})\n" +
						"MERGE(b:Object{name:row.destName,mmsi:row.destId})\n" +
						"WITH a, b, row\n" +
						"CALL apoc.create.relationship(a, row.typeName, {startTime:row.startTime,endTime:row.endTime,createDate:row.createDate,count:row.count,fileSize:row.fileSize,key:row.key,src:row.id,dest:row.destId,srcIp:row.srcIp,destIp:row.destIp,dataSource:row.dataSource},b) YIELD rel\n" +
						"RETURN rel;";
				this.neo4jClient
						.query(a)
						.in(database)
						.run()
						.counters()
						.propertiesSet();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		end =System.currentTimeMillis();
		LOGGER.info("thoi gian lưu {}",end-start);
		return 1;

	}

	public int saveUpdateAsym(List<Row> datas, String key, String timeStart, String timeEnd, Integer type) throws ExecutionException, InterruptedException {
//		System.out.println(new Date().toString());
//		List<String> keyNode = new ArrayList<>();
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
//		String createTime = timeStart;
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//		Calendar cal = Calendar.getInstance();
//		List<CompletableFuture<ValueReport>> listResult = new ArrayList<>();
//		for (Row row: datas
//		) {
//			timeStart = row.getAs("dateTime");
//			try {
//				cal.setTime(df.parse(timeStart));
//				if(type==1) {
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
////					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//
//					cal.add(Calendar.DAY_OF_MONTH, 1);
//				} else if(type==2){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.MONTH, 1);
//				} else if(type==3){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.MONTH, 0);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.YEAR, 1);
//				} else if(type==4){
//					cal.add(Calendar.WEEK_OF_YEAR, 1);
//				}
//				timeEnd= df.format(cal.getTime());
//			}catch (Exception ex){
//
//			}
//			CompletableFuture<ValueReport> bridge = saveMutil.checkData(type,timeStart,timeEnd,row.getAs("idsSrc"),row.getAs("idsDest"));
//			listResult.add(bridge);
//		}
//
//		CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
//				.thenApply(v -> listResult.stream()
//						.map(CompletableFuture::join)
//						.collect(Collectors.toList())
//				);
//		Map<String,ValueReport> mapValue = new HashMap<>();
//		for (CompletableFuture<ValueReport> result : listResult) {
//			ValueReport valueReport = result.get();
//			if(valueReport!=null)
//				mapValue.put(valueReport.getIdsSrc()+valueReport.getIdsDest()+valueReport.getDateTime(),valueReport);
//		}
//
//		for (Row row: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = row.getAs("idsSrc");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("idsDest");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("nameSrc")+ "',ids: '" + row.getAs("idsSrc")
//					+ "',ips: '" + row.getAs("ipsSrc") + "',longitude: '" + row.getAs("longitudeSrc") + "',latitude: '" + row.getAs("latitudeSrc")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("nameDest") +"',ids: '" + row.getAs("idsDest")
//					+ "',ips: '" + row.getAs("ipsDest") + "',longitude: '" + row.getAs("longitudeDest") + "',latitude: '" + row.getAs("latitudeDest")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("idsSrc")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("idsDest")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			timeStart = row.getAs("dateTime");
//			try {
//				cal.setTime(df.parse(timeStart));
//				if(type==1) {
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
////					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//
//					cal.add(Calendar.DAY_OF_MONTH, 1);
//				} else if(type==2){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.MONTH, 1);
//				} else if(type==3){
//					cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//					cal.set(Calendar.DAY_OF_MONTH, 1);
//					cal.set(Calendar.MONTH, 0);
//					cal.set(Calendar.HOUR_OF_DAY,0);
//					cal.clear(Calendar.MINUTE);
//					cal.clear(Calendar.SECOND);
//					cal.clear(Calendar.MILLISECOND);
//					timeStart = df.format(cal.getTime());
//					cal.add(Calendar.YEAR, 1);
//				} else if(type==4){
//					cal.add(Calendar.WEEK_OF_YEAR, 1);
//				}
//				timeEnd= df.format(cal.getTime());
//			}catch (Exception ex){
//
//			}
////			List<ValueReport> valueReports = checkDataAIS(type,timeStart,timeEnd,row.getAs("idsSrc"),row.getAs("idsDest"));
//			if(mapValue.get(row.getAs("idsSrc").toString()+row.getAs("idsDest").toString()+timeStart)==null) {
//				setMatch.add(matchQueryDest);
//				setMatch.add(matchQuerySrc);
//
//				relation += "(" + srcId + ") - [" + uuid + ": MEDIA { ";
//				relation += "count:" + row.getAs("count");
//				Object a = row.getAs("WebCount");
//				Long value = row.getAs("WebCount");
//				if (value > 0) {
//					relation += ",WebCount" + ":" + value;
//					relation += ",WebFileSize" + ":" + row.getAs("WebFileSize");
//				}
//
//				value = row.getAs("VoiceCount");
//				if (value > 0) {
//					relation += ",VoiceCount" + ":" + value;
//					relation += ",VoiceFileSize" + ":" + row.getAs("VoiceFileSize");
//				}
//				value = row.getAs("TransferFileCount");
//				if (value > 0) {
//					relation += ",TransferFileCount" + ":" + value;
//					relation += ",TransferFileFileSize" + ":" + row.getAs("TransferFileFileSize");
//				}
//				value = row.getAs("VideoCount");
//				if (value > 0) {
//					relation += ",VideoCount" + ":" + value;
//					relation += ",VideoFileSize" + ":" + row.getAs("VideoFileSize");
//				}
//				value = row.getAs("UNDEFINEDCount");
//				if (value > 0) {
//					relation += ",UNDEFINEDCount" + ":" + value;
//					relation += ",UNDEFINEDFileSize" + ":" + row.getAs("UNDEFINEDFileSize");
//				}
//				value = row.getAs("EmailCount");
//				if (value > 0) {
//					relation += ",EmailCount" + ":" + value;
//					relation += ",EmailFileSize" + ":" + row.getAs("EmailFileSize");
//				}
//				relation += ",createDate:'" + createTime;
//				relation += "',startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//				relation += ",src:'" + row.getAs("idsSrc") + "',dest:'" + row.getAs("idsDest") + "'";
//				relation += "}] -> ("
//						+ destId + "),";
//			} else {
//				List<ValueReport> valueReports = new ArrayList<>();
//				ValueReport valueReport = mapValue.get(row.getAs("idsSrc").toString()+row.getAs("idsDest").toString()+timeStart);
//				valueReports.add(valueReport);
////				saveMutil.saveData(valueReports,timeStart,timeEnd,row,key);
//				saveData(valueReports,timeStart,timeEnd,row,key);
//			}
//		}
//		List<String> nodeCreated = new ArrayList<>();
//		if(type==1){
//			nodeCreated = redisRepository.findNodeDay(keyNode);
//		} else if(type==2){
//			nodeCreated = redisRepository.findNodeMonth(keyNode);
//		} else if(type==3){
//			nodeCreated = redisRepository.findNodeYear(keyNode);
//		} else if(type==4){
//			nodeCreated = redisRepository.findNodeWeek(keyNode);
//		}
//		List<String> finalNodeCreated = nodeCreated;
//		List<String> nodeCreate = set.stream().filter(c-> finalNodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				for (String tmp : nodeCreate
//				) {
//					dbquery +=" USE fabric."+key+ " MERGE " + tmp ;
//					try {
//						this.neo4jClient
//								.query(dbquery)
//								.in(database())
//								.run()
//								.counters()
//								.propertiesSet();
//					}catch (Exception exx){
//						String ids = tmp.substring(tmp.indexOf("ids")+4,tmp.indexOf("ips")-1);
//						String query = " USE fabric."+key +" MATCH (m:Object)  where m.ids ="+ids;
//						query += " set m +=";
//						String node = tmp.substring(tmp.indexOf("{"),tmp.length()-1);
//						query += node;
//						this.neo4jClient
//								.query(query)
//								.in(database())
//								.run()
//								.counters()
//								.propertiesSet();
//					}
//
//				}
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				if(type==1){
//					redisRepository.saveNodeDay(keyToCreateNote.get(tmp), tmp);
//				} else if(type==2){
//					redisRepository.saveNodeMonth(keyToCreateNote.get(tmp), tmp);
//				} else if(type==3){
//					redisRepository.saveNodeYear(keyToCreateNote.get(tmp), tmp);
//				} else if(type==4){
//					redisRepository.saveNodeWeek(keyToCreateNote.get(tmp), tmp);
//				}
//
//			}
//			dbquery = " USE fabric." + key;
//		}
////		dbquery = " USE fabric." + key;
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//		if(relation.contains("MEDIA")) {
//
//			dbquery += relation;
////			saveMutil.saveNeo4j(dbquery);
//
//			this.neo4jClient
//					.query(dbquery)
//					.in(database())
//					.run()
//					.counters()
//					.propertiesSet();
//		}
		return 1;

	}


	public CompletableFuture<String> saveData(ValueReport valueReport, String timeStart, String timeEnd , String[] data, String key){

		String query = " MATCH (m:Object) - [r:"+valueReport.getMediaType()+"] -> (p:Object) where id(r)="+valueReport.getId();
		query += " set r +={";
		Long count = Integer.valueOf(data[4]) +valueReport.getCount();
		query += "count:" + count;
		Long value = Integer.valueOf(data[5]) +valueReport.getFileSize();
		query += ",fileSize" + ":" +value ;
		query += "} ";
		query = " USE fabric."+key + query;
		this.neo4jClient
				.query(query)
				.in(database())
				.run()
				.counters()
				.propertiesSet();
		return CompletableFuture.completedFuture("oke");
	}

	public CompletableFuture<String> saveDataAis(List<ValueReport> valueReports, String timeStart, String timeEnd , Row row, String key){
//		ValueReport valueReport = valueReports.get(0);
//		String query = " MATCH (m:Object) - [r:AIS] -> (p:Object) where r.startTime >= '"+timeStart+"' and r.startTime < '"+timeEnd+"' and r.src='"+row.getAs("idsSrc")+"' and r.dest='"+row.getAs("idsDest")+"'";
//		query += " set r +={";
//		Long count = (Long) row.getAs("count") +valueReport.getCount();
//		query += "count:" + count;
//		query += "} ";
//		query = " USE fabric."+key + query;
//		this.neo4jClient
//				.query(query)
//				.in(database())
//				.run()
//				.counters()
//				.propertiesSet();
		return CompletableFuture.completedFuture("oke");
	}

	public int saveReportAis(List<Row> datas, String key, String timeStart, String timeEnd, Integer type) {
////		key = "vsat2022";
//		List<String> keyNode = new ArrayList<>();
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String dbquery = " USE fabric."+key;
////		dbquery="";
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//
//		for (Row row: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			String srcId = row.getAs("idsSrc");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("idsDest");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("nameSrc")+ "',ids: '" + row.getAs("idsSrc")
//					+ "',ips: '" + row.getAs("ipsSrc") + "',longitude: '" + row.getAs("longitudeSrc") + "',latitude: '" + row.getAs("latitudeSrc")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("nameDest") +"',ids: '" + row.getAs("idsDest")
//					+ "',ips: '" + row.getAs("ipsDest") + "',longitude: '" + row.getAs("longitudeDest") + "',latitude: '" + row.getAs("latitudeDest")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("idsSrc")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("idsDest")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			setMatch.add(matchQueryDest);
//			setMatch.add(matchQuerySrc);
//
//			relation += "(" + srcId + ") - [" + uuid + ": AIS { ";
//			relation += ",count:" + row.getAs("count");
//			relation += ",startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//			relation += ",src:'" + row.getAs("idsSrc")+"',dest:'"+row.getAs("idsDest")+"'";
//			relation += "}] -> ("
//					+ destId + "),";
//		}
//		List<String> nodeCreated = new ArrayList<>();
//		if(type==1){
//			nodeCreated = redisRepository.findNodeDay(keyNode);
//		} else if(type==2){
//			nodeCreated = redisRepository.findNodeMonth(keyNode);
//		} else if(type==3){
//			nodeCreated = redisRepository.findNodeYear(keyNode);
//		} else if(type==4){
//			nodeCreated = redisRepository.findNodeWeek(keyNode);
//		}
//		List<String> finalNodeCreated = nodeCreated;
//		List<String> nodeCreate = set.stream().filter(c-> finalNodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
//				dbquery = " ";
//				for (String tmp : nodeCreate
//				) {
//					dbquery += " MERGE " + tmp ;
//				}
////				dbquery = dbquery.substring(0, dbquery.length() - 1);
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				if(type==1){
//					redisRepository.saveNodeDay(keyToCreateNote.get(tmp), tmp);
//				} else if(type==2){
//					redisRepository.saveNodeMonth(keyToCreateNote.get(tmp), tmp);
//				} else if(type==3){
//					redisRepository.saveNodeYear(keyToCreateNote.get(tmp), tmp);
//				} else if(type==4){
//					redisRepository.saveNodeWeek(keyToCreateNote.get(tmp), tmp);
//				}
//
//			}
//			dbquery = " USE fabric." + key;
////			dbquery="";
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//
//		dbquery +=relation;
//
//		this.neo4jClient
//				.query( dbquery )
//				.in( database() )
//				.run()
//				.counters()
//				.propertiesSet();
		return 1;

	}

	public int saveObjectUpdateReprocessing(List<DataNeo4j> datas, String key, String timeStart, String timeEnd) {
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		Calendar cal = Calendar.getInstance();
//		cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//		cal.add(Calendar.HOUR, 1);
//		cal.clear(Calendar.MINUTE);
//		cal.clear(Calendar.SECOND);
//		cal.clear(Calendar.MILLISECOND);
//		String createDate = df.format(cal.getTime());
//		key = "vsat2022";
//		List<String> keyNode = new ArrayList<>();
//
//		String dbquery = " USE fabric."+key;
////		dbquery="";
//		Map<String,String> keyToCreateNote = new HashMap<>();
//		Set<String> set = new LinkedHashSet<>();
//		Set<String> setMatch = new LinkedHashSet<>();
//		String relation = " CREATE ";
//
//		for (DataNeo4j data: datas
//		) {
//			String uuid = "a" + UUID.randomUUID().toString();
//			uuid = uuid.replace("-", "_");
//			Row row = data.getMedias().get(0);
//			String srcId = row.getAs("id");
//			srcId = "a" + srcId.replace(".", "_");
//			srcId = srcId.replace("-", "_");
//			srcId = srcId.replace("?", "_");
//			String destId = row.getAs("destId");
//			destId = "a" + destId.replace(".", "_");
//			destId = destId.replace("-", "_");
//			destId = destId.replace("?", "_");
//
//			String querySrc = "(" + srcId + ":Object {name: '" + row.getAs("name")+ "',ids: '" + row.getAs("id")
//					+ "',ips: '" + row.getAs("ips") + "',longitude: '" + row.getAs("longitude") + "',latitude: '" + row.getAs("latitude")
//					+ "'})";
//			String queryDest = "(" + destId + ":Object {name: '" + row.getAs("destName") +"',ids: '" + row.getAs("destId")
//					+ "',ips: '" + row.getAs("destIps") + "',longitude: '" + row.getAs("destLongitude") + "',latitude: '" + row.getAs("destLatitude")
//					+ "'})";
//			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + row.getAs("id")
//					+ "'})";
//			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + row.getAs("destId")
//					+ "'})";
//			long count = 0;
//			keyNode.add(srcId);
//			keyToCreateNote.put(querySrc,srcId);
//			keyNode.add(destId);
//			keyToCreateNote.put(queryDest,destId);
//			set.add(querySrc);
//			set.add(queryDest);
//			setMatch.add(matchQueryDest);
//			setMatch.add(matchQuerySrc);
//
//			relation += " Merge(" + srcId + ") - [" + uuid + ": MEDIA { ";
//			int check = 0;
//			for (Row tmp : data.getMedias()
//			) {
//				if (check == 0) {
//					check++;
//				} else {
//					relation += ",";
//				}
//				relation += tmp.getAs("typeName") + "Count:" + tmp.getAs("typeSize");
//				relation += "," + tmp.getAs("typeName") + "FileSize:" + tmp.getAs("fileSize");
//				Long count1 = tmp.getAs("typeSize");
//				count += count1;
//				timeStart = tmp.getAs("eventTime");
//				try {
//					cal.setTime(df.parse(timeStart));
//					cal.add(Calendar.HOUR,1);
//					timeEnd= df.format(cal.getTime());
//				}catch (Exception ex){
//
//				}
//
//			}
//			relation += ",count:" + count;
//			relation += ",createDate:'" + createDate+"'";
//			relation += ",startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
//			relation += ",src:'" + row.getAs("id")+"',dest:'"+row.getAs("destId")+"'";
//			relation += "}] -> ("
//					+ destId + ") ";
//		}
//		List<String> nodeCreated = redisRepository.findNode(keyNode);
//		List<String> nodeCreate = set.stream().filter(c-> nodeCreated.contains(c)==false).collect(Collectors.toList());
//		if(nodeCreate.size()>0) {
//			dbquery += " CREATE ";
//			for (String tmp : nodeCreate
//			) {
//				dbquery += tmp + ",";
//			}
//			dbquery = dbquery.substring(0, dbquery.length() - 1);
//			try {
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			} catch (Exception ex){
////				dbquery = " ";
//				dbquery = " USE fabric."+key;
//				for (String tmp : nodeCreate
//				) {
//					dbquery += " MERGE " + tmp ;
//				}
////				dbquery = dbquery.substring(0, dbquery.length() - 1);
//				this.neo4jClient
//						.query(dbquery)
//						.in(database())
//						.run()
//						.counters()
//						.propertiesSet();
//			}
//
//			for (String tmp : nodeCreate
//			) {
//				redisRepository.saveNode(keyToCreateNote.get(tmp), tmp);
//			}
//			dbquery = " USE fabric." + key;
////			dbquery="";
//		}
//		for (String tmp : setMatch
//		) {
//			dbquery += tmp ;
//		}
//		relation = relation.substring(0,relation.length()-1);
//
//		dbquery +=relation;
//
//		this.neo4jClient
//				.query( dbquery )
//				.in( database() )
//				.run()
//				.counters()
//				.propertiesSet();
		return 1;

	}


	public int updateNode(String query) {
//		String key = "use fabric.s02022 ";
//		key ="";
//		query=key+query;

		this.neo4jClient
				.query( query )
				.in( database() )
				.run()
				.counters()
				.propertiesSet();
		return 1;

	}

	public int saveAisUpdate(List<AisValueSpark> datas,  String key, String timeStart, String timeEnd) {
		key = "vsat2022";
		String createDate = timeStart;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dbquery = " USE fabric."+key;
//		dbquery="";
		Map<String,String> keyToCreateNote = new HashMap<>();
		Set<String> set = new LinkedHashSet<>();
		Set<String> setMatch = new LinkedHashSet<>();
		String relation = " CREATE";
		List<String> keyNode = new ArrayList<>();
		int check = 0;
		Calendar cal = Calendar.getInstance();
		for (AisValueSpark data: datas
		) {
			String uuid = "a" + UUID.randomUUID().toString();
			uuid = uuid.replace("-", "_");
			String srcId = data.getSrc().getId();
			srcId = "a" + srcId.replace(".", "_");
			srcId = srcId.replace("-", "_");
			srcId = srcId.replace("?", "_");
			String destId = data.getDest().getId();
			destId = "a" + destId.replace(".", "_");
			destId = destId.replace("-", "_");
			destId = destId.replace("?", "_");
			String querySrc = "(" + srcId + ":Object {name: '" + data.getSrc().getName() + "',ids: '" + data.getSrc().getId()
					+ "',ips: '" + data.getSrc().getIps() + "',longitude: '" + data.getSrc().getLongitude() + "',latitude: '" + data.getSrc().getLatitude()
					+ "'})";
			String queryDest = "(" + destId + ":Object {name: '" + data.getDest().getName()+ "',ids: '" +data.getDest().getId()
					+ "',ips: '" + data.getDest().getIps() + "',longitude: '" + data.getDest().getLongitude() + "',latitude: '" + data.getDest().getLatitude()
					+ "'})";
			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + data.getSrc().getId()
					+ "'})";
			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + data.getDest().getId()
					+ "'})";
			long count = 0;
			keyNode.add(srcId);
			keyToCreateNote.put(querySrc,srcId);
			keyNode.add(destId);
			keyToCreateNote.put(queryDest,destId);
			set.add(querySrc);
			set.add(queryDest);
			setMatch.add(matchQueryDest);
			setMatch.add(matchQuerySrc);

			relation += "(" + srcId + ") - [" + uuid + ": AIS { ";
			if (check == 0) {
				check++;
			} else {
				relation += ",";

			}
			timeStart =data.getEventTime();
			try {
				cal.setTime(df.parse(timeStart));
				cal.add(Calendar.HOUR,1);
				timeEnd = df.format(cal.getTime());
			}catch (Exception ex){

			}
			relation += "count:" + data.getCount();
			relation += ",createDate:'" + createDate+"'";
			relation += ",startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
			relation += ",src:'" + data.getSrc().getId()+"',dest:'"+data.getDest().getId()+"'";
			relation += "}] -> ("
					+ destId + "),";
		}
		List<String> nodeCreated = redisRepository.findNode(keyNode);
		List<String> nodeCreate = set.stream().filter(c-> nodeCreated.contains(c)==false).collect(Collectors.toList());
		if(nodeCreate.size()>0) {
			dbquery += " CREATE ";
			for (String tmp : nodeCreate
			) {
				dbquery += tmp + ",";
			}
			dbquery = dbquery.substring(0, dbquery.length() - 1);
			try {
				this.neo4jClient
						.query(dbquery)
						.in(database())
						.run()
						.counters()
						.propertiesSet();
			} catch (Exception ex){
				dbquery = " USE fabric."+"vsat2022";
				for (String tmp : nodeCreate
				) {
					dbquery =" USE fabric."+"vsat2022"+ " MERGE " + tmp ;
					try {
						this.neo4jClient
								.query(dbquery)
								.in(database())
								.run()
								.counters()
								.propertiesSet();
					}catch (Exception exx){
						String ids = tmp.substring(tmp.indexOf("ids")+4,tmp.indexOf("ips")-1);
						String query = " USE fabric."+"vsat2022" +" MATCH (m:Object)  where m.ids ="+ids;
						query += " set m +=";
						String node = tmp.substring(tmp.indexOf("{"),tmp.length()-1);
						query += node;
						this.neo4jClient
								.query(query)
								.in(database())
								.run()
								.counters()
								.propertiesSet();
					}

				}
			}

			for (String tmp : nodeCreate
			) {
				redisRepository.saveNode(keyToCreateNote.get(tmp), tmp);
			}
			dbquery = " USE fabric." + key;
//			dbquery=" ";
		}
		for (String tmp : setMatch
		) {
			dbquery += tmp ;
		}
//		dbquery = dbquery.substring(0,dbquery.length()-1);
		relation = relation.substring(0,relation.length()-1);

		dbquery +=relation;

		this.neo4jClient
				.query( dbquery )
				.in( database() )
				.run()
				.counters()
				.propertiesSet();
		return 1;

	}

	public int saveAisUpdateReprocessing(List<AisValueSpark> datas,  String key, String timeStart, String timeEnd) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
		cal.add(Calendar.HOUR, 1);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		String createDate = df.format(cal.getTime());
		key = "vsat2022";
		String dbquery = " USE fabric."+key;
//		dbquery="";
		Map<String,String> keyToCreateNote = new HashMap<>();
		Set<String> set = new LinkedHashSet<>();
		Set<String> setMatch = new LinkedHashSet<>();
		String relation = " CREATE";
		List<String> keyNode = new ArrayList<>();
		int check = 0;
		for (AisValueSpark data: datas
		) {
			String uuid = "a" + UUID.randomUUID().toString();
			uuid = uuid.replace("-", "_");
			String srcId = data.getSrc().getId();
			srcId = "a" + srcId.replace(".", "_");
			srcId = srcId.replace("-", "_");
			srcId = srcId.replace("?", "_");
			String destId = data.getDest().getId();
			destId = "a" + destId.replace(".", "_");
			destId = destId.replace("-", "_");
			destId = destId.replace("?", "_");
			String querySrc = "(" + srcId + ":Object {name: '" + data.getSrc().getName() + "',ids: '" + data.getSrc().getId()
					+ "',ips: '" + data.getSrc().getIps() + "',longitude: '" + data.getSrc().getLongitude() + "',latitude: '" + data.getSrc().getLatitude()
					+ "'})";
			String queryDest = "(" + destId + ":Object {name: '" + data.getDest().getName()+ "',ids: '" +data.getDest().getId()
					+ "',ips: '" + data.getDest().getIps() + "',longitude: '" + data.getDest().getLongitude() + "',latitude: '" + data.getDest().getLatitude()
					+ "'})";
			String matchQuerySrc = " merge(" + srcId + ":Object {ids: '" + data.getSrc().getId()
					+ "'})";
			String matchQueryDest = " merge(" + destId + ":Object {ids: '" + data.getDest().getId()
					+ "'})";
			long count = 0;
			keyNode.add(srcId);
			keyToCreateNote.put(querySrc,srcId);
			keyNode.add(destId);
			keyToCreateNote.put(queryDest,destId);
			set.add(querySrc);
			set.add(queryDest);
			setMatch.add(matchQueryDest);
			setMatch.add(matchQuerySrc);

			relation += " Merge(" + srcId + ") - [" + uuid + ": AIS { ";
			if (check == 0) {
				check++;
			} else {
				relation += ",";

			}
			timeStart =data.getEventTime();
			try {
				cal.setTime(df.parse(timeStart));
				cal.add(Calendar.HOUR,1);
				timeEnd = df.format(cal.getTime());
			}catch (Exception ex){

			}
			relation += "count:" + data.getCount();
			relation += ",createDate:'" + createDate+"'";
			relation += ",startTime:'" + timeStart + "',endTime:'" + timeEnd + "'";
			relation += ",src:'" + data.getSrc().getId()+"',dest:'"+data.getDest().getId()+"'";
			relation += "}] -> ("
					+ destId + ") ";
		}
		List<String> nodeCreated = redisRepository.findNode(keyNode);
		List<String> nodeCreate = set.stream().filter(c-> nodeCreated.contains(c)==false).collect(Collectors.toList());
		if(nodeCreate.size()>0) {
			dbquery += " CREATE ";
			for (String tmp : nodeCreate
			) {
				dbquery += tmp + ",";
			}
			dbquery = dbquery.substring(0, dbquery.length() - 1);
			try {
				this.neo4jClient
						.query(dbquery)
						.in(database())
						.run()
						.counters()
						.propertiesSet();
			} catch (Exception ex){
//				dbquery = " ";
				dbquery = " USE fabric."+key;
				for (String tmp : nodeCreate
				) {
					dbquery += " MERGE " + tmp ;
				}
//				dbquery = dbquery.substring(0, dbquery.length() - 1);
				this.neo4jClient
						.query(dbquery)
						.in(database())
						.run()
						.counters()
						.propertiesSet();
			}

			for (String tmp : nodeCreate
			) {
				redisRepository.saveNode(keyToCreateNote.get(tmp), tmp);
			}
			dbquery = " USE fabric." + key;
//			dbquery=" ";
		}
		for (String tmp : setMatch
		) {
			dbquery += tmp ;
		}
//		dbquery = dbquery.substring(0,dbquery.length()-1);
		relation = relation.substring(0,relation.length()-1);

		dbquery +=relation;

		this.neo4jClient
				.query( dbquery )
				.in( database() )
				.run()
				.counters()
				.propertiesSet();
		return 1;

	}


//	public List<MovieResultDto> searchMoviesByTitle(String title) {
//		return this.movieRepository.findSearchResults(title)
//				.stream()
//				.map(MovieResultDto::new)
//				.collect(Collectors.toList());
//	}

	/**
	 * This is an example of when you might want to use the pure driver in case you have no need for mapping at all, neither in the
	 * form of the way the {@link Neo4jClient} allows and not in form of entities.
	 *
	 * @return A representation D3.js can handle
	 */

	private Session sessionFor(String database) {
		if (database == null) {
			return driver.session();
		}
		return driver.session(SessionConfig.forDatabase(database));
	}

	private String database() {
		return databaseSelectionProvider.getDatabaseSelection().getValue();
	}

	public List<ValueReport> fetchReport(Integer type, String fromDate, String toDate) {

		var nodes = new ArrayList<>();
		var links = new ArrayList<>();
		String key="use fabric.";
		List<ValueReport> result = new ArrayList<>();
		if(type==1){
			key+="vsat2022";
		} else if(type==2){
			key+="vsatday";
		}  else if(type==3){
			key+="vsatmonth";
		} else if(type==4){
			key+="vsatday";
		}

		try (Session session = sessionFor(database())) {

			String query = " MATCH (m:Object) - [r:MEDIA] -> (p:Object) where r.createDate >= '"+fromDate+"' and r.createDate < '"+toDate+"'  RETURN m AS start, r , p as end ";
			query =key+query;
			String finalQuery = query;
			System.out.println("nhận"+ new Date().toString());
			var records = session.readTransaction(tx -> tx.run(finalQuery).list());
			System.out.println("xong"+ new Date().toString());
			records.forEach(record -> {

//				var movie = Map.of("label", "start", "title", record.get("end").asString());

				var targetIndex = nodes.size();
//				nodes.add(movie);
				String idsSrc = record.get("start").get("ids").asString();
				String ipsSrc = record.get("start").get("ips").asString();
				String longitudeSrc = record.get("start").get("longitude").asString();
				String latitudeSrc = record.get("start").get("latitude").asString();
				String nameSrc = record.get("start").get("name").asString();
				String idsDest = record.get("end").get("ids").asString();
				String ipsDest = record.get("end").get("ips").asString();
				String longitudeDest = record.get("end").get("longitude").asString();
				String latitudeDest = record.get("end").get("latitude").asString();
				String nameDest = record.get("end").get("name").asString();
				Long webCount=0L;
				Long webFileSize =0L;
				if(record.get("r").get("WebCount") instanceof NullValue ==false) {
					webCount = record.get("r").get("WebCount").asLong();
					webFileSize = record.get("r").get("WebFileSize").asLong();
				}
				Long voiceCount=0L;
				Long voiceFileSize =0L;
				if(record.get("r").get("VoiceCount") instanceof NullValue ==false) {
					voiceCount = record.get("r").get("VoiceCount").asLong();
					voiceFileSize = record.get("r").get("VoiceFileSize").asLong();
				}
				Long transferFileCount=0L;
				Long transferFileFileSize =0L;
				if(record.get("r").get("TransferFileCount") instanceof NullValue ==false) {
					transferFileCount = record.get("r").get("TransferFileCount").asLong();
					transferFileFileSize = record.get("r").get("TransferFileFileSize").asLong();
				}
				Long videoCount=0L;
				Long videoFileSize =0L;
				if(record.get("r").get("VideoCount") instanceof NullValue ==false) {
					videoCount = record.get("r").get("VideoCount").asLong();
					videoFileSize = record.get("r").get("VideoFileSize").asLong();
				}
				Long emailCount=0L;
				Long emailFileSize =0L;
				if(record.get("r").get("EmailCount") instanceof NullValue ==false) {
					emailCount = record.get("r").get("EmailCount").asLong();
					emailFileSize = record.get("r").get("EmailFileSize").asLong();
				}
				Long UNDEFINEDCount=0L;
				Long UNDEFINEDFileSize =0L;
				if(record.get("r").get("UNDEFINEDCount") instanceof NullValue ==false) {
					UNDEFINEDCount = record.get("r").get("UNDEFINEDCount").asLong();
					UNDEFINEDFileSize = record.get("r").get("UNDEFINEDFileSize").asLong();
				}
				Long count = record.get("r").get("count").asLong();
				String time = "";
				String dateTime ="";
				DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				//day
				if (type == 1) {
					try {
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						time = df.format(startTime);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());


					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else if (type == 2) { //mount
					try {
						DateFormat df = new SimpleDateFormat("yyyy-MM");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						time = df.format(startTime);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.DAY_OF_MONTH, 1);
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());

					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if (type == 3) { //mount
					try {
						DateFormat df = new SimpleDateFormat("yyyy");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						time = df.format(startTime);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						cal.set(Calendar.MONTH,0);
						cal.set(Calendar.DAY_OF_MONTH, 1);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());

					} catch (ParseException e) {
						e.printStackTrace();
					}
				}  else if (type == 4) { //week
					try {
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						Integer week = cal.get(Calendar.WEEK_OF_YEAR);
						if(week<10){
							time = String.valueOf("0"+week);
						} else {
							time = String.valueOf(week);
						}
						time = String.valueOf(week);
						cal.set(Calendar.DAY_OF_WEEK, 1);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());

					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				ValueReport valueReport = new ValueReport();
				valueReport.setCount(count);
				valueReport.setDateTime(dateTime);
				valueReport.setTime(time);
				valueReport.setNameDest(nameDest);
				valueReport.setNameSrc(nameSrc);
//				valueReport.setLatitudeDest(latitudeDest);
//				valueReport.setLatitudeSrc(latitudeSrc);
//				valueReport.setLongitudeSrc(longitudeSrc);
//				valueReport.setLongitudeDest(longitudeDest);
				valueReport.setIdsDest(idsDest);
				valueReport.setIdsSrc(idsSrc);
				valueReport.setIpsDest(ipsDest);
				valueReport.setIpsSrc(ipsSrc);
//				valueReport.setEmailCount(emailCount);
//				valueReport.setEmailFileSize(emailFileSize);
//				valueReport.setWebCount(webCount);
//				valueReport.setWebFileSize(webFileSize);
//				valueReport.setVideoCount(videoCount);
//				valueReport.setVideoFileSize(videoFileSize);
//				valueReport.setVoiceCount(voiceCount);
//				valueReport.setVoiceFileSize(voiceFileSize);
//				valueReport.setTransferFileCount(transferFileCount);
//				valueReport.setTransferFileFileSize(transferFileFileSize);
//				valueReport.setUNDEFINEDCount(UNDEFINEDCount);
//				valueReport.setUNDEFINEDFileSize(UNDEFINEDFileSize);
				result.add(valueReport);

//				String latitudeSrc = record.get("start").get("latitude").asString();
//				record.get("actors").asList(Value::asString).forEach(name -> {
//					var actor = Map.of("label", "actor", "title", name);
//
//					int sourceIndex;
//					if (nodes.contains(actor)) {
//						sourceIndex = nodes.indexOf(actor);
//					} else {
//						nodes.add(actor);
//						sourceIndex = nodes.size() - 1;
//					}
//					links.add(Map.of("source", sourceIndex, "target", targetIndex));
//				});
			});
			session.close();
		}

		System.out.println(result.size());

		return result;
	}

	public List<ValueReport> fetchReportAis(Integer type, String fromDate, String toDate) {

		var nodes = new ArrayList<>();
		var links = new ArrayList<>();
		List<ValueReport> result = new ArrayList<>();
		String key="use fabric.";
		if(type==1){
			key+="vsat2022";
		} else if(type==2){
			key+="vsatday";
		}  else if(type==3){
			key+="vsatmonth";
		} else if(type==4){
			key+="vsatday";
		}

		try (Session session = sessionFor(database())) {
			String query = " MATCH (m:Object) - [r:AIS] -> (p:Object) where r.createDate >= '"+fromDate+"' and r.createDate < '"+toDate+"'  RETURN m AS start, r , p as end ";
			query = key+query;
			String finalQuery = query;
			var records = session.readTransaction(tx -> tx.run(finalQuery).list());
			records.forEach(record -> {

//				var movie = Map.of("label", "start", "title", record.get("end").asString());

				var targetIndex = nodes.size();
//				nodes.add(movie);
				String idsSrc = record.get("start").get("ids").asString();
				String ipsSrc = record.get("start").get("ips").asString();
				String longitudeSrc = record.get("start").get("longitude").asString();
				String latitudeSrc = record.get("start").get("latitude").asString();
				String nameSrc = record.get("start").get("name").asString();
				String idsDest = record.get("end").get("ids").asString();
				String ipsDest = record.get("end").get("ips").asString();
				String longitudeDest = record.get("end").get("longitude").asString();
				String latitudeDest = record.get("end").get("latitude").asString();
				String nameDest = record.get("end").get("name").asString();
				Long count = record.get("r").get("count").asLong();
				String time = "";
				String dateTime ="";
				DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				//day
				if (type == 1) {
					try {
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						time = df.format(startTime);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());


					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else if (type == 2) { //mount
					try {
						DateFormat df = new SimpleDateFormat("yyyy-MM");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						time = df.format(startTime);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.DAY_OF_MONTH, 1);
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());

					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if (type == 3) { //mount
					try {
						DateFormat df = new SimpleDateFormat("yyyy");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						time = df.format(startTime);
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						cal.set(Calendar.MONTH,0);
						cal.set(Calendar.DAY_OF_MONTH, 1);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());

					} catch (ParseException e) {
						e.printStackTrace();
					}
				}  else if (type == 4) { //week
					try {
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Date startTime = dff.parse(record.get("r").get("startTime").asString());
						Calendar cal = Calendar.getInstance();
						cal.setTime(startTime);
						Integer week = cal.get(Calendar.WEEK_OF_YEAR);
						if(week<10){
							time = String.valueOf("0"+week);
						} else {
							time = String.valueOf(week);
						}
						time = String.valueOf(week);
						cal.set(Calendar.DAY_OF_WEEK, 1);
						cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
						cal.set(Calendar.HOUR_OF_DAY,0);
						cal.clear(Calendar.MINUTE);
						cal.clear(Calendar.SECOND);
						cal.clear(Calendar.MILLISECOND);
						dateTime = dff.format(cal.getTime());

					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				ValueReport valueReport = new ValueReport();
				valueReport.setCount(count);
				valueReport.setDateTime(dateTime);
				valueReport.setTime(time);
				valueReport.setNameDest(nameDest);
				valueReport.setNameSrc(nameSrc);
//				valueReport.setLatitudeDest(latitudeDest);
//				valueReport.setLatitudeSrc(latitudeSrc);
//				valueReport.setLongitudeSrc(longitudeSrc);
//				valueReport.setLongitudeDest(longitudeDest);
				valueReport.setIdsDest(idsDest);
				valueReport.setIdsSrc(idsSrc);
				valueReport.setIpsDest(ipsDest);
				valueReport.setIpsSrc(ipsSrc);
				result.add(valueReport);
			});
			session.close();
		}
		System.out.println(result.size());

		return result;
	}

	public List<ValueReport> checkDataMedia(Integer type, String fromDate, String toDate, String idsSrc, String idsDest) {

		var nodes = new ArrayList<>();
		var links = new ArrayList<>();
		List<ValueReport> result = new ArrayList<>();
		String key="use fabric.";
		if(type==1){
			key+="vsatday";
		}else if(type==2){
			key+="vsatmonth";
		}  else if(type==3){
			key+="vsatyear";
		}  else if(type==4){
			key+="vsatweek";
		}

		try (Session session = sessionFor(database())) {
			String query = " MATCH (m:Object) - [r:MEDIA] -> (p:Object) where r.startTime >= '"+fromDate+"' and r.startTime < '"+toDate+"' and r.src='"+idsSrc+"' and r.dest='"+idsDest+"'  RETURN  r  limit 1";
			query = key+query;
			String finalQuery = query;
			var records = session.readTransaction(tx -> tx.run(finalQuery).list());
			records.forEach(record -> {

//				var movie = Map.of("label", "start", "title", record.get("end").asString());

				var targetIndex = nodes.size();
//				nodes.add(movie);
				Long count = record.get("r").get("count").asLong();
				String time = "";
				String dateTime ="";
				DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Long webCount=0L;
				Long webFileSize =0L;
				if(record.get("r").get("WebCount") instanceof NullValue ==false) {
					webCount = record.get("r").get("WebCount").asLong();
					webFileSize = record.get("r").get("WebFileSize").asLong();
				}
				Long voiceCount=0L;
				Long voiceFileSize =0L;
				if(record.get("r").get("VoiceCount") instanceof NullValue ==false) {
					voiceCount = record.get("r").get("VoiceCount").asLong();
					voiceFileSize = record.get("r").get("VoiceFileSize").asLong();
				}
				Long transferFileCount=0L;
				Long transferFileFileSize =0L;
				if(record.get("r").get("TransferFileCount") instanceof NullValue ==false) {
					transferFileCount = record.get("r").get("TransferFileCount").asLong();
					transferFileFileSize = record.get("r").get("TransferFileFileSize").asLong();
				}
				Long videoCount=0L;
				Long videoFileSize =0L;
				if(record.get("r").get("VideoCount") instanceof NullValue ==false) {
					videoCount = record.get("r").get("VideoCount").asLong();
					videoFileSize = record.get("r").get("VideoFileSize").asLong();
				}
				Long emailCount=0L;
				Long emailFileSize =0L;
				if(record.get("r").get("EmailCount") instanceof NullValue ==false) {
					emailCount = record.get("r").get("EmailCount").asLong();
					emailFileSize = record.get("r").get("EmailFileSize").asLong();
				}
				ValueReport valueReport = new ValueReport();
				valueReport.setCount(count);
//				valueReport.setEmailCount(emailCount);
//				valueReport.setEmailFileSize(emailFileSize);
//				valueReport.setWebCount(webCount);
//				valueReport.setWebFileSize(webFileSize);
//				valueReport.setVideoCount(videoCount);
//				valueReport.setVideoFileSize(videoFileSize);
//				valueReport.setVoiceCount(voiceCount);
//				valueReport.setVoiceFileSize(voiceFileSize);
//				valueReport.setTransferFileCount(transferFileCount);
//				valueReport.setTransferFileFileSize(transferFileFileSize);
				result.add(valueReport);

			});
		}
		return result;
	}

	public List<ValueReport> checkDataAIS(Integer type, String fromDate, String toDate, String idsSrc, String idsDest) {
		List<ValueReport> result = new ArrayList<>();
		String key="use fabric.";
		if(type==1){
			key+="vsatday";
		}else if(type==2){
			key+="vsatmonth";
		}  else if(type==3){
			key+="vsatyear";
		}  else if(type==4){
			key+="vsatweek";
		}

		try (Session session = sessionFor(database())) {
			String query = " MATCH (m:Object) - [r:AIS] -> (p:Object) where r.startTime >= '"+fromDate+"' and r.startTime < '"+toDate+"' and r.src='"+idsSrc+"' and r.dest='"+idsDest+"'  RETURN  r  limit 1";
			query = key+query;
			String finalQuery = query;
			var records = session.readTransaction(tx -> tx.run(finalQuery).list());
			records.forEach(record -> {
				Long count = record.get("r").get("count").asLong();
				ValueReport valueReport = new ValueReport();
				valueReport.setCount(count);
				result.add(valueReport);

			});
		}
		return result;
	}




//	private MovieDetailsDto toMovieDetails(TypeSystem ignored, org.neo4j.driver.Record record) {
//		var movie = record.get("movie");
//		return new MovieDetailsDto(
//				movie.get("title").asString(),
//				movie.get("cast").asList((member) -> {
//					var result = new CastMemberDto(
//							member.get("name").asString(),
//							member.get("job").asString()
//					);
//					var role = member.get("role");
//					if (role.isNull()) {
//						return result;
//					}
//					return result.withRole(role.asString());
//				})
//		);
//	}
}
