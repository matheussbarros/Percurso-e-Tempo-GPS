package br.com.percursoetempogps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    DecimalFormat df = new DecimalFormat("#,##0.00");


    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int RESQUEST_PERMISSION_GPS = 1001;
    private static boolean gpsLigado;
    private ImageView gpsImageView;
    private TextView distPercorrida;
    private TextInputEditText searchInput;
   // private TextView localTextView;

    private Button permicaoGpsBt;
    private Button ativaGpsBt;
    private Button desativaGpsBt;
    private Button iniciaPercursoBt;
    private Button terminaPercursoBt;

    private ImageButton searchBtn;

    private Chronometer tempoChronometer;
    private Boolean timeRunning = false;
    private Boolean percursoIniciado = false;

    private double distancia = 0;
    Location atual = null;
    private double lat;
    private double lon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // localTextView = findViewById(R.id.localTextView);
        gpsImageView = findViewById(R.id.gpsImageView);
        tempoChronometer = findViewById(R.id.tempoChronometer);
        distPercorrida = findViewById(R.id.distPercorrida);
        permicaoGpsBt = findViewById(R.id.permicaoGpsBt);
        ativaGpsBt = findViewById(R.id.ativaGpsBt);
        desativaGpsBt = findViewById(R.id.desativaGpsBt);
        iniciaPercursoBt = findViewById(R.id.iniciaPercursoBt);
        terminaPercursoBt = findViewById(R.id.terminaPercursoBt);
        searchBtn = findViewById(R.id.searchBtn);
        searchInput = findViewById(R.id.searchInput);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Uri gmmIntentUri = Uri.parse(String.format("geo:%f,%f?q=%s",
                        lat,
                        lon,
                        searchInput.getText()));

                // Diz que a intenção é abrir uma action View
                Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                //especifica que será o Google Maps
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);

            }
        });


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (percursoIniciado) {

                    lat = location.getLatitude();
                    lon = location.getLongitude();

                   // String t = String.format("Lat: %f, Long: %f", lat, lon);
                   // localTextView.setText(t);

                    if (atual == null) {
                        atual = location;
                    } else {
                        distancia = distancia + location.distanceTo(atual);
                        atual = location;
                    }

                    String dist = String.format("%f", distancia);
                    distPercorrida.setText(dist);

                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        permicaoGpsBt.setOnClickListener(this);
        ativaGpsBt.setOnClickListener(this);
        desativaGpsBt.setOnClickListener(this);
        iniciaPercursoBt.setOnClickListener(this);
        terminaPercursoBt.setOnClickListener(this);

    }


    private void exibeMensagem(String mensagem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.dialogTitle);
        builder.setMessage(mensagem);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.permicaoGpsBt:
                //PEDE PERMISSÃO
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, permissions, RESQUEST_PERMISSION_GPS);
                //Log.v("PTG:", " Foi dada Permição ");
                break;
            case R.id.ativaGpsBt:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {  //Liga Hardware de GPS
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
                            0,
                            locationListener
                    );
                    gpsLigado = true;
                    gpsImageView.setImageResource(R.drawable.gpson);
                  //  Log.v("PTG:", " GPS foi ativado ");
                } else {
                    exibeMensagem(getString(R.string.permissionDeniedGps));
                }
                break;
            case R.id.desativaGpsBt:
                if (!gpsLigado) {
                    exibeMensagem(getString(R.string.GpsNotEnabled));
                  //  Log.v("PTG:", " GPS não está ativado ");
                }
                locationManager.removeUpdates(locationListener);
                gpsLigado = false;
                gpsImageView.setImageResource(R.drawable.gpsoff);
                Log.v("PTG:", " O gps foi desativado ");
                break;


            case R.id.iniciaPercursoBt:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {  //Liga Hardware de GPS
                    exibeMensagem(getString(R.string.permissionDeniedGps));
                } else if (!gpsLigado) {
                    exibeMensagem(getString(R.string.enableGpsForRoute));
                } else {

                    percursoIniciado = true;

                    if (!timeRunning) {
                      tempoChronometer.setBase(SystemClock.elapsedRealtime());
                        tempoChronometer.start();
                        timeRunning = true;
                    }
                }
                break;

            case R.id.terminaPercursoBt:

                if (!percursoIniciado) {

                    exibeMensagem(getString(R.string.courseNotStarted));

                } else {
                    String distFinal = getString(R.string.distanceMessage, distancia);
                    //Toast.makeText(this,distFinal, Toast.LENGTH_SHORT).show();

                    String tempoFinal = getString(R.string.timeTextViewMessage, tempoChronometer.getText().toString());
                   // Toast.makeText(this,tempoFinal, Toast.LENGTH_SHORT).show();
                    exibeMensagem(distFinal + "\n" +tempoFinal);

                    percursoIniciado = false;

                    distPercorrida.setText("0");
                    distancia = 0;
                    if (timeRunning) {
                        tempoChronometer.stop();

                        tempoChronometer.setBase(SystemClock.elapsedRealtime());
                        timeRunning = false;
                    }
                }
                break;
        }

    }
}
