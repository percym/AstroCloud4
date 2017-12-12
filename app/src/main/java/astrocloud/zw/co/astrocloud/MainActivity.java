package astrocloud.zw.co.astrocloud;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity" ;
    CountryCodePicker countryCodePicker ;
    EditText phoneNumber, editCode;
    Button btn_start_verification , btn_verify ,btn_resend;
    private FirebaseAuth mAuth;
    String formatedNumber;
    RelativeLayout relMain;
    public String mVerificationId="9";
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(MainActivity.this, UploadActivity.class));
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        countryCodePicker = findViewById(R.id.countryCodeHolder);
        phoneNumber = findViewById(R.id.phoneNumber);
        editCode = findViewById(R.id.code);
        btn_start_verification = findViewById(R.id.btn_start_verification);
        btn_verify = findViewById(R.id.btn_verify);
        btn_resend = findViewById(R.id.btn_resend);
        relMain = findViewById(R.id.relMain);

        countryCodePicker.registerCarrierNumberEditText(phoneNumber);

        if(App.getInstance()==null){
            App.getInstance();
        }
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                snackShower("invalid phone number");

            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };


    btn_start_verification.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!validatePhoneNumber()){
                return;
            }

            startPhoneNumberVerification(formatedNumber);
        }
    });

    btn_verify.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String code = editCode.getText().toString();
            if (TextUtils.isEmpty(code)) {
                snackShower("Verification code can not be empty");
                return;
            }
            if(!validatePhoneNumber()){
                snackShower("Invalid phone number.");
                return;
            }
//            if(mVerificationId.isEmpty()){
//                snackShower("in valid VerificationId");
//                return;
//            }
            verifyPhoneNumberWithCode(mVerificationId, code);
        }
    });
    btn_resend.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mResendToken==null) {
                snackShower("Try requesting verification code  ");
                return ;
            }
            resendVerificationCode(formatedNumber,mResendToken);

        }
    });

    }
    private boolean validatePhoneNumber() {
        String countryCode =countryCodePicker.getSelectedCountryCode();
        String phoneNumber = this.phoneNumber.getText().toString();
        if(phoneNumber.startsWith("0")){
            phoneNumber = phoneNumber.replace("0","");
        }
        //String fullNumber = countryCode.concat(phoneNumber);
        String fullNumber = countryCodePicker.getFullNumber();
        formatedNumber = fullNumber;
        if (TextUtils.isEmpty(fullNumber)) {
            snackShower("Invalid phone number.");
            return false;
        }
        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(MainActivity.this, UploadActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                              snackShower("Invalid Credentials.");
                            }
                        }
                    }
                });
    }
    private void snackShower(String message){
        Snackbar snackbar;
        snackbar = Snackbar.make(relMain, message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.mainGreen));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
      //  getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
