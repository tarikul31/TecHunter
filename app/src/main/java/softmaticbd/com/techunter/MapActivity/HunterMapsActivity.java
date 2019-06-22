package softmaticbd.com.techunter.MapActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import softmaticbd.com.techunter.LoginActivity.SignInActivity;
import softmaticbd.com.techunter.Model.Hunter;
import softmaticbd.com.techunter.Model.ServiceExpert;
import softmaticbd.com.techunter.Operation.FeedbackActivity;
import softmaticbd.com.techunter.Profile.HunterProfile;
import softmaticbd.com.techunter.R;

public class HunterMapsActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ProgressDialog pDialog;

    // todo for map integration
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest myLocationRequest;
    private Location myLocation;
    private Marker hnMarker, providerMarker;
    private SupportMapFragment mapFragment;

    //TODO GeoFire
    private GeoFire geoFire;
    private GeoQuery geoQuery;

    // todo App dependency
    private String expertId;
    private static final int CALL = 1;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorBrown, R.color.colorPrimary, R.color.colorOrange, R.color.colorDeepAsh};

    // todo for firebase db
    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private DatabaseReference findRef;
    private DatabaseReference findServiceRef,deliveryRef;
    private DatabaseReference serviceRef;
    private Task<Void> assignExpRef;
    private String userId;
    private FirebaseUser user;

    // todo for navLoad
    private TextView hName, hEmail;
    private ImageView hImage;
    private View view;

    //todo utils
    private String TAG = "Response";
    private double radius = 3;
    private final int ACCESS_FINE_LOCATION = 1;
    private boolean hunterRequest = false;
    private boolean findExpert = false;
    private List<String> keys = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private Hunter hunter;

    //todo for selecting option
    private RadioGroup rgOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnFindExpert, btnSubmitOp;
    private LinearLayout exLayout;

    // todo for operation
    private LinearLayout serviceLayout;
    private TextView tvService, tvServicePrice;
    private Button btnConService,btnCancelService;
    private ServiceExpert serviceExpert;
    private Boolean isServiceConfirm = false;
    private EditText svLocation;
    private String proKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunter_maps);
        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);


        drawer = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        view = navigationView.getHeaderView(0);
        //todo for nav load
        hName = view.findViewById(R.id.navNameId);
        hEmail = view.findViewById(R.id.navEmailId);
        hImage = view.findViewById(R.id.navProfileImageId);

        //todo progress Bar
        pDialog = new ProgressDialog(HunterMapsActivity.this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data.....");
        pDialog.setCancelable(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            checkUsePermission();
        }

//        //todo for selecting option
//        rgOptions = findViewById(R.id.rgOption);
//        rbOption1 = findViewById(R.id.rbOption1);
//        rbOption1.setOnClickListener(this);
//        rbOption2 = findViewById(R.id.rbOption2);
//        rbOption2.setOnClickListener(this);
//        rbOption3 = findViewById(R.id.rbOption3);
//        rbOption3.setOnClickListener(this);
//        rbOption4 = findViewById(R.id.rbOption4);
//        rbOption4.setOnClickListener(this);

        btnSubmitOp = findViewById(R.id.btnSubmitOption);
        btnSubmitOp.setOnClickListener(this);
        btnFindExpert = findViewById(R.id.btnFindExpert);
        btnFindExpert.setOnClickListener(this);
        exLayout = findViewById(R.id.expertLayout);
        // todo for request operation
        serviceLayout = findViewById(R.id.serviceLayout);
        tvService = findViewById(R.id.tvService);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        btnConService = findViewById(R.id.btnConService);
        btnConService.setOnClickListener(this);
        svLocation = findViewById(R.id.serviceLocation);
        btnCancelService = findViewById(R.id.btnCancelService);
        btnCancelService.setOnClickListener(this);

        // todo firebase db
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        user = auth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference()
                .child("TecHunter")
                .child("Hunter")
                .child("Profile")
                .child(userId);
        initMap();
        onNavBarLoad();
    }

    private void initMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHunt);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HunterMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
        } else {
            mapFragment.getMapAsync(this);
        }
    }

    public boolean checkUsePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HunterMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
        }
        builtGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
    }

    private synchronized void builtGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        if (hnMarker != null) {
            hnMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13), 2000, null);
        hnMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng).title("Tech Hunter")
        );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(1000);
        myLocationRequest.setFastestInterval(1000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HunterMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, myLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HunterMapsActivity.this);
                    builder.setTitle("Error ...!!!");
                    builder.setMessage("Permission invalid");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
            break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.navHnProfile: {
                startActivity(new Intent(getApplicationContext(), HunterProfile.class));
            }
            break;
//            case R.id.navHnReview: {
//                startActivity(new Intent(HunterMapsActivity.this, FeedbackActivity.class));
//            }
//            break;
            case R.id.navHnFaq: {
                Toast.makeText(getApplicationContext(), "FAQ data is Shown ", Toast.LENGTH_SHORT).show();
            }
            break;
            case R.id.navHnLogout: {
                Toast.makeText(getApplicationContext(), "Logout button is Clicked ", Toast.LENGTH_SHORT).show();
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }
            break;
        }
        return false;
    }

    private void onNavBarLoad() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    hunter = dataSnapshot.getValue(Hunter.class);
                    hName.setText(hunter.getName());
                    hEmail.setText(hunter.getEmail());
                    Glide.with(HunterMapsActivity.this).load(hunter.getProfileImage()).into(hImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllServiceProvider() {
        findServiceRef = FirebaseDatabase.getInstance().getReference()
                .child("Location")
                .child("ServiceExpert");
        geoFire = new GeoFire(findServiceRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                expertId = key;
                double latitude = location.latitude;
                double longitude = location.longitude;
                keys.add(expertId);
                addMarker(new LatLng(latitude, longitude));
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void addMarker(LatLng latLng) {
        providerMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .position(latLng).title("Service Expert")
        );
        markers.add(providerMarker);
        pDialog.dismiss();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int index = markers.indexOf(marker);
        if (!marker.equals(this.hnMarker)) {
            proKey = keys.get(index);
            pDialog.show();
            serviceRef = FirebaseDatabase.getInstance().getReference()
                    .child("Service")
                    .child(proKey);
            serviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            serviceExpert = snapshot.getValue(ServiceExpert.class);
                            serviceLayout.setVisibility(View.VISIBLE);
                            tvService.setText("Service Name : "+serviceExpert.getExpService());
                            tvServicePrice.setText("Service Cost : "+serviceExpert.getExpPrice());
                            pDialog.dismiss();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == btnFindExpert) {
            hunterRequest = true;
            pDialog.show();
            findRef = FirebaseDatabase.getInstance().getReference()
                    .child("FindRequest")
                    .child("Hunter");
            geoFire = new GeoFire(findRef);
            geoFire.setLocation(userId, new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    getAllServiceProvider();
                }
            });

//            exLayout.setVisibility(View.VISIBLE);
//            btnFindExpert.setVisibility(View.INVISIBLE);

        }
        if (v == btnSubmitOp) {
            exLayout.setVisibility(View.GONE);
        }
        if (v == btnConService) {
            serviceLayout.setVisibility(View.GONE);
            if (TextUtils.isEmpty(svLocation.getText().toString())){
                svLocation.requestFocus();
                svLocation.setError("REQUIRED");
                serviceLayout.setVisibility(View.VISIBLE);
                return;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Product Confirmation");
            alert.setMessage("Are you Sure to Order this Product ? ");
            alert.setCancelable(false);
            alert.setPositiveButton("Yes", (dialog, which) -> {
                isServiceConfirm = true;
                sendService(proKey);
                dialog.dismiss();
            });
            alert.setNegativeButton("No", (dialog, which) -> {
                serviceLayout.setVisibility(View.GONE);
                dialog.dismiss();
            });
            alert.show();
        }
        if (v == btnCancelService){
            serviceLayout.setVisibility(View.GONE);
        }
    }

    private void sendService(final String key){
        Map<String,Object> maps = new HashMap<>();
        maps.put("ServiceLocation", svLocation.getText().toString());
        serviceRef.child(serviceExpert.getExpId())
                .updateChildren(maps)
                .addOnSuccessListener(task -> {
                    serviceExpert.setServiceLocation(svLocation.getText().toString());
                    assignExpRef = FirebaseDatabase.getInstance().getReference()
                            .child("HunterRequest")
                            .child(key)
                            .child(userId)
                            .setValue(serviceExpert).addOnCompleteListener(task1 ->{
                                getDeliveryResult();
//                                Toast.makeText(getBaseContext(),"Success",Toast.LENGTH_SHORT).show();
                            });
                });

    }
    private void getDeliveryResult() {
        deliveryRef = FirebaseDatabase.getInstance().getReference()
                .child("Activity").child("Provider")
                .child(proKey).child(userId);
        deliveryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(HunterMapsActivity.this);
                    alert.setTitle("Response");
                    alert.setMessage("Your Service Delivery Has Been Successful... Stay with Us");
                    alert.setCancelable(false);
                    alert.setPositiveButton("Ok", (dialog, which) -> {
                        deliveryRef.removeValue();
                        startActivity(new Intent(HunterMapsActivity.this, FeedbackActivity.class).putExtra("proKey", proKey));

//                        deliveryRef.removeValue();
//                        removeFindRequest();
                        dialog.dismiss();
                    });
                    alert.show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeFindRequest() {
        findRef = FirebaseDatabase.getInstance().getReference().child("FindRequest").child("Hunter");
        geoFire = new GeoFire(findRef);
        geoFire.removeLocation(userId, (key, error) -> {
            if (providerMarker != null) {
                providerMarker.remove();
            }
        });

    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }

}
