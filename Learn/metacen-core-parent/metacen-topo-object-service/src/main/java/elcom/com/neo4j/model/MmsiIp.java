package elcom.com.neo4j.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
public class MmsiIp {

    @Id
    @Column(name = "mmsi")
    private Long mmsi;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "type")
    private Integer type;
    @Column(name = "data_source")
    private Long dataSource;

    public MmsiIp(String ipAddress, Integer type, Long mmsi, Long dataSource) {
        this.ipAddress=ipAddress;
        this.type=type;
        this.mmsi=mmsi;
        this.dataSource=dataSource;
    }

    public MmsiIp() {

    }

    public String getIpAddress() {
        return ipAddress;
    }


    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
    }

    public Long getDataSource() {
        return dataSource;
    }

    public void setDataSource(Long dataSource) {
        this.dataSource = dataSource;
    }
}
