package com.elcom.metacen.contact.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "data_collector_config", schema = "metacen_contact")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataCollectorConfig implements Serializable {

    @Id
    @Column(name = "collect_type")
    private String collectType;

    @Column(name = "config_value")
    private String configValue;

    @Column(name = "is_running_process")
    private boolean isRunningProcess;

    @Column(name = "note")
    private String note;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "updated_by")
    private String updatedBy;
}
