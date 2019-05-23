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

package es.wolfi.app.passman;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

	@BindView (R.id.fragment_vault_name)
	TextView vault_name;
	@BindView (R.id.fragment_vault_password)
	EditText vault_password;
	@BindView (R.id.fragment_vault_unlock)
	Button btn_unlock;
	@BindView (R.id.vault_lock_screen_chk_save_pw)
	CheckBox chk_save;

	@Inject
	DataStore mDataStore;

	private
	RelativeLayout mRelativeLayout = null;

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
	}

	@Override
	public
	View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// Inflate the layout for this fragment
		mRelativeLayout = (RelativeLayout) inflater.inflate( R.layout.fragment_vault_lock_screen, container, false );
		return mRelativeLayout;
	}

	@Override
	public
	void onViewCreated ( View view, @Nullable Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );
		ButterKnife.bind( this, view );
		vault = mDataStore.getActiveVault();
		Timber.d( "Vault guid: %s", vault.guid );
		vault_name.setText( vault.name );
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
		String password = vault_password.getText().toString();
		if ( vault.unlock( password ) )
		{
			if ( chk_save.isChecked() )
			{
				mDataStore.putVaultPassword( vault, vault_password.getText().toString() );
			}

			CredentialListFragment fragment = new CredentialListFragment();
			getActivity().getSupportFragmentManager()
					.beginTransaction()
					.replace( R.id.fragment_container, fragment, CredentialListFragment.FRAG_TAG )
					.commit();
			return;
		}

		Snackbar.make( mRelativeLayout, getString( R.string.wrong_vault_pw ), Snackbar.LENGTH_LONG )
				//.setAction( "Action", null )
				.show();
	}
}
