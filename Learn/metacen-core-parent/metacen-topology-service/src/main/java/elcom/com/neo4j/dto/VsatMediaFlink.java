package elcom.com.neo4j.dto;

import elcom.com.neo4j.clickhouse.model.VsatMedia;

import java.util.List;

public class VsatMediaFlink {
    private List<VsatMedia> vsatMediaList;

    public List<VsatMedia> getVsatMediaList() {
        return vsatMediaList;
    }

    public void setVsatMediaList(List<VsatMedia> vsatMediaList) {
        this.vsatMediaList = vsatMediaList;
    }
}
