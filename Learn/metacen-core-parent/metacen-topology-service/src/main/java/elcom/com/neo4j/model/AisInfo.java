package elcom.com.neo4j.model;

import lombok.Data;
import net.bytebuddy.implementation.bind.annotation.Empty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "ais_info")
public class AisInfo implements Serializable {
    @Id
    @Column(name = "mmsi")
    private Long mmsi;

    @Column(name = "name")
    private String name;
    @Column(name = "source_ip")
    private String sourceIp;

    public AisInfo(Long mmsi, String name) {
        this.mmsi = mmsi;
        this.name = name;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AisInfo() {
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
}
