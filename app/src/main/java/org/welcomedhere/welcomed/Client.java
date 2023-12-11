package org.welcomedhere.welcomed;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;


public class Client extends Thread {
    // set constants
    public static final String IP_ADDRESS = "ec2-54-151-43-5.us-west-1.compute.amazonaws.com";   // TODO: make this encrypted value in keystore
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
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("AKIAQIQXYNDUOJOBQ5U4", "iPmqoEg9yEFn4eQZPJHgL9GDYZy2BeB6JPU7ST9L"));
        S3AsyncClient s3Client = S3AsyncClient.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(Region.US_WEST_1)
                .build();

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(image.path)
                .build();

        File imageFile = new File(context.getFilesDir(), "imageFile.jpg");

        // Start the call to Amazon S3, not blocking to wait for the result
        CompletableFuture<GetObjectResponse> responseFuture =
                s3Client.getObject(GetObjectRequest.builder()
                                .bucket(BUCKET_NAME)
                                .key(image.path)
                                .build(),
                        AsyncResponseTransformer.toFile(imageFile));

        // When future is complete (either successfully or in error), handle the response
        CompletableFuture<GetObjectResponse> operationCompleteFuture =
                responseFuture.whenComplete((getObjectResponse, exception) -> {
                    if (getObjectResponse != null) {
                        // At this point, the file my-file.out has been created with the data
                    } else {
                        // Handle the error
                        exception.printStackTrace();
                    }
                });

        // We could do other work while waiting for the AWS call to complete in
        // the background, but we'll just wait for "whenComplete" to finish instead
        operationCompleteFuture.join();

        return imageFile;
    }

    private void sendImageToBucket(ImageInfo image)
    {
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("AKIAQIQXYNDUOJOBQ5U4", "iPmqoEg9yEFn4eQZPJHgL9GDYZy2BeB6JPU7ST9L"));

        S3AsyncClient s3Client = S3AsyncClient.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(Region.US_WEST_1)
                .build();

        String imageFilePath = getFilePathFromUri(image.photoUri);


        s3Client.putObject(builder -> builder.bucket(BUCKET_NAME).key(image.path), Paths.get(imageFilePath));

        System.out.println("File uploaded successfully!");
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

    private String getFilePathFromUri(Uri uri)
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
            return filePath;
            // 'file' contains the File object representing the file corresponding to the Uri
        } else {
            // Handle the case where filePath is null or couldn't be retrieved
            return null;
        }
    }
}