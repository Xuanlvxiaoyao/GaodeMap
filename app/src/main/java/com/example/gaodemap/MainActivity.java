package com.example.gaodemap;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

import static com.example.gaodemap.R.id.location;

public class MainActivity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener, GeocodeSearch.OnGeocodeSearchListener, AMap.InfoWindowAdapter {
    MapView mMapView = null;
    private AMap aMap;
    private ActionBar actionBar;
    private RouteSearch routeSearch;
    private UiSettings uiSettings;
    private Intent intent;
    private LatLng latLng;
    private LatLng myLatLng;
    private boolean flag=true;
    private ArrayList<String> list;
    private ListView lv;
    private AlertDialog dialog;
    private Marker mk;
    private GeocodeSearch geocodeSearch;
    private GeocodeSearch geocoderSearch;
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        v = LayoutInflater.from(this).inflate(R.layout.lv, null, false);
        lv= (ListView) v.findViewById(R.id.lv);
        actionBar = getActionBar();


        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();

        //设置Logo位置  缩放按钮显示
        uiSettings = aMap.getUiSettings();
        uiSettings.setLogoBottomMargin(-100);
        uiSettings.setZoomControlsEnabled(false);

        //定位
        orientation();

        //添加marker覆盖物
        MarkerOptions markerOptions = new MarkerOptions();
        latLng = new LatLng(39.908860, 116.397390);
        markerOptions.position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dog))
                .alpha(0.6f)
                 .title("marker");
        aMap.addMarker(markerOptions);

        //设置窗口信息
        aMap.setInfoWindowAdapter(this);


        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mk=marker;
                return false;
            }
        });

       aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
           @Override
           public void onMapClick(LatLng latLng) {

               mk.hideInfoWindow();
           }
       });

        //设置窗口监听，进行导航
        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
               initdl(flag);
                dialog.show();
                //构造 GeocodeSearch 对象，并设置监听。

            }
        });

//        // 绘制曲线
//        aMap.addPolyline((new PolylineOptions())
//                .add(new LatLng(39.5427, 116.2317), new LatLng(45.808, 126.55))
//                .geodesic(true).color(Color.RED));

        routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(this);

    }

    /*
    * 初始化Dialog，如果手机有高德、百度、腾讯地图app  Dialog上会显示
    * 点击Dialog上的条目，调用手机上的app地图进行导航
    * 如果手机上没有地图APP，调用高德自带的地图进行导航
    * */
    private void initdl(boolean b) {
        if(b){
           flag=false;
            boolean avilibleqq = isAvilible(MainActivity.this, "com.tencent.map");
            boolean aviliblenavi = isAvilible(MainActivity.this, "com.autonavi.minimap");
            boolean aviliblebd = isAvilible(MainActivity.this, "com.baidu.BaiduMap");
            list=new ArrayList<>();
            if(aviliblebd){
                list.add("百度地图");
            }
            if(avilibleqq){
                list.add("腾讯地图");
            }
            if(aviliblenavi){
                list.add("高德地图");
            }
            if(!aviliblebd&&avilibleqq&&aviliblenavi){
                /*
                *当手机上没有地图app，调用高德自带的地图进行导航
                *
                * 逆地理编码方法
                 */
                Inverse_geographic();
            }

            ArrayAdapter adapter=new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,list);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (list.get(position).equals("腾讯地图")) {
                        //腾讯地图
                        if ( latLng != null) {
                            // 腾讯地图
                            Intent naviIntent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("qqmap://map/routeplan?type=drive&from=" +"&fromcoord=" + "&to=" + null + "&tocoord=" + latLng.latitude + "," + latLng.longitude + "&policy=0&referer=appName"));
                            startActivity(naviIntent);
                        }
                    } else if (list.get(position).equals("百度地图")) {
                        if (latLng != null) {
                            // 百度地图
                            Intent naviIntent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("baidumap://map/geocoder?location=" + latLng.latitude + "," + latLng.longitude));
                            startActivity(naviIntent);
                        }
                    } else if (list.get(position).equals("高德地图")) {
                        if (latLng != null) {
                            // 高德地图
                            Intent naviIntent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat=" + latLng.latitude + "&dlon=" + latLng.longitude + "&dname=目的地&dev=0&t=2"));
                            startActivity(naviIntent);
                        }
                    }

                    dialog.dismiss();

                }

            });
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(v);
            dialog = builder.create();
        }

    }

    //逆地理编码方法
    public void Inverse_geographic(){
        geocodeSearch = new GeocodeSearch(MainActivity.this);
        geocodeSearch.setOnGeocodeSearchListener(this);
        //通过GeocodeQuery设置查询参数,调用getFromLocationNameAsyn(GeocodeQuery geocodeQuery) 方法发起请求。
        //address表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode都ok
        GeocodeQuery query = new GeocodeQuery("天安门", "010");
        geocodeSearch.getFromLocationNameAsyn(query);
        flag=true;
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //定位
    public void orientation(){
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(999999999); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                myLatLng = new LatLng(latitude, longitude);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case location:
                orientation();
                break;

            case R.id.in_door:
                aMap.showIndoorMap(true);
                break;
            case R.id.night:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;
            case R.id.weixing:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.navi:
                aMap.setMapType(AMap.MAP_TYPE_NAVI);
                break;
            case R.id.guihua:

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

      /* 检查手机上是否安装了指定的软件
     * @param context
     * @param packageName：应用包名
     * @return
             */
    public static boolean isAvilible(Context context, String packageName){
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if(packageInfos != null){
            for(int i = 0; i < packageInfos.size(); i++){
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    //地理编码回调方法
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {

    }

    //逆地理编码回调方法
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        LatLonPoint latLonPoint = result.getGeocodeAddressList().get(0).getLatLonPoint();
        double latitude = latLonPoint.getLatitude();
        double longitude = latLonPoint.getLongitude();
        LatLng lat=new LatLng(latitude,longitude);
        Intent intent=new Intent(this,NavActivity.class);
        Bundle bundle=new Bundle();
        bundle.putParcelable("start",myLatLng);
        bundle.putParcelable("end",lat);
        intent.putExtra("bundle",bundle);
        startActivity(intent);
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return LayoutInflater.from(MainActivity.this).inflate(R.layout.info_window, null, false);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
