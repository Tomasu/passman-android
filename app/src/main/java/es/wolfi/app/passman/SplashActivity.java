package es.wolfi.app.passman;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import javax.inject.Inject;

import timber.log.Timber;

public
class SplashActivity extends BaseActivity
{
	private static final int REQUEST_GET_ACCOUNTS = 1;

	@Inject
	DataStore mDataStore;

	@Override
	protected
	void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		Timber.d( "onCreate" );

		if (mDataStore.haveHost())
		{
			Timber.d( "have host! launch password list!" );

			// go to PasswordList
			//
			PasswordList.launch( this );

			Timber.d( "finish!" );
			finish();
			return;
		}

		// go to login

		boolean haveReadContacts = ContextCompat.checkSelfPermission( this, android.Manifest.permission.READ_CONTACTS ) ==
				PackageManager.PERMISSION_GRANTED;
		boolean haveWriteContacts = ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_CONTACTS ) ==
				PackageManager.PERMISSION_GRANTED;
		boolean haveGetAccounts = ContextCompat.checkSelfPermission( this, android.Manifest.permission.GET_ACCOUNTS ) ==
				PackageManager.PERMISSION_GRANTED;

		Timber.d( "readContacts: %s, writeContacts: %s, getAccounts: %s", haveReadContacts, haveWriteContacts, haveGetAccounts );

		if  ( !haveReadContacts || !haveGetAccounts || !haveWriteContacts)
		{
			Timber.d( "don't have GET_ACCOUNTS | READ_CONTACTS | WRITE_CONTACTS permission" );

			if ( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.GET_ACCOUNTS ) )
			{
				// show explanation..
				Timber.d( "should show GET_ACCOUNTS request rationale!" );
			}

			if ( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.READ_CONTACTS ) )
			{
				// show explanation..
				Timber.d( "should show READ_CONTACTS request rationale!" );
			}

			if ( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.WRITE_CONTACTS ) )
			{
				// show explanation..
				Timber.d( "should show WRITE_CONTACTS request rationale!" );
			}


			Timber.d( "request GET_ACCOUNTS!" );
			ActivityCompat.requestPermissions( this, new String[] {
					android.Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS
			}, REQUEST_GET_ACCOUNTS );
		}
		else
		{
			Timber.d( "already have GET_ACCOUNTS permission" );
			doLogin();
		}
	}

	@Override
	public
	void onRequestPermissionsResult (
			final int requestCode, @NonNull final String[] permissions,
			@NonNull final int[] grantResults )
	{
		Timber.d( "onRequestPermissionsResult" );

		if ( requestCode != REQUEST_GET_ACCOUNTS )
		{
			Timber.w( "unsupported request code?!" );
			return;
		}

		for ( int i = 0; i < permissions.length; i++)
		{
			if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
			{
				Timber.w( "permission %s denied!", permissions[i]);
				return;
			}
		}

		doLogin();
	}

	private void doLogin()
	{
		Timber.d( "go to login!" );
		LoginActivity.launch( this, new LoginICallback() );
		finish();
	}

	private static
	class LoginICallback implements ICallback
	{
		@Override
		public
		void onTaskFinished ()
		{
			// nada
		}
	}
}
