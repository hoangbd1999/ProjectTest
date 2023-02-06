package elcom.com.neo4j.node;

import lombok.Data;

@Data
public class Relationships {
    private Integer start ;
    private Integer end ;
    private String type;

//    private Long WebCount;
//    private Long WebFileSize ;
//    private Long VoiceCount ;
//    private Long VoiceFileSize ;
//    private Long TransferFileCount ;
//    private Long TransferFileFileSize ;
//    private Long VideoCount;
//    private Long VideoFileSize ;
//    private Long EmailCount ;
//    private Long EmailFileSize ;
    private Long fileSize;
    private Long count  ;
    private String startTime ;
    private String endTime ;
    private String srcIp ;
    private String destIp ;

    private String src ;
    private String dest ;
    private String dataSource;
    private Integer page ;
    private Integer size;
    public void addRelation(Relationships relationships){
        count+=relationships.getCount();
        fileSize+= relationships.getFileSize();
//        WebCount+=relationships.getWebCount();
//        WebFileSize+=relationships.getWebFileSize();
//        VoiceCount+=relationships.getVoiceCount();
//        VoiceFileSize+=relationships.getVoiceFileSize();
//        VideoCount+= relationships.getVideoCount();
//        VideoFileSize+= relationships.getVideoFileSize();
//        TransferFileCount+= relationships.getTransferFileCount();
//        TransferFileFileSize+= relationships.getTransferFileFileSize();
//        EmailCount+= relationships.getEmailCount();
//        EmailFileSize+= relationships.getEmailFileSize();
    }
    public void subRelation(Relationships relationships){
        count= count- relationships.getCount();
        fileSize= fileSize- relationships.getFileSize();
    }


}
