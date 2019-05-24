package es.wolfi.app.passman.ui;

import android.content.Context;

import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import es.wolfi.app.passman.SingleTon;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public
abstract class BaseFragment extends Fragment
{
	@Inject
	SingleTon mTon;

	@Override
	public
	void onAttach ( final Context context )
	{
		AndroidSupportInjection.inject( this );
		super.onAttach( context );
	}

	public
	SingleTon getTon()
	{
		return mTon;
	}
}
