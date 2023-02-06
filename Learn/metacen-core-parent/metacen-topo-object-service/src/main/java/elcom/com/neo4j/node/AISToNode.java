package elcom.com.neo4j.node;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class AISToNode {
    private String objId;

    private Long countryId;

    private String imo;

    private Long typeId;

    private Long dimA;

    private Long dimB;

    private Long dimC;

    private Long dimD;

    private Double draugth;

    private Double rot;

    private Double sog;

    private Double cog;

    private Double longitude;

    private Double latitude;

    private Long navStatus;

    private Long trueHanding;

    private String callSign;

    private String name;

    private String eta;

    private String destination;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private Timestamp eventTime;

    private Long isMaster;

    private String mmsiMaster;

    private Long sourceId;

    private Long sourcePort;

    private String sourceIp;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private Timestamp procTime;


    private Long destId;

    private Long destPort;

    private Long destIp;

    private Integer direction;

    private Integer isUfo;

    private Integer dataSource;

    private String dataSourceName;

    private String areaIds;

    private String groupIds;

    private Long count;

    private Integer hasMedia;

    private Long partName;

    private Long timeKey;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private Timestamp ingestTime;

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public String getImo() {
        return imo;
    }

    public void setImo(String imo) {
        this.imo = imo;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getDimA() {
        return dimA;
    }

    public void setDimA(Long dimA) {
        this.dimA = dimA;
    }

    public Long getDimB() {
        return dimB;
    }

    public void setDimB(Long dimB) {
        this.dimB = dimB;
    }

    public Long getDimC() {
        return dimC;
    }

    public void setDimC(Long dimC) {
        this.dimC = dimC;
    }

    public Long getDimD() {
        return dimD;
    }

    public void setDimD(Long dimD) {
        this.dimD = dimD;
    }

    public Double getDraugth() {
        return draugth;
    }

    public void setDraugth(Double draugth) {
        this.draugth = draugth;
    }

    public Double getRot() {
        return rot;
    }

    public void setRot(Double rot) {
        this.rot = rot;
    }

    public Double getSog() {
        return sog;
    }

    public void setSog(Double sog) {
        this.sog = sog;
    }

    public Double getCog() {
        return cog;
    }

    public void setCog(Double cog) {
        this.cog = cog;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Long getNavStatus() {
        return navStatus;
    }

    public void setNavStatus(Long navStatus) {
        this.navStatus = navStatus;
    }

    public Long getTrueHanding() {
        return trueHanding;
    }

    public void setTrueHanding(Long trueHanding) {
        this.trueHanding = trueHanding;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    public Long getIsMaster() {
        return isMaster;
    }

    public void setIsMaster(Long isMaster) {
        this.isMaster = isMaster;
    }

    public String getMmsiMaster() {
        return mmsiMaster;
    }

    public void setMmsiMaster(String mmsiMaster) {
        this.mmsiMaster = mmsiMaster;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Long sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public Timestamp getProcTime() {
        return procTime;
    }

    public void setProcTime(Timestamp procTime) {
        this.procTime = procTime;
    }

    public Long getDestId() {
        return destId;
    }

    public void setDestId(Long destId) {
        this.destId = destId;
    }

    public Long getDestPort() {
        return destPort;
    }

    public void setDestPort(Long destPort) {
        this.destPort = destPort;
    }

    public Long getDestIp() {
        return destIp;
    }

    public void setDestIp(Long destIp) {
        this.destIp = destIp;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Integer getIsUfo() {
        return isUfo;
    }

    public void setIsUfo(Integer isUfo) {
        this.isUfo = isUfo;
    }

    public Integer getDataSource() {
        return dataSource;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getAreaIds() {
        return areaIds;
    }

    public void setAreaIds(String areaIds) {
        this.areaIds = areaIds;
    }

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getHasMedia() {
        return hasMedia;
    }

    public void setHasMedia(Integer hasMedia) {
        this.hasMedia = hasMedia;
    }

    public Long getPartName() {
        return partName;
    }

    public void setPartName(Long partName) {
        this.partName = partName;
    }

    public Long getTimeKey() {
        return timeKey;
    }

    public void setTimeKey(Long timeKey) {
        this.timeKey = timeKey;
    }

    public Timestamp getIngestTime() {
        return ingestTime;
    }

    public void setIngestTime(Timestamp ingestTime) {
        this.ingestTime = ingestTime;
    }
}
