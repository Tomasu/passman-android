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

package es.wolfi.app.passman.ui.credential;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.bierbaumer.otp_authenticator.TOTPHelper;

import org.apache.commons.codec.binary.Base32;

import javax.inject.Inject;

import es.wolfi.app.passman.DataStore;
import es.wolfi.app.passman.databinding.FragmentCredentialDisplayBinding;
import es.wolfi.app.passman.ui.BaseFragment;
import es.wolfi.passman.API.Credential;
import es.wolfi.passman.API.Vault;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CredentialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public
class CredentialFragment extends BaseFragment
{
	public static final String FRAG_TAG = "CREDENTIAL_ITEM";

	public static String CREDENTIAL = "credential_guid";

	private
	FragmentCredentialDisplayBinding mBinding;

	@Inject
	DataStore mDataStore;

	private Credential credential;
	private Handler handler;
	private Runnable otp_refresh;

	private OnCredentialFragmentInteraction mListener;

	public
	CredentialFragment ()
	{
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param credentialGUID The guid of the credential to display.
	 * @return A new instance of fragment CredentialFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static
	CredentialFragment newInstance ( @NonNull String credentialGUID )
	{
		CredentialFragment fragment = new CredentialFragment();

		Bundle b = new Bundle();
		b.putString( CREDENTIAL, checkNotNull(credentialGUID, "Null guid?!") );
		fragment.setArguments( b );

		return fragment;
	}

	@Override
	public
	void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		if ( getArguments() != null )
		{
			Vault activeVault = mDataStore.getActiveVault();
			String guid = checkNotNull( getArguments().getString( CREDENTIAL ), "Null guid?!");

			credential = activeVault.findCredentialByGUID( guid );
			Timber.d( "credential: %s", credential );
		}

		handler = new Handler();
		otp_refresh = new Runnable()
		{
			@Override
			public
			void run ()
			{
				int progress = (int) ( System.currentTimeMillis() / 1000 ) % 30;
				mBinding.credentialOtpProgress.setProgress( progress * 100 );

				ObjectAnimator animation = ObjectAnimator.ofInt( mBinding.credentialOtpProgress, "progress",
																				 ( progress + 1 ) * 100 );
				animation.setDuration( 1000 );
				animation.setInterpolator( new LinearInterpolator() );
				animation.start();

				mBinding.credentialOtp.setText( TOTPHelper.generate( new Base32().decode( credential.getOtp() ) ) );
				handler.postDelayed( this, 1000 );
			}
		};
	}

	@Override
	public
	void onResume ()
	{
		super.onResume();
		handler.post( otp_refresh );
	}

	@Override
	public
	void onPause ()
	{
		super.onPause();
		handler.removeCallbacks( otp_refresh );
	}

	@Override
	public
	View onCreateView (
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// Inflate the layout for this fragment
		mBinding = FragmentCredentialDisplayBinding.inflate( inflater, container, false );
		return mBinding.getRoot();
	}

	@Override
	public
	void onAttach ( Context context )
	{
		super.onAttach( context );
	}

	@Override
	public
	void onViewCreated ( View view, @Nullable Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );

		mBinding.credentialLabel.setText( credential.getLabel() );
		mBinding.credentialUser.setText( credential.getUsername() );
		mBinding.credentialPassword.setModePassword();
		mBinding.credentialPassword.setText( credential.getPassword() );
		mBinding.credentialEmail.setModeEmail();
		mBinding.credentialEmail.setText( credential.getEmail() );
		mBinding.credentialUrl.setText( credential.getUrl() );
		mBinding.credentialDescription.setText( credential.getDescription() );
		mBinding.credentialOtp.setEnabled( false );
	}

	@Override
	public
	void onDetach ()
	{
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public
	interface OnCredentialFragmentInteraction
	{
		// TODO: Update argument type and name
		void onCredentialFragmentInteraction ( Credential credential );
	}
}
