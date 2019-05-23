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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnCredentialListFragmentInteractionListener}
 * interface.
 */
public
class CredentialListFragment extends BaseFragment implements OnCredentialListFragmentInteractionListener
{
	public static final String FRAG_TAG = "CREDENTIAL_LIST_FRAGMENT";

	// TODO: Customize parameter argument names
	private static final String ARG_COLUMN_COUNT = "column-count";
	// TODO: Customize parameters
	private int mColumnCount = 1;
	private AsyncTask filterTask = null;

	private Vault mVault = null;

	@Inject
	DataStore mDataStore;

	@Inject
	PassmanApi mApi;

	private RecyclerView mRecyclerView;

	private CompositeDisposable mDisposable = new CompositeDisposable();

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
		View view = inflater.inflate( R.layout.fragment_credential_item_list, container, false );

		// Set the adapter
		View credentialView = view.findViewById( R.id.list );
		if ( credentialView instanceof RecyclerView )
		{
			Context context = credentialView.getContext();

			mRecyclerView = (RecyclerView) credentialView;
			if ( mColumnCount <= 1 )
			{
				mRecyclerView.setLayoutManager( new LinearLayoutManager( context ) );
			}
			else
			{
				mRecyclerView.setLayoutManager( new GridLayoutManager( context, mColumnCount ) );
			}

			mVault = mDataStore.getActiveVault();
			if (mVault == null)
			{
				Snackbar.make( container, "No active vault?!", Snackbar.LENGTH_LONG ).show();
				getActivity().getSupportFragmentManager().popBackStack();
				return view;
			}

			final EditText searchInput = (EditText) view.findViewById( R.id.search_input );

			searchInput.addTextChangedListener( new TextWatcher()
			{
				@Override
				public
				void beforeTextChanged ( CharSequence charSequence, int i, int i1, int i2 )
				{

				}

				@Override
				public
				void onTextChanged ( CharSequence charSequence, int i, int i1, int i2 )
				{
					String searchText = searchInput.getText().toString().toLowerCase();
					if ( filterTask != null )
					{
						filterTask.cancel( true );
					}
					filterTask = new FilterListAsyncTask( searchText, mRecyclerView, CredentialListFragment.this );
					ArrayList< Credential > input[] = new ArrayList[] { mVault.getCredentials() };
					filterTask.execute( (Object[]) input );
				}

				@Override
				public
				void afterTextChanged ( Editable editable )
				{

				}
			} );

			mRecyclerView.setAdapter( new CredentialViewAdapter( mVault.getCredentials(), this ) );
		}

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

		if ( mVault != null )
		{
			List< Credential > credentials = mVault.getCredentials();
			//Timber.d( "credentials: %s", credentials );

			if ( credentials == null || credentials.size() < 1 )
			{
				Timber.d( "credentials null or empty?!" );

				Single< Vault > getVaultObservable = mApi.getVault( mVault.guid )
						.observeOn( AndroidSchedulers.mainThread() );

				mDisposable.add(
						getVaultObservable.subscribeWith( new VaultDisposableSingleObserver() ) );
			}
			else
			{
//				Credential secondCred = mVault.getCredentials().get( 1 );
//				Timber.d( "second cred email: %s", secondCred.getEmail() );
			}
		}
		else
		{
			Snackbar.make( mRecyclerView, "Active vault not set?!", Snackbar.LENGTH_LONG )
					.show();
			getActivity().getSupportFragmentManager().popBackStack();
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
//		Timber.d( "selected item: %s", item.getGuid() );
		CredentialFragment credentialFragment = CredentialFragment.newInstance( item.getGuid() );

		getActivity().getSupportFragmentManager()
				.beginTransaction()
				.addToBackStack( null )
				.replace( R.id.fragment_container, credentialFragment, CredentialFragment.FRAG_TAG)
				.commit();
	}

	private
	class VaultDisposableSingleObserver extends DisposableSingleObserver< Vault >
	{
		@Override
		public
		void onSuccess ( final Vault vault )
		{
			mDataStore.putVault( vault.guid, vault );
			mDataStore.setActiveVault( vault );
			mVault = vault;

			if (!vault.unlock( mDataStore.getVaultPassword( vault ) ))
			{
				Timber.e( "failed to (re)unlock vault?!?!" );
			}
			else
			{
				Timber.d( "vault re-unlocked!" );
			}

//			Credential secondCred = mVault.getCredentials().get( 1 );
//			Timber.d( "second cred email: %s", secondCred.getEmail() );

			mRecyclerView.setAdapter( new CredentialViewAdapter( mVault.getCredentials(), CredentialListFragment.this ) );
		}

		@Override
		public
		void onError ( final Throwable e )
		{
			Timber.w( e, "getVault request failed!" );

			if ( e instanceof HttpException )
			{
				HttpException httpException = (HttpException) e;

				if (httpException.code() == 403)
				{
					Snackbar.make( mRecyclerView, "Not authenticated!", Snackbar.LENGTH_LONG )
							.show();
				}
				else
				{
					Snackbar.make( mRecyclerView, "Request failed: " + httpException.message(), Snackbar.LENGTH_LONG )
							.show();
				}
			}
			else
			{
				Snackbar.make( mRecyclerView, "unexpected error: " + e.getMessage(), Snackbar.LENGTH_LONG )
						.show();
			}

			getActivity().getSupportFragmentManager().popBackStack();
		}
	}
}
