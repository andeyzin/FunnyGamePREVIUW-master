package com.example.andrey.luckytube;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Random;


class Machine extends AsyncTask<String, Void, String> {


    public static final String apiKey = "AIzaSyBtR6GsaU4fUKAI4tZuLJfljOD8fIiF0S8";

    private static YouTube youtube;
    private static YouTube.Search.List search;


    @Override
    protected String doInBackground(String... strings) {
        String ret = null;
        String query = strings[1];
        String duration = strings[2];
        switch(strings[0]){
            case "randomWord":
                //------------------------------randomWord------------------------------------------
                Document doc;
                Elements els = null;
                try {
                    doc = Jsoup.connect("https://kartaslov.ru/ассоциации-к-слову/" + strings[1].toLowerCase() + "/").get();
                    els = doc.getElementsByClass("wordLink");
                } catch (IOException e) {}

                List<String> list = els.eachText();

                if (els == null || els.eachText().size() == 0 || !els.hasText()){
                    list.clear();
                    list.add(strings[1]);
                }
                System.out.println(els.eachText());
                System.out.println("\n\n\n\n\n\n\n" + list.toString());
                Random r = new Random();

                ret = list.get(r.nextInt(list.size()));
                query = ret;
            case "startVideo":
                //-----------------------------startVideo-------------------------------------------

                try {
                    youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY,
                            new HttpRequestInitializer() {
                                public void initialize(HttpRequest request) throws IOException {}
                            }
                    ).setApplicationName("randVideo").build();
                    search = youtube.search().list("id,snippet");

                    r = new Random();
                    search.setQ(query);
                    search.setKey(apiKey);
                    search.setType("video");
                    search.setMaxResults((long) 50);
                    search.setVideoDuration(duration);

                    SearchListResponse searchResponse = search.execute();
                    List<SearchResult> searchResultList = searchResponse.getItems();

                    ret = searchResultList.get(r.nextInt(searchResultList.size())).toString();

                } catch (GoogleJsonResponseException e) {
                    e.printStackTrace();
                    ret = "There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    ret = "There was an IO error: " + e.getCause() + " : " + e.getMessage();
                } catch (Exception e) {
                    ret = e.getMessage();
                }
                break;
        }
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(ret);
            URL newUrl = new URL(jsonObj.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url"));
            MainActivity.videoId =  jsonObj.getJSONObject("id").getString("videoId");
            MainActivity.videoTitle = jsonObj.getJSONObject("snippet").getString("title");
            MainActivity.preview = BitmapFactory.decodeStream(newUrl.openConnection() .getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(final String results) {
        super.onPostExecute(results);
        MainActivity.onFinishTask();

    }
}

class Auth {
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";

    public static Credential authorize(List<String> scopes, String credentialDatastore) throws IOException {
        Reader clientSecretReader = new InputStreamReader(Auth.class.getResourceAsStream("/client_secrets.json"));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);
        if (clientSecrets.getDetails().getClientId().startsWith("Enter") || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.exit(1);
        }
        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setCredentialDataStore(datastore)
                .build();
        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }
}

