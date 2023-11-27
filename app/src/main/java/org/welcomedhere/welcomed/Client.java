package org.welcomedhere.welcomed;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.firebase.auth.FirebaseAuth;

import com.amazonaws.regions.Regions;

import java.io.*;
import java.net.*;


public class Client extends Thread {
    // set constants
    public static final String IP_ADDRESS = "ec2-54-241-147-131.us-west-1.compute.amazonaws.com";   // TODO: make this encrypted value in keystore
    public static final int PORT = 23657;   // TODO: make this encrypted value in keystore
    private final String BUCKET_NAME = "welcomed-bucket";
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
        else if(object instanceof ImageInfo)
        {
            if (parameter == "GET")
            {
                getImageFromBucket((ImageInfo) object);
            } else if (parameter == "SEND") {
                sendImageToBucket((ImageInfo) object);
            }
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

    public File getImageFromBucket(ImageInfo image)
    {
        CognitoCachingCredentialsProvider credentialsProvider;
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-west-1:39690302-b15b-46d2-87cd-fdc5a58fbacb", // Identity Pool ID
                Regions.US_WEST_1 // Region
        );

        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);

        File imageFile = null;

        try {
            File outputDir = context.getCacheDir();
            imageFile = File.createTempFile("prefix", "extension", outputDir);

            S3Object o = s3Client.getObject(BUCKET_NAME, image.path);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(imageFile);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
                System.out.println("downloaded successfully!");
            }
            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            return null;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
        return imageFile;
    }

    private void sendImageToBucket(ImageInfo image)
    {
        CognitoCachingCredentialsProvider credentialsProvider;
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-west-1:39690302-b15b-46d2-87cd-fdc5a58fbacb", // Identity Pool ID
                Regions.US_WEST_1 // Region
        );

        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);

        File file = getFileFromUri(image.photoUri);
        PutObjectRequest putRequest = new PutObjectRequest(BUCKET_NAME, image.path, file);
        ObjectMetadata metadata = new ObjectMetadata();
        putRequest.setMetadata(metadata);

        s3Client.putObject(putRequest);
        System.out.println("File uploaded successfully!");
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

            ImageInfo info = new ImageInfo(userID + ".png", null);
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

    private File getFileFromUri(Uri uri)
    {
        // Assuming 'uri' is the Uri you want to convert to a File

        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }

        if (filePath != null) {
            File file = new File(filePath);
            return file;
            // 'file' contains the File object representing the file corresponding to the Uri
        } else {
            // Handle the case where filePath is null or couldn't be retrieved
            return null;
        }
    }
}