package es.wolfi.app.passman.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.SavedStateVMFactory;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import javax.inject.Inject;

import es.wolfi.app.passman.DataStore;
import es.wolfi.app.passman.R;
import es.wolfi.app.passman.databinding.FragmentLoginBasicBinding;
import es.wolfi.app.passman.ui.BaseFragment;
import es.wolfi.passman.API.PassmanApi;
import es.wolfi.passman.API.Vault;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import retrofit2.HttpException;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public
class LoginBasicFragment extends BaseFragment
{

	private LoginBasicViewModel mViewModel;

	private FragmentLoginBasicBinding mBinding;

	private CompositeDisposable mDisposable = new CompositeDisposable();

	@Inject
	DataStore mDataStore;

	@Inject
	PassmanApi mApi;

	public static
	LoginBasicFragment newInstance ()
	{
		return new LoginBasicFragment();
	}

	@Override
	public
	View onCreateView (
			@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState )
	{
		mBinding = FragmentLoginBasicBinding.inflate( inflater, container, false );

		mBinding.loginButton.setOnClickListener( v -> onLoginButtonPressed() );

		mBinding.loginHostInput.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			@Override
			public
			void onFocusChange ( final View v, final boolean hasFocus )
			{
				if ( !hasFocus )
				{
					// TODO: actually check host here
					Timber.d( "host view lost focus, check host!" );
				}
			}
		} );

		getLifecycle().addObserver( new LifecycleObserver() );

		return mBinding.getRoot();
	}

	@Override
	public
	void onViewCreated ( @NonNull final View view, @Nullable final Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );

		if ( getArguments() != null )
		{
			String hostname = getArguments().getString( "hostname" );
			//LoginBasicFragmentArgs.fromBundle( getArguments() ).getHostname();
			//mViewModel.setHost( hostname );
			mBinding.loginHostInput.setText( hostname );
			Timber.d( "hostname: %s", hostname );
		}

	}

	@Override
	public
	void onActivityCreated ( @Nullable Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );

		mViewModel = ViewModelProviders.of( this, new SavedStateVMFactory( this ) )
				.get( LoginBasicViewModel.class );

		mBinding.setViewModel( mViewModel );

		if ( getArguments() != null )
		{
			String hostname = getArguments().getString( "hostname" );
			//LoginBasicFragmentArgs.fromBundle( getArguments() ).getHostname();
			//mViewModel.setHost( hostname );
			mBinding.loginHostInput.setText( hostname );
			Timber.d( "hostname: %s", hostname );
			mViewModel.setHost( hostname );
		}
		// TODO: Use the ViewModel
	}

	private
	String getHostName ()
	{
		return mBinding.loginHostInput.getText().toString();
	}

	private
	String getUsername ()
	{
		return mBinding.loginUserInput.getText().toString();
	}

	private
	String getPassword ()
	{
		return mBinding.loginPassInput.getText().toString();
	}

	private
	boolean checkInputState ( TextInputLayout layout, boolean state, String text )
	{
		if ( !state )
		{
			layout.setError( text );
			layout.setErrorEnabled( true );
			return false;
		}
		else
		{
			layout.setErrorEnabled( false );
			layout.setError( "" );
			return true;
		}
	}

	private
	void onLoginButtonPressed ()
	{
		String hostname = getHostName();

		mBinding.loginBasicErrorText.setVisibility( View.INVISIBLE );

		if ( !checkInputState( mBinding.loginHostInputLayout, hostname.length() > 0, "Required" ) )
		{
			Timber.d( "host invalid" );
			return;
		}

		if ( !checkInputState( mBinding.loginUserInputLayout, getUsername().length() > 0,
									 "Required" ) )
		{
			Timber.d( "user invalid" );
			return;
		}

		if ( !checkInputState( mBinding.loginPassInputLayout, getPassword().length() > 0,
									 "Required" ) )
		{
			Timber.d( "pass invalid" );
			return;
		}

		mBinding.loginButton.showLoading();

		mApi.setHost( getHostName() );
		mApi.setBasicCreds( getUsername(), getPassword() );

		ListVaultsObserver observer = mApi.listVaults()
				.observeOn( AndroidSchedulers.mainThread() )
				.subscribeWith( new ListVaultsObserver( this ) );

		mDisposable.add( observer );
	}

	private
	void onLoginSuccess ( final List< Vault > vaultList )
	{
		Timber.d( "got vaults back! valid auth!" );

		mBinding.loginButton.showLoading();

		// cache vaults we get here, so we don't have to re-fetch them.
		// can't think of a better api call to make to check if we have valid
		// credentials
		mDataStore.putVaults( vaultList );
		mDataStore.setBasicAuth( getHostName(), getUsername(), getPassword() );

		navigateTo( R.id.nav_loginBasic_to_vlist );
	}

	public
	void onLoginError ( final Throwable e )
	{
		Timber.d( "LOGIN ERROR: %s", e.getMessage() );
		Timber.w( e );

		int messageId = R.string.login_unknownerror;
		if (e instanceof HttpException)
		{
			HttpException httpException = (HttpException) e;
			int code = httpException.code();
			switch ( code )
			{
				case 401:
					messageId = R.string.login_unauthorized;
					break;

				case 500:
					messageId = R.string.login_servererror;
					break;
			}
		}

		mBinding.loginButton.hideLoading();

		mBinding.loginBasicErrorText.setText( messageId );

		// Not sure we want this error text here...
		//mBinding.loginBasicErrorText.setVisibility( View.VISIBLE );

		Snackbar.make( mBinding.getRoot(), "Login failed?!", Snackbar.LENGTH_LONG )
				.show();
	}

	private static
	class ListVaultsObserver extends DisposableSingleObserver< List< Vault > >
	{
		private LoginBasicFragment mFragment;

		public
		ListVaultsObserver ( @NonNull LoginBasicFragment fragment )
		{
			mFragment = checkNotNull( fragment, "Null fragment?!" );
		}

		@Override
		public
		void onSuccess ( final List< Vault > vaultList )
		{
			mFragment.onLoginSuccess( vaultList );
		}

		@Override
		public
		void onError ( final Throwable e )
		{
			mFragment.onLoginError( e );
		}
	}

	private static
	class LifecycleObserver implements DefaultLifecycleObserver
	{
		private LoginBasicFragment mFragment = null;

		@Override
		public
		void onCreate ( @NonNull final LifecycleOwner owner )
		{
			Timber.d( "onCreate!" );
			mFragment = (LoginBasicFragment) owner;
		}

		@Override
		public
		void onDestroy ( @NonNull final LifecycleOwner owner )
		{
			Timber.d( "onDestroy" );
			mFragment = null;
		}

		@Override
		public
		void onResume ( @NonNull final LifecycleOwner owner )
		{
			Timber.d( "onResume!" );
		}

		@Override
		public
		void onPause ( @NonNull final LifecycleOwner owner )
		{
			Timber.d( "onPause" );
			mFragment.mDisposable.clear();
		}
	}
}
