package com.example.menupop.login

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.BuildConfig
import com.example.menupop.mainActivity.MainActivity
import com.example.menupop.R
import com.example.menupop.databinding.ActivityLoginBinding
import com.example.menupop.signup.SignupActivity

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
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LoginActivityTAG"
    }

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private var sharedPreferences: SharedPreferences? = null
    private var socialLoginManager : SocialLoginManager? = null
    private val googleSignInClient: GoogleSignInClient by lazy { getGoogleClient() }
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null


    private val googleAuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(applicationContext, "Google sign in Failed", Toast.LENGTH_LONG)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        identitySaveCheck()
        setObservers()
        setListeners()
    }

    private fun setObservers(){
        loginViewModel.mergeAccount.observe(this) { result ->
            Log.d(TAG, "mergeAccount: $result")
            if (result.result == "success") {
                loginViewModel.saveIdentifier(sharedPreferences!!, result.identifier)
                isNewUserCheck(result.isNewUser, result.identifier)
            }
        }

        loginViewModel.loginResult.observe(this) {
            Log.d(TAG, "onCreate: ${it.isNewUser} ${it.identifier} ${it.result}")
            if (it.result == "failed") {
                showCustomDialog()
            } else {
                loginViewModel.saveIdentifier(sharedPreferences!!, it.identifier)
                isNewUserCheck(it.isNewUser, it.identifier)
            }
        }

        loginViewModel.socialLoginResult.observe(this) { result ->
            Log.d(TAG, "socialLoginResult: ${result.isNewUser} ${result.identifier} ${result.result}")
            when (result.result) {
                "local_login" -> showSocialWarningDialog(result.identifier)
                "failed" -> Toast.makeText(this, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                else -> {
                    loginViewModel.saveIdentifier(sharedPreferences!!, result.identifier)
                    isNewUserCheck(result.isNewUser, result.identifier)
                }
            }
        }
    }

    fun init() {
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this

        sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        socialLoginManager = SocialLoginManager(this)
    }

    private fun isNewUserCheck(isNewUser: Int, identifier: Int) {
//        if (isNewUser == 1) {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            return
//        }
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("identifier", identifier)
        startActivity(intent)
        finish()
    }

    private fun identitySaveCheck() {
        val identifier = sharedPreferences!!.getInt("identifier", 0)
        if (identifier != 0) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("identifier", identifier)
            startActivity(intent)
            finish()
        }
    }

    private fun setListeners() {
        binding.loginButton.setOnClickListener {
            val id = binding.loginIdEditText.text.toString().trim()
            val password =
                binding.loginPasswordEditText.text.toString().trim().hashCode().toString()
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
            val intent = Intent(this, FindIdActivity::class.java)
            startActivity(intent)
        }

        binding.loginFindPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
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

    private fun getHash() {
        val keyHash = Utility.getKeyHash(this)
    }

    private fun kakaoLoginRequest() {
        val apiKey = BuildConfig.KAKAO_NATIVE_APP_KEY_STELLA
        KakaoSdk.init(this, apiKey)

        lifecycleScope.launch {
            val response = socialLoginManager!!.requestKakaoSocialLogin()

            if(response.accessToken.isNotEmpty()){
                Log.d(TAG, "kakaoLoginRequest: ${response.accessToken}")
                UserApiClient.instance.accessTokenInfo { _, _ ->
                    UserApiClient.instance.me { user, _ ->
                        Log.d(TAG, "kakaoLoginRequest nickName: ${user?.kakaoAccount?.profile?.nickname}")
                        loginViewModel.socialLoginRequest(user?.kakaoAccount?.email.toString())
                    }
                }
            }else{
                Log.d(LoginViewModel.TAG, "requestKakaoSocialLogin token is Empty")
            }
        }
    }

    private fun showSocialWarningDialog(identifier: Int) {
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



    private fun getGoogleClient(): GoogleSignInClient {
        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope("https://www.googleapis.com/auth/pubsub"))
            .requestServerAuthCode(BuildConfig.GOOGLE_LOGIN_KEY)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(applicationContext, googleSignInOption)
    }

    private fun signIn() {
        googleSignInClient.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_LOGIN_KEY)
            .requestProfile()
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // [END config_signin]

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        val signInIntent: Intent = mGoogleSignInClient!!.signInIntent
        googleAuthLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                Log.d(TAG, "signInWithCredential:success")
                val user = mAuth!!.currentUser
                Log.d(TAG, "onComplete: " + user!!.email)
                loginViewModel.socialLoginRequest(user.email.toString())
            }
        }
    }

    private fun requestNaverLogin() {
        val naverClientId = BuildConfig.SOCIAL_LOGIN_INFO_NAVER_CLIENT_ID
        val naverClientSecret = BuildConfig.SOCIAL_LOGIN_INFO_NAVER_CLIENT_SECRET
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret, naverClientName)

        loginViewModel.requestNaverSocialLogin()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        sharedPreferences = null
        socialLoginManager = null
    }
}

