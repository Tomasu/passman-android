package es.wolfi.app.passman.ui;

import android.app.SearchManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import javax.inject.Inject;

import es.wolfi.app.passman.R;
import timber.log.Timber;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public abstract
class SearchableFragment extends BaseFragment
{
	@Inject
	SearchManager mSearchManager;

	private
	SearchView mSearchView = null;

	private
	SearchView.OnQueryTextListener mQueryTextListener = null;

	private
	boolean mIsSearchOpen = false;

	private
	String mQueryString = null;

	protected
	boolean isSearchOpen ()
	{
		return mIsSearchOpen;
	}

	protected
	String getQueryString ()
	{
		return mQueryString;
	}

	@Override
	public
	void onCreate ( @Nullable final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setHasOptionsMenu( true );
	}

	protected abstract
	void onSearchTextSubmit(final String query);

	protected abstract
	void onSearchTextChange ( final String query );

	@Override
	public abstract
	void onCreateOptionsMenu ( @NonNull final Menu menu, @NonNull final MenuInflater inflater );

	@Override
	public
	boolean onOptionsItemSelected ( @NonNull final MenuItem item )
	{
		if ( item.getItemId() == R.id.action_search )
		{
			mSearchView.setIconified( false );
			return true;
		}

		return super.onOptionsItemSelected( item );
	}

	protected
	void setupMenu ( @NonNull final Menu menu, @NonNull final MenuInflater inflater, @MenuRes int id )
	{
		inflater.inflate( id, menu );

		MenuItem searchItem = menu.findItem( R.id.action_search );

		if (searchItem != null)
		{
			mSearchView = (SearchView) searchItem.getActionView();
		}

		if (mSearchView != null)
		{
			mSearchView.setSearchableInfo( mSearchManager.getSearchableInfo( getActivity().getComponentName() ) );

			mQueryTextListener = new SearchView.OnQueryTextListener() {
				@Override
				public
				boolean onQueryTextSubmit ( final String query )
				{
					Timber.d("search submit: %s", query);

					// keep text in search view
					mSearchView.setQuery( query, false );

					mQueryString = query;
					onSearchTextSubmit( query );

					return true;
				}

				@Override
				public
				boolean onQueryTextChange ( final String newText )
				{
					Timber.d( "search text change: %s", newText );
					mQueryString = newText;

					onSearchTextChange( newText );
					return true;
				}
			};

			mSearchView.setOnQueryTextListener( mQueryTextListener );
			mSearchView.setOnSearchClickListener( v -> mIsSearchOpen = true );

			mSearchView.setOnCloseListener( () ->
													  {
														  mIsSearchOpen = false;
														  return true;
													  } );
		}

		super.onCreateOptionsMenu( menu, inflater );
	}
}
