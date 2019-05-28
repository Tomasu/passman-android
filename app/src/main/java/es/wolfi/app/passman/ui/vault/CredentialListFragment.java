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
package es.wolfi.app.passman.ui.vault;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import es.wolfi.app.passman.DataStore;
import es.wolfi.app.passman.R;
import es.wolfi.app.passman.databinding.FragmentCredentialListBinding;
import es.wolfi.app.passman.ui.SearchableFragment;
import es.wolfi.passman.API.Credential;
import es.wolfi.passman.API.PassmanApi;
import es.wolfi.passman.API.Vault;
import es.wolfi.utils.FilterListAsyncTask;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import retrofit2.HttpException;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * {@link OnCredentialListFragmentInteractionListener}
 * interface.
 */
public
class CredentialListFragment extends SearchableFragment
		implements OnCredentialListFragmentInteractionListener, FilterListAsyncTask.OnListFilteredListener<Credential>
{
	public static final String FRAG_TAG = "CREDENTIAL_LIST_FRAGMENT";

	// TODO: Customize parameter argument names
	private static final String ARG_COLUMN_COUNT = "column-count";
	// TODO: Customize parameters
	private int mColumnCount = 1;
	private FilterListAsyncTask<Credential> filterTask = null;

	private Vault mVault = null;

	@Inject
	DataStore mDataStore;

	@Inject
	PassmanApi mApi;

	private FragmentCredentialListBinding mBinding;

	private CompositeDisposable mDisposable = new CompositeDisposable();
	private Single< Vault > mGetVaultObservable = null;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public
	CredentialListFragment ()
	{
	}

	// TODO: Customize parameter initialization
	@SuppressWarnings ("unused")
	public static
	CredentialListFragment newInstance ( int columnCount )
	{
		CredentialListFragment fragment = new CredentialListFragment();
		Bundle args = new Bundle();
		args.putInt( ARG_COLUMN_COUNT, columnCount );
		fragment.setArguments( args );
		return fragment;
	}

	@Override
	public
	void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		if ( getArguments() != null )
		{
			mColumnCount = getArguments().getInt( ARG_COLUMN_COUNT );
		}
	}

	@Override
	public
	View onCreateView (
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		mBinding = FragmentCredentialListBinding.inflate( inflater, container, false );

		View view = mBinding.getRoot();

		// Set the adapter
		Context context = mBinding.list.getContext();

		if ( mColumnCount <= 1 )
		{
			mBinding.list.setLayoutManager( new LinearLayoutManager( context ) );
		}
		else
		{
			mBinding.list.setLayoutManager( new GridLayoutManager( context, mColumnCount ) );
		}

		mVault = mDataStore.getActiveVault();
		if ( mVault == null )
		{
			Snackbar.make( container, "No active vault?!", Snackbar.LENGTH_LONG ).show();
			Navigation.findNavController( requireActivity(), R.id.nav_host_fragment ).navigateUp();
			return view;
		}

		mBinding.credentialListSwipeLayout.setOnRefreshListener( () -> updateVault( true ) );

		return view;
	}


	@Override
	public
	void onAttach ( Context context )
	{
		super.onAttach( context );
	}

	@Override
	public
	void onResume ()
	{
		Timber.d( "onResume" );
		super.onResume();

		mVault = mDataStore.getActiveVault();

		updateVault();
	}

	private
	void updateVault()
	{
		updateVault( false );
	}

	private
	void updateVault(boolean force)
	{
		Timber.d( "updateVault: force=%s", force );

		if ( mVault != null )
		{
			// TODO: limit automatic refreshes to one every X minutes.

			if (force && mGetVaultObservable != null)
			{
				Timber.d( "forced and observable not null! dispose!" );
				mDisposable.clear();
				mGetVaultObservable = null;
			}

			if (mGetVaultObservable == null)
			{
				Timber.d( "no existing observable" );

				List< Credential > credentials = mVault.getCredentials();
				//Timber.d( "credentials: %s", credentials );

				if ( force || credentials == null || credentials.size() < 1 )
				{
					Timber.d( "credentials null or empty?!" );

					mGetVaultObservable = mApi.getVault( mVault.guid )
							.observeOn( AndroidSchedulers.mainThread() );

					mDisposable.add( mGetVaultObservable.subscribeWith( new VaultDisposableSingleObserver() ) );
				}
				else
				{
					Timber.d( "have cached credentials" );
					updateList( credentials );
				}
			}
			else
			{
				Timber.d( "have existing observable.. possibly waiting for api?" );
				updateList( mVault.getCredentials() );
			}
		}
		else
		{
			Snackbar.make( mBinding.list, "Active vault not set?!", Snackbar.LENGTH_LONG ).show();
			Navigation.findNavController( requireActivity(), R.id.nav_host_fragment ).navigateUp();
		}
	}

	private
	void updateRecyclerView ( @NonNull List< Credential > credentialList )
	{
		Timber.d( "updateRecyclerView: %d", credentialList.size() );
		CredentialViewAdapter adapter = new CredentialViewAdapter( credentialList, this );
		mBinding.list.setAdapter( adapter );
	}

	private
	void updateList(@NonNull List<Credential> credentialList)
	{
		checkNotNull( credentialList, "Null credentialList?!" );

		Timber.d( "update list: %d items", credentialList.size() );

		if ( isSearchOpen() && !getQueryString().isEmpty() )
		{
			Timber.d( "search open and query not empty, do filter!" );
			runFilter( getQueryString() );
		}
		else
		{
			Timber.d( "update unfiltered!" );
			updateRecyclerView( credentialList );
			mBinding.credentialListSwipeLayout.setRefreshing( false );
		}

	}

	@Override
	public
	void onDetach ()
	{
		super.onDetach();
	}

	@Override
	public
	void onStop ()
	{
		super.onStop();

		mDisposable.clear();
	}

	@Override
	public
	void onListFragmentInteraction ( final Credential item )
	{
		Bundle args = new Bundle();
		args.putString( "credential_guid", item.getGuid() );
		args.putString( "credential_name", item.getLabel() );

		Navigation.findNavController( requireActivity(), R.id.nav_host_fragment )
				.navigate( R.id.nav_clist_to_cred, args );

	}

	private
	void onVaultGetSuccess ( @NonNull Vault vault )
	{
		mDataStore.putVault( vault.guid, vault );
		mDataStore.setActiveVault( vault );
		mVault = vault;

		if ( !vault.unlock( mDataStore.getVaultPassword( vault ) ) )
		{
			Timber.e( "failed to (re)unlock vault?!?!" );
		}
		else
		{
			Timber.d( "vault re-unlocked!" );
		}

		//			Credential secondCred = mVault.getCredentials().get( 1 );
		//			Timber.d( "second cred email: %s", secondCred.getEmail() );

		updateList( vault.getCredentials() );
	}

	@Override
	public
	void onListFiltered (
			@NonNull final List< Credential > filteredList )
	{
		Timber.d( "onListFiltered: Credential!" );
		updateRecyclerView( filteredList );

		mBinding.credentialListSwipeLayout.setRefreshing( false );
	}

	@Override
	protected
	void onSearchTextSubmit ( final String query )
	{
		Timber.d( "onSearchTextSubmit: %s", query );
	}

	@Override
	protected
	void onSearchTextChange ( final String query )
	{
		Timber.d( "onSearchTextChange: %s", query );

		runFilter( query );
	}

	private
	void runFilter(@NonNull String query)
	{
		String searchText = query.toLowerCase();
		if ( filterTask != null )
		{
			filterTask.cancel( true );
		}

		filterTask = new FilterListAsyncTask< Credential >( searchText, CredentialListFragment.this );

		getLifecycle().addObserver( filterTask );

		ArrayList< Credential > input[] = new ArrayList[] { mVault.getCredentials() };
		filterTask.execute( input );
	}

	@Override
	public
	void onCreateOptionsMenu (
			@NonNull final Menu menu, @NonNull final MenuInflater inflater )
	{
		setupMenu( menu, inflater, R.menu.menu_credential_list );
	}

	private
	class VaultDisposableSingleObserver extends DisposableSingleObserver< Vault >
	{
		@Override
		public
		void onSuccess ( final Vault vault )
		{
			onVaultGetSuccess( vault );
			mGetVaultObservable = null;
		}

		@Override
		public
		void onError ( final Throwable e )
		{
			Timber.w( e, "getVault request failed!" );

			if ( e instanceof HttpException )
			{
				HttpException httpException = (HttpException) e;

				if ( httpException.code() == 403 )
				{
					Snackbar.make( mBinding.list, "Not authenticated!", Snackbar.LENGTH_LONG ).show();
				}
				else
				{
					Snackbar.make( mBinding.list, "Request failed: " + httpException.message(),
										Snackbar.LENGTH_LONG ).show();
				}
			}
			else
			{
				Snackbar.make( mBinding.list, "unexpected error: " + e.getMessage(),
									Snackbar.LENGTH_LONG ).show();
			}

			Navigation.findNavController( requireActivity(), R.id.nav_host_fragment ).navigateUp();
		}
	}

	private
	class SearchTextWatcher implements TextWatcher
	{
		@Override
		public
		void beforeTextChanged ( CharSequence charSequence, int i, int i1, int i2 )
		{
			Timber.d( "beforeTextChanged" );
		}

		@Override
		public
		void onTextChanged ( CharSequence charSequence, int i, int i1, int i2 )
		{

		}

		@Override
		public
		void afterTextChanged ( Editable editable )
		{

		}
	}
}
