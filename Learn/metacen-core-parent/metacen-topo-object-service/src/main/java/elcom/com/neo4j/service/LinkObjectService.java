package elcom.com.neo4j.service;

import java.util.Map;

public interface LinkObjectService {
    public void addLinkObject(Map<String,Object> body);
    public void updateNode(Map<String,Object> body);

    public void createLinkContainsObject(Map<String,Object> body);

    public void deleteNode(Map<String,Object> body);


    public void createIndex(String query);
}
