public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // গুগল সাইন-ইন অপশন কনফিগার করা
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // সুনির্দিষ্ট গুগল সাইন-ইন বাটনে ক্লিক লজিক যুক্ত করা
        com.google.android.material.button.MaterialButton googleSignInButton = findViewById(R.id.googleSignInButton);
        
        // যদি সাধারণ Button হয়, তবে নিচের লাইনে শুধু `Button` লিখবে:
        // Button googleSignInButton = findViewById(R.id.googleSignInButton);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this, "Login Successful: " + account.getDisplayName(), Toast.LENGTH_LONG).show();
            } catch (ApiException e) {
                Toast.makeText(this, "Login Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
