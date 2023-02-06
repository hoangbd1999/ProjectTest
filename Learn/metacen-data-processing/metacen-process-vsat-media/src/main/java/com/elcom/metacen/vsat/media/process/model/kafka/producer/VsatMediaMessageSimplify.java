package com.elcom.metacen.vsat.media.process.model.kafka.producer;

import com.elcom.metacen.dto.redis.Job;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Admin metacen kafka topic: `VSAT_MEDIA_RAW_RETRY` Topic chứa dữ liệu
 * VSAT media cần được xử lý lại
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VsatMediaMessageSimplify extends Job implements Serializable {

    private String mediaUuidKey;
    private String mediaFileUrl;
    private String sourceIp;
    private String destIp;
    private Long dataSourceId;
    private String mediaTypeName;
    private String fileType;
    private int retryNum; // Mặc định khi tạo mới là = 0, có lỗi thì ++
}
