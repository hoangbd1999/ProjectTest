package com.elcom.metacen.contact.model;


import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author hoangbd
 */
@Entity
@Table(name = "object_files", schema = "metacen_contact")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "ObjectFiles.findAll", query = "SELECT t FROM ObjectFiles t")})
public class ObjectFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Size(max = 200)
    @Column(name = "object_id")
    private String objectId;

    @Size(max = 20)
    @Column(name = "file_type")
    private String fileType;

    @Size(max = 100)
    @Column(name = "object_type")
    private String objectType;

    @Size(max = 300)
    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "is_deleted")
    private Integer isDeleted;


    public ObjectFiles() {
    }

    public ObjectFiles(String objectId,String fileType, String objectType, String imagePath, Integer isDeleted) {
        this.objectId = objectId;
        this.fileType = fileType;
        this.objectType = objectType;
        this.imagePath = imagePath;
        this.isDeleted = isDeleted;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
