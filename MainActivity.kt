package com.example.fortysix

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val api = "https://boipenal.infinityfreeapp.com/api.php"
    private var page = 1
    private lateinit var name: EditText
    private lateinit var phone: EditText
    private lateinit var consent: TextView
    private lateinit var button: Button
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        name=findViewById(R.id.nameInput); phone=findViewById(R.id.phoneInput)
        consent=findViewById(R.id.consent); button=findViewById(R.id.nextButton)
        status=findViewById(R.id.status)
        button.setOnClickListener { nextPage() }
    }

    private fun nextPage() {
        if(page==1){
            if(name.text.toString().trim().isEmpty()){name.error="Enter your name";return}
            page=2; phone.visibility=View.VISIBLE; consent.visibility=View.VISIBLE
            name.visibility=View.GONE; button.text="Verify & Register"
            findViewById<TextView>(R.id.subtitle).text="Enter mobile number"
        } else {
            val p=phone.text.toString().trim()
            if(p.length<7){phone.error="Enter a valid mobile number";return}
            button.isEnabled=false
            registerDevice(name.text.toString().trim(),p)
        }
    }

    private fun registerDevice(userName:String, mobile:String){
        val deviceId = getOrCreateId()
        val body=JSONObject().apply{
            put("name",userName); put("mobile",mobile); put("device_id",deviceId)
        }.toString().toRequestBody("application/json".toMediaType())
        val req=Request.Builder().url("$api?action=register_device").post(body).build()
        OkHttpClient().newCall(req).enqueue(object:Callback{
            override fun onFailure(call:Call,e:IOException)=runOnUiThread{
                button.isEnabled=true; status.text="Could not connect. Check internet and try again."
            }
            override fun onResponse(call:Call,response:Response)=runOnUiThread{
                val text=response.body?.string()?:"{}"
                if(response.isSuccessful){
                    page=3; name.visibility=View.GONE; phone.visibility=View.GONE; consent.visibility=View.GONE
                    button.visibility=View.GONE
                    findViewById<TextView>(R.id.subtitle).text="Successfully Verified"
                    status.text="✓ Successfully Verified\nDevice registered with AK47 panel."
                } else {
                    button.isEnabled=true; status.text=JSONObject(text).optString("error","Registration failed")
                }
            }
        })
    }

    private fun getOrCreateId():String{
        val prefs=getSharedPreferences("46",0)
        var id=prefs.getString("device_id",null)
        if(id==null){id=UUID.randomUUID().toString();prefs.edit().putString("device_id",id).apply()}
        return id!!
    }
}
