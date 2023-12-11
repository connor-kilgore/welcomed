package org.welcomedhere.welcomed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NearbyPlaceFinder extends Thread{

    private String url;
    private String key;
    private String data = null;
    String queryType;
    double latitude;
    double longitude;

    ArrayList<NearbyPlace> places = new ArrayList<>();

    public NearbyPlaceFinder(double latitude, double longitude, String radius, String type, String key, String queryType){
        this.latitude = latitude;
        this.longitude = longitude;
        this.queryType = queryType;
        if(queryType.equals("nearby"))
        {
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + // url start
                    "location=" + latitude + "," + longitude +  // location
                    "&radius=" + radius + // search radius
                    "&type=" + type + // type of place
                    "&key=" + key; // api key
        }
        else
        {
            url = "https://maps.googleapis.com/maps/api/place/textsearch/json?" + // url start
                    "location=" + latitude + "," + longitude +  // location
                    "&rankby=distance" + // find closest
                    "&query=" + type + // type of place
                    "&key=" + key; // api key
        }

        this.key = key;
    }


    @Override
    public void run(){
        try {
            data = downloadUrl(url);
            places = parseJsonData(queryType);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    private Bitmap getImageBitMap(String photoRef) throws IOException {
        URL photoUrl = new URL("https://maps.googleapis.com/maps/api/place/photo?" + // url
                "maxwidth=400" + // photo size
                "&photo_reference=" + photoRef + // photo reference
                "&key=" + key);   // api key
        byte[] bytebuffer = new byte[2048];

        // create input/output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            // open stream and read bytes
            is = photoUrl.openStream();
            int n;

            while ((n = is.read(bytebuffer)) > 0 ) {
                bos.write(bytebuffer, 0, n);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // close streams
        bos.close();
        is.close();

        // convert to bitmap and return
        return BitmapFactory.decodeByteArray(bytebuffer, 0, bytebuffer.length);
    }
     */

    private String downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream in = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String line = "";

        while((line = reader.readLine()) != null){
            builder.append(line);
        }
        String data = builder.toString();
        reader.close();
        return data;
    }

    private ArrayList<NearbyPlace> parseJsonData(String queryType) throws JSONException {
        ArrayList<NearbyPlace> ret = new ArrayList<>();
        JSONArray places = new JSONObject(data).getJSONArray("results");
        for (int index = 0; index < places.length(); index++)
        {
            try {
                JSONObject oneObject = places.getJSONObject(index);

                // String photoRef = oneObject.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
                String addressQuery = "vicinity";
                if(queryType.equals("text"))
                {
                    addressQuery = "formatted_address";
                }

                // create a NearbyPlace
                NearbyPlace newPlace = new NearbyPlace(oneObject.getString("place_id"), oneObject.getString(addressQuery),
                        oneObject.getString("name"),
                        getDistance(Math.toRadians(latitude), Math.toRadians((double)oneObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat")),
                                Math.toRadians(longitude), Math.toRadians((double)oneObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng"))),
                        null);
                ret.add(newPlace);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    private double getDistance(double lat1, double lat2, double long1, double long2)
    {
         return Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(long2-long1))*3958.8;
    }
}