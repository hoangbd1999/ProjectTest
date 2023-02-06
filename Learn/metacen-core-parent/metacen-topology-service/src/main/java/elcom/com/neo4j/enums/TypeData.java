package elcom.com.neo4j.enums;

/**
 * @author hanh
 */
public enum TypeData {
    AIS(0, "AIS"),
    Audio(1, "Audio"),
    Video(2, "Video"),
    Web(3, "Web"),
    Email(4, "Email"),
    TransferFile(5, "TransferFile"),
    UNKNOW(6,"Không xác định")
    ;

    private int code;
    private String description;

    TypeData(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TypeData of(String type){
        switch (type) {
            case "AIS":
                return TypeData.AIS;
            case "Audio":
                return TypeData.Audio;
            case "Video":
                return TypeData.Video;
            case "Web":
                return TypeData.Web;
            case "Email":
                return TypeData.Email;
            case "TransferFile":
                return TypeData.TransferFile;
            default: return TypeData.UNKNOW;
        }
    }

    public int code() {
        return code;
    }
    public String description() {
        return description;
    }



    public static TypeData of(int code) {
        TypeData[] validFlags = TypeData.values();
        for (TypeData validFlag : validFlags) {
            if (validFlag.code() == code) {
                return validFlag;
            }
        }

        return UNKNOW;
    }


}
