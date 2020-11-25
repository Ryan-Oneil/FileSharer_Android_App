package biz.oneilindustries.filesharer.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import biz.oneilindustries.filesharer.MainActivity;
import biz.oneilindustries.filesharer.R;
import biz.oneilindustries.filesharer.service.AuthService;

import static biz.oneilindustries.filesharer.config.Values.APP_NAME;

public class LoginActivity extends AppCompatActivity {

    private AuthService authService;
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start,
                                  int before, int count) {
            checkInput();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        redirectIfLoggedIn();
        authService = new AuthService(getApplicationContext());

        final Button button = findViewById(R.id.loginButton);

        TextView userNameInput = findViewById(R.id.loginUsernameInput);
        userNameInput.addTextChangedListener(textWatcher);

        TextView passwordInput = findViewById(R.id.loginPasswordInput);
        passwordInput.addTextChangedListener(textWatcher);

        button.setOnClickListener(v -> {
            boolean loginSuccess = authService.loginUser(userNameInput.getText().toString(), passwordInput.getText().toString());

            if (loginSuccess) {
                redirectIfLoggedIn();
            }
        });
    }

    protected void checkInput() {
        TextView userNameInput = findViewById(R.id.loginUsernameInput);
        TextView passwordInput = findViewById(R.id.loginPasswordInput);

        if (!userNameInput.getText().toString().isEmpty() && !passwordInput.getText().toString().isEmpty()) {
            final Button button = findViewById(R.id.loginButton);
            button.setEnabled(true);
        }
    }

    protected void redirectIfLoggedIn() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
            finish();
        }
    }
}