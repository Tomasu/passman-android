package es.wolfi.app.passman.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import es.wolfi.app.passman.CredentialFragment;
import es.wolfi.app.passman.CredentialListFragment;
import es.wolfi.app.passman.VaultListFragment;
import es.wolfi.app.passman.VaultUnlockFragment;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
@Module
public abstract
class FragmentModule
{
	@Provides
	@IntoMap
	@ClassKey (CredentialFragment.class)
	static
	CredentialFragment provideCredentialDisplay ()
	{
		return new CredentialFragment();
	}

	@Provides
	@IntoMap
	@ClassKey (CredentialListFragment.class)
	static
	CredentialListFragment provideCredentialItemFragment ()
	{
		return new CredentialListFragment();
	}

	@ContributesAndroidInjector
	abstract
	CredentialFragment contributeCredentialDisplayFragment ();

	@ContributesAndroidInjector
	abstract
	CredentialListFragment contributeCredentialItemFragment ();

	@ContributesAndroidInjector
	abstract
	VaultListFragment contrubuteVaultListFragment();

	@ContributesAndroidInjector
	abstract
	VaultUnlockFragment contributeVaultListFragment();
}
