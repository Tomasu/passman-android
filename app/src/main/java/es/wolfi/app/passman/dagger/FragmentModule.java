package es.wolfi.app.passman.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import es.wolfi.app.passman.ui.credential.CredentialFragment;
import es.wolfi.app.passman.ui.login.LoginBasicFragment;
import es.wolfi.app.passman.ui.login.LoginClientV2Fragment;
import es.wolfi.app.passman.ui.login.LoginFragment;
import es.wolfi.app.passman.ui.vault.CredentialListFragment;
import es.wolfi.app.passman.ui.vault.VaultUnlockFragment;
import es.wolfi.app.passman.ui.vaultlist.VaultListFragment;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
@Module
public abstract
class FragmentModule
{
	@ContributesAndroidInjector
	abstract
	LoginFragment contributeLoginFragment ();

	@ContributesAndroidInjector
	abstract
	LoginBasicFragment contributeLoginBasicFragment ();

	@ContributesAndroidInjector
	abstract
	LoginClientV2Fragment contributeLoginClientFragment ();

	@ContributesAndroidInjector
	abstract
	VaultListFragment contrubuteVaultListFragment ();

	@ContributesAndroidInjector
	abstract
	VaultUnlockFragment contributeVaultListFragment ();

	@ContributesAndroidInjector
	abstract
	CredentialFragment contributeCredentialDisplayFragment ();

	@ContributesAndroidInjector
	abstract
	CredentialListFragment contributeCredentialItemFragment ();
}
