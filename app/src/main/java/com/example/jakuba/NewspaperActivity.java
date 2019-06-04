package com.example.jakuba;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class NewspaperActivity extends AppCompatActivity {

    public static final String TAG = "NewspaperActivity";
    private ListView listApps;
    DbHelper dbHelper;
    private String feedUrl = "https://www.newsweek.com/rss";
    private int feedLimit = 10;
    private String feedCachedUrl = "INVALIDATED";
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspaper);
        listApps = findViewById(R.id.xmlListView);
        setTitle("World News");
        dbHelper = new DbHelper(this);
        if(savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }

        downloadUrl(String.format(feedUrl, feedLimit));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menuNews:
                feedUrl = "https://www.newsweek.com/rss";
                setTitle("World News");
                break;
            case R.id.menuEntertaiment:
                feedUrl = "http://feeds.skynews.com/feeds/rss/entertainment.xml";
                setTitle("Entertainment");
                break;
            case R.id.menuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml";
                setTitle("Top Songs");
                break;
            case R.id.menuMovie:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topMovies/xml";
                setTitle("Top Movies");
                break;
            case R.id.menuRefresh:
                feedCachedUrl = "INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        downloadUrl(String.format(feedUrl, feedLimit));
        return true;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);
    }

    private void downloadUrl(String feedUrl) {
        if(!feedUrl.equalsIgnoreCase(feedCachedUrl)) {
            Log.d(TAG, "downloadUrl: starting Asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);
            feedCachedUrl = feedUrl;
            Log.d(TAG, "downloadUrl: done");
        } else {
            Log.d(TAG, "downloadUrl: URL not changed");
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            FeedAdapter feedAdapter = new FeedAdapter(NewspaperActivity.this, R.layout.list_record,
                    parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was " + response);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();

                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data: " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception.  Needs permisson? " + e.getMessage());
//                e.printStackTrace();
            }

            return null;
        }
    }

    private class ParseApplications {
        private static final String TAG = "ParseApplications";
        private ArrayList<FeedEntry> applications;

        public ParseApplications() {
            this.applications = new ArrayList<>();
        }

        public ArrayList<FeedEntry> getApplications() {
            return applications;
        }

        public boolean parse(String xmlData) {
            boolean status = true;
            FeedEntry currentRecord = null;
            boolean inEntry = false;
            String textValue = "";

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(xmlData));
                int eventType = xpp.getEventType();
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = xpp.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if("entry".equalsIgnoreCase(tagName) || "item".equalsIgnoreCase(tagName)) {
                                inEntry = true;
                                currentRecord = new FeedEntry();
                            }
                            break;

                        case XmlPullParser.TEXT:
                            textValue = xpp.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            if(inEntry) {
                                if("entry".equalsIgnoreCase(tagName)) {
                                    applications.add(currentRecord);
                                    inEntry = false;
                                } else if ("item".equalsIgnoreCase(tagName)) {
                                    dbHelper.addData(currentRecord.getName(), currentRecord.getArtist(), currentRecord.getSummary());
                                    applications.add(currentRecord);
                                    inEntry = false;
                                } else if("name".equalsIgnoreCase(tagName)) {
                                    currentRecord.setName(textValue);
                                } else if("artist".equalsIgnoreCase(tagName)) {
                                    currentRecord.setArtist(textValue);
                                } else if("summary".equalsIgnoreCase(tagName)) {
                                    currentRecord.setSummary(textValue);
                                } else if("title".equalsIgnoreCase(tagName)) {
                                    currentRecord.setName(textValue);
                                } else if("description".equalsIgnoreCase(tagName)) {
                                    currentRecord.setSummary(textValue);
                                }
                            }
                            break;
                        default:
                    }
                    eventType = xpp.next();

                }

            } catch(Exception e) {
                status = false;
                e.printStackTrace();
            }

            return status;
        }
    }
}
