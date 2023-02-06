package com.elcom.metacen.raw.data.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 *
 * @author Admin
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PositionOverallRequest {

    private String fromTime;
    
    private String toTime;
    
    private String term; // Từ khóa tìm kiếm
    
    private List<String> groupIds; // id nhóm đối tượng
    
    private List<Integer> countryIds; // id quốc gia
    
    //TODO: chưa làm tìm kiếm theo vùng
    private List<Integer> areaIds; // id vùng ( quản lý trong danh bạ vùng )
    
    private List<String> tileCoordinates; // mã vùng ( fix cố định danh sách mã vùng theo chuẩn chung )
    
    private String mmsiVsat;
    
    private String mmsiAis;
    
    private String imoVsat;
    
    private String imoAis;
    
    private List<String> dataVendorsVsat; //  nguồn cung cấp dữ liệu: VSAT-01, AIS-01, ...
    
    private List<String> dataVendorsAis; //  nguồn cung cấp dữ liệu: VSAT-01, AIS-01, ...
    
    private String sourceType; // nguồn khai thác: nguồn AIS hay nguồn VSAT
    
    private List<Integer> dataSourceIds; // id nguồn thu ( nếu là nguồn VSAT )
    
    private String sourceIps; // ip nguồn ( nếu là nguồn VSAT )
    
    private String destIps; // ip đích ( nếu là nguồn VSAT )
    
    private boolean mediaFilterAccept; // Chỉ lấy ra những vị trí có media
    
    private List<Integer> mediaFilterType; // 1, 2, 3, 4, 5, 8 -> Audio, Video, Web, Email, TransferFiles, Undefined
    
    private List<String> mediaFilterFormat; // jpeg, png, pdf, docx, .......
    
    private Integer limit;
}
