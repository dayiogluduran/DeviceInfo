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
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;


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
    long miliss;


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

        IzinKontroEt();

        //Battery
        this.registerReceiver(this.batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        konumAl();
        goster();
        dosyayaKaydet();
    }

    private void IzinKontroEt() {
        String[] izinler = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE
        };
        int phoneStateCode = 02;
        int locCode = 03;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                getDeviceInfo();
            } else {
                requestPermissions(izinler, phoneStateCode);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                konumAl();
                locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
            } else {
                requestPermissions(izinler, locCode);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 02: {
                //-- Kullanıcı izin isteğini iptal ederse if - else bloğunun içindeki kodlar çalışmayacaktır. Böyle bir durumda yapılacak işlemleri bu kısımda kodlayabilirsiniz.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceInfo();
                } else {
                    //-- Kullanıcı istemiş olduğunuz izni reddederse bu kod bloğu çalışacaktır.
                }
                return;
            }
            case 03: {
                //-- Kullanıcı izin isteğini iptal ederse if - else bloğunun içindeki kodlar çalışmayacaktır. Böyle bir durumda yapılacak işlemleri bu kısımda kodlayabilirsiniz.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    konumAl();
                    locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
                } else {
                    //-- Kullanıcı istemiş olduğunuz izni reddederse bu kod bloğu çalışacaktır.
                }
                return;
            }
            //-- Farklı 'case' blokları ekleyerek diğer izin işlemlerinizin sonuçlarını da kontrol edebilirsiniz.. Biz burada sadece değerini 67 olarak tanımladığımız izin işlemini kontrol ettik.
        }
    }

    private String milisToHour(long milliseconds) {

        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int days = (int) ((milliseconds / (1000 * 60 * 60 * 24)));

        String time;
        if (days > 0) {
            time = days + "gün " + hours + "saat " + minutes + "dk " + seconds + "sn";
        } else if (hours > 0 && minutes > 0) {
            time = hours + "saat " + minutes + "dk " + seconds + "sn";
        } else if (hours == 0 && minutes > 0) {
            time = minutes + "dk " + seconds + "sn";
        } else if (hours == 0 && minutes == 0) {
            time = "Cihaz daha yeni açıldı";
        } else {
            time = "HATA ! ";
        }
        return time;
    }


    public void goster() {
        btnBilgi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signalLevel();
                getDeviceInfo();
                konumAl();

                //upTime
                miliss = android.os.SystemClock.elapsedRealtime();
                acikOlanSure = milisToHour(miliss);

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

    public void signalLevel() {
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
    }


    public void getDeviceInfo() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
        imsi = telephonyManager.getSubscriberId();
        iccd = telephonyManager.getSimSerialNumber();
        os = Build.VERSION.RELEASE + " - " + "Api : " + Build.VERSION.SDK_INT;
        aygitAdi = Build.MANUFACTURER + " - " + Build.MODEL;
        cpu = Build.CPU_ABI;
        apn = telephonyManager.getNetworkOperatorName();
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
