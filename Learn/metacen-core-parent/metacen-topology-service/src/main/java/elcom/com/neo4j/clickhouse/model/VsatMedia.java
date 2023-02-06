package elcom.com.neo4j.clickhouse.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.type.BigIntegerType;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class VsatMedia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "mediaTypeName")
    private String mediaTypeName;

    @Column(name = "sourceIp")
    private String sourceIp;

    @Column(name = "sourceId")
    private BigInteger sourceId;

    @Column(name = "destIp")
    private String destIp;

    @Column(name = "destId")
    private BigInteger destId;

    @Column(name = "sourceName")
    private String sourceName;

    @Column(name = "destName")
    private String destName;

    @Column(name = "fileSize")
    private Long fileSize;

    @Column(name = "dataSource")
    private Integer dataSource;

    @Column(name = "ingestTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp ingestTime;

    @Column(name = "eventTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp eventTime;

    public String getMediaTypeName() {
        return mediaTypeName;
    }

    public void setMediaTypeName(String mediaTypeName) {
        this.mediaTypeName = mediaTypeName;
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

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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

    public Timestamp getIngestTime() {
        return ingestTime;
    }

    public void setIngestTime(Timestamp ingestTime) {
        this.ingestTime = ingestTime;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public BigInteger getSourceId() {
        return sourceId;
    }

    public void setSourceId(BigInteger sourceId) {
        this.sourceId = sourceId;
    }

    public BigInteger getDestId() {
        return destId;
    }

    public void setDestId(BigInteger destId) {
        this.destId = destId;
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
        return "com.elcom.itscore.data.model.RecognitionPlate[ recognitionPK=" + this.destIp+ " ]";
    }

}
