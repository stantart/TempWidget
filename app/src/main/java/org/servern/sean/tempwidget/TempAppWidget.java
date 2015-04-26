package org.servern.sean.tempwidget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Implementation of App Widget functionality.
 */
public class TempAppWidget extends AppWidgetProvider {

    private static final String LOG = "tempWidget";
    private static final String url = "";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.i(LOG, "onUpdate method called");
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, TempAppWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(), TempService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        // Update the widgets via the service
        context.startService(intent);
    }

/*    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(LOG, "onUpdate");

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            // Register an onClickListener
            Intent clickIntent = new Intent(context, TempAppWidget.class);
            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            Log.d(LOG, "appWidgetId = " + appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.temp_app_widget);
            remoteViews.setOnClickPendingIntent(R.id.appwidget_id, pendingIntent);
//            remoteViews.setTextViewText(R.id.appwidget_id, ".." );

            //refresh widget to show text
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }*/

//    @Override
//    public void onReceive(Context context, Intent intent){
//        super.onReceive(context, intent);
//        Log.d(LOG, "onReceive, intent = " + intent.getAction().toString());
//
//        /*if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")){
//            updateWidget(context,".. ℃");
//            new RequestTask(context).execute(url);
//            updateListener(context);
//        }*/
//    }

    public void updateListener(Context context){
        Log.d(LOG, "Updating clickListener");
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        //get widget ids for widgets created
        ComponentName widget = new ComponentName(context, TempAppWidget.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widget);
        for (int i = 0; i < widgetIds.length; i++) {
            int appWidgetId = widgetIds[i];
            Intent clickIntent = new Intent(context, TempAppWidget.class);
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

//    public class UpdateWidgetService extends IntentService {
//        public UpdateWidgetService() {
//            super("UpdateWidgetService");
//        }
//
//        @Override
//        protected void onHandleIntent(Intent intent) {
//            Log.i(LOG, "onStartCommand called");
//            // create some random data
//
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
//                    .getApplicationContext());
//
//            int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//
//            ComponentName tempWidget = new ComponentName(getApplicationContext(),
//                    TempAppWidget.class);
//            int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(tempWidget);
//            Log.d(LOG, "From Intent " + String.valueOf(allWidgetIds.length));
//            Log.d(LOG, "Direct " + String.valueOf(allWidgetIds2.length));
//
//
//            for (int widgetId : allWidgetIds) {
//
//
//                //Toast.makeText(getApplicationContext(), "HTTP request sent...", Toast.LENGTH_SHORT).show();
//
//                RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
//                        R.layout.temp_app_widget);
//
//                // Set the text
//                remoteViews.setTextViewText(R.id.appwidget_id, "..");
//
//                // Register an onClickListener
//                Intent clickIntent = new Intent(this.getApplicationContext(),
//                        TempAppWidget.class);
//
//                clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//                remoteViews.setOnClickPendingIntent(R.id.appwidget_id, pendingIntent);
//                appWidgetManager.updateAppWidget(widgetId, remoteViews);
//            }
//        }
//    }

}
