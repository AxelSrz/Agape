package com.example.raula.agape;

import java.util.List;

/**
 * Created by raula on 2/21/2017.
 */

public class LocationModel {
    int longitude;
    int latitude;

    static List<LocationModel> locationList;

    public LocationModel(int longt, int lat){
        longitude = longt;
        latitude = lat;
    }
}
