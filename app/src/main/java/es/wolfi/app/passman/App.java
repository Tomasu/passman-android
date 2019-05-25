package es.wolfi.app.passman;

import android.app.Activity;
import android.app.Application;

import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.support.HasSupportFragmentInjector;
import es.wolfi.app.passman.dagger.DaggerApplicationComponent;
import es.wolfi.passman.API.PassmanApi;
import timber.log.Timber;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public
class App extends Application implements HasActivityInjector, HasSupportFragmentInjector
{
	@Inject
	DispatchingAndroidInjector< Activity > mActivityDispatchingAndroidInjector;

	@Inject
	DispatchingAndroidInjector< Fragment > mFragmentDispatchingAndroidInjector;

	@Inject
	DataStore mDataStore;

	@Inject
	PassmanApi mApi;

	@Override
	public
	void onCreate ()
	{
		if ( BuildConfig.DEBUG )
		{
			Timber.plant( new Timber.DebugTree() );
		}
		else
		{
			//Timber.plant( new CrashReportingTree() );
		}

		DaggerApplicationComponent.builder().application( this ).create( this ).inject( this );

		super.onCreate();

		if (mDataStore.haveHost())
		{
			Timber.d( "have host! setup stuff!" );
			mApi.setHost( mDataStore.getHost() );
			if (mDataStore.isTokenAuth())
			{
				mApi.setUserName( mDataStore.getUserName() );
				mApi.setAuthToken( mDataStore.getAuthToken() );
			}
			else if (mDataStore.isBasicAuth())
			{
				mApi.setBasicCreds( mDataStore.getUserName(), mDataStore.getPassword() );
			}
		}
	}

	@Override
	public
	AndroidInjector< Activity > activityInjector ()
	{
		return mActivityDispatchingAndroidInjector;
	}

	@Override
	public
	AndroidInjector< Fragment > supportFragmentInjector ()
	{
		return mFragmentDispatchingAndroidInjector;
	}

}
