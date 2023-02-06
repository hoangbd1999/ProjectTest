package com.elcom.metacen.content.service.impl;

import com.elcom.metacen.content.dto.FolderDTO;
import com.elcom.metacen.content.dto.PointDTO;
import com.elcom.metacen.content.service.PathService;
import org.springframework.stereotype.Service;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PathServiceImpl implements PathService {
    @Override
    public List<FolderDTO> getListImages(List<PointDTO> pointDTOS, List<FolderDTO> listFolderCheck) {
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

    @Override
    public List<FolderDTO> getListImages(PointDTO pointDTO, double distant, List<FolderDTO> listFolderCheck) {
        List<PointDTO> listPoint2 = createPath(pointDTO,distant);
        Path2D  poly =createPoly(listPoint2);
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

    public PointDTO getMidpoint(PointDTO pointFirst, PointDTO pointSecond){
        double pi = 3.14159265358979323846264338327950288419716939937510D;
        double φ1 = pointFirst.getLatitude()*pi/180;
        double λ1 = pointFirst.getLongitude()*pi/180;
        double φ2 = pointSecond.getLatitude()*pi/180;
        double λ2 = pointSecond.getLongitude()*pi/180;
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
        return mid;
    }

    public static double distance(double lat1, double lon1 , double lat2,
                                  double lon2) {
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
}
