package com.example.menupop.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.mainActivity.MainActivity
import com.example.menupop.R
import com.example.menupop.signup.SignupActivity
import com.example.menupop.databinding.LoginBinding
import com.example.menupop.findId.FindIdActivity
import com.example.menupop.resetPassword.ResetPasswordActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private  val TAG = "LoginActivity"
    private lateinit var binding : LoginBinding
    lateinit var kakaoCallback: (OAuthToken?, Throwable?) -> Unit
    private val googleSignInClient: GoogleSignInClient by lazy { getGoogleClient() }
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null

    val googleApiKey = com.example.menupop.BuildConfig.GOOGLE_API_KEY

    private val googleAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
            Toast.makeText(applicationContext, "Google sign in Failed", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        IdentitySaveCheck()

        init()

        initOnClickListener()

        loginViewModel.mergeAccount.observe(this){ result ->
            Log.d(TAG, "onCreate: ${result}")
            if(result.result == "success"){
                var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
                loginViewModel.saveIdentifier(sharedPreferences,result.identifier)
                isNewUserCheck(result.isNewUser)
            }
        }


        loginViewModel.loginResult.observe(this, Observer { it ->
            Log.d(TAG, "onCreate: ${it.isNewUser} ${it.identifier} ${it.result}")
            if (it.result == "failed"){
                showCustomDialog()
            } else {
                var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
                loginViewModel.saveIdentifier(sharedPreferences,it.identifier)
                isNewUserCheck(it.isNewUser)
            }
        })
        loginViewModel.socialLoginResult.observe(this){ result ->
            Log.d(TAG, "onCreate: ${result.isNewUser} ${result.identifier} ${result.result}")
            if(result.result == "local_login"){
                showSocialWarningDialog(result.identifier)
            } else if(result.result == "failed"){
                Toast.makeText(this,"잠시 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
            } else {
                var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
                loginViewModel.saveIdentifier(sharedPreferences,result.identifier)
                isNewUserCheck(result.isNewUser)
            }
        }
    }
    fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this
    }
    private fun isNewUserCheck(isNewUser:Int){
        if (isNewUser == 1){
            var intent = Intent(this, SignupActivity :: class.java)
            startActivity(intent)
            return
        }
        var intent = Intent(this, MainActivity :: class.java)
        startActivity(intent)
        finish()
    }
    private fun IdentitySaveCheck(){
        var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        val identifier = sharedPreferences.getInt("identifier",0)
        if(identifier != 0){
            var intent = Intent(this, MainActivity :: class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun initOnClickListener(){
        binding.loginButton.setOnClickListener {
            var id = binding.loginIdEditText.text.toString().trim()
            var password = binding.loginPasswordEditText.text.toString().trim().hashCode().toString()
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    loginViewModel.requestLogin(id, password)
                }
            }
        }
        binding.loginSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        binding.loginFindId.setOnClickListener {
            val intent = Intent(this,FindIdActivity::class.java)
            startActivity(intent)
        }
        binding.loginFindPassword.setOnClickListener {
            val intent = Intent(this,ResetPasswordActivity::class.java)
            startActivity(intent)
        }
        binding.googleLoginButton.setOnClickListener {
            signIn()
        }
        binding.naverLoginButton.setOnClickListener {
            requestNaverLogin()
        }
        binding.kakaoLoginButton.setOnClickListener {
            kakaoLoginRequest()
            getHash()
        }
    }
    private fun getHash(){
        val keyHash = Utility.getKeyHash(this)
        Log.d(TAG, "getHash: ${keyHash}")
    }
    private fun kakaoLoginRequest(){
        val apiKey = com.example.menupop.BuildConfig.KAKAO_NATIVE_APP_KEY
        KakaoSdk.init(this,apiKey)
        loginViewModel.requestKakaoSocialLogin()
    }
    private fun showSocialWarningDialog(identifier:Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_account_merge)
        dialog.show()
        dialog.findViewById<Button>(R.id.account_merge_agree).setOnClickListener {
            loginViewModel.socialAccountMergeLocalAccount(identifier)
        }
        dialog.findViewById<Button>(R.id.account_merge_disagree).setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_warning)
        dialog.show()
    }

    private fun socialLoginRequest(email : String){
        loginViewModel.socialLoginRequest(email)
    }
    fun btnKakaoLogin(callback : (OAuthToken?, Throwable?) -> Unit) {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        }else{
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun requestGoogleLogin() {
        googleSignInClient.signOut()
        val signInIntent = googleSignInClient.signInIntent
        googleAuthLauncher.launch(signInIntent)
    }

    private fun getGoogleClient(): GoogleSignInClient {
        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope("https://www.googleapis.com/auth/pubsub"))
            .requestServerAuthCode(googleApiKey)
            .requestEmail() // 이메일도 요청할 수 있다.
            .build()

        return GoogleSignIn.getClient(applicationContext, googleSignInOption)
    }
    private fun signIn() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleApiKey)
            .requestProfile()
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // [END config_signin]

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        val signInIntent: Intent = mGoogleSignInClient!!.getSignInIntent()
        googleAuthLauncher.launch(signInIntent)
    }
    private fun firebaseAuthWithGoogle(acct:GoogleSignInAccount ) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]

        var  credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener { result ->
            if(result.isSuccessful){
                Log.d(TAG, "signInWithCredential:success")
                val user = mAuth!!.currentUser
                Log.d(TAG, "onComplete: " + user!!.email)
            }
        }
            }
    private fun requestNaverLogin(){
        val naverClientId = com.example.menupop.BuildConfig.SOCIAL_LOGIN_INFO_NAVER_CLIENT_ID
        val naverClientSecret = com.example.menupop.BuildConfig.SOCIAL_LOGIN_INFO_NAVER_CLIENT_SECRET
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret , naverClientName)

        loginViewModel.requestNaverSocialLogin()
    }
    }

