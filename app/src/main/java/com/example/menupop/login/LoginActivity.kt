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
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private  val TAG = "LoginActivity"
    private lateinit var binding : LoginBinding
    lateinit var kakaoCallback: (OAuthToken?, Throwable?) -> Unit
    private val googleSignInClient: GoogleSignInClient by lazy { getGoogleClient() }
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
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
        setContentView(R.layout.login)

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
        binding = DataBindingUtil.setContentView(this, R.layout.login)

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
            var id = binding.loginIdEditText.text.toString().replace(" ", "")
            var password = binding.loginPasswordEditText.text.toString().replace(" ", "")
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    loginViewModel.requestLogin(id, password.trim().hashCode().toString().toString())
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
        KakaoSdk.init(this,getString(R.string.KAKAO_NATIVE_APP_KEY))
        setKakaoCallback()
        btnKakaoLogin()
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
    fun btnKakaoLogin() {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this, callback = kakaoCallback)
        }else{
            UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
        }
    }

    fun setKakaoCallback() {
        kakaoCallback = { token, error ->
            if (error != null) {
                Log.d(TAG, "setKakaoCallback: ${error.toString()}")
            }
            else if (token != null) {
                Log.d("[카카오로그인]","로그인에 성공하였습니다.\n${token.accessToken}")
                UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                    UserApiClient.instance.me { user, error ->
                        Log.d(TAG, "setKakaoCallback: 닉네임: ${user?.kakaoAccount?.profile?.nickname} 이메일 : ${user?.kakaoAccount?.email}")
                        socialLoginRequest(user?.kakaoAccount?.email.toString())
                    }
                }
            }
            else {
                Log.d(TAG, "setKakaoCallback: 토큰==null")
            }
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
            .requestServerAuthCode(getString(R.string.GOOGLE_API_KEY)) // string 파일에 저장해둔 client id 를 이용해 server authcode를 요청한다.
            .requestEmail() // 이메일도 요청할 수 있다.
            .build()

        return GoogleSignIn.getClient(applicationContext, googleSignInOption)
    }
    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.GOOGLE_API_KEY))
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
        val naverClientId = getString(R.string.social_login_info_naver_client_id)
        val naverClientSecret = getString(R.string.social_login_info_naver_client_secret)
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret , naverClientName)
        var naverToken :String? = ""

        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                val email = response.profile?.email
                loginViewModel.socialLoginRequest(email!!)
                Log.d(TAG, "onSuccess: ${email}")
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d(TAG, "onFailure: ${errorCode} ${errorDescription}")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        /** OAuthLoginCallback을 authenticate() 메서드 호출 시 파라미터로 전달하거나 NidOAuthLoginButton 객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다. */
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                naverToken = NaverIdLoginSDK.getAccessToken()

                //로그인 유저 정보 가져오기
                NidOAuthLogin().callProfileApi(profileCallback)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d(TAG, "onFailure: ${errorCode} ${errorDescription}")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }
    }

