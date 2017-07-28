package com.durandayioglu.deviceinfos;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {
    Button btnBilgi, btnYaz;
    TextView txtImsi, txtImei, txtIccd, txtApn, txtSignal, txtBatarya, txtKonum, txtAygitAdi, txtAcikOlanSure, txtOs, txtCpu;
    String imsi, imei, iccd, apn, signal, batarya, konum, aygitAdi, acikOlanSure, os, cpu;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;
    private TelephonyManager telephonyManager;
    String mydate;
    String dosyayaYazilacak;
    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Settings
        txtImsi = (TextView) findViewById(R.id.txtImsi);
        txtImei = (TextView) findViewById(R.id.txtImei);
        txtIccd = (TextView) findViewById(R.id.txtICCD);
        txtApn = (TextView) findViewById(R.id.txtApn);
        txtSignal = (TextView) findViewById(R.id.txtSignal);
        txtBatarya = (TextView) findViewById(R.id.txtBattery);
        txtKonum = (TextView) findViewById(R.id.txtLocation);
        txtAygitAdi = (TextView) findViewById(R.id.txtDeviceName);
        txtAcikOlanSure = (TextView) findViewById(R.id.txtTime);
        txtOs = (TextView) findViewById(R.id.txtOs);
        txtCpu = (TextView) findViewById(R.id.txtCpu);
        btnBilgi = (Button) findViewById(R.id.btnGoster);
        btnYaz = (Button) findViewById(R.id.btnYaz);


        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            getDeviceInfo();
        }

        konumAl();


        //Battery
        this.registerReceiver(this.batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));





        goster();
        dosyayaKaydet();
    }


    public void goster() {
        btnBilgi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signalLevel();

                //upTime
                long miliss = android.os.SystemClock.elapsedRealtime();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", new Locale("tr-TR"));
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = new Date(miliss);
                String result = formatter.format(date);
                acikOlanSure = result;

                txtImsi.setText("IMSI : " + imsi);
                txtImei.setText("IMEI : " + imei);
                txtIccd.setText("ICCD : " + iccd);
                txtApn.setText("APN : " + apn);
                txtSignal.setText("Sinyal Seviyesi : " + signal);
                txtKonum.setText("Konum : " + konum);
                txtAygitAdi.setText("Cihaz : " + aygitAdi);
                txtAcikOlanSure.setText("Açık Kalma Süresi : " + acikOlanSure);
                txtOs.setText("Android Sürümü : " + os);
                txtCpu.setText("Cpu : " + cpu);
                txtBatarya.setText("Batarya Seviyesi : " + batarya);
            }
        });
    }

    public void signalLevel(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> all = tm.getAllCellInfo();
        String a = all.get(0).getClass().getName();

        if (a.equals("android.telephony.CellInfoLte")) {
            CellInfoLte cellInfoLte = (CellInfoLte) all.get(0);
            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
            signal = String.valueOf(cellSignalStrengthLte.getDbm() + " dB");
        } else if (a.equals("android.telephony.CellInfoWcdma")) {
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) all.get(0);
            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
            signal = String.valueOf(cellSignalStrengthWcdma.getDbm() + " dB");

        } else if (a.equals("android.telephony.CellInfoGsm")) {
            CellInfoGsm cellInfoGsm = (CellInfoGsm) all.get(0);
            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
            signal = String.valueOf(cellSignalStrengthGsm.getDbm() + " dB");
        }
    }


    public void dosyayaKaydet() {
        btnYaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dosyayaYaz();
            }
        });
    }

    public void konumAl() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                konum = location.getLatitude() + " - " + location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET
                }, 10);
                return;
            }
        } else {
            locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
        }
        locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_READ_PHONE_STATE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getDeviceInfo();
        } else if (requestCode == 10 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
        }
    }

    public void getDeviceInfo() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
        imsi = telephonyManager.getSubscriberId();
        iccd = telephonyManager.getSimSerialNumber();
        os = Build.VERSION.RELEASE + " - " + "Api : " + Build.VERSION.SDK_INT;
        aygitAdi = Build.MANUFACTURER + " - " + Build.MODEL;
        cpu = Build.CPU_ABI;
    }

    private BroadcastReceiver batteryInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batarya = "% " + String.valueOf(level);
        }
    };

    public void dosyayaYaz() {
        mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        dosyayaYazilacak = "Tarih : " + mydate + "\n" + "IMEI : " + imei + "\n" + "IMSI : " + imsi + "\n" + "ICCD : " + iccd + "\n" + "APN : " + apn + "\n" + "Signal : " + signal + "\n" + "Konum : " + konum + "\n" + "Aygıt Adı : " + aygitAdi + "\n" + "Açık Olduğu Süre : " + acikOlanSure + "\n" + "OS : " + os + "\n" + "CPU : " + cpu + "\n" + "Batarya : " + batarya + "\n";

        File path = getApplicationContext().getExternalFilesDir(null);
        File file = new File(path, "deviceInfos.txt");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            stream.write(dosyayaYazilacak.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
