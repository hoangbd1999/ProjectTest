package elcom.com.neo4j.node;

/*import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;*/

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * @author Mark Angrish
 * @author Michael J. Simons
 */
@Node
public class ObjectToNode {

	@Id
	private String ip;

//	@Id
	private Integer id;

	private String objId;

	private Integer isUfo;

	private Integer isHq;

	private Integer countryId;

	private Integer typeId;

	private String name;

	private String extra;

	private Integer port;

	private String phone;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public Integer getIsUfo() {
		return isUfo;
	}

	public void setIsUfo(Integer isUfo) {
		this.isUfo = isUfo;
	}

	public Integer getIsHq() {
		return isHq;
	}

	public void setIsHq(Integer isHq) {
		this.isHq = isHq;
	}

	public Integer getCountryId() {
		return countryId;
	}

	public void setCountryId(Integer countryId) {
		this.countryId = countryId;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
