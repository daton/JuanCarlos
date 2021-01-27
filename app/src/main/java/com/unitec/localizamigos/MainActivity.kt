package com.unitec.localizamigos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_main)
        //Vamos a poner un codigo gracioso!! vamos a activar el vibrador!!!
     var v= getSystemService(VIBRATOR_SERVICE) as Vibrator
      // invocamos el vibrador
      v.vibrate(3000)
        

    }
}