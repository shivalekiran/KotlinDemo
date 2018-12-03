package com.aperotechnologies.googlevisionapidemo.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.aperotechnologies.googlevisionapidemo.R
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_zing_scan.*

class ZingScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zing_scan)
        btn_act_scan.setOnClickListener {
            //            val intentIntegrator = IntentIntegrator(this)
//            intentIntegrator.setOrientationLocked(true)
////            intentIntegrator.setDesiredBarcodeFormats()
//            intentIntegrator.initiateScan()

            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.let {
            result.contents?.let {
                Toast.makeText(this, "ACtivity Code :${it} ", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
