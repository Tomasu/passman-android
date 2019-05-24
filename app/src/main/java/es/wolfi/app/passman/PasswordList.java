/**
 * Passman Android App
 *
 * @copyright Copyright (c) 2016, Sander Brand (brantje@gmail.com)
 * @copyright Copyright (c) 2016, Marcos Zuriaga Miguel (wolfi@wolfi.es)
 * @license GNU AGPL version 3 or any later version
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.wolfi.passman.API.PassmanApi;
import timber.log.Timber;

public
class PasswordList extends BaseActivity
{
	public static final String FRAG_TAG_VAULTS = "vaults";

	static boolean running = false;

	@Inject
	SharedPreferences mSharedPreferences;

	@BindView (R.id.content)
	CoordinatorLayout mCoordinatorLayout;

	private NavController mNavController;

	private AppBarConfiguration mAppBarConfiguration;

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
	protected
	void onCreate ( Bundle savedInstanceState )
	{
		Timber.d( "onCreate!" );

		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_password_list );

		ButterKnife.bind( this );

		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setHomeButtonEnabled( true );
		getSupportActionBar().setDisplayHomeAsUpEnabled( false );

		mNavController = Navigation.findNavController( this, R.id.nav_host_fragment );
		mAppBarConfiguration = new AppBarConfiguration.Builder( mNavController.getGraph() ).build();

		NavigationUI.setupActionBarWithNavController( this, mNavController );
		NavigationUI.setupWithNavController( toolbar, mNavController, mAppBarConfiguration );

		running = true;
	}

	@Override
	public
	boolean onSupportNavigateUp ()
	{
		return NavigationUI.navigateUp( mNavController, mAppBarConfiguration )
				|| super.onSupportNavigateUp();
	}

	@Override
	protected
	void onResume ()
	{
		super.onResume();
		Timber.d( "onResume! running=%s", running );

		// @TODO: Display loading screen while checking credentials!

		Timber.d( "in onResume before showVaults!" );
		displayVaultList();
	}

	@Override
	protected
	void onPause ()
	{
		super.onPause();

		mDataStore.save();

		Timber.d( "onPause! running=%s", running );
	}

	private
	void displayVaultList ()
	{
		NavDestination currentDestination = mNavController.getCurrentDestination();
		if ( currentDestination != null )
		{
			int curDestination = currentDestination.getId();

			if ( curDestination == R.id.nav_toVaultListFragment )
			{
				Timber.d( "already on vault list page" );
				return;
			}
			else if ( curDestination == R.id.nav_toCredentialListFragment )
			{
				Timber.d( "already on credential list page" );
				return;
			}
			else if ( curDestination == R.id.nav_toCredentialFragment )
			{
				Timber.d( "already on credential fragment" );
				return;
			}
		}

		Timber.d( "navigate to vault list!" );
		mNavController.navigate( R.id.nav_toVaultListFragment );
	}

	@Override
	public
	boolean onCreateOptionsMenu ( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.menu_password_list, menu );
		return true;
	}

	@Override
	public
	boolean onOptionsItemSelected ( MenuItem item )
	{
		int id = item.getItemId();
		switch ( id )
		{
			case R.id.action_settings:
				showNotImplementedMessage();
				return true;

			case R.id.action_refresh:
				showNotImplementedMessage();
				return true;

			case android.R.id.home:
				displayVaultList();
				return true;

			case R.id.action_logout:
				onLogoutPressed();
				return true;

			default:
				return super.onOptionsItemSelected( item );
		}
	}

	private
	void onLogoutPressed ()
	{
		Timber.d( "LOGOUT!" );

		mDataStore.clear();

		LoginActivity.launch( this, null );
	}

	private
	void showNotImplementedMessage ()
	{
		Toast.makeText( this, R.string.not_implemented_yet, Toast.LENGTH_SHORT ).show();
	}
}
