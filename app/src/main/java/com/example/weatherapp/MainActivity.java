package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity
{
    EditText editText;
    String city;
    TextView resultTextView;
    String message="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText=findViewById(R.id.editText);
        resultTextView=findViewById(R.id.resultTextView);
    }

    public void getWeather(View view)
    {
        try
        {
            DownloadTask task=new DownloadTask();
            JsonTask jsonTask=new JsonTask();

            //Suppose city name is city="New Delhi" or "San Francisco"
            //The browser automatically converts it into "New%20Delhi" or "San%20Francisco"
            //Incase it doesn't then here is the code to do it manually
            city=editText.getText().toString();
            String encodedCityName= URLEncoder.encode(editText.getText().toString(), "UTF-8");

            resultTextView.setText("");

            //Getting the entire JSON data from the URL
            task.execute("https://openweathermap.org/data/2.5/weather?q="+encodedCityName+"&appid=439d4b804bc8187953eb36d2a8c26a02");

            //When we press the button, the virtual onscreen keyboard should automatically go down/hide itself
            //For this
            InputMethodManager mgr=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
        }

    }


    public class DownloadTask extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            Log.i("JSON", s);
            message="";

            try {
                JSONObject jsonObject=new JSONObject(s);

                String coordInfo="[";
                coordInfo+=jsonObject.getString("coord");
                coordInfo+="]";

                String weatherInfo=jsonObject.getString("weather");

                String mainInfo="[";
                mainInfo+=jsonObject.getString("main");
                mainInfo+="]";

                String windInfo="[";
                windInfo+=jsonObject.getString("wind");
                windInfo+="]";

                String cloudInfo="[";
                cloudInfo+=jsonObject.getString("clouds");
                cloudInfo+="]";


                JSONArray coordArray=new JSONArray(coordInfo);
                JSONArray weatherArray=new JSONArray(weatherInfo);
                JSONArray mainArray=new JSONArray(mainInfo);
                JSONArray windArray=new JSONArray(windInfo);
                JSONArray cloudArray=new JSONArray(cloudInfo);


                for(int i=0;i<coordArray.length();i++)
                {
                    JSONObject jsonPart=coordArray.getJSONObject(i);

                    Double lat=jsonPart.getDouble("lat");
                    Double lon=jsonPart.getDouble("lon");

                    //Thread.sleep(1000);

                    JsonTask jsonTask=new JsonTask();
                    String elevationURL="https://elevation-api.io/api/elevation?points=("+lat+","+lon+")&key=shgcY1fRb3JbYV8u6-6oVyeJrTarV7";
                    jsonTask.execute(elevationURL);
                    //Double elevation=jsonTask.elevation;

                    String lat1=Double.toString(Math.abs(lat));
                    String lon1=Double.toString(Math.abs(lon));
                   // String elevationString=Double.toString(elevation);

                    Log.i("lat1", lat1);
                    Log.i("lon1", lon1);
                    //Log.i("elevationString", elevationString);

                    if(!lat1.equals("") && !lon1.equals("") && lat>=0 && lon>=0)
                    {
                        message+="City: "+city+"\n"+"Latitude: "+lat1+" North\n"+"Longitude: "+lon1+" East\n";
                    }
                    if(!lat1.equals("") && !lon1.equals("") && lat>=0 && lon<0)
                    {
                        message+="City: "+city+"\n"+"Latitude: "+lat1+" North\n"+"Longitude: "+lon1+" West\n";
                    }
                    if(!lat1.equals("") && !lon1.equals("") && lat<0 && lon<0)
                    {
                        message+="City: "+city+"\n"+"Latitude: "+lat1+" South\n"+"Longitude: "+lon1+" West\n";
                    }
                    if(!lat1.equals("") && !lon1.equals("") && lat<0 && lon>=0)
                    {
                        message+="City: "+city+"\n"+"Latitude: "+lat1+" South\n"+"Longitude: "+lon1+" East\n";
                    }
                }

                for(int i=0;i<weatherArray.length();i++)
                {
                    JSONObject jsonPart=weatherArray.getJSONObject(i);

                    String main=jsonPart.getString("main");
                    String description=jsonPart.getString("description");

                    if(!main.equals("") && !description.equals(""))
                    {
                        message+="Brief Report: "+main+"\n"+"Detailed Report: "+description+"\n";
                    }
                }

                for(int i=0;i<mainArray.length();i++) {
                    JSONObject jsonPart = mainArray.getJSONObject(i);

                    String temp = jsonPart.getString("temp");
                    String feels_like = jsonPart.getString("feels_like");
                    String temp_min = jsonPart.getString("temp_min");
                    String temp_max = jsonPart.getString("temp_max");
                    String pressure = jsonPart.getString("pressure");
                    String humidity = jsonPart.getString("humidity");


                    if (!temp.equals("") && !feels_like.equals("") && !temp_min.equals("") && !temp_max.equals("") && !pressure.equals("") && !humidity.equals("")) {
                        message += "Temperature: " + temp + " celsius\n" + "Feels Like: " + feels_like + " celsius\n" + "Minimum Temperature: " + temp_min + " celsius\n" + "Maximum Temperature: " + temp_max + " celsius\n" + "Pressure :" + pressure + " millibars\n" + "Humidity: " + humidity + "%\n";
                    }
                }


                for(int i=0;i<windArray.length();i++)
                {
                    JSONObject jsonPart=windArray.getJSONObject(i);

                    String windspeed=jsonPart.getString("speed");
                    ///String winddeg=jsonPart.getString("deg");


                    if(!windspeed.equals("") /*&& !winddeg.equals("")*/)
                    {
                        message+="Wind Speed: "+windspeed+" kmph\n"/*+"Wind Degree: "+winddeg+"\n"*/;
                    }
                }

                for(int i=0;i<cloudArray.length();i++)
                {
                    JSONObject jsonPart=cloudArray.getJSONObject(i);


                    String clouds=jsonPart.getString("all");


                    if(!clouds.equals(""))
                    {
                        message+="Cloud Cover: "+clouds+"%\n";
                    }
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }


    public class JsonTask extends AsyncTask<String,String,String>
    {
        double elevation=0;

        @Override
        protected String doInBackground(String... urls)
        {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
                return null;
            }
        }


        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            Log.i("Elevation", s);

            try {
                JSONObject jsonObject=new JSONObject(s);
                String elevationInfo=jsonObject.getString("elevations");
                JSONArray elevationArray=new JSONArray(elevationInfo);

                for(int i=0;i<elevationArray.length();i++)
                {
                    JSONObject jsonPart=elevationArray.getJSONObject(i);
                    String elevationString=jsonPart.getString("elevation");
                    elevation=Double.parseDouble(elevationString);
                }

                Log.i("Elevation: ", String.valueOf(elevation));
                message+="Elevation: "+elevation+" mtrs above sea level\n";

                if(!message.equals(""))
                {
                    resultTextView.setText(message);
                }
                else
                {
                    //resultTextView.setText("");
                    Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Could not find weather for this city", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }


}


/*
for(int i=0;i<coordArray.length();i++)
                {
                    JSONObject jsonPart=coordArray.getJSONObject(i);

                    //String name=jsonPart.getString("name");
                    String lat=jsonPart.getString("lat");
                    //Log.i("lat", jsonPart.getString("lat"));
                    String lon=jsonPart.getString("lon");
                    //Log.i("lon", jsonPart.getString("lon"));
                    String main=jsonPart.getString("main");
                    String description=jsonPart.getString("description");
                    String temp=jsonPart.getString("temp");
                    String feels_like=jsonPart.getString("feels_like");
                    String temp_min=jsonPart.getString("temp_min");
                    String temp_max=jsonPart.getString("temp_max");
                    String pressure=jsonPart.getString("pressure");
                    String humidity=jsonPart.getString("humidity");
                    String visibility=jsonPart.getString("visibility");
                    String windspeed=jsonPart.getString("speed");
                    String winddeg=jsonPart.getString("deg");
                    String clouds=jsonPart.getString("clouds");


                    if(!lat.equals("") && !lon.equals("") && !main.equals("") && !description.equals("") && !temp.equals("") && !feels_like.equals("") && !temp_min.equals("") && !temp_max.equals("") && !pressure.equals("") && !humidity.equals("") && !visibility.equals("") && !windspeed.equals("") && !winddeg.equals("") && !clouds.equals(""))
                    {
                        message+="City: "+city+"\n"+"Latitude: "+lat+"\n"+"Longitude: "+lon+"\n"+"Brief Report: "+main+"\n"+"Detailed Report: "+description+"\n"+"Temperature: "+temp+"\n"+"Feels Like: "+feels_like+"\n"+"Minimum Temperature: "+temp_min+"\n"+"Maximum Temperature: "+temp_max+"\n"+"Pressure :"+pressure+"\n"+"Humidity: "+humidity+"\n"+"Visibility: "+visibility+"\n"+"Wind Speed: "+windspeed+"\n"+"Wind Degree: "+winddeg+"\n"+"Cloud Cover: "+clouds;
                    }

                    //Log.i("lon", jsonPart.getString("lon"));
                    //Log.i("lat", jsonPart.getString("lat"));
                    //Log.i("description", jsonPart.getString("description"));
                    //Log.i("icon", jsonPart.getString("icon"));
                }
 */
