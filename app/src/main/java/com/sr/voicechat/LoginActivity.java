Button btnLoginDirect = findViewById(R.id.btnLoginDirect);
if (btnLoginDirect != null) {
    btnLoginDirect.setOnClickListener(v -> {
        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    });
}
