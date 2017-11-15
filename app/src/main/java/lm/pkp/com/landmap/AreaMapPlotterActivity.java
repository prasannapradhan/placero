package lm.pkp.com.landmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.MapWrapperLayout;
import lm.pkp.com.landmap.custom.OnInfoWindowElemTouchListener;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.FileUtil;

public class AreaMapPlotterActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LinkedHashMap<Marker, PositionElement> areaMarkers = new LinkedHashMap<>();
    private Polygon polygon = null;
    private Marker centerMarker = null;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_plotter);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);


        UiSettings settings = googleMap.getUiSettings();
        settings.setMapToolbarEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(true);


        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(googleMap, getPixelsFromDp(getApplicationContext(), 39 + 20));
        this.infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.info_window, null);
        this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);
        this.infoButton = (Button) infoWindow.findViewById(R.id.button);
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.drawable.round_but_green_sel), //btn_default_normal_holo_light
                getResources().getDrawable(R.drawable.round_but_red_sel)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                PositionElement markedPosition = areaMarkers.get(marker);
                pdh.deletePosition(markedPosition);
                areaMarkers.remove(markedPosition);
                polygon.remove();
                plotPolygonUsingPositions();
            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                String markerTitle = marker.getTitle();
                infoTitle.setText(markerTitle);
                infoSnippet.setText(marker.getSnippet());
                if (markerTitle.indexOf("Center") != -1) {
                    infoButton.setVisibility(View.INVISIBLE);
                    return infoWindow;
                } else {
                    infoButton.setVisibility(View.VISIBLE);
                    infoButtonListener.setMarker(marker);
                }
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });
        plotPolygonUsingPositions();
    }

    private void plotPolygonUsingPositions() {
        final AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        List<PositionElement> positionElements = AreaContext.getInstance().getPositions();
        int noOfPositions = positionElements.size();

        Set<Marker> markerSet = areaMarkers.keySet();
        for (Marker m : markerSet) {
            m.remove();
        }
        areaMarkers = new LinkedHashMap<>();
        if (centerMarker != null) {
            centerMarker.remove();
        }

        PolygonOptions polyOptions = new PolygonOptions();
        polyOptions = polyOptions.strokeColor(Color.RED).fillColor(Color.DKGRAY);

        double latTotal = 0.0;
        double lonTotal = 0.0;
        for (int i = 0; i < noOfPositions; i++) {
            PositionElement pe = positionElements.get(i);
            Marker m = drawMarkerUsingPosition(pe);
            latTotal += pe.getLat();
            lonTotal += pe.getLon();
            areaMarkers.put(m, pe);
            polyOptions.add(m.getPosition());
        }
        polygon = googleMap.addPolygon(polyOptions);

        final double latAvg = latTotal / noOfPositions;
        final double lonAvg = lonTotal / noOfPositions;
        zoomCameraToPosition(latAvg, lonAvg);

        double polygonAreaSqMt = SphericalUtil.computeArea(polygon.getPoints());
        double polygonAreaSqFt = polygonAreaSqMt * 10.7639;

        final AreaElement ae = AreaContext.getInstance().getAreaElement();
        ae.setCenterLat(latAvg);
        ae.setCenterLon(lonAvg);
        ae.setMeasureSqFt(polygonAreaSqFt);

        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            adh.updateArea(ae);
            new Thread(new Runnable() {
                public void run() {
                    adh.updateAreaOnServer(ae);
                }
            }).start();
        } else {
            showWarningMessage("You do not have permissions to update the place specs. " +
                    "\nWhatever changes you do will be lost.");
        }

        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latAvg, lonAvg));
        centerMarker = googleMap.addMarker(markerOptions);
        centerMarker.setVisible(true);
        centerMarker.setAlpha(1);
        centerMarker.setTitle(ae.getName().concat(",").concat(ae.getDescription()));
        centerMarker.showInfoWindow();

        new TakeSnapshotOfPlotAsyncTask().execute();
    }

    public Marker drawMarkerUsingPosition(final PositionElement pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLon());
        Marker marker = googleMap.addMarker((new MarkerOptions().position(position)));
        marker.setTitle(pe.getUniqueId());
        marker.setDraggable(true);
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker marker) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                PositionElement newPositionElem = new PositionElement();
                String pid = UUID.randomUUID().toString();
                newPositionElem.setUniqueId(pid);
                newPositionElem.setUniqueAreaId(AreaContext.getInstance().getAreaElement().getUniqueId());
                newPositionElem.setName("P_" + pid);
                newPositionElem.setDescription("No Description");
                newPositionElem.setTags("");
                newPositionElem.setLat(marker.getPosition().latitude);
                newPositionElem.setLon(marker.getPosition().longitude);

                PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                pdh.insertPositionLocally(newPositionElem);
                pdh.insertPositionToServer(newPositionElem);
                AreaContext.getInstance().addPosition(newPositionElem);

                pdh.deletePosition(areaMarkers.get(marker));
                AreaContext.getInstance().removePosition(areaMarkers.get(marker));

                polygon.remove();
                plotPolygonUsingPositions();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }
        });
        return marker;
    }

    private void zoomCameraToPosition(double latAvg, double lonAvg) {
        LatLng position = new LatLng(latAvg, lonAvg);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 19f);
        googleMap.animateCamera(cameraUpdate);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 19f));
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        googleMap.clear();
        googleMap = null;

        Intent positionMarkerIntent = new Intent(AreaMapPlotterActivity.this, AreaDetailsActivity.class);
        startActivity(positionMarkerIntent);
    }


    private void showWarningMessage(String message) {
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private class TakeSnapshotOfPlotAsyncTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            takeSnap();
            return null;
        }
    }

    private void takeSnap() {
        View v = getWindow().getDecorView().getRootView();
        v.setDrawingCacheEnabled(true);
        Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        try {
            final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
            final File imageStorageDir = LocalFolderStructureManager.getImageStorageDir();
            final String dirPath = imageStorageDir.getAbsolutePath();

            String screenShotFolderPath = dirPath + areaElement.getUniqueId();
            final File screenshotFolder = new File(screenShotFolderPath);
            if(!screenshotFolder.exists()){
                screenshotFolder.mkdirs();
            }
            String screenShotFileName = "plot_" + areaElement.getUniqueId() + "_ss.png";
            String screenShotFilePath = screenShotFolderPath + File.separatorChar + screenShotFileName;
            File screenShotFile = new File(screenShotFilePath);
            if(!screenShotFile.exists()){
                screenShotFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(screenShotFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            DriveResource dr = new DriveResource();
            dr.setContainerDriveId(AreaContext.getInstance().getImagesRootDriveResource().getDriveId());
            dr.setContentType("Image");
            dr.setUserId(UserContext.getInstance().getUserElement().getEmail());
            dr.setMimeType(FileUtil.getMimeType(screenShotFile));
            dr.setAreaId(areaElement.getUniqueId());
            dr.setName(screenShotFileName);
            dr.setPath(screenShotFilePath);
            dr.setSize(screenShotFile.length() + "");
            dr.setType("file");
            dr.setUniqueId(UUID.randomUUID().toString());

            areaElement.getDriveResources().add(dr);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
