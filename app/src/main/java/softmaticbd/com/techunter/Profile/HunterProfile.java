package softmaticbd.com.techunter.Profile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import softmaticbd.com.techunter.MapActivity.HunterMapsActivity;
import softmaticbd.com.techunter.Model.Hunter;
import softmaticbd.com.techunter.R;

public class HunterProfile extends AppCompatActivity implements View.OnClickListener {

    private EditText eFullName, ePhone, eEmail, eAddress;
    private Button btnSubmit;
    private ImageView ivPro;

    //todo for firebase db
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;

    // todo utils
    private static final String TAG = "ProfileCustomer";
    private static final String REQUIRED = "Required";
    private Uri resultUri;
    private Bitmap bitmap = null;
    private String profileImage;
    private String userID;
    private static final int GALLERY_INTENT = 1;
    private String name, phone, email, address;
    private boolean isProfileExist = false;
    private ProgressDialog pDialog;
    private Hunter hunter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunter_profile);
        eFullName = findViewById(R.id.cusFullNameId);
        ePhone = findViewById(R.id.cusPhoneId);
        eEmail = findViewById(R.id.cusEmailId);
        eAddress = findViewById(R.id.cusAddressId);
        ivPro = findViewById(R.id.ivCusProfile);
        btnSubmit = findViewById(R.id.btnCusSubmit);
        btnSubmit.setOnClickListener(this);
        ivPro.setOnClickListener(this);

        //todo for progress dialog
        pDialog = new ProgressDialog(HunterProfile.this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data..");
        pDialog.setCancelable(false);

        // todo for databaseRef
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("TecHunter").child("Hunter").child("Profile").child(userID);
        storageReference = FirebaseStorage.getInstance().getReference().child("TecHunter").child("Hunter").child(userID);
        getUserInfo();

    }

    @Override
    public void onClick(View v) {
        if (v == ivPro) {
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), GALLERY_INTENT);
            isProfileExist = true;
        }
        if (v == btnSubmit) {
            pDialog.show();
            createHunterProfile();
        }
    }

    private void getUserInfo() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    hunter = dataSnapshot.getValue(Hunter.class);
                    eFullName.setText(hunter.getName());
                    ePhone.setText(hunter.getPhone());
                    eEmail.setText(hunter.getEmail());
                    eAddress.setText(hunter.getAddress());
                    profileImage = hunter.getProfileImage();
                    Glide.with(getApplication()).load(profileImage).into(ivPro);
                    isProfileExist = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void createHunterProfile() {
        name = eFullName.getText().toString();
        phone = ePhone.getText().toString();
        email = eEmail.getText().toString();
        address = eAddress.getText().toString();

        if (name.isEmpty()) {
            pDialog.dismiss();
            eFullName.setError("Enter the Full Name");
            eFullName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            pDialog.dismiss();
            ePhone.setError("Enter the phone Number");
            ePhone.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            pDialog.dismiss();
            eEmail.setError("Enter the address");
            eEmail.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            pDialog.dismiss();
            eAddress.setError("Enter Father Name");
            eAddress.requestFocus();
            return;
        }
        if (resultUri != null) {

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);

            Task<Uri> tasks = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        pDialog.dismiss();
                        Toast.makeText(getApplication(), "Upload Unsuccessful...", Toast.LENGTH_SHORT).show();
                    }

                    return storageReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    profileImage = task.getResult().toString();
                    setCustomer();
                    pDialog.dismiss();
                }
            });

        } else if (isProfileExist) {
            setCustomer();
        } else {
            Log.e(TAG, "Image is " + REQUIRED);
            Toast.makeText(HunterProfile.this, "Please Again Upload Image", Toast.LENGTH_LONG).show();
            pDialog.dismiss();
        }
    }

    private void setCustomer() {
        hunter = new Hunter(name, phone, email, address, profileImage);
        reference.setValue(hunter)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(HunterProfile.this, "Upload Successful...", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(HunterProfile.this, HunterMapsActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK) {

            final Uri imageUri = data.getData();
            resultUri = imageUri;
            ivPro.setImageURI(imageUri);
        }
    }

}