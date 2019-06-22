package softmaticbd.com.techunter.Operation;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import softmaticbd.com.techunter.MapActivity.HunterMapsActivity;
import softmaticbd.com.techunter.Model.HunterReview;
import softmaticbd.com.techunter.Model.Provider;
import softmaticbd.com.techunter.R;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView disName, disAddress, eReview;
    private Button btnSendReview;
    private String disReview;
    private RatingBar ratingBar;
    private float abc;
    private Toolbar toolbar;

    // todo firebase db
    private DatabaseReference reviewRef, disRef;
    private FirebaseAuth auth;
    private String userId;
    private String proKey;
    // todo rating
    private Provider provider;
    private HunterReview customerReview;
    private String name, address, review, userRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);

        disName = findViewById(R.id.disNameRev);
        disAddress = findViewById(R.id.disAddressRev);
        eReview = findViewById(R.id.disReviewId);
        btnSendReview = findViewById(R.id.btnReviewSend);
        btnSendReview.setOnClickListener(this);
        ratingBar = findViewById(R.id.ratingBarId);
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            abc = rating;
            if (abc == 1) {
                eReview.setText("Below Average");
            } else if (abc == 2) {
                eReview.setText("Average");
            } else if (abc == 3) {
                eReview.setText("Good");
            } else if (abc == 4) {
                eReview.setText("Above Average");
            } else if (abc == 5) {
                eReview.setText("Excellent");
            }
            userRating = Float.toString(abc);
        });
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        getDistributorKey();

    }

    private void getDistributorKey() {
        proKey = getIntent().getExtras().getString("proKey");

        disRef = FirebaseDatabase.getInstance().getReference()
                .child("TecHunter")
                .child("Provider")
                .child("Profile").child(proKey);
        disRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    provider = dataSnapshot.getValue(Provider.class);
                    disName.setText(provider.getName());
                    disAddress.setText(provider.getShopAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnSendReview){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Hunter Review");
            builder.setMessage("Thank you for your Review : " + abc);
            builder.setCancelable(true);
            builder.setPositiveButton("Ok", (dialog, which) -> {
                sendReview();
                dialog.cancel();

            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
    private void sendReview(){
        name = disName.getText().toString();
        address = disAddress.getText().toString();
        review = eReview.getText().toString();
        reviewRef = FirebaseDatabase.getInstance().getReference()
                .child("Review")
                .child("Provider");
        customerReview = new HunterReview(name, address, review, userRating);
        reviewRef.setValue(customerReview)
                .addOnSuccessListener(aVoid -> {
                    removeFindRequest();
//                    startActivity(new Intent(FeedbackActivity.this, CustomerMapsActivity.class));
                });
    }
    private void removeFindRequest() {
        DatabaseReference findRef = FirebaseDatabase.getInstance().getReference().child("FindRequest").child("Hunter");
        findRef.removeValue();
        startActivity(new Intent(FeedbackActivity.this, HunterMapsActivity.class));

    }
}
