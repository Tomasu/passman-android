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

package es.wolfi.app.passman.ui.vault;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import es.wolfi.app.passman.DataStore;
import es.wolfi.app.passman.R;
import es.wolfi.app.passman.databinding.FragmentVaultLockScreenBinding;
import es.wolfi.app.passman.ui.BaseFragment;
import es.wolfi.passman.API.Vault;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VaultUnlockFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public
class VaultUnlockFragment extends BaseFragment
{
	public static final String FRAG_TAG = "VAULT_UNLOCK_FRAGMENT";

	private Vault vault;

	@Inject
	DataStore mDataStore;

	private
	FragmentVaultLockScreenBinding mBinding;

	private Handler mHandler;

	public
	VaultUnlockFragment ()
	{
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param vault The vault
	 * @return A new instance of fragment VaultUnlockFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static
	VaultUnlockFragment newInstance ( Vault vault )
	{
		VaultUnlockFragment fragment = new VaultUnlockFragment();
		fragment.vault = vault;
		return fragment;
	}

	@Override
	public
	void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mHandler = new Handler();
	}

	@Override
	public
	View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		mBinding = FragmentVaultLockScreenBinding.inflate( inflater, container, false );
		return mBinding.getRoot();
	}

	@Override
	public
	void onViewCreated ( View view, @Nullable Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );
		ButterKnife.bind( this, view );
		vault = mDataStore.getActiveVault();
		Timber.d( "Vault guid: %s", vault.guid );
		mBinding.fragmentVaultName.setText( vault.name );

		mHandler.post( new Runnable() {
			@Override
			public
			void run ()
			{
				mBinding.fragmentVaultPassword.clearFocus();
				mBinding.fragmentVaultPassword.requestFocus();
				InputMethodManager im = (InputMethodManager) getActivity().getSystemService( Context.INPUT_METHOD_SERVICE );
				im.toggleSoftInput( InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY );
			}
		} );

	}

	@Override
	public
	void onDetach ()
	{
		super.onDetach();
	}

	@OnClick (R.id.fragment_vault_unlock)
	void onBtnUnlockClick ()
	{
		String password = mBinding.fragmentVaultPassword.getText().toString();
		Timber.d( "onUnlockClick: %s", password );

		if ( vault.unlock( password ) )
		{
			Timber.d( "successfully unlocked vault" );

			if ( mBinding.vaultLockScreenChkSavePw.isChecked() )
			{
				Timber.d( "save vault password" );
				mDataStore.putVaultPassword( vault, password );
			}

			Bundle args = new Bundle();
			args.putString( "vault_name", vault.name );
			navigateTo( R.id.nav_unlock_to_credential_list, args );

			return;
		}

		Snackbar.make( mBinding.getRoot(), getString( R.string.wrong_vault_pw ), Snackbar.LENGTH_LONG )
				//.setAction( "Action", null )
				.show();
	}
}
