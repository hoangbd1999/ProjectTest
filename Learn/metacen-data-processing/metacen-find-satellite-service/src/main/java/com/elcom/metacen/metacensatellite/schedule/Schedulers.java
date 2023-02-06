package com.elcom.metacen.metacensatellite.schedule;

//import com.elcom.itscore.recognition.flink.clickhouse.service.RecognitionPlateClickHouseService;

import com.elcom.metacen.metacensatellite.dto.FolderDTO;
import com.elcom.metacen.metacensatellite.dto.PointDTO;
import com.elcom.metacen.metacensatellite.redis.RedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 *
 * @author anhdv
 */
@Configuration
@Service
public class Schedulers {

    @Autowired
    private RedisRepository redisRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedulers.class);

//    @Scheduled( fixedDelayString = "1000")
//    public void test(){
//        Path2D prettyPoly =new Path2D.Double();
//        //Shape dra//POLYGON((111.55337276291309 19.37960417405081,112.74291652307515 19.41493715702592,112.80180482803367 18.46094661669793,111.5887057458882 18.402058311739417,111.55337276291309 19.37960417405081))
//        // for loop to retrieve x,y coordinates for each location
//        prettyPoly.moveTo(39.2658156875,116.966926375);
//        prettyPoly.lineTo(39.1779250625,127.338020125);
//        prettyPoly.lineTo(36.2775344375,127.51380137);
//        prettyPoly.lineTo(36.7169875625,116.966926375);
//        prettyPoly.lineTo(39.2658156875,116.966926375);
//        prettyPoly.closePath();
//        Object a= prettyPoly.getBounds();
//
//
//        // check if contain
////        boolean h = prettyPoly.getBounds().contains(131.117317, 26.4337844375);
////        boolean h = prettyPoly.getBounds().contains(118.373176375, 29.6857375625);
////        boolean h = prettyPoly.getBounds().contains(128.041145125, 38.2111281875);
//        boolean h = prettyPoly.contains(38.9687453744325,127.42415293254251);
//        if(h == true){
//            System.out.println("true");
//        }else{
//            System.out.println("false");
//        }
//    }

    @Scheduled( fixedDelayString = "1000")
    public void testMid(){
        Long start = System.currentTimeMillis();
        List<FolderDTO> folderDTOS= redisRepository.findFolder();
        Long end = System.currentTimeMillis();
        LOGGER.info(" {} -{} = {}",end,start,end-start);
        //37.889353 122.347252
        String a ="";
        PointDTO pointFirst = new PointDTO();
        pointFirst.setLatitude(21.51190943750001);
        pointFirst.setLongitude(108.13391856250001);
        PointDTO pointSecond = new PointDTO();
        pointSecond.setLatitude(20.80878443750001);
        pointSecond.setLongitude(109.62805918750001);
        List<PointDTO> listPoint1 = createPath(pointFirst,pointSecond);
//        PointDTO mid = getMidpoint(pointFirst,pointSecond);
        PointDTO mid = new PointDTO();
        mid.setLatitude(20.12929581591575D);
        mid.setLongitude(107.71857942297453D);

        List<PointDTO> listPoint2 = createPath(mid,90000.0D);
        for (PointDTO point: listPoint2
             ) {
            a+=point.getLongitude()+" "+point.getLatitude()+",";
        }
        System.out.println( " Miles\n");
        Path2D prettyPolyPath =new Path2D.Double();
        prettyPolyPath.moveTo(listPoint2.get(0).getLatitude(),listPoint2.get(0).getLongitude());
        for (int i=1;i<listPoint2.size();i++){
            prettyPolyPath.lineTo(listPoint2.get(i).getLatitude(),listPoint2.get(i).getLongitude());
        }
        prettyPolyPath.lineTo(listPoint2.get(0).getLatitude(),listPoint2.get(0).getLongitude());
        prettyPolyPath.closePath();
//        Path2D prettyPolyImages =new Path2D.Double();
//        List<PointDTO> pointImages = createRectangle(
//                20.690808738947133,107.74369279626305,
//                19.610968835014045,109.50157170964249);
//        prettyPolyImages.moveTo(pointImages.get(0).getLatitude(),pointImages.get(0).getLongitude());
//        for (int i=1;i<pointImages.size();i++){
//            prettyPolyImages.lineTo(pointImages.get(i).getLatitude(),pointImages.get(i).getLongitude());
//        }
//        prettyPolyImages.lineTo(pointImages.get(0).getLatitude(),pointImages.get(0).getLongitude());
//        prettyPolyImages.closePath();
//        boolean h = prettyPolyPath.intersects(prettyPolyImages.getBounds2D());
//        if(h == true){
//            System.out.println("true");
//        }else{
//            System.out.println("false");
//        }
        List<FolderDTO> folderDTOS1= getListImages(prettyPolyPath,folderDTOS);
        end = System.currentTimeMillis();
        LOGGER.info(" {} -{} = {}",end,start,end-start);
        LOGGER.info(" {} -{} = {}",end,start,end-start);

//        midPoint(pointFirst.getLatitude(),pointFirst.getLongitude(),pointSecond.getLatitude(),pointSecond.getLongitude());
//        LOGGER.info("mid {} -{}",mid.getLatitude(),mid.getLongitude());
    }
    private List<FolderDTO> getListImages(Path2D poly, List<FolderDTO> listFolderCheck){
        List<FolderDTO> result = new ArrayList<>();
        for (FolderDTO folder: listFolderCheck
             ) {
            Path2D imagePoly = createImages(folder);
            boolean h = poly.intersects(imagePoly.getBounds2D());
            if(h == true){
                result.add(folder);
            }
        }
        return result;
    }

    private List<FolderDTO> getListImages(List<PointDTO> pointDTOS, List<FolderDTO> listFolderCheck){
        Set<FolderDTO> folderDTOS = new HashSet<>();
        for (int i=0;i<pointDTOS.size()-1;i++
             ) {
            PointDTO mid = getMidpoint(pointDTOS.get(i),pointDTOS.get(i+1));
            Double distant = distance(pointDTOS.get(i).getLatitude(),pointDTOS.get(i).getLongitude(),pointDTOS.get(i+1).getLatitude(),pointDTOS.get(i+1).getLongitude());
            List<PointDTO> listPoint2 = createPath(mid,distant);
            Path2D poly= createPoly(listPoint2);
            for (FolderDTO folder: listFolderCheck
            ) {
                Path2D imagePoly = createImages(folder);
                boolean h = poly.intersects(imagePoly.getBounds2D());
                if(h == true){
                    folderDTOS.add(folder);
                }
            }

        }
        List<FolderDTO> result = new ArrayList<>(folderDTOS);

        return result;
    }
    private Path2D createPoly(List<PointDTO> listPoint2){
        Path2D prettyPolyPath =new Path2D.Double();
        prettyPolyPath.moveTo(listPoint2.get(0).getLatitude(),listPoint2.get(0).getLongitude());
        for (int j=1;j<listPoint2.size();j++){
            prettyPolyPath.lineTo(listPoint2.get(j).getLatitude(),listPoint2.get(j).getLongitude());
        }
        prettyPolyPath.lineTo(listPoint2.get(0).getLatitude(),listPoint2.get(0).getLongitude());
        prettyPolyPath.closePath();
        return prettyPolyPath;
    }

    private Path2D createImages(FolderDTO folderDTO){
        Path2D prettyPolyImages =new Path2D.Double();
        prettyPolyImages.moveTo(folderDTO.getLat1(),folderDTO.getLong1());
        prettyPolyImages.lineTo(folderDTO.getLat2(),folderDTO.getLong2());
        prettyPolyImages.lineTo(folderDTO.getLat3(),folderDTO.getLong3());
        prettyPolyImages.lineTo(folderDTO.getLat4(),folderDTO.getLong4());
        prettyPolyImages.lineTo(folderDTO.getLat1(),folderDTO.getLong1());
        prettyPolyImages.closePath();
        return prettyPolyImages;
    }

    private List<PointDTO> createPath(PointDTO point1,PointDTO point2){
        PointDTO mid = getMidpoint(point1,point2);
        List<PointDTO> pointList = new ArrayList<>();
        double distance = distance(point1.getLatitude(),point1.getLongitude(),point2.getLatitude(),point2.getLongitude());
        for (int i=0;i<17;i++
             ) {
                double bear= 21.17*i;
                PointDTO pointTmp= PointDTO.getPoint(bear,mid,(float) distance);
                pointList.add(pointTmp);

        }
        return pointList;
    }
    private List<PointDTO> createPath(PointDTO point1,double distance){
        List<PointDTO> pointList = new ArrayList<>();
        for (int i=0;i<17;i++
        ) {
            double bear= 21.17*i;
            PointDTO pointTmp= PointDTO.getPoint(bear,point1,(float) distance);
            pointList.add(pointTmp);

        }
        return pointList;
    }

//    private PointDTO createPointWithDistance(PointDTO pointMid,double distance,double bear){
//        double dist = 150/6371;
//        double brng = Math.toRadians(bear);
//        double lat1 = Math.toRadians(pointMid.getLatitude());
//        double lon1 = Math.toRadians(pointMid.getLongitude());
//
//        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
//        double a = Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
//        System.out.println("a = " +  a);
//        double lon2 = lon1 + a;
//
//        lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;
//
//    }


    List<PointDTO> createRectangle(double lat1,double lon1,double lat2,double lon2){

        PointDTO pointA = new PointDTO();
        pointA.setLatitude(lat1);
        pointA.setLongitude(lon1);

        PointDTO pointB = new PointDTO();
        pointB.setLatitude(lat1);
        pointB.setLongitude(lon2);
        PointDTO pointC = new PointDTO();
        pointC.setLatitude(lat2);
        pointC.setLongitude(lon2);
        PointDTO pointD = new PointDTO();
        pointD.setLatitude(lat2);
        pointD.setLongitude(lon1);
        List<PointDTO> pointDTOS = new ArrayList<>();
        pointDTOS.add(pointA);
        pointDTOS.add(pointB);
        pointDTOS.add(pointC);
        pointDTOS.add(pointD);
        return pointDTOS;

    }

//    public static void midPoint(double lat1,double lon1,double lat2,double lon2){
//
//
//        double pi = 3.14159265358979323846264338327950288419716939937510D;
//        double dLon = (lon2 - lon1)*pi/180;
//        lat1 = lat1*pi/180;
//        lon1 = lon1*pi/180;
//        lat2 = lat2*pi/180;
//
//
////        //convert to radians
////        lat1 = Math.toRadians(lat1);
////        lat2 = Math.toRadians(lat2);
////        lon1 = Math.toRadians(lon1);
//
//        double Bx = Math.cos(lat2) * Math.cos(dLon);
//        double By = Math.cos(lat2) * Math.sin(dLon);
//        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
//        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
//
//
//        //print out in degrees
//        System.out.println(lat3*180/pi + " " + lon3*180/pi);
//    }

    public double calculateBearingBetween(PointDTO pointFirst, PointDTO pointSecond) {
        double startLongitude = pointFirst.getLongitude() * 3.141592653589793D / 180.0D;
        double startLatitude = pointFirst.getLatitude() * 3.141592653589793D / 180.0D;
        double endLongitude = pointSecond.getLongitude() * 3.141592653589793D / 180.0D;
        double endLatitude = (double)((float)(pointSecond.getLatitude() * 3.141592653589793D / 180.0D));
        double y = Math.sin(endLongitude - startLongitude) * Math.cos(endLatitude);
        double x = Math.cos(startLatitude) * Math.sin(endLatitude) - Math.sin(startLatitude) * Math.cos(endLatitude) * Math.cos(endLongitude - startLongitude);
        double θ = Math.atan2(y, x);
        double brng = (θ * 180.0D / 3.141592653589793D + 360.0D) % 360.0D;
        return brng;
    }

    public PointDTO createPoint(PointDTO pointFirst, PointDTO pointSecond){
        double bearing = this.calculateBearingBetween(pointFirst, pointSecond);
        if (bearing > 90.0D) {
            bearing -= 90.0D;
        } else {
            bearing = 90.0D - bearing;
        }
        PointDTO top= new PointDTO();
        PointDTO bottom= new PointDTO();

        PointDTO point1 = PointDTO.getPoint(bearing, pointFirst, 1000.0F);
        PointDTO point2 = PointDTO.getPoint((bearing + 180.0D) % 360.0D, pointFirst, 1000.0F);
        if (point1.getLongitude() > point2.getLongitude()) {
           top=point1;
            bottom=point2;
        } else {
            top=point2;
            bottom=point1;
        }
        return top;
    }

    public PointDTO getMidpoint(PointDTO pointFirst, PointDTO pointSecond){
        double pi = 3.14159265358979323846264338327950288419716939937510D;
        double φ1 = pointFirst.getLatitude()*pi/180;
        double λ1 = pointFirst.getLongitude()*pi/180;
        double φ2 = pointSecond.getLatitude()*pi/180;
        double λ2 = pointSecond.getLongitude()*pi/180;
//        double π = 3.141592653589793D;
//        double pi = 3.14159265358979323846264338327950288419716939937510D;

        if (Math.abs(λ2 - λ1) > pi) λ1 += 2 * pi; // crossing anti-meridian

        double φ3 = (φ1 + φ2) / 2;
        double f1 = Math.tan(pi / 4 + φ1 / 2);
        double f2 = Math.tan(pi / 4 + φ2 / 2);
        double f3 = Math.tan(pi / 4 + φ3 / 2);
        double λ3 = ((λ2 - λ1) * Math.log(f3) + λ1 * Math.log(f2) - λ2 * Math.log(f1)) / Math.log(f2 / f1);

        if (!Double.isFinite(λ3)) λ3 = (λ1 + λ2) / 2; // parallel of latitude
        PointDTO mid = new PointDTO();
        double lat = φ3*180/pi;
        double lon = λ3*180/pi;
        mid.setLatitude(lat);
        mid.setLongitude(lon);
//        if (Math.abs(pointSecond.getLongitude()-pointFirst.getLongitude()) > Math.PI)
//            pointFirst.setLongitude(pointFirst.getLongitude()+2*Math.PI);
//        PointDTO mid = new PointDTO();
//        mid.setLatitude((pointFirst.getLatitude()+pointSecond.getLatitude())/2);
//        double f1 = Math.tan(Math.PI/4 + pointFirst.getLatitude()/2);
//        double f2 = Math.tan(Math.PI/4 + pointSecond.getLatitude()/2);
//        double f3 = Math.tan(Math.PI/4 + mid.getLatitude()/2);
//        double longitudeMid = ( (pointSecond.getLongitude()-pointFirst.getLongitude())*Math.log(f3) + pointFirst.getLongitude()*Math.log(f2) - pointSecond.getLongitude()*Math.log(f1) ) / Math.log(f2/f1);
//        if (!Double.isFinite(longitudeMid)) longitudeMid = (pointFirst.getLongitude()+pointSecond.getLongitude())/2;
//        mid.setLongitude(longitudeMid);
        return mid;
    }

    public static double distance(double lat1, double lon1 , double lat2,
                                  double lon2) {
//        double dist = org.apache.lucene.util.SloppyMath.haversinMeters(lat1, lon1, lat2, lon2);
        double R = 6371e3; // metres
        double φ1 = lat1*Math.PI/180;
        double λ1 = lon1*Math.PI/180;
        double φ2 = lat2*Math.PI/180;
        double λ2 = lon2*Math.PI/180;
        double Δφ = φ2 - φ1;
        double Δλ = λ2 - λ1;
        double a = Math.sin(Δφ/2)*Math.sin(Δφ/2) + Math.cos(φ1)*Math.cos(φ2) * Math.sin(Δλ/2)*Math.sin(Δλ/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d;
    }





    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        return scheduler;
    }
}
