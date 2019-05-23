package es.wolfi.app.passman.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public final
class RequestInterceptor implements Interceptor
{
	private volatile String mToken;
	private volatile HttpUrl mBaseUrl;

	public
	void setHost ( @NonNull final String host )
	{
		//this.mHost = checkNotNull( host, "Null host?!" );
		this.mBaseUrl = HttpUrl.parse( host );
	}

	public
	void setToken ( @NonNull final String token )
	{
		mToken = "Bearer " + checkNotNull( token, "Null token?!" );
	}

	@Override
	public
	okhttp3.Response intercept ( Chain chain ) throws IOException
	{
		Request request = chain.request();
		if ( mBaseUrl != null )
		{
			HttpUrl newUrl = mBaseUrl.newBuilder().addEncodedPathSegments( request.url().encodedPath() ).build();
			//request.url().newBuilder().encodedPath( ).host( mBaseUrl.host() ).build();
			//HttpUrl newUrl = HttpUrl.parse( host ).( request.url().encodedPath() );
			request = request.newBuilder()
					.url( newUrl )
					.addHeader( "Authorization", mToken )
					.build();

			Timber.d( "request: %s : %s", request, request.url() );
		}

		Response response = chain.proceed( request );
		Timber.d( "response: %s", response );
		//Timber.d( "%s", response.peekBody( 1024 ).);

		return response;
	}
}
