package huyndph30375.fpoly.bookapp.global;

import static huyndph30375.fpoly.bookapp.global.Constants.MAX_BYTES_PDF;
import static huyndph30375.fpoly.bookapp.global.Constants.VND;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    public static final String TAG = "PDF Book TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static String formatTime(long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.CHINESE);
        calendar.setTimeInMillis(timestamp);

        //format time to dd/MM//yyyy
        String date = DateFormat.format("dd//MM/yyyy", calendar).toString();
        return date;
    }

    public static void deleteBookPdf(Context context, String bookId, String bookTitle, String bookUrl) {
        //khoi tao cac bien can dung
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Deleting The Book " + bookTitle);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Delete Successfully The Book" + bookTitle, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void loadPdfSize(String pdfUrl, String pdfTile, TextView size, Context context) {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @SuppressLint({"SetTextI18n", "DefaultLocale"})
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double byteFile = storageMetadata.getSizeBytes();

                        //convert byte to kb or mb
                        double kb = byteFile / 1024;
                        double mb = byteFile / 1024;

                        if (mb >= 1) {
                            size.setText(Constants.SIZE + String.format("%.2f", mb) + " MB");
                        } else if (kb >= 1) {
                            size.setText(Constants.SIZE + String.format("%.2f", kb) + " KB");
                        } else {
                            size.setText(Constants.SIZE + String.format("%.2f", byteFile) + " bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void loadPdfUrl(String url, PDFView pdfView, Context context, ProgressBar progress, TextView pageTv) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait...");
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        //set to pdfview github
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progress.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progress.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progress.setVisibility(View.INVISIBLE);

                                        if (pageTv != null) {
                                            pageTv.setText("" + Constants.PAGE + nbPages);
                                        }
                                    }
                                })
                                .load();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress.setVisibility(View.INVISIBLE);
                    }
                });

    }

    public static void loadCategory(String CateId, TextView categoryTv) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(CateId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = "" + snapshot.child("category").getValue();
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void loadPrice(String idBook, TextView priceTv) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(idBook)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String price = "" + snapshot.child("price").getValue();

                        // format tien
                        // Format the price
                        String formattedPrice = formatVndEditText(price);

                        priceTv.setText(VND + formattedPrice);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static String formatVndEditText(String input) {
        // Remove non-numeric characters
        String cleanString = input.replaceAll("[^\\d]", "");

        // Insert commas for thousands, hundreds of thousands, millions, etc.
        if (!cleanString.isEmpty()) {
            StringBuilder formatted = new StringBuilder(cleanString);
            int length = formatted.length();

            for (int i = length - 3; i > 0; i -= 3) {
                formatted.insert(i, ",");
            }

            return formatted.toString();
        } else {
            return "";
        }
    }


    public static void calculatorViewCount(String bookId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount = "" + snapshot.child("viewerCount").getValue();

                        //xu ly logic
                        if (viewCount.equals("") || viewCount.equals("null")) {
                            viewCount = "0";
                        }

                        long newViewCount = Long.parseLong(viewCount) + 1;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewerCount", newViewCount);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void calculatorFabCount(Context context, String bookId) {
        //check user co login hay khong, neu k thi phai dang ky tai khoan
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You must login by account to add favourite", Toast.LENGTH_SHORT).show();
        } else {
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", "" + bookId);
            hashMap.put("timestamp", "" + timestamp);

            //save to database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Fab").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: " + " add favorites successfully " + hashMap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.getMessage());
                        }
                    });
        }
    }

    public static void deleteFab(Context context, String bookId) {
        //check user co login hay khong, neu k thi phai dang ky tai khoan
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You must login by account to add favourite", Toast.LENGTH_SHORT).show();
        } else {
            //save to database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Fab").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: " + " remove ok with " + bookId);
                            Toast.makeText(context, "Remove Fab successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.getMessage());
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
