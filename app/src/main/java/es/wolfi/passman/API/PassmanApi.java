package es.wolfi.passman.API;

import androidx.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import es.wolfi.app.passman.network.RequestInterceptor;
import io.reactivex.Single;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public
class PassmanApi
{
	private
	PassmanService mService;

	private
	RequestInterceptor mRequestInterceptor;

	private
	String mUserName;

	@Inject
	public
	PassmanApi (@NonNull PassmanService passmanService, @NonNull RequestInterceptor requestInterceptor)
	{
		mService = checkNotNull(passmanService, "Null service?!");
		mRequestInterceptor = checkNotNull( requestInterceptor, "Null requestinterceptor?!" );
	}

	public
	void setUserName ( final String userName )
	{
		mUserName = userName;
	}

	public
	void setHost ( @NonNull String host )
	{
		Timber.d("set host! %s", host);
		mRequestInterceptor.setHost( host + "/index.php/apps/passman/api/v2/");
	}

	public
	void setAuthToken( @NonNull final String authToken )
	{
		mRequestInterceptor.setToken( authToken );
	}

	public
	Single< List< Vault > > listVaults ()
	{
		return mService.listVaults();
	}

	public
	Single< Vault > getVault ( @NonNull String guid )
	{
		return mService.getVault( guid );
	}

}
