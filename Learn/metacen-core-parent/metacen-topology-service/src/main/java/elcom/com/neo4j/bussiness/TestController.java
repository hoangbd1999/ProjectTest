package elcom.com.neo4j.bussiness;

import elcom.com.neo4j.clickhouse.service.MetaCenMediaService;
import elcom.com.neo4j.dto.FilterDto;
import elcom.com.neo4j.dto.ResponseTopo;
import elcom.com.neo4j.node.AIS;
import elcom.com.neo4j.node.ValueReport;
import elcom.com.neo4j.repository.AISRelationshipRepository;
import elcom.com.neo4j.service.impl.ObjectServiceImpl;
import elcom.com.neo4j.service.impl.TopoVsatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

@RestController
public class TestController {

    @Autowired
    private ObjectServiceImpl relationshipRepository;
    @Autowired
    private TopoVsatService topoVsatService;

    @Autowired
    private MetaCenMediaService vsatService;

    @PostMapping("/v1.0/graph")
    public ResponseTopo findByTitle(@RequestBody FilterDto data) throws ParseException {
        System.out.println("nhận"+ new Date().toString());
////        Long now = System.currentTimeMillis();
////        String a=topoVsatService.test();
////        List<Long> id= new ArrayList<>();
////        id.add(15L);
////        id.add(16L);
////        id.add(17L);
////        id.add(18L);
////        id.add(19L);
////        topoVsatService.test2(id);
//////        relationshipRepository.deleteRelation(a,"metacenhour");
////        Long end =System.currentTimeMillis();
////        System.out.println(now+" "+end);
////        System.out.println(end-now);
//        List<String> ids = vsatService.findNodeImportant("2021-12-15 00:00:00","2022-02-10 00:00:00",15,0);
//        System.out.println("aa");
        ResponseTopo a = topoVsatService.getTopoTest1(data.getStartTime(),data.getEndTime(),data.getIps(),data.getType(),data.getTypeRelation(),data.getDeep(),
                data.getTypeData(),data.getDataSource(),data.getExactly(),data.getIds(),data.getPage());
        return a;
    }

}
