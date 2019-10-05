package com.sparkyts.guessthecelebrity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    ArrayList<Pair> nameAndImage = new ArrayList<>();
    SecureRandom random = new SecureRandom();
    int currentPerson = 0;
    String[] branchList = {"computer", "it", "civil", "mechanical", "electronics", "mathematics"};

    protected class Pair{
        String name, url;

        Pair(String name, String url){
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String>{

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage("Loading...");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                filterNameAndImage(result);
                showRandomImageAndOptions();
                this.dialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {

                StringBuilder sb = new StringBuilder();
                int i = 0;
                for(int j = 0 ;  j < branchList.length ; j++){
                    URL url = new URL(urls[j]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();

                    while((i=inputStream.read())!=-1){
                        if((char)i!='\n')
                            sb.append((char)i);
                    }

                }
                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Failed";
        }
    }

    private class DownloadImageTask extends  AsyncTask <String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... imageUrls) {
            try {
                URL url = new URL(imageUrls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                return BitmapFactory.decodeStream(urlConnection.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadTask task = new DownloadTask();
        task.execute("https://git.org.in/"+ branchList[0] +"-engineering/faculty-members/",
                     "https://git.org.in/"+ branchList[1] +"-engineering/faculty-members/",
                     "https://git.org.in/"+ branchList[2] +"-engineering/faculty-members/",
                     "https://git.org.in/"+ branchList[3] +"-engineering/faculty-members/",
                     "https://git.org.in/"+ branchList[4] +"-engineering/faculty-members/",
                     "https://git.org.in/"+ branchList[5] +"-engineering/faculty-members/");
    }

    private void showRandomImageAndOptions() throws ExecutionException, InterruptedException {
        ImageView imageView = findViewById(R.id.imageView);
        DownloadImageTask imageTask = new DownloadImageTask();
        currentPerson = random.nextInt(nameAndImage.size());

        Bitmap bitmap = imageTask.execute(nameAndImage.get(currentPerson).getUrl()).get();
        imageView.setImageBitmap(bitmap);

        populateOptions();
    }

    private void populateOptions() {
        HashSet<Integer> options = new HashSet<>();
        options.add(currentPerson);
        while(options.size()!=4)
            options.add(random.nextInt(nameAndImage.size()));

        int i = 1;
        ConstraintLayout layout = findViewById(R.id.layout);
        for(int option: options){
            ((Button)layout.findViewWithTag("name"+i)).setText(nameAndImage.get(option).getName());
            i++;
        }

    }

    public void verifyName(View view) throws ExecutionException, InterruptedException {
        if(((Button)view).getText()==nameAndImage.get(currentPerson).getName())
            Toast.makeText(this,"Correct!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Wrong! It was " + nameAndImage.get(currentPerson).getName(), Toast.LENGTH_SHORT).show();
        showRandomImageAndOptions();
    }

    private void filterNameAndImage(String result) {
        String[] userData = result.split("class=\"teamlist-popup zoom-anim-dialog mfp-hide \"");
        Pattern namePattern = Pattern.compile("<div class=\"dv-panel-title\">(.*?)</div>");
        Pattern imgUrlPattern = Pattern.compile("<div class=\"dv-panel-image\"><img src=\"(.*?)\"");
        Matcher nameMatcher, urlMatcher;
        for(String uData : userData){

            nameMatcher = namePattern.matcher(uData);
            urlMatcher = imgUrlPattern.matcher(uData);

            if(nameMatcher.find() && urlMatcher.find()){
                nameAndImage.add(new Pair(nameMatcher.group(1).trim(), urlMatcher.group(1)));
                System.out.println(urlMatcher.group(1));
            }
        }
    }

}