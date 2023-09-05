package org.welcomedhere.welcomed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;

import org.welcomedhere.welcomed.User;

import java.io.*;
import java.net.*;


public class Client extends Thread {
    // set constants
    public static final String IP_ADDRESS = "ec2-54-241-147-131.us-west-1.compute.amazonaws.com";
    public static final int PORT = 23657;
    public User profileResult = null;
    private String parameter;
    private Object object;
    private User profile;
    private String queryType;
    public Context context;

    public Client(String parameter)
    {
        this.parameter = parameter;
    }

    // general constructor
    public Client(Object object, String parameter)
    {
        this.object = object;
        this.parameter = parameter;
    }


    // called when new thread is created
    @Override
    public void run()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        // check if review type
        if(object instanceof Review)
        {
            if(parameter == "create")
            {
                Review review = (Review)object;
                review.date = (String) getObjectFromServer();
            }
        }
        else if(object instanceof Report)
        {
            if(parameter == "create")
            {
                sendObjectToServer();
            }
        }
        else if(object instanceof KarmaListing)
        {
            if(parameter == "updateKarma")
            {
                sendObjectToServer();
            }
        }
        else if(object instanceof User)
        {
            if (parameter == "GET")
            {
                profileResult = (User)getObjectFromServer();
            }
            else
            {
                sendObjectToServer();
            }
        }
        else if(object instanceof Uri)
        {
            sendPictureToServer((Uri) object, parameter);
        }
    }

    private void sendObjectToServer()
    {
        try
        {
            // create a socket
            Socket socket = new Socket(IP_ADDRESS, PORT);

            // create output stream
            ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
            // create input stream
            ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());

            // send review to server
            toServer.writeObject(object);

            // close socket
            socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void sendPictureToServer(Uri currentUri, String operation)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try
        {
            // create a socket
            Socket socket = new Socket(IP_ADDRESS, PORT);

            // find the photo and convert it to a bitmap
            File selected_photo;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(FileChooser.getPath(context, currentUri), options);

            // compress the bitmap into a PNG byte array
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            byte[] byteArray = byteStream.toByteArray();

            // create an object output stream
            ObjectOutputStream  oos = new ObjectOutputStream(socket.getOutputStream());
            // get the current userID
            String userID = FirebaseAuth.getInstance().getUid();

            ImageInfo info = new ImageInfo(userID + ".png", byteArray.length);
            if(operation.equals("sendProfilePicture"))
            {
                info.op = ImageInfo.ImageOpertion.SEND_PROFILE_PICTURE;
            }
            else if(operation.equals("sendReviewPicture"))
            {
                info.op = ImageInfo.ImageOpertion.SEND_REVIEW_PICTURE;
            }
            else if(operation.equals("sendReportPicture"))
            {
                info.op = ImageInfo.ImageOpertion.SEND_REPORT_PICTURE;
            }
            else if(operation.equals("getProfilePicture"))
            {
                info.op = ImageInfo.ImageOpertion.GET_PROFILE_PICTURE;
            }
            else if(operation.equals("getReviewPicture"))
            {
                info.op = ImageInfo.ImageOpertion.GET_REVIEW_PICTURE;
            }
            else if(operation.equals("getReportPicture"))
            {
                info.op = ImageInfo.ImageOpertion.GET_REPORT_PICTURE;
            }

            // send the image info to the server
            oos.writeObject(info);
            oos.flush();

            // send the complete byte array to the server
            byteStream.writeTo(socket.getOutputStream());

        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Object getObjectFromServer()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        try
        {
            // create a socket
            Socket socket = new Socket(IP_ADDRESS, PORT);

            // create output stream
            ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
            // create output stream
            PrintWriter toServerStr = new PrintWriter(socket.getOutputStream(), true);
            // create input stream
            ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());

            // check if object is not null
            if(object != null)
            {
                // send input to server
                toServer.writeObject(object);
            }
            else
            {
                // send input to server
                toServer.writeObject(parameter);
            }


            //capture response
            Object returnVal = fromServer.readObject();

            // close socket
            socket.close();

            // return the given reviews
            return returnVal;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}