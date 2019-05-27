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

package es.wolfi.app.passman.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Iterator;

import javax.inject.Inject;

import es.wolfi.app.passman.DataStore;
import es.wolfi.app.passman.R;
import es.wolfi.app.passman.databinding.ActivityMainBinding;
import es.wolfi.passman.API.PassmanApi;
import timber.log.Timber;

public
class MainActivity extends BaseActivity
{
	public static final String FRAG_TAG_VAULTS = "vaults";

	static boolean running = false;

	@Inject
	SharedPreferences mSharedPreferences;

	private NavController mNavController;

	private AppBarConfiguration mAppBarConfiguration;

	ActivityMainBinding mBinding;

	@Inject
	PassmanApi mApi;

	@Inject
	DataStore mDataStore;
	private MenuItem mSearchItem;

	/**
	 * Displays this activity
	 *
	 * @param c
	 */
	public static
	void launch ( Context c )
	{
		Intent i = new Intent( c, MainActivity.class );
		i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
		c.startActivity( i );
	}

	@Override
	protected
	void onCreate ( Bundle savedInstanceState )
	{
		Timber.d( "onCreate!" );

		super.onCreate( savedInstanceState );
		mBinding = ActivityMainBinding.inflate( getLayoutInflater() );
		setContentView( mBinding.getRoot() );

		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		toolbar.setNavigationIcon( R.drawable.ic_home );

		setSupportActionBar( toolbar );

		mNavController = Navigation.findNavController( this, R.id.nav_host_fragment );
		mAppBarConfiguration = new AppBarConfiguration.Builder( mNavController.getGraph() ).build();

		NavigationUI.setupActionBarWithNavController( this, mNavController );
		NavigationUI.setupWithNavController( toolbar, mNavController, mAppBarConfiguration );

		mNavController.addOnDestinationChangedListener(
				new NavDestinationChangedListener( this ) );

		if (!mDataStore.haveHost())
		{
			mNavController.popBackStack();
			mNavController.navigate( R.id.nav_toLogin );
		}

		running = true;
	}

	@Override
	public
	boolean onSupportNavigateUp ()
	{
		Timber.d( "onSupportNavigateUp: %s", mNavController.getCurrentDestination() );
		if (mNavController.getCurrentDestination().getId() == R.id.nav_toVaultListFragment)
		{
			Timber.d( "onSupportNavigateUp have vault list frag" );
			finish();
			return true;
		}

		return NavigationUI.navigateUp( mNavController, mAppBarConfiguration )
				|| super.onSupportNavigateUp();
	}

	@Override
	public
	void onBackPressed ()
	{
		Timber.d( "onBackPressed: %s", mNavController.getCurrentDestination().getLabel() );
		if ( mNavController.getCurrentDestination().getId() == R.id.nav_toVaultListFragment )
		{
			Timber.d( "onBackPressed have vault list frag" );
			finish();
			return;
		}

		super.onBackPressed();
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

		if ( mDataStore.haveHost() )
		{
			// assume we're logged in here...

			Timber.d( "navigate to vault list!" );

			mNavController.navigate( R.id.nav_toVaultListFragment );
			return;
		}

		//mNavController.navigate( R.id.nav_toVaultListFragment );
	}

	String getViewName (@IdRes int id )
	{
		if (id == -1)
		{
			return "no-id";
		}

		try
		{
			return getResources().getResourceName( id );
		} catch ( Resources.NotFoundException e )
		{
			return "not-found";
		}
	}

	void recurseView (String pfx, @NonNull ViewGroup group )
	{
		Timber.d( "%s%s %s", pfx, group.getClass().getSimpleName(), getViewName( group.getId() ) );

		for ( int i = 0; i < group.getChildCount(); i++ )
		{
			View v = group.getChildAt( i );

			if (v instanceof ViewGroup)
			{
				recurseView( pfx + "   ", (ViewGroup) v );
			}
			else
			{
				Timber.d( "%s%s %s", pfx + "   ", v.getClass().getSimpleName(), getViewName( v.getId() ) );
			}
		}
	}

	@Override
	public
	boolean onCreateOptionsMenu ( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.menu_password_list, menu );

		mSearchItem = menu.findItem( R.id.action_search );
		SearchView searchView = (SearchView) mSearchItem.getActionView();

		//searchView.setBackgroundColor( getResources().getColor( R.color.pressed_color ) );


		searchView.setOnSearchClickListener( new View.OnClickListener() {
			@Override
			public
			void onClick ( final View v )
			{
				Timber.d( "on search click!" );
				recurseView( "", mBinding.toolbar );

				for ( int i = 0; i < mBinding.toolbar.getChildCount(); i++ )
				{
					View vv = mBinding.toolbar.getChildAt( i );
					if ( vv instanceof AppCompatImageButton )
					{
						int curDest = mNavController.getCurrentDestination().getId();
						if (mNavController.getGraph().getStartDestination() == curDest)
						{
							// got the home/back button
							// set it to the home icon since we don't want it
							// to be the back button when at the home screen

							Timber.d( "cur dest: %s", getResources().getResourceName( curDest ) );
							AppCompatImageButton imageButton = (AppCompatImageButton) vv;
							imageButton.setImageResource( R.drawable.ic_home );
							break;
						}
					}
				}
			}
		} );

		searchView.setOnCloseListener( new SearchView.OnCloseListener() {
			@Override
			public
			boolean onClose ()
			{
				Timber.d( "search closed!" );
				return false;
			}
		} );

		searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
			@Override
			public
			boolean onQueryTextSubmit ( final String query )
			{
				Timber.d( "text submit: %s", query );
				if (!searchView.isIconified())
				{
					searchView.setIconified( true );
				}

				searchView.setQuery( query, false );
				//searchItem.collapseActionView();
				return false;
			}

			@Override
			public
			boolean onQueryTextChange ( final String newText )
			{
				Timber.d( "text change: %s", newText );
				return false;
			}
		} );

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

//			case R.id.action_search:
//				onActionSearch();
//				return true;

			default:
				return super.onOptionsItemSelected( item );
		}
	}

	private
	void onActionSearch ()
	{
		Timber.d( "SEARCH!" );
		recurseView( "", mBinding.toolbar );
		//mBinding.toolbarSearch.setVisibility( View.VISIBLE );
	}

	private
	void onLogoutPressed ()
	{
		Timber.d( "LOGOUT!" );

		mDataStore.clear();

		NavOptions navOptions = new NavOptions.Builder()
				.setPopUpTo( R.id.nav_toVaultListFragment, true ).build();
		mNavController.navigate( R.id.nav_toLogin, null, navOptions );
		//LoginActivity.launch( this, null );
	}

	private
	void showNotImplementedMessage ()
	{
		Toast.makeText( this, R.string.not_implemented_yet, Toast.LENGTH_SHORT ).show();
	}

	private static
	class NavDestinationChangedListener implements NavController.OnDestinationChangedListener
	{
		private final MainActivity mActivity;

		public
		NavDestinationChangedListener ( final MainActivity activity ) {mActivity = activity;}

		@Override
		public
		void onDestinationChanged (
				@NonNull final NavController controller, @NonNull final NavDestination destination,
				@Nullable final Bundle arguments )
		{
			int navId = destination.getId();
			Toolbar toolbar = mActivity.mBinding.toolbar;

			Iterator<NavDestination> it = controller.getGraph().iterator();
			while ( it.hasNext() )
			{
				NavDestination dest = it.next();
				if (dest.getId() == navId)
				{
					Timber.d( "selected dest: %s", dest.getLabel() );
				}
			}

			if (mActivity.mSearchItem != null)
			{
				if ( navId == R.id.nav_toVaultListFragment || navId == R.id.nav_toCredentialListFragment )
				{
					mActivity.mSearchItem.setVisible( true );
				}
				else
				{
					mActivity.mSearchItem.setVisible( false );
				}
			}

			if ( navId == R.id.nav_toLogin || navId == R.id.nav_toLoginClient
					|| navId == R.id.nav_toLoginBasic
				/*|| navId == R.id.nav_toLoginNCApp*/ )
			{
				Timber.d( "login page" );
				toolbar.setVisibility( View.GONE );
			}
			else
			{
				toolbar.setVisibility(View.VISIBLE);
				if ( controller.getGraph().getStartDestination() == navId)
				{
					Timber.d( "go HOME!" );
					toolbar.setNavigationIcon( R.drawable.ic_home );


				}
				else
				{
					Timber.d( "other page" );
				}
			}
		}
	}
}
