package kahlout.me.rangebuddy.Libraries;

/**
 * Created by malikahlout on 29/01/2017.
 *
 * changed to convert meters to yards
 *
 */

/// Method to calculate the distance between to points on a map.


public class DistanceMath {

    public static double distanceYards(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = distance / 0.9144; // convert to yards

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);


        return Math.sqrt(distance);
    }

    public static double distanceMeters(double lat1, double lat2, double lon1,
                                       double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters


        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);


        return Math.sqrt(distance);
    }

}