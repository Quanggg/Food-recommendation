package com.example.foodrecommendation

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.foodrecommendation.model.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView


class MainActivity : AppCompatActivity() {
    private val TAG = "Quang"
    private lateinit var navController: NavController
    private lateinit var bottomNavigation: CurvedBottomNavigationView
    private val loginViewModel by viewModels<LoginViewModel>()
    private var launchSignIn = true
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    )
    { result ->
        onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        observeAuthenticationState()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        //setupActionBarWithNavController(navController)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setMenuItems(menuItems, 0)
        bottomNavigation.setupWithNavController(navController)
        changeBottomNavigationColor()

    }

    private fun onSuccess() {
        Log.d(TAG, "Login successful! User token: ${FirebaseAuth.getInstance().currentUser}")
    }

    private fun onExitApp() {
        Log.d(TAG, "Exit app")
        this.finishAndRemoveTask()
    }

    private fun launchSignInFlow() {
        val signInIntent = createSignInIntent()
        signInLauncher.launch(signInIntent)
    }

    private fun createSignInIntent(): Intent {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
            //AuthUI.IdpConfig.TwitterBuilder().build())
        )

        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAlwaysShowSignInMethodScreen(true)
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .setTheme(R.style.LoginTheme)
            .build()
    }

    private fun onSignInResult(
        result: FirebaseAuthUIAuthenticationResult
    ) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK)
            onSuccess()
        else {
            Log.d(TAG, result.toString())
            if (response == null)
                onExitApp()
        }
    }

    private fun observeAuthenticationState() {
        loginViewModel.authenticationState.observe(this, { authenticationState ->
            Log.d(TAG, authenticationState.toString())
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.d(
                        TAG,
                        "Already signed in! Current user token: ${FirebaseAuth.getInstance().currentUser?.displayName}"
                    )
                    launchSignIn = true
                }
                LoginViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    if (launchSignIn)
                        launchSignInFlow()

                    launchSignIn = false
                }
                else -> {
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private val menuItems = arrayOf(
//        CbnMenuItem(
//            R.drawable.ic_home,
//            R.drawable.avd_home,
//            R.id.foodListFragment
//        ),
        CbnMenuItem(
            R.drawable.ic_dashboard, // the icon
            R.drawable.avd_dashboard, // the AVD that will be shown in FAB
            R.id.verticalListFragment // optional if you use Jetpack Navigation
        ),
        CbnMenuItem(
            R.drawable.ic_camera,
            R.drawable.avd_camera,
            R.id.cameraFragment
        ),
        CbnMenuItem(
            R.drawable.ic_settings,
            R.drawable.avd_settings,
            R.id.settingsFragment
        )
    )

    private fun changeBottomNavigationColor()
    {
        val flag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (flag) {
            Configuration.UI_MODE_NIGHT_YES -> {
                bottomNavigation.selectedColor = ("#FFFFFF").toColorInt()
                bottomNavigation.navBackgroundColor = ("#000000").toColorInt()
                bottomNavigation.fabBackgroundColor = ("#000000").toColorInt()
                Log.d("Q", "Dark")
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                bottomNavigation.selectedColor = ("#000000").toColorInt()
                bottomNavigation.navBackgroundColor = ("#FFFFFF").toColorInt()
                bottomNavigation.fabBackgroundColor = ("#FFFFFF").toColorInt()
                Log.d("Q", "Light")
            }
        }
    }
}
