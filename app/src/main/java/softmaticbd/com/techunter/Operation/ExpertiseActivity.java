package softmaticbd.com.techunter.Operation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import softmaticbd.com.techunter.LoginActivity.SignInActivity;
import softmaticbd.com.techunter.MapActivity.ProviderMapsActivity;
import softmaticbd.com.techunter.Model.ServiceExpert;
import softmaticbd.com.techunter.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ExpertiseActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private TextView tvExpert, tvExpPrice;
    private Button btnExpSubmit;
    private Toolbar toolbar;
    private String expItem;

    private FirebaseAuth auth;
    private DatabaseReference reference,proLocationRef;
    private StorageReference storageReference;
    private String userId;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expertise);
        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);

        tvExpert = findViewById(R.id.tvExpertId);
        tvExpPrice = findViewById(R.id.tvExpPriceId);
        btnExpSubmit = findViewById(R.id.btnExpSubmit);
        btnExpSubmit.setOnClickListener(this);

        spinner = findViewById(R.id.spinnerExpId);
        ArrayAdapter<CharSequence> myAdapter = ArrayAdapter.createFromResource(this, R.array.service, android.R.layout.simple_spinner_item);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(this);

        //todo for progress dialog
        pDialog = new ProgressDialog(ExpertiseActivity.this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data..");
        pDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        reference = FirebaseDatabase.getInstance().getReference().child("Service").child(userId);
        proLocationRef = FirebaseDatabase.getInstance().getReference().child("Location").child("ServiceExpert");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        expItem = parent.getItemAtPosition(position).toString();
        long itemPosition = parent.getItemIdAtPosition(position);
        if (itemPosition == 0) {
            tvExpert.setText(expItem);
            tvExpPrice.setText("1000");
        } else if (itemPosition == 1) {
            tvExpert.setText(expItem);
            tvExpPrice.setText("500");
        } else if (itemPosition == 2) {
            tvExpert.setText(expItem);
            tvExpPrice.setText("800");
        } else if (itemPosition == 3) {
            tvExpert.setText(expItem);
            tvExpPrice.setText("1000");
        } else if (itemPosition == 4) {
            Toast.makeText(getApplicationContext(), "Hello Wait Some Time " + expItem, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (v == btnExpSubmit){
            pDialog.show();
            onSubmitItem();
        }

    }
    private void onSubmitItem(){
        String exp = tvExpert.getText().toString();
        String price = tvExpPrice.getText().toString();
        String expId = reference.push().getKey();
        ServiceExpert expert = new ServiceExpert(expId, exp, price);
        reference.child(expId).setValue(expert)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Data Insert Successful",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ExpertiseActivity.this, ProviderMapsActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"!!!!! Data Insert Failed ",Toast.LENGTH_LONG).show();
                    }
                });
    }
}
