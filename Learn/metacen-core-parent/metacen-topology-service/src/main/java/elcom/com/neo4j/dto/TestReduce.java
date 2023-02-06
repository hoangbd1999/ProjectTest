package elcom.com.neo4j.dto;

public class TestReduce {
    private String mount;
    private Long count;
    private Long size;

    public TestReduce() {
    }

    public TestReduce(String key, Long count, Long fileSize) {
        this.mount=key;
        this.count=count;
        this.size=fileSize;
    }

    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
