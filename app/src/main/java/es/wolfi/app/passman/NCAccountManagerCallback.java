package es.wolfi.app.passman;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import es.wolfi.app.passman.ui.MainActivity;
import es.wolfi.app.passman.ui.login.LoginActivity;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public class NCAccountManagerCallback implements AccountManagerCallback< Bundle >
{
	private LoginActivity mActivity;

	@Inject
	public
	NCAccountManagerCallback ( @NonNull LoginActivity activity )
	{
		mActivity = checkNotNull( activity, "Null context?!" );
	}

	@Override
	public
	void run (
			final AccountManagerFuture< Bundle > future )
	{
		Timber.d( "in account manager callback!" );
		try
		{
			Bundle results = future.getResult();

			Timber.d( "got auth token: %s", results.toString() );

			String authUser = results.getString( "authAccount", "" );
			int atIndex = authUser.indexOf( '@' );

			String username = authUser.substring( 0, atIndex );
			String host = "https://" + authUser.substring( atIndex + 1, authUser.length() );

			String authtoken = results.getString( "authtoken" );
			if ( authtoken == null )
			{
				Timber.e( "failed to get an auth token?!" );
				mActivity.onAuthenticationFailed( null, "authtoken missing" );
				return;
			}

			mActivity.onAuthenticated( host, username, authtoken );

			MainActivity.launch( mActivity );
			mActivity.finish();
		}
		catch ( OperationCanceledException | IOException | AuthenticatorException e )
		{
			e.printStackTrace();
			mActivity.onAuthenticationFailed( e, "unexpected error" );
		}
	}
}
