package huyndph30375.fpoly.bookapp.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.HashMap;

import huyndph30375.fpoly.bookapp.R;
import huyndph30375.fpoly.bookapp.databinding.ActivityEditProfileBinding;
import huyndph30375.fpoly.bookapp.global.MyApplication;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    private FirebaseAuth firebaseAuth;

    private Uri imgUri = null;

    private String firstName = "", lastName = "", fullName = "";

    private ProgressDialog progressDialog;

    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadUser();


        listenter();

    }

    private void listenter() {
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //login click to change pick image
        binding.updateImageAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenuOptionToPickImage();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private void validateData() {

        firstName = binding.inputFirstName.getText().toString().trim();
        lastName = binding.inputLastName.getText().toString().trim();
        fullName = firstName + " " + lastName;
        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(this, "Please Enter First Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "Please Enter Last Name", Toast.LENGTH_SHORT).show();
        } else {
            if (imgUri == null) {
                updateProfile("");
            } else {
                upLoadImage();
            }
        }
    }

    private void upLoadImage() {
        progressDialog.setMessage("Update Your Image...");
        progressDialog.show();

        String filePathAndName = "ProfileImages/" + firebaseAuth.getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = "" + uriTask.getResult();

                        updateProfile(uploadedImageUrl);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfile(String imageUrl) {
        progressDialog.setMessage("Updating Your Profile...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + fullName);
        if (imgUri != null) {
            hashMap.put("imageProfile", "" + imageUrl);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Update User Successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showMenuOptionToPickImage() {
        PopupMenu popupMenu = new PopupMenu(EditProfileActivity.this, binding.updateImageAction);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, " Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, " Photos");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int position = menuItem.getItemId();
                if (position == 0) {
                    //user pick camera
                    pickCameraToChooseImage();
                } else if (position == 1) {
                    //user pick photos
                    pickPhotosToChooseImage();

                }


                return false;
            }
        });
    }

    private void pickPhotosToChooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        photosActivityResult.launch(intent);

    }

    private void pickCameraToChooseImage() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image Avatar For Your Account");
        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        cameraActivityResult.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Glide.with(EditProfileActivity.this)
                                .load(imgUri)
                                .placeholder(R.drawable.ic_person)
                                .into(binding.profileImage);
                        binding.profileImage.setImageURI(imgUri);
                        binding.profileImage.setImageURI(imgUri);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }

                }
            });

    private ActivityResultLauncher<Intent> photosActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imgUri = data.getData();
                        //load image
                        Glide.with(EditProfileActivity.this)
                                .load(imgUri)
                                .placeholder(R.drawable.ic_person)
                                .into(binding.profileImage);
                        binding.profileImage.setImageURI(imgUri);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void loadUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get infor of user from database
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String imageProfile = "" + snapshot.child("imageProfile").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String userType = "" + snapshot.child("userType").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        //set data ui
                        String[] nameParts = name.split(" ");

                        String firstName = "";
                        String lastName = "";

                        if (nameParts.length > 1) {
                            lastName = nameParts[nameParts.length - 1]; // Last part is considered as last name

                            // Combine the remaining parts as first name
                            StringBuilder firstNameBuilder = new StringBuilder();
                            for (int i = 0; i < nameParts.length - 1; i++) {
                                firstNameBuilder.append(nameParts[i]).append(" ");
                            }
                            firstName = firstNameBuilder.toString().trim();
                        } else if (nameParts.length == 1) {
                            firstName = nameParts[0]; // If only one part, consider it as first name
                        }

                        binding.inputFirstName.setText(firstName);
                        binding.inputLastName.setText(lastName);

                        try {
                            if (!isDestroyed()){
                                Glide.with(EditProfileActivity.this)
                                        .load(imageProfile)
                                        .placeholder(R.drawable.ic_person)
                                        .into(binding.profileImage);
                            }
                        }
                        catch (Exception e){
                            e.getMessage();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}