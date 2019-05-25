package es.wolfi.app.passman.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import dagger.android.support.AndroidSupportInjection;
import es.wolfi.app.passman.R;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public
abstract class BaseFragment extends Fragment
{
	@Override
	public
	void onAttach ( final Context context )
	{
		AndroidSupportInjection.inject( this );
		super.onAttach( context );
	}

	public
	NavController getNavController()
	{
		return Navigation.findNavController( requireActivity(), R.id.nav_host_fragment );
	}

	public
	void navigateTo ( @IdRes int id )
	{
		getNavController().navigate( id );
	}

	public
	void navigateTo ( @IdRes int id, Bundle args )
	{
		getNavController().navigate( id, args );
	}

}
