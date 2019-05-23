package es.wolfi.app.passman.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import es.wolfi.app.passman.SingleTon;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
@Module
public
class SingleTonModule
{
	@Provides
	@Singleton
	static
	SingleTon provideSingleTon()
	{
		return new SingleTon();
	}
}
