package es.wolfi.app.passman.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import es.wolfi.app.passman.App;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
@Singleton
@Component(modules = {
		ApplicationModule.class, SingleTonModule.class, NetworkModule.class, FragmentModule.class
})
public
interface ApplicationComponent extends AndroidInjector< App >
{
//	@Override
//	void inject ( App instance );

	@Component.Builder
	abstract class Builder extends AndroidInjector.Builder<App> {
		@BindsInstance
		public abstract Builder application( Application app );

		@Override
		public
		abstract ApplicationComponent build ();
	}
}
