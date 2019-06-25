package xyz.gsora.toot

import MastodonTypes.AppCreationResponse
import MastodonTypes.OAuthResponse
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import xyz.gsora.toot.Mastodon.Mastodon
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    @BindView(R.id.userAtInstance)
    lateinit var userAtInstance: EditText
    @BindView(R.id.login)
    lateinit var login: Button
    @BindView(R.id.progress)
    lateinit var progress: ProgressBar

    private var ut: UserTuple? = null
    private var m: Mastodon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ButterKnife.bind(this)
        ut = UserTuple()
        m = Mastodon.instance
        setupUserStringTest()

        // I truly don't understand why I need to set these here, while they're already been set via XML.
        progress.visibility = View.INVISIBLE
        login.isEnabled = true

        // parse intent data if any (aka if the browser redirected here)
        val intent = intent
        val uri = intent.data
        if (!(uri == null || !uri.toString().startsWith(Mastodon.REDIRECT_URI) || Toot.hasLoggedIn())) {
            userAtInstance.isEnabled = false
            login.isEnabled = false
            progress.visibility = View.VISIBLE
            handleOAuthReply(uri)
        }

    }

    /**
     * Setup the user/instance URI validator.
     */
    private fun setupUserStringTest() {
        userAtInstance.addTextChangedListener(object : TextWatcher {
            private val USER_INSTANCE_PATTERN = Pattern.compile("(\\w+)@(\\w+\\.\\w+)")

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val p = USER_INSTANCE_PATTERN.matcher(s)

                //                if (p.find()) {
                //                    // 1 = username
                //                    // 2 = instanceURI
                //                    ut.setUser(p.group(1));
                //                    ut.setInstanceURI(p.group(2));
                //                } else {
                //                    ut.setUser("");
                //                    ut.setInstanceURI("");
                //                }

                ut!!.user = "giakovlev"
                ut!!.instanceURI = "vfsmpmapps08.fsm.northwestern.edu"

                login.isEnabled = true
            }

            override fun afterTextChanged(s: Editable) {
                val result = s.toString().replace(" ".toRegex(), "")
                if (s.toString() != result) {
                    userAtInstance.setText(result)
                    userAtInstance.setSelection(result.length)
                }
            }
        })
    }

    /**
     * Listener method for clicks on the "login" button.
     *
     * @param view the button who fired the event.
     */
    fun onLoginClick(view: View) {
        // start some animations
        progress.visibility = View.VISIBLE

        // disable all the UI components
        userAtInstance.isEnabled = false
        //   login.setEnabled(false);

        // save user and instance uri since we're about to log in
        Toot.saveUsernameInstanceTuple(ut!!)

        val createApp = m!!.createMastodonApplication()
        createApp
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        Consumer<AppCreationResponse> { this.handleApplicationCreationRequest(it) },
                        Consumer<Throwable> { this.handleApplicationCreationError(it) }
                )
    }

    /**
     * Handle a succesful application creation request.
     *
     * @param acr a valid AppCreationResponse instance
     */
    private fun handleApplicationCreationRequest(acr: AppCreationResponse) {
        // since we successfully created an application, we can save its data and the UserTuple
        Toot.saveUsernameInstanceTuple(ut!!)
        Toot.saveClientID(acr.clientId)
        Toot.saveClientSecret(acr.clientSecret)

        // build an URI and throw it into the browser
        val destination = Uri.parse("https://" + Toot.getInstanceURI() + "/oauth/authorize").buildUpon()
                .appendQueryParameter("client_id", acr.clientId)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("redirect_uri", Mastodon.REDIRECT_URI)
                .appendQueryParameter("scope", Mastodon.SCOPES)
                .build()

        val browser = Intent(Intent.ACTION_VIEW, destination)
        startActivity(browser)
    }

    /**
     * Handle an unsuccessful application creation request.
     *
     * @param error the request error
     */
    private fun handleApplicationCreationError(error: Throwable) {
        // TODO: fix my dumbness
        Log.d(TAG, "application creation error: $error")

        Toasty.error(applicationContext, "Something went wrong :(\n$error", Toast.LENGTH_SHORT, true).show()

        userAtInstance.isEnabled = true
        login.isEnabled = true
        progress.visibility = View.INVISIBLE
    }


    /**
     * Handle a successful OAuth code request.
     *
     * @param uri object containing the URL-encoded code.
     */
    private fun handleOAuthReply(uri: Uri) {
        var code: String? = null
        try {
            code = uri.getQueryParameter("code")
        } catch (e: Exception) {
            handleApplicationCreationError(e)
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "oauth reply code: " + code!!)
        }

        // requesting OAuth tokens
        val oauth = code?.let { m!!.requestOAuthTokens(it) }
        oauth?.let {
            it.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        Consumer<OAuthResponse> { this.handleSuccessfulOAuthReply(it) },
                        Consumer<Throwable> { this.handleUnsuccessfulOAuthReply(it) }
                )
        }
    }

    /**
     * Handle a successful OAuth response.
     *
     * @param oAuthResponse the successful OAuth response.
     */
    private fun handleSuccessfulOAuthReply(oAuthResponse: OAuthResponse) {
        Toot.markLoggedIn()
        Toot.saveOAuthAccessToken(oAuthResponse.accessToken)
        Toot.saveOAuthRefreshToken(oAuthResponse.refreshToken)

        // send everyone to the main view!
        val timeline = Intent(this, TimelineFragmentContainer::class.java)
        startActivity(timeline)
        finish()
    }

    /**
     * Handle an unsuccessful OAuth response
     *
     * @param error the error fired from the network stack.
     */
    private fun handleUnsuccessfulOAuthReply(error: Throwable) {
        // TODO: fix my dumbness
        Log.d(TAG, "OAuth error: $error")

        Toasty.error(applicationContext, "Something went wrong :(\n$error", Toast.LENGTH_SHORT, true).show()

        userAtInstance.isEnabled = true
        login.isEnabled = true
        progress.visibility = View.INVISIBLE
    }

    companion object {

        private val TAG = LoginActivity::class.java.simpleName
    }
}
