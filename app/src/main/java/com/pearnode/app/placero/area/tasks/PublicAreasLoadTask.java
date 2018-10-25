package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import com.pearnode.app.placero.area.model.Address;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.permission.PermissionElement;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.tags.TagsDBHelper;
import com.pearnode.constants.APIRegistry;

/**
 * Created by Rinky on 21-10-2017.
 */

public class PublicAreasLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext;
    private AreaDBHelper adh;
    private PositionsDBHelper pdh;
    private PermissionsDBHelper pmh;
    private TagsDBHelper tdh;
    private MediaDataBaseHandler pmdh;

    private AsyncTaskCallback callback;

    public PublicAreasLoadTask(Context appContext) {
        localContext = appContext;
        adh = new AreaDBHelper(localContext);
        pdh = new PositionsDBHelper(localContext);
        pmh = new PermissionsDBHelper(localContext, null);
        tdh = new TagsDBHelper(localContext, null);
        pmdh = new MediaDataBaseHandler(localContext);
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = APIRegistry.PUBLIC_AREAS_SEARCH;
            URL url = null;
            if (postDataParams.length > 0) {
                JSONObject postDataParam = postDataParams[0];
                String searchKey = postDataParam.getString("sk");
                url = new URL(urlString + "?sk=" + URLEncoder.encode(searchKey, "utf-8"));
            } else {
                url = new URL(urlString);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(
                        conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            } else {
                return new String("false : " + responseCode);
            }
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONArray responseArr = new JSONArray(s);
            for (int i = 0; i < responseArr.length(); i++) {
                JSONObject responseObj = (JSONObject) responseArr.get(i);
                JSONObject areaObj = (JSONObject) responseObj.get("area");

                Area area = new Area();
                area.setName(areaObj.getString("name"));
                area.setCreatedBy(areaObj.getString("created_by"));
                area.setDescription(areaObj.getString("description"));
                area.getCenterPosition().setLat(areaObj.getDouble("center_lat"));
                area.getCenterPosition().setLng(areaObj.getDouble("center_lon"));
                area.setId(areaObj.getString("id"));
                area.setDirty(0);
                area.setDirtyAction("none");
                area.setType(areaObj.getString("type"));

                AreaMeasure measure = new AreaMeasure(areaObj.getDouble("measure_sqft"));
                area.setMeasure(measure);
                adh.insertArea(area);

                String addressText = areaObj.getString("address");
                Address address = Address.fromStoredAddress(addressText);
                if(address != null){
                    area.setAddress(address);
                    tdh.insertTagsLocally(address.getTags(), "area", area.getId());
                }

                JSONArray positionsArr = (JSONArray) responseObj.get("positions");
                for (int p = 0; p < positionsArr.length(); p++) {
                    JSONObject positionObj = (JSONObject) positionsArr.get(p);
                    Position pe = new Position();
                    pe.setId((String) positionObj.get("unique_id"));
                    pe.setAreaRef((String) positionObj.get("area_ref"));
                    pe.setName((String) positionObj.get("name"));
                    pe.setDescription((String) positionObj.get("description"));
                    pe.setLat(positionObj.getDouble("lat"));
                    pe.setLng(positionObj.getDouble("lon"));
                    pe.setTags((String) positionObj.get("tags"));
                    pe.setCreatedOnMillis(positionObj.getString("created_on"));
                    pdh.insertPositionFromServer(pe);
                }

                JSONArray mediaElements = responseObj.getJSONArray("resources");
                for (int d = 0; d < mediaElements.length(); d++) {
                    JSONObject mediaObj = (JSONObject) mediaElements.get(d);
                    Media media = new Media();
                    media.setPlaceRef(mediaObj.getString("place_ref"));
                    media.setName(mediaObj.getString("name"));
                    media.setType(mediaObj.getString("type"));
                    media.setTfName(mediaObj.getString("tf_name"));
                    media.setTfPath(mediaObj.getString("tf_path"));
                    media.setRfName(mediaObj.getString("rf_name"));
                    media.setRfPath(mediaObj.getString("rf_path"));
                    media.setCreatedOn(System.currentTimeMillis());
                    pmdh.addMedia(media);
                }

                JSONArray permissionsArr = (JSONArray) responseObj.get("permissions");
                for (int e = 0; e < permissionsArr.length(); e++) {
                    JSONObject permissionObj = (JSONObject) permissionsArr.get(e);
                    PermissionElement pe = new PermissionElement();
                    pe.setUserId(permissionObj.getString("user_id"));
                    pe.setAreaId(permissionObj.getString("area_id"));
                    pe.setFunctionCode(permissionObj.getString("function_code"));
                    pmh.insertPermissionLocally(pe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finalizeTaskCompletion();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }
}
