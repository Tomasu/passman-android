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

package es.wolfi.utils;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;

public
class FilterListAsyncTask< T extends Filterable >
		extends AsyncTask< List< T >, Integer, List< T > > implements LifecycleEventObserver
{

	private String filter;
	private OnListFilteredListener<T> mListFilteredListener = null;

	public
	FilterListAsyncTask ( String filter, OnListFilteredListener< T > mListener )
	{
		this.filter = filter;
		this.mListFilteredListener = mListener;
	}

	@SafeVarargs
	@Override
	protected final
	List< T > doInBackground ( List< T >... list )
	{
		return ListUtils.filterList( filter, list[ 0 ] );
	}

	@Override
	protected
	void onPostExecute ( List< T > filteredList )
	{
		if ( mListFilteredListener != null)
		{
			mListFilteredListener.onListFiltered( filteredList );
		}
	}

	@Override
	public
	void onStateChanged (
			@NonNull final LifecycleOwner source, @NonNull final Lifecycle.Event event )
	{
		// forget our listener on certain lifecycle events
		switch ( event )
		{
			//case ON_PAUSE:
			case ON_STOP:
			case ON_DESTROY:
				mListFilteredListener = null;
				break;
		}
	}

	public
	interface OnListFilteredListener< T >
	{
		void onListFiltered ( @NonNull List< T > filteredList );
	}
}
