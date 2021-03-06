package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.area.db.AreaDatabaseHandler;
import com.pearnode.app.placero.area.model.Address;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.permission.Permission;
import com.pearnode.app.placero.permission.PermissionDatabaseHandler;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionDatabaseHandler;
import com.pearnode.app.placero.tags.TagDatabaseHandler;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.User;
import com.pearnode.constants.APIRegistry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class UserAreaDetailsLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext;
    private AreaDatabaseHandler adh;
    private PositionDatabaseHandler pdh;
    private PermissionDatabaseHandler pmh;
    private TagDatabaseHandler tdh;
    private MediaDataBaseHandler pmdh;

    private AsyncTaskCallback callback;

    public UserAreaDetailsLoadTask(Context appContext) {
        localContext = appContext;
        adh = new AreaDatabaseHandler(localContext);
        pdh = new PositionDatabaseHandler(localContext);
        pmh = new PermissionDatabaseHandler(localContext, null);
        tdh = new TagDatabaseHandler(localContext, null);
        pmdh = new MediaDataBaseHandler(localContext);
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = APIRegistry.USER_AREA_SEARCH + "?us=";
            JSONObject postDataParam = postDataParams[0];
            String searchKey = postDataParam.getString("us");
            String callUrl = urlString + URLEncoder.encode(searchKey, "utf-8");
            URL url = new URL(callUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            if(s == null){
                finalizeTaskCompletion();
                return;
            }
            JSONObject responseObj = new JSONObject(s);
            JSONArray dataArr = responseObj.getJSONArray("data");
            for (int i = 0; i < dataArr.length(); i++) {
                JSONObject dataObj = (JSONObject) dataArr.get(i);
                JSONObject detailObj = dataObj.getJSONObject("detail");

                Area ae = new Area();
                ae.setId(detailObj.getString("id"));
                ae.setName(detailObj.getString("name"));
                ae.setCreatedBy(detailObj.getString("createdBy"));
                ae.setDescription(detailObj.getString("description"));
                ae.getCenterPosition().setLat(detailObj.getDouble("center_lat"));
                ae.getCenterPosition().setLng(detailObj.getDouble("center_lon"));

                double msqFt = detailObj.getDouble("msqft");
                AreaMeasure measure = new AreaMeasure(msqFt);
                ae.setMeasure(measure);

                String addressText = detailObj.getString("address");
                Address address = Address.fromStoredAddress(addressText);
                if (address != null) {
                    ae.setAddress(address);
                    tdh.addTags(address.getTags(), "area", ae.getId());
                }
                ae.setType(detailObj.getString("type"));
                ae.setDirty(0);
                ae.setDirtyAction("none");
                adh.insertArea(ae);

                JSONObject permissionObj = dataObj.getJSONObject("permission");
                Permission permission = new Permission();
                permission.setUserId(permissionObj.getString("source_user"));
                permission.setAreaId(permissionObj.getString("area_id"));
                permission.setFunctionCode(permissionObj.getString("function_codes"));
                pmh.addPermission(permission);

                JSONArray positions = dataObj.getJSONArray("positions");
                for (int p = 0; p < positions.length(); p++) {
                    JSONObject positionObj = (JSONObject) positions.get(p);
                    Position position = new Position();
                    position.setId(positionObj.getString("id"));
                    position.setAreaRef(positionObj.getString("area_ref"));
                    position.setName(positionObj.getString("name"));
                    position.setDescription(positionObj.getString("description"));
                    position.setLat(positionObj.getDouble("lat"));
                    position.setLng(positionObj.getDouble("lng"));
                    position.setTags(positionObj.getString("tags"));
                    position.setType(positionObj.getString("type"));
                    position.setCreatedOn(positionObj.getString("created_on"));
                    pdh.addPostion(position);
                }

                JSONArray mediaElements = dataObj.getJSONArray("medias");
                for (int d = 0; d < mediaElements.length(); d++) {
                    JSONObject mediaObj = (JSONObject) mediaElements.get(d);
                    Media media = new Media();
                    media.setId(mediaObj.getString("id"));
                    media.setPlaceRef(mediaObj.getString("place_ref"));
                    media.setName(mediaObj.getString("name"));
                    media.setType(mediaObj.getString("type"));
                    media.setTfName(mediaObj.getString("tf_name"));
                    media.setTfPath(mediaObj.getString("tf_path"));
                    media.setRfName(mediaObj.getString("rf_name"));
                    media.setRfPath(mediaObj.getString("rf_path"));
                    media.setLat(mediaObj.getString("lat"));
                    media.setLng(mediaObj.getString("lng"));
                    media.setCreatedOn(System.currentTimeMillis());
                    pmdh.addMedia(media);
                }
            }

            User user = UserContext.getInstance().getUser();
            tdh.addTags(user.getSelections().getTags(), "user", user.getEmail());

        } catch (Exception e) {
            e.printStackTrace();
        }
        finalizeTaskCompletion();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        if (callback != null) {
            callback.taskCompleted("");
        }
    }
}
