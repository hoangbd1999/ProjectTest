package elcom.com.neo4j.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ufo_ip")
public class ObjectUndefinedIp {


     @Id
     @Column(name = "id")
     private Long id;

     @Column(name = "ufo_id")
     private String ufoId;
     @Column(name = "data_source")
     private Long dataSource;
     @Column(name = "ip_address")
     private String ipAddress;

     public ObjectUndefinedIp(String ufoId, Long dataSource, String ipAddress) {
          this.ufoId = ufoId;
          this.dataSource = dataSource;
          this.ipAddress = ipAddress;
     }

     public ObjectUndefinedIp() {
     }

     public Long getId() {
          return id;
     }

     public void setId(Long id) {
          this.id = id;
     }

     public String getUfoId() {
          return ufoId;
     }

     public void setUfoId(String ufoId) {
          this.ufoId = ufoId;
     }

     public Long getDataSource() {
          return dataSource;
     }

     public void setDataSource(Long dataSource) {
          this.dataSource = dataSource;
     }

     public String getIpAddress() {
          return ipAddress;
     }

     public void setIpAddress(String ipAddress) {
          this.ipAddress = ipAddress;
     }
}
