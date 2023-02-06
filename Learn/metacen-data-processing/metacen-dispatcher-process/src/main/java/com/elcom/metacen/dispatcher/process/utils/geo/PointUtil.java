package com.elcom.metacen.dispatcher.process.utils.geo;

import com.elcom.metacen.dispatcher.process.utils.JSONConverter;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author anhdv
 */
public class PointUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PointUtil.class);
    
    private Point[] geoPoints;

    public PointUtil() {
    }
    
    public PointUtil(Point[] points) {
        this.geoPoints = points;
    }

    public boolean contains(Point test) {
        int i, j;
        boolean result = false;
        for ( i = 0, j = geoPoints.length - 1; i < geoPoints.length; j = i++ ) {
            try {
                if ( geoPoints[i].y > test.y != geoPoints[j].y > test.y
                    && ( test.x < (geoPoints[j].x - geoPoints[i].x) * (test.y - geoPoints[i].y) / (geoPoints[j].y - geoPoints[i].y) + geoPoints[i].x ) )
                    result = !result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    
    public static boolean containsPoint(Point[] areaPoints, Point point) {
        int i, j;
        boolean result = false;
        for ( i = 0, j = areaPoints.length - 1; i < areaPoints.length; j = i++ ) {
            try {
                if ( areaPoints[i].y > point.y != areaPoints[j].y > point.y
                    && ( point.x < (areaPoints[j].x - areaPoints[i].x) * (point.y - areaPoints[i].y) / (areaPoints[j].y - areaPoints[i].y) + areaPoints[i].x ) )
                    result = !result;
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
            }
        }
        return result;
    }
    
    public static boolean geoPolygonIntersect(Coordinate[] coordinatesArea1, Coordinate[] coordinatesArea2) {
        GeometryFactory gf = new GeometryFactory();
        try {
            return gf.createPolygon(coordinatesArea1).intersects(gf.createPolygon(coordinatesArea2));
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return false;
        }
    }
    
//    public static void main(String[] args) {
//        
//        GeometryFactory gf = new GeometryFactory();
//        
//        Coordinate[] c1 = { new Coordinate(110.413667, 19.14369), new Coordinate(108.431189, 18.112922), new Coordinate(110.165801, 18.077879)
//                                , new Coordinate(110.200844, 18.594758), new Coordinate(110.413667, 19.14369) };
//        Geometry g1 = gf.createPolygon(c1);
//        
//        Coordinate[] c2 = { new Coordinate(108.378625, 17.026599), new Coordinate(109.026914, 17.464632), new Coordinate(108.913026, 19.427022)
//                                , new Coordinate(108.387385, 19.391979), new Coordinate(108.378625, 17.026599) };
//        Geometry g2 = gf.createPolygon(c2);
//        
//        Coordinate[] c3 = { new Coordinate(20.08358, 114.02689), new Coordinate(19.55506, 113.9485), new Coordinate(19.1853, 117.29314)
//                                , new Coordinate(20.34105, 117.37153), new Coordinate(20.08358, 114.02689) };
//        Geometry g3 = gf.createPolygon(c3);
//        
////        Geometry g4 = g1.intersection(g2);
//        
//        System.out.println("res: " + g1.intersects(g2));
//        
//        System.out.println("res: " + g1.intersects(g3));
//        
////        IntersectUtils.intersects(args, args);
//        
////        int[] xPoints1 = new int[] { 109, 110, 114, 115, 112, 110  };
////        int[] yPoints1 = new int[] { 16, 14, 14, 17, 19, 17 };
////        Polygon polygon1 = new Polygon(xPoints1, yPoints1, 6);
////        Area area1 = new Area(polygon1);
////
////        int[] xPoints2 = new int[] { 104, 105, 111 };
////        int[] yPoints2 = new int[] { 1, 2, 16 };
////        Polygon polygon2 = new Polygon(xPoints2, yPoints2, 3);
////        Area area2 = new Area(polygon2);
////
////        Rectangle r1 = new Rectangle(75,50,2,2);
////        
////        area2.intersect(area1);
////
////        System.out.println("isEmpty: " + area2.isEmpty());
//        
////        List<AreaPointsDTO> arrLst = new ArrayList<>();
////        arrLst.add(new AreaPointsDTO(109.9000740641, 16.8532021309));
////        arrLst.add(new AreaPointsDTO(110.5921693674, 14.6384971604));
////        arrLst.add(new AreaPointsDTO(114.1564601793, 14.2924495087));
////        arrLst.add(new AreaPointsDTO(115.2292078994, 17.510692669));
////        arrLst.add(new AreaPointsDTO(112.6684552773, 19.0679071014));
////        arrLst.add(new AreaPointsDTO(110.0730978899, 17.8221355555));
////        arrLst.add(new AreaPointsDTO(109.9000740641, 16.8532021309));
////        
////        System.out.println("res: " + containsPoint2(arrLst, new Point(Double.parseDouble("104.99982"), Double.parseDouble("1.7205538"))));
////        System.out.println("res: " + containsPoint2(arrLst, new Point(Double.parseDouble("105.98767"), Double.parseDouble("2.7141738"))));
////        System.out.println("res: " + containsPoint2(arrLst, new Point(Double.parseDouble("111.80700953746523"), Double.parseDouble("16.410983233321193"))));
//    }
    
    public static boolean containsPoint2(List<AreaPointsDTO> areaPoints, Point point) {
        
        LOGGER.info("==> [CHECK POINT] areaPoints: {} ### point: {}", JSONConverter.toJSON(areaPoints), JSONConverter.toJSON(point));
        
        int i, j;
        boolean result = false;
        for ( i = 0, j = areaPoints.size() - 1; i < areaPoints.size(); j = i++ ) {
            try {
                if ( areaPoints.get(i).getLatitude() > point.y != areaPoints.get(j).getLatitude() > point.y
                    && ( point.x < (areaPoints.get(j).getLongitude() - areaPoints.get(i).getLongitude()) * (point.y - areaPoints.get(i).getLatitude()) / (areaPoints.get(j).getLatitude() - areaPoints.get(i).getLatitude()) + areaPoints.get(i).getLongitude() ) )
                    result = !result;
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
            }
        }
        return result;
    }
    
    public static boolean pointInsideAreaWithTwoPoints(double originLongitude, double originLatitude, double cornerLongitude, double cornerLatitude, List<Point> pointToChecks) {
        
        LOGGER.info("==> [CHECK POINT] originLongitude: {}, originLatitude: {}, cornerLongitude: {}, cornerLatitude: {} ### pointToChecks: {}"
                , originLongitude, originLatitude, cornerLongitude, cornerLatitude, JSONConverter.toJSON(pointToChecks));
        
        for( Point pointToCheck : pointToChecks )
            if( pointToCheck.getX() >= originLongitude && pointToCheck.getX() <= cornerLongitude && pointToCheck.getY() >= originLatitude && pointToCheck.getY() <= cornerLatitude )
                return true;
        return false;
    }
    
    public static double calculateAngleBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        try {
            
            double angle0 = Math.atan2(x2 - x1, y2 - y1);
            
            double angle1 = Math.toDegrees(angle0);
            
            double angle2 = Math.ceil( -angle1 / 360 ) * 360;
            
            // Keep angle between 0 and 360
            angle1 += angle2;

            return angle1;
            
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return 0;
    }
    
    public static final double RAD_360_DEG = Math.PI * 360d / 180d;
    public static final double RAD_180_DEG = Math.PI * 180d / 180d;
    public static final double RAD_90_DEG = Math.PI * 90d / 180d;
    /**
     * @return The angle from north from p1 to p2. Returns (in radians) -180 to
     * 180, with 0 as north.
     */
    public static double getAngleBearing(double p1x, double p1y, double p2x, double p2y) {
        double result = Math.atan2(p2y - p1y, p2x - p1x) + RAD_90_DEG;

        if (result > RAD_180_DEG) {
            result = result - RAD_360_DEG;
        }

        return result;
    }
    
    /**
     * Calculates the angle from centerPt to targetPt in degrees. The return
     * should range from [0,360), rotating CLOCKWISE, 0 and 360 degrees
     * represents NORTH, 90 degrees represents EAST, etc...
     *
     * Assumes all points are in the same coordinate space. If they are not, you
     * will need to call SwingUtilities.convertPointToScreen or equivalent on
     * all arguments before passing them to this function.
     *
     * @param centerPt Point we are rotating around.
     * @param targetPt Point we want to calcuate the angle to.
     * @return angle in degrees. This is the angle from centerPt to targetPt.
     */
    public static double calcRotationAngleInDegrees(java.awt.Point centerPt, java.awt.Point targetPt) {
        // calculate the angle theta from the deltaY and deltaX values
        // (atan2 returns radians values from [-PI,PI])
        // 0 currently points EAST.  
        // NOTE: By preserving Y and X param order to atan2,  we are expecting 
        // a CLOCKWISE angle direction.  
        double theta = Math.atan2(targetPt.y - centerPt.y, targetPt.x - centerPt.x);

        // rotate the theta angle clockwise by 90 degrees 
        // (this makes 0 point NORTH)
        // NOTE: adding to an angle rotates it clockwise.  
        // subtracting would rotate it counter-clockwise
        theta += Math.PI / 2.0;

        // convert from radians to degrees
        // this will give you an angle from [0->270],[-180,0]
        double angle = Math.toDegrees(theta);

        // convert to positive range [0-360)
        // since we want to prevent negative angles, adjust them now.
        // we can assume that atan2 will not return a negative value
        // greater than one partial rotation
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }
}
