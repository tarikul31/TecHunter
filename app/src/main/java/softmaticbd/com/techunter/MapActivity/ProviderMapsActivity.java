package softmaticbd.com.techunter.MapActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import softmaticbd.com.techunter.LoginActivity.SignInActivity;
import softmaticbd.com.techunter.Model.Hunter;
import softmaticbd.com.techunter.Model.HunterReview;
import softmaticbd.com.techunter.Model.Provider;
import softmaticbd.com.techunter.Model.ServiceExpert;
import softmaticbd.com.techunter.Operation.ExpertiseActivity;
import softmaticbd.com.techunter.Profile.HunterProfile;
import softmaticbd.com.techunter.Profile.ProviderProfile;
import softmaticbd.com.techunter.R;

public class ProviderMapsActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;

    // todo for map integration
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest myLocationRequest;
    private Location myLocation;
    private Marker marker, providerMarker;

    // todo App dependency
    private SupportMapFragment mapFragment;
    private static final int CALL = 1;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorBrown, R.color.colorPrimary, R.color.colorOrange, R.color.colorDeepAsh};

    // todo for firebase db
    private FirebaseAuth auth;
    private DatabaseReference reference, hunterRef, hunProfileRef;
    private String userId;
    private FirebaseUser user;
    private DatabaseReference hunReqRef, hReqReff;

    // todo for navLoad
    private TextView prName, prEmail;
    private ImageView prImage;
    private View view;

    //todo phone call
    private LinearLayout showUserLayout, showServiceLayout;
    private ImageView ivPhoneCall;
    private Button btnShowUser;
    private static final int REQUEST_CALL = 2;

    // todo for assign customer
    private Hunter hunter;
    private ServiceExpert serviceExpert;
    private LinearLayout hnProfileLayout, requestLayout, operationLayout;
    private TextView hName, hPhone, hService, hServiceCost, hDeliveryAdd;
    private String hKey;
    private Button btnAccept, btnCancel, btnDeliveryReq;

    //todo for Utils
    private Provider provider;

    // todo review
    private HunterReview hunterReview;
    private TextView tvReview;
    private RatingBar ratingBar;
    private LinearLayout reviewLayout;
    private DatabaseReference reviewRef;
    private Button btnReviewClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_maps);
        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);


        drawer = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navViewPro);
        navigationView.setNavigationItemSelectedListener(this);
        view = navigationView.getHeaderView(0);
        //todo for nav load
        prName = view.findViewById(R.id.proNameID);
        prEmail = view.findViewById(R.id.proEmailID);
        prImage = view.findViewById(R.id.proImageID);

        // todo for assign Customer
        hnProfileLayout = findViewById(R.id.hnProfileLayoutID);
        requestLayout = findViewById(R.id.requestLayoutID);

        hName = findViewById(R.id.rHnNameID);
        hPhone = findViewById(R.id.rHnPhoneID);
        hService = findViewById(R.id.rHnServiceName);
        hServiceCost = findViewById(R.id.rHnServicePrice);
        hDeliveryAdd = findViewById(R.id.rHnDeliveryAddress);

        btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(this);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        btnDeliveryReq = findViewById(R.id.btnDeliveryReq);
        btnDeliveryReq.setOnClickListener(this);
        //todo phone call
        showUserLayout = findViewById(R.id.showUserLayout);
        showServiceLayout = findViewById(R.id.showServiceLayout);
        btnShowUser = findViewById(R.id.btnShowUser);
        btnShowUser.setOnClickListener(this);
        ivPhoneCall = findViewById(R.id.hnPhoneCallID);
        ivPhoneCall.setOnClickListener(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            checkUsePermission();
        }

        //todo for review
        reviewLayout = findViewById(R.id.reviewLayout);
        tvReview = findViewById(R.id.tvCusReviewId);
        ratingBar = findViewById(R.id.ratingBarId);
        btnReviewClose = findViewById(R.id.btnCloseRevId);
        btnReviewClose.setOnClickListener(this);

        // todo firebase db
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference()
                .child("TecHunter")
                .child("Provider")
                .child("Profile")
                .child(userId);
        initMap();
        onNavBarLoad();
        getAssignCustomer();

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

    private void initMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPro);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProviderMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
        } else {
            mapFragment.getMapAsync(this);
        }

    }

    private void getAssignCustomer() {
        hunterRef = FirebaseDatabase.getInstance().getReference().child("HunterRequest").child(userId);
        hunterRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    hKey = dataSnapshot.getKey();
                    serviceExpert = dataSnapshot.getValue(ServiceExpert.class);
                    requestLayout.setVisibility(View.VISIBLE);
//                    Toast.makeText(ProviderMapsActivity.this, ""+dataSnapshot.getValue(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProviderMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
        }
        builtGoogleApiClient();
        mMap.setMyLocationEnabled(true);
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
        myLocation = location;
        if (marker != null) {
            marker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13), 2000, null);
        marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(latLng).title("Service Provider")
        );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(1000);
        myLocationRequest.setFastestInterval(1000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProviderMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProviderMapsActivity.this);
                    builder.setTitle("Error ...!!!");
                    builder.setMessage("Permission invalid");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                    });
                    builder.show();
                }
            }
            break;
            case REQUEST_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall();
                } else {
                    Toast.makeText(ProviderMapsActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.navProProfile: {
                startActivity(new Intent(getApplicationContext(), ProviderProfile.class));
            }
            break;
            case R.id.navProExpert: {
                startActivity(new Intent(ProviderMapsActivity.this, ExpertiseActivity.class));
            }
            break;
            case R.id.navProFAQ: {
                Toast.makeText(getApplicationContext(), "FAQ data is Shown ", Toast.LENGTH_SHORT).show();
            }
            break;
            case R.id.navProLogout: {
                Toast.makeText(getApplicationContext(), "Logout button is Clicked ", Toast.LENGTH_SHORT).show();
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }
            break;
        }
        return false;
    }

    private void onNavBarLoad() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    provider = dataSnapshot.getValue(Provider.class);
                    prName.setText(provider.getName());
                    prEmail.setText(provider.getEmail());
                    Glide.with(ProviderMapsActivity.this).load(provider.getProfileImage()).into(prImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnAccept) {
            hnProfileLayout.setVisibility(View.VISIBLE);
            requestLayout.setVisibility(View.GONE);
            acceptRequest(hKey);
        }
        if (v == btnCancel) {
            Toast.makeText(getApplicationContext(), "Clear All data", Toast.LENGTH_SHORT).show();
            cancelRequest();
        }
        if (v == btnShowUser) {
            showServiceLayout.setVisibility(View.GONE);
            showUserLayout.setVisibility(View.VISIBLE);
        }
        if (v == ivPhoneCall) {
            makePhoneCall();
        }
        if (v == btnDeliveryReq) {
            hnProfileLayout.setVisibility(View.GONE);
            FirebaseDatabase.getInstance().getReference()
                    .child("Activity").child("Provider").child(userId)
                    .child(hKey).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(ProviderMapsActivity.this);
                        alert.setTitle("Confirmation");
                        alert.setMessage("Thank you for you Providing Service.. Stay with Us");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Ok", (dialog, which) -> {
                            getCustomerReview();
                            dialog.dismiss();
                        });
                        alert.show();
                    });
        }
        if (v == btnReviewClose){
            reviewRef = FirebaseDatabase.getInstance().getReference().child("Review");
            reviewRef.removeValue();
            reviewLayout.setVisibility(View.GONE);
            removeHunter();

        }

    }

    private void acceptRequest(String key) {
        hunReqRef = FirebaseDatabase.getInstance().getReference().
                child("TecHunter")
                .child("Hunter")
                .child("Profile")
                .child(key);
        hunReqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    hunter = dataSnapshot.getValue(Hunter.class);
                    hName.setText("Customer Name : " + hunter.getName());
                    hPhone.setText(hunter.getPhone());
                    hService.setText("Service Name : " + serviceExpert.getExpService());
                    hServiceCost.setText("Service Cost : " + serviceExpert.getExpPrice());
                    hDeliveryAdd.setText("Service Location : " + serviceExpert.getServiceLocation());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCustomerReview() {
        reviewRef = FirebaseDatabase.getInstance().getReference()
                .child("Review");
        reviewRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    reviewLayout.setVisibility(View.VISIBLE);
                    hunterReview = dataSnapshot.getValue(HunterReview.class);
                    tvReview.setText("Hunter Review : "+hunterReview.getReview());
                    ratingBar.setRating(Float.valueOf(hunterReview.getUserRating()));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makePhoneCall() {
        String number = hPhone.getText().toString();
        if (number.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ProviderMapsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(ProviderMapsActivity.this, "Phone Number Empty ", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeHunter() {
        hKey = null;
        hName.setText("");
        hPhone.setText("");
        hService.setText("");
        hServiceCost.setText("");
        hDeliveryAdd.setText("");
        showServiceLayout.setVisibility(View.VISIBLE);
        showUserLayout.setVisibility(View.GONE);
        cancelRequest();
    }

    private void cancelRequest() {
        requestLayout.setVisibility(View.GONE);
        hReqReff = FirebaseDatabase.getInstance().getReference().child("HunterRequest").child(userId);
        hReqReff.removeValue();
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