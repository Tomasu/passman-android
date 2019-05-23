/**
 *  Passman Android App
 *
 * @copyright Copyright (c) 2016, Sander Brand (brantje@gmail.com)
 * @copyright Copyright (c) 2016, Marcos Zuriaga Miguel (wolfi@wolfi.es)
 * @license GNU AGPL version 3 or any later version
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package es.wolfi.app.passman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import javax.inject.Inject;

import butterknife.BindView;
import es.wolfi.passman.API.PassmanApi;
import timber.log.Timber;

public class PasswordList extends BaseActivity
{
    public static final String FRAG_TAG_VAULTS = "vaults";

    static boolean running = false;

    @Inject
    SharedPreferences mSharedPreferences;

    @BindView( R.id.content )
    CoordinatorLayout mCoordinatorLayout;

    @Inject
    PassmanApi mApi;

    @Inject
    DataStore mDataStore;

    /**
     * Displays this activity
     *
     * @param c
     */
    public static
    void launch ( Context c )
    {
        Intent i = new Intent( c, PasswordList.class );
        i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
        c.startActivity( i );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d( "onCreate!" );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list);

        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        running = true;
    }

    @Override
    protected
    void onResume ()
    {
        super.onResume();
        Timber.d( "onResume! running=" + running );

        // @TODO: Display loading screen while checking credentials!

//        final AppCompatActivity self = this;
//        Core.checkLogin( this, false, new FutureCallback< Boolean >()
//        {
//            @Override
//            public
//            void onCompleted ( Exception e, Boolean result )
//            {
//                if ( result )
//                {
//                    Timber.d( "in checkLogin/onCompleted" );
//                    showVaults();
//                    return;
//                }
//
//                Timber.d( "not logged in! show login!" );
//                Timber.e( e );
//                // If not logged in, show login form!
//                LoginActivity.launch( self, new ICallback()
//                {
//                    @Override
//                    public
//                    void onTaskFinished ()
//                    {
//                        Timber.d( "in checkLogin/LoginActivity.onTaskFinished" );
//                        showVaults();
//                    }
//                } );
//
//            }
//        } );

        Timber.d( "in onResume before showVaults!" );
        displayVaultList();
    }

    @Override
    protected
    void onPause ()
    {
        super.onPause();

        mDataStore.save();

        Timber.d( "onPause! running=" + running );
    }

    private
    void displayVaultList ()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment vaultListFragment = fragmentManager.findFragmentByTag( VaultListFragment.FRAG_TAG );
        if ( vaultListFragment != null && vaultListFragment.isVisible() )
        {
            // just ignore showVaults since we're already in the vault list
            Timber.d( "have vaults and a vault list is already displayed" );
            return;
        }

        Fragment credListFragment = fragmentManager.findFragmentByTag( CredentialListFragment.FRAG_TAG );
        if (credListFragment != null && credListFragment.isVisible())
        {
            Timber.d( "cred list visible. do nothing" );
            return;
        }

        VaultListFragment fragment = new VaultListFragment();

        getSupportFragmentManager().beginTransaction()
              //.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim
              // .slide_in_left, R.anim.slide_out_right)
              .replace( R.id.fragment_container, fragment, FRAG_TAG_VAULTS )
              .commit();
    }
//
//    private
//    void onVaultListSuccess ( final HashMap< String, Vault > body )
//    {
//        mDataStore.mergeVaultList( body );
//        displayVaultList();
//    }

//    public
//    void showActiveVault()
//    {
//        Timber.d( "showActiveVault" );
//
//        Vault vault = mDataStore.getActiveVault();
//        if (vault == null)
//        {
//            Timber.w( "no active vault?!" );
//            recreate();
//            return;
//        }
//
//        if (vault.getCredentials() != null) {
//            Timber.d( "vault has credentials" );
//            if (vault.is_unlocked()) {
//                Timber.d( "vault is unlocked, show cred item fragment" );
//
//                CredentialListFragment fragment = new CredentialListFragment();
//
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
//                        .replace(R.id.fragment_container, fragment, "vault")
//                        .addToBackStack(null)
//                        .commit();
//            }
//            else {
//                Timber.d( "vault is locked, show unlock vault" );
//                showUnlockVault();
//            }
//        }
//        else {
//            Timber.d( "vault has no credentials?" );
//
//            Call<Vault> getVaultCall = mApi.getVault( vault.guid );
//            getVaultCall.enqueue( new Callback< Vault >() {
//                @Override
//                public
//                void onResponse (
//                      final Call< Vault > call, final Response< Vault > response )
//                {
//                    if (response.code() == 401)
//                    {
//                        recreate();
//                        return;
//                    }
//
//                    Snackbar.make( mCoordinatorLayout, R.string.net_error , Snackbar.LENGTH_LONG ).show();
//
//                    // some kind of error attempting fetch
//                }
//
//                @Override
//                public
//                void onFailure ( final Call< Vault > call, final Throwable t )
//                {
//
//                }
//            } );
//
//            Vault.getVault(this, vault.guid, new FutureCallback<Vault>() {
//                @Override
//                public void onCompleted(Exception e, Vault result) {
//                    if (e != null) {
//                        // Not logged in, restart activity
//                        if (e.getMessage() != null && e.getMessage().equals("401")) {
//                            recreate();
//                        }
//
//                        Timber.e(e, "Unknown network error");
//
//                        Toast.makeText(getApplicationContext(), getString(R.string.net_error), Toast.LENGTH_LONG).show();
//                        new android.os.Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                showVaults();
//                            }
//                        }, 30000);
//                        return;
//                    }
//
//                    SingleTon ton = getTon();
//                    // Update the vault record to avoid future loads
//                    ((HashMap<String, Vault>)ton.getExtra(SettingValues.VAULTS.toString())).put(result.guid, result);
//
//                    ton.addExtra(SettingValues.ACTIVE_VAULT.toString(), result);
//                    showActiveVault();
//                }
//            });
//
//        }
//    }

//    void showUnlockVault() {
//        Timber.d( "showUnlockVault" );
//
//        Vault v = (Vault) getTon().getExtra(SettingValues.ACTIVE_VAULT.toString());
//        if (v.unlock(mSharedPreferences.getString(v.guid, ""))){
//            Timber.d( "unlocked vault, show active" );
//            showActiveVault();
//            return;
//        }
//
//        Timber.d( "show unlock vault fragment" );
//        getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_left)
//                .replace( R.id.fragment_container, new VaultUnlockFragment(), "vault" )
//                .addToBackStack(null)
//                .commit();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_password_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings :
                showNotImplementedMessage();
                return true;
            case R.id.action_refresh :
                showNotImplementedMessage();
                return true;
            case android.R.id.home :
                displayVaultList();
                return true;

            case R.id.action_logout:
                onLogoutPressed();
                return true;

            default :
                return super.onOptionsItemSelected(item);
        }
    }

    private
    void onLogoutPressed ()
    {
        Timber.d( "LOGOUT!" );

        mDataStore.clear();

        recreate();
    }

    private void showNotImplementedMessage() {
        Toast.makeText(this, R.string.not_implemented_yet, Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onListFragmentInteraction(Vault item) {
//        Timber.d( "selected vault: %s", item );
//        getTon().addExtra(SettingValues.ACTIVE_VAULT.toString(), item);
//
//        // TODO: show progress...
//
//
//
//        showActiveVault();
//    }

//    @Override
//    public void onListFragmentInteraction(Credential item) {
//        Timber.d( "selected item: %s", item );
//        getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
//                .replace( R.id.content_password_list, CredentialFragment.newInstance( item.getGuid() ), "credential" )
//                .addToBackStack(null)
//                .commit();
//    }

//    @Override
//    public void onVaultUnlock(Vault vault) {
//        Timber.d( "unlock vault: %s", vault );
//        getSupportFragmentManager().popBackStack();
//        showActiveVault();
//    }

//    @Override
//    public void onCredentialFragmentInteraction(Credential credential) {
//        Timber.d( "cred interact: %s", credential);
//    }
//

}
