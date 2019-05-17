package com.android.autocompletetextviewdynamic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class AutocompleteAdapter extends ArrayAdapter implements Filterable {
    private ArrayList mCountry;
    private String COUNTRY_URL =
            "http://stars.g2evolution.com/stage/api/rest/admin/mobile/visit/organization_populate?search_param=";

    JSONParser jsonParser = new JSONParser();

    public AutocompleteAdapter(Context context, int resource) {
        super(context, resource);
        mCountry = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mCountry.size();
    }

    @Override
    public Country getItem(int position) {
        return (Country) mCountry.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    try{
                        //get data from the web
                        String term = constraint.toString();
                        mCountry = (ArrayList) new DownloadCountry().execute(term).get();
                    }catch (Exception e){
                        Log.e("testing","EXCEPTION "+e);
                    }
                    filterResults.values = mCountry;
                    filterResults.count = mCountry.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };

        return myFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.auto_complete_layout,parent,false);

        //get Country
        Country contry = (Country) mCountry.get(position);

        TextView countryName = (TextView) view.findViewById(R.id.countryName);

        countryName.setText(contry.getName());

        return view;
    }

    //download mCountry list
    private class DownloadCountry extends AsyncTask<String, String, ArrayList>{
        String status;
        String strresponse;
        String strdata;
        String strcode, strtype, strmessage;
        @Override
        protected ArrayList doInBackground(String... params) {

          /*  List<NameValuePair> userpramas = new ArrayList<NameValuePair>();


            userpramas.add(new BasicNameValuePair("search_param", URLEncoder.encode(params[0])));
            Log.e("testing", "userpramas " + userpramas);

            JSONObject json = jsonParser.makeHttpRequest("http://stars.g2evolution.com/stage/api/rest/admin/mobile/visit/organization_populate", "GET", userpramas);
            Log.e("testing", "json " + json);
*/
            try {
                //Create a new COUNTRY SEARCH url Ex "search.php?term=india"
                String NEW_URL = COUNTRY_URL + URLEncoder.encode(params[0],"UTF-8");
                Log.e("testing", "JSON RESPONSE URL " + NEW_URL);

                Log.e("testing", "userpramas " + URLEncoder.encode(params[0]));

                URL url = new URL(NEW_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }

                //parse JSON and store it in the list
                String jsonString =  sb.toString();
                ArrayList countryList = new ArrayList<>();

                Log.e("testing","jsonString = "+jsonString);
                JSONObject  json = new JSONObject(jsonString);
                status = json.getString("status");
                strresponse = json.getString("response");
                JSONObject  jsonobject = new JSONObject(strresponse);
                strcode = jsonobject.getString("code");
                strtype = jsonobject.getString("type");
                strmessage = jsonobject.getString("message");
                if (status.equals("success")) {
                    status = json.getString("status");
                    strresponse = json.getString("response");
                    strdata = json.getString("data");


                    JSONArray jsonArray = new JSONArray(strdata);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        //store the country name
                        Country country = new Country();
                        country.setName(jo.getString("name"));
                        country.setLatitude(jo.getString("latitude"));
                        country.setLongitude(jo.getString("longitude"));
                        country.setLocation(jo.getString("location"));
                        countryList.add(country);
                    }


                } else {
                }

                //return the countryList
                return countryList;

            } catch (Exception e) {
                Log.e("testing", "EXCEPTION " + e);
                return null;
            }
        }
    }
}