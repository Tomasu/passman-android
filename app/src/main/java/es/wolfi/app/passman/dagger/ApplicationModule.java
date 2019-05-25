package es.wolfi.app.passman.dagger;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import es.wolfi.app.passman.DataStore;
import es.wolfi.app.passman.SplashActivity;
import es.wolfi.app.passman.ui.MainActivity;
import es.wolfi.app.passman.ui.login.LoginActivity;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
@Module ( includes = AndroidSupportInjectionModule.class )
public abstract
class ApplicationModule
{
	@ContributesAndroidInjector
	abstract
	SplashActivity contributeSplashInjector();

	@ContributesAndroidInjector
	abstract
	LoginActivity contributeLoginInjector();

	@ContributesAndroidInjector
	abstract
	MainActivity contributePasswordListInjector ();

	@Provides
	@Singleton
	static Context provideApplicationContext( Application application )
	{
		return application.getBaseContext();
	}

//	@Binds
//	abstract Application bindsApplication(App application);

	@Provides
	@Singleton
	static
	SharedPreferences provideSharedPreferences( @GlobalContext Context context )
	{
		return PreferenceManager.getDefaultSharedPreferences( context );
	}

	@Provides
	@Singleton
	static
	AccountManager provideAccountManager( @GlobalContext Context context )
	{
		return AccountManager.get( context );
	}

//	@Provides
//	@Singleton
//	static
//	DNSSD provideDNSSD( @GlobalContext Context context )
//	{
//		return new DNSSDEmbedded( context );
//	}

	@Provides
	@Singleton
	static
	DataStore provideDataStore ( @NonNull Gson gson, @NonNull SharedPreferences preferences)
	{
		return new DataStore( gson, preferences );
	}
}
