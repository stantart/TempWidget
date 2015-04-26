package org.servern.sean.tempwidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class TempService extends Service {

    private static final String LOG = "tempWidgetService";
    private static final String url = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG, "onStartCommand called");
        // create some random data

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        ComponentName tempWidget = new ComponentName(getApplicationContext(), TempAppWidget.class);
        int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(tempWidget);
        Log.d(LOG, "From Intent " + String.valueOf(allWidgetIds.length));
        Log.d(LOG, "Direct " + String.valueOf(allWidgetIds2.length));


        for (int widgetId : allWidgetIds) {

            new RequestTask(getApplicationContext()).execute(url);

            //Toast.makeText(getApplicationContext(), "HTTP request sent...", Toast.LENGTH_SHORT).show();

            RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
                    R.layout.temp_app_widget);

            // Set the text in the widget
            remoteViews.setTextViewText(R.id.appwidget_id,"..");

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(), TempAppWidget.class);
            Intent clickIntent2 = new Intent("ACTION_APPWIDGET_UPDATE");

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.appwidget_id, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        stopSelf();
        super.onStartCommand(intent,flags,startId);
        return Service.START_REDELIVER_INTENT;
    }


    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public void updateListener(Context context){
        Log.d(LOG, "Updating clickListener");
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        //get widget ids for widgets created
        ComponentName widget = new ComponentName(context, TempAppWidget.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widget);
        for (int i = 0; i < widgetIds.length; i++) {
            int appWidgetId = widgetIds[i];
            Intent clickIntent = new Intent(context, TempAppWidget.class);
            Intent clickIntent2 = new Intent("ACTION_APPWIDGET_UPDATE");

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            Log.d(LOG, "appWidgetId = " + appWidgetId);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.temp_app_widget);
            remoteViews.setOnClickPendingIntent(R.id.appwidget_id, pendingIntent);
        }
    }

    public void updateWidget(Context context, String text){
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        //get widget ids for widgets created
        ComponentName widget = new ComponentName(context, TempAppWidget.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widget);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.temp_app_widget);
        remoteViews.setTextViewText(R.id.appwidget_id, text );

        Log.d(LOG, "Update widget text with: " + text);
        updateListener(context);

        //refresh widget to show text
        widgetManager.updateAppWidget(widgetIds, remoteViews);
    }

    public class RequestTask extends AsyncTask<String, String, String> {

        private Context mContext;

        public RequestTask(Context context) {

            mContext = context;
        }
        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString;
            try {
                Log.d(LOG, "sleep for 400ms...");
                Thread.sleep(400);
                Log.d(LOG, "Send HTTP Request");
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    Log.d(LOG, "HTTP Result is = " + responseString);
                } else{
                    //Closes the connection.
                    Log.w(LOG, "Something is wrong with the StatusCode: " + statusLine.getReasonPhrase());
                    response.getEntity().getContent().close();

                    throw new IOException();
                }
            }
            catch (InterruptedException e) {
                responseString = "-";
                e.printStackTrace();
            }
            catch (ClientProtocolException e) {
                responseString = "-";
            }
            catch (IOException e) {
                responseString = "-";
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(LOG, "in onPostExecute");
            updateWidget(this.mContext, result + " \u2103");
        }
    }
}
