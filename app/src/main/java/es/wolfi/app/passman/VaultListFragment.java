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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import es.wolfi.passman.API.PassmanApi;
import es.wolfi.passman.API.Vault;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import retrofit2.HttpException;
import timber.log.Timber;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public
class VaultListFragment extends BaseFragment implements OnListFragmentInteractionListener
{
	public static final String FRAG_TAG = "VAULT_LIST_FRAGMENT";

	private static final String TAG = VaultListFragment.class.getSimpleName();

	// TODO: Customize parameter argument names
	private static final String ARG_COLUMN_COUNT = "column-count";
	// TODO: Customize parameters
	private int mColumnCount = 1;

	@Inject
	PassmanApi mApi;

	@Inject
	DataStore mDataStore;

	private Single< List< Vault > > mListVaultsSingle = null;

	private RecyclerView mRecyclerView;

	private
	CompositeDisposable mDisposable = new CompositeDisposable();

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public
	VaultListFragment () { }

	// TODO: Customize parameter initialization
	@SuppressWarnings ("unused")
	public static
	VaultListFragment newInstance ( int columnCount )
	{
		VaultListFragment fragment = new VaultListFragment();
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

		Timber.d( "onCreate" );

		if ( getArguments() != null )
		{
			mColumnCount = getArguments().getInt( ARG_COLUMN_COUNT );
			Timber.d( "arg %s: %d", ARG_COLUMN_COUNT, mColumnCount );
		}
	}

	@Override
	public
	View onCreateView (
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// TODO: logcat> TextInputLayout  I  EditText added is not a TextInputEditText. Please switch to using that class instead.
		View view = inflater.inflate( R.layout.fragment_vault_list, container, false );

		mRecyclerView = view.findViewById( R.id.list );

		Timber.d( "onCreateView: setup view!" );
		// Set the adapter
//		if ( view instanceof RecyclerView )
//		{
			Context context = view.getContext();
			//mRecyclerView = (RecyclerView) view;

			if ( mColumnCount <= 1 )
			{
				mRecyclerView.setLayoutManager( new LinearLayoutManager( context ) );
			}
			else
			{
				mRecyclerView.setLayoutManager( new GridLayoutManager( context, mColumnCount ) );
			}
	//	}

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
	void onDetach ()
	{
		super.onDetach();
		Timber.d( "onDetach" );
	}

	@Override
	public
	void onResume ()
	{
		super.onResume();

		Timber.d( "onResume!" );

		if ( mDataStore.getNumVaults() < 1 )
		{
			// no vaults
			if ( mListVaultsSingle == null )
			{
				Timber.d( "no vaults, fetch" );

				// request list of vaults
				mListVaultsSingle = mApi.listVaults().observeOn( AndroidSchedulers.mainThread() );
				mDisposable.add( mListVaultsSingle.subscribeWith(
						new ListVaultsDisposableObserver() ));
			}
		}
		else
		{
			updateList();
		}
	}

	@Override
	public
	void onStop ()
	{
		super.onStop();

		mDisposable.clear();
	}

	private
	void updateList ()
	{
		List< Vault > vaultList = mDataStore.getVaults();

		Timber.d( "update list: %d items", vaultList.size() );

		VaultViewAdapter adapter = new VaultViewAdapter( vaultList, this );
		mRecyclerView.setAdapter( adapter );
	}

	private
	void onVaultListSuccess ( final List< Vault > body )
	{
		mDataStore.putVaults(body);
		updateList();
	}

	@Override
	public
	void onListFragmentInteraction ( final Vault vault )
	{
		mDataStore.setActiveVault( vault );

		String vaultPass = mDataStore.getVaultPassword( vault );
		if ( vaultPass != null && vault.unlock(vaultPass) )
		{
			Timber.d( "vault already unlocked" );
			showVault();
		}
		else
		{
			Timber.d( "vault locked" );
			showUnlockVault();
		}
	}

	private
	void showUnlockVault ()
	{
		Timber.d( "vault is locked. show unlock frag" );

		Vault activeVault = mDataStore.getActiveVault();
		Bundle args = new Bundle();
		args.putString( "vault_name", activeVault.name );

		Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
				.navigate( R.id.nav_vlist_to_vault_unlock, args );
	}

	private
	void showVault ()
	{
		Timber.d( "vault is unlocked, show vault!" );

		Vault activeVault = mDataStore.getActiveVault();
		Bundle args = new Bundle();
		args.putString( "vault_name", activeVault.name );

		Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
				.navigate( R.id.nav_vlist_to_credential_list, args );
	}

	private
	class ListVaultsDisposableObserver extends DisposableSingleObserver< List< Vault > >
	{
		@Override
		public
		void onSuccess (
				final List< Vault > vaultMap )
		{
			Timber.d( "listVaults success!" );
			onVaultListSuccess( vaultMap );
		}

		@Override
		public
		void onError ( final Throwable e )
		{
			if (e instanceof HttpException )
			{
				HttpException httpException = (HttpException) e;
				if( httpException.code() == 403 )
				{
					// not authenticated...

					Snackbar.make( mRecyclerView, "Not authenticated", Snackbar.LENGTH_LONG ).show();
					getActivity().finish();
				}
			}
			else
			{
				Timber.e( e, "Unexpected error?!" );
			}
		}
	}
}
