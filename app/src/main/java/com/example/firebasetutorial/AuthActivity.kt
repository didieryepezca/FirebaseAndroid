package com.example.firebasetutorial

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(2000) // Aguantamos unos segundos para que se note la Splash Screen
        setTheme(R.style.Theme_FirebaseTutorial) // Primero se carga el "TemaSplash" luego al finalizar carga el normal
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //Declaramos funcion que se ejecutara en la carga
        setup()
        session()

        //Recuperar el id del dispositivo al que le enviamos las notificaciones
        notification()
    }

    //Si hacemos Logout, deberiamos volver a mostrar el Layout de Logeo
    override fun onStart() {
        super.onStart()
        authLayout.visibility = View.VISIBLE //authLayout es el id de nuestro Layout del login
    }


    //mostramos una notificacion en segundo plano
    private fun notification() {
        FirebaseInstallations.getInstance().id.addOnCompleteListener() {
            it.result?.let {
                println("Este es el identificador del dispositivo: ${it}")
            }
        }
    }

    //COMPROBAR SI EXISTE UNA SESION INICIADA
    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) //obtenemos el string creado en "strings.xml"
        val email:String? = prefs.getString("email",null)
        val provider:String? = prefs.getString("provider",null)

        if(email!=null && provider!= null){
            authLayout.visibility = View.INVISIBLE //No mostrar la pantalla de logeo en caso haya una sesion iniciada
            showHome(email, ProveedorTipo.valueOf(provider))
        }
    }

    private fun setup(){

        title = "Autenticación"

        //Registro de nuevo usuario
        signInButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.text.toString()
                    ,passwordEditText.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            //los simbolos de preguntas admiten emails nulos
                            showHome(it.result?.user?.email?:"",ProveedorTipo.BASICO)
                        }else{
                            showAlert(emailEditText.text.toString(),passwordEditText.text.toString())
                        }
                }
            }else{
                showAlert(emailEditText.text.toString(),passwordEditText.text.toString())
            }
        }

        //autenticacion con Google Gmail.
        googleButton.setOnClickListener(){

            //Configuración de Google
            val googleConf:GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("971518502159-6mrecrii16v4g369d2oo6pa74gmcfohu.apps.googleusercontent.com") //client id de archivo google-services.json
                    .requestEmail()
                    .build()

            var googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut() // si se hace click en Logeo con google se sale de todas las que esten logeadas.
            startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)

        }
        //Logeo de usuario
        loginButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString()
                    ,passwordEditText.text.toString()).addOnCompleteListener{
                    if(it.isSuccessful){
                        //los simbolos de preguntas admiten emails nulos
                        showHome(it.result?.user?.email?:"",ProveedorTipo.BASICO)
                    }else{
                        showAlert(emailEditText.text.toString(),passwordEditText.text.toString())
                    }
                }
            }else{
                showAlert(emailEditText.text.toString(),passwordEditText.text.toString())
            }
        }
    }

    private fun showAlert(email:String, password: String){
        var builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR")

        if (email.isEmpty() && password.isEmpty()){
            builder.setMessage("Por favor ingrese sus Credenciales.")
        } else if (email.isEmpty()){
            builder.setMessage("Por favor ingrese su Usuario/Email.")
        } else if (password.isEmpty()){
            builder.setMessage("Por favor ingrese su Contraseña.")
        } else {
            builder.setMessage("Hubo un problema al autenticarse.")
        }

        builder.setPositiveButton("Aceptar",null)
        var dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProveedorTipo){
        //navegamos y colocamos los datos en la pantalla Home
        val homeIntent = Intent(this,HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener() {
                            if (it.isSuccessful) {
                                //los simbolos de preguntas admiten emails nulos
                                showHome(account.email ?: "", ProveedorTipo.GOOGLE)
                            } else {
                                showAlert(emailEditText.text.toString(),passwordEditText.text.toString())
                            }
                        }
                }
            }catch (e: ApiException){
                showAlert(emailEditText.text.toString(),passwordEditText.text.toString())
            }

        }
    }

}