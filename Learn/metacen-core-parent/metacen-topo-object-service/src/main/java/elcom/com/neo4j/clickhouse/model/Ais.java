package elcom.com.neo4j.clickhouse.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Ais implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "sourceIp")
    private String sourceIp;

    @Column(name = "sourceId")
    private BigInteger sourceId;

    @Column(name = "destIp")
    private String destIp;

    @Column(name = "sourceName")
    private String sourceName;

    @Column(name = "dataSource")
    private Integer dataSource;

    @Column(name = "count")
    private Integer count;

    @Column(name = "eventTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp eventTime;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestIp() {
        return destIp;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public Integer getDataSource() {
        return dataSource;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public String toGMT7JsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public BigInteger getSourceId() {
        return sourceId;
    }

    public void setSourceId(BigInteger sourceId) {
        this.sourceId = sourceId;
    }
}
