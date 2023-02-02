package com.example.stripepayment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.googlepaylauncher.GooglePayEnvironment;
import com.stripe.android.googlepaylauncher.GooglePayLauncher;
import com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    Button b,bb;
    PaymentSheet paymentSheet;
    String SECRET_KEY = "sk_test_51MWDfnSIP9wNLJmxJqoRFX0pqod0AYnjH4duoWEfUsXu29r2VBg4Yw4cr3ItL9R72ykrwRgGwNYlV8YiwfsYbPcG00uWcboHKF";
    String PUBLISHER_KEY = "pk_test_51MWDfnSIP9wNLJmxdgfJPLTG4hVpVDEm7jh7Y3Ikt2abs74XaSnb0t3nCRkBYQuoicDBFPDjY5XjOIx28jdqO9Et001MSxOQGi";
    String customerID;
    String ephericalKey;
    String clientSecret;
    private Button googlePayButton;
   GooglePayLauncher googlePayLauncher;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b = findViewById(R.id.button);
        bb=findViewById(R.id.button2);

        PaymentConfiguration.init(this, PUBLISHER_KEY);
        fetchApi();



        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(SECRET_KEY!=null){
                   PaymentFlow();

               }
               else{

                   Toast.makeText(MainActivity.this, "APi Loaded....", Toast.LENGTH_SHORT).show();

               }

            }
        });

        bb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googlePayLauncher.presentForPaymentIntent(clientSecret);
            }
        });

        paymentSheet = new PaymentSheet(this, PaymentSheetResult -> {

            onPaymentResult(PaymentSheetResult);

        });




        googlePayLauncher = new GooglePayLauncher(
                this,
                new GooglePayLauncher.Config(
                        GooglePayEnvironment.Test,
                        "US",
                        "Widget Store"
                ),
                this::onGooglePayReady,
                this::onGooglePayResult
        );


    }

    private void onGooglePayReady(boolean isReady) {
        bb.setEnabled(isReady);
    }

    private void onGooglePayResult(@NotNull GooglePayLauncher.Result result) {
        // implemented below
    }


    private void onGooglePayResult(@NotNull GooglePayPaymentMethodLauncher.Result result) {
        if (result instanceof GooglePayPaymentMethodLauncher.Result.Completed) {
            // Payment details successfully captured.
            // Send the paymentMethodId to your server to finalize payment.
            final String paymentMethodId =
                    ((GooglePayPaymentMethodLauncher.Result.Completed) result).getPaymentMethod().id;
        } else if (result instanceof GooglePayPaymentMethodLauncher.Result.Canceled) {
            // User canceled the operation
        } else if (result instanceof GooglePayPaymentMethodLauncher.Result.Failed) {
            // Operation failed; inspect `result.getError()` for the exception
        }
    }





    private  void fetchApi(){




        String url = "https://api.stripe.com/v1/customers";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    customerID = jsonObject.getString("id");
                    Toast.makeText(MainActivity.this, customerID, Toast.LENGTH_SHORT).show();
                    getEphericalKey(customerID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                return header;
            }


        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            fetchApi();
            Toast.makeText(this, "payment Success", Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, ((PaymentSheetResult.Failed) paymentSheetResult).getError().toString(), Toast.LENGTH_SHORT).show();


        }

        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "payment Cancelled", Toast.LENGTH_SHORT).show();


        }
    }

    private void getEphericalKey(String customerID) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    ephericalKey = jsonObject.getString("id");
                    Toast.makeText(MainActivity.this, ephericalKey, Toast.LENGTH_SHORT).show();
                    getClentSecret(ephericalKey);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                header.put("Stripe-Version", "2022-11-15");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);


    }

    private void getClentSecret(String ephericalKey) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    clientSecret = jsonObject.getString("client_secret");
                    Toast.makeText(MainActivity.this, clientSecret, Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                params.put("amount", "300");
                params.put("currency", "inr");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }

    private void PaymentFlow() {
        paymentSheet.presentWithPaymentIntent(clientSecret, new PaymentSheet.Configuration("Sanjay",new PaymentSheet.CustomerConfiguration(customerID, ephericalKey),new PaymentSheet.GooglePayConfiguration(
                PaymentSheet.GooglePayConfiguration.Environment.Test,
                "IND" +
                        ""
        )));


    }
}