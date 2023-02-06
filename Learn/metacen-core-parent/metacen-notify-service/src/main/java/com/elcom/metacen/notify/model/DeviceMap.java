package com.elcom.metacen.notify.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "device_map")
@Getter
@Setter
@NoArgsConstructor
public class DeviceMap {
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "last_time_online")
    private Instant lastTimeOnline;

    @Column(name = "patrol_violation")
    private boolean patrolViolation;
}
