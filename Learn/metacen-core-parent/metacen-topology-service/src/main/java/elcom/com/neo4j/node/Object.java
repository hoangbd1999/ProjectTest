package elcom.com.neo4j.node;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
//import org.springframework.data.neo4j.core.schema.Node;

import java.util.ArrayList;
import java.util.List;


//@JsonIdentityInfo(generator=JSOGGenerator.class)
@Node
public class Object {
    @Id
    private String mmsi;
    private String name;
    private Integer id;

    public Object() {
    }

    public Object(String ips, String ids, String name, Integer id, String longitude, String latitude) {
        this.mmsi = ids;
        this.name = name;
        this.id = id;
    }

    public Object(String ips, String ids, String name, Integer id) {
        this.mmsi = ids;
        this.name = name;
        this.id = id;
    }

    public Object( String name, Integer id) {
        this.name = name;
        this.id = id;
    }
    public Object( String mmsi,String name, Integer id) {
        this.name = name;
        this.id = id;
        this.mmsi=mmsi;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    //
//    @Relationship(type = "AIS")
//    private List<AIS> ais;
//
//    public List<AIS> getAis() {
//        return ais;
//    }

//    public void setAis(List<AIS> ais) {
//        this.ais = ais;
//    }

    public String getMmsi() {
        return mmsi;
    }

    public void setMmsi(String mmsi) {
        this.mmsi = mmsi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
