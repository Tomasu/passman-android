package es.wolfi.app.passman.dagger;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import es.wolfi.app.passman.network.RequestInterceptor;
import es.wolfi.passman.API.PassmanApi;
import es.wolfi.passman.API.PassmanService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
@Module
public abstract
class NetworkModule
{
	@Provides
	@Singleton
	static PassmanService providePassmanService ( @NonNull Retrofit retrofit )
	{
		return retrofit.create( PassmanService.class );
	}

	@Provides
	@Singleton
	static Retrofit provideRetrofit (
			@NonNull OkHttpClient okHttpClient, @NonNull GsonConverterFactory gsonConverterFactory,
			@NonNull RxJava2CallAdapterFactory rxJava2CallAdapterFactory )
	{
		return new Retrofit.Builder()
				.baseUrl( "http://localhost" ) // this gets ignored, we setup a dynamic url in the okHttp interceptor
				.addConverterFactory( gsonConverterFactory )
				.addCallAdapterFactory( rxJava2CallAdapterFactory )
				.client( okHttpClient )
				.build();
	}

	@Provides
	@Singleton
	static GsonConverterFactory provideGsonConverterFactory ( Gson gson )
	{
		return GsonConverterFactory.create( gson );
	}

	@Provides
	@Singleton
	static RxJava2CallAdapterFactory provideRxJava2CallAdapterFactory ()
	{
		return RxJava2CallAdapterFactory.createAsync();
	}

	@Provides
	@Singleton
	static Gson provideGson ()
	{
		return new GsonBuilder().setLenient().create();
	}

	@Provides
	@Singleton
	static HttpLoggingInterceptor provideHttpLoggingInterceptor()
	{
		return new HttpLoggingInterceptor(
				new HttpLoggingInterceptor.Logger() {
					@Override
					public
					void log ( final String message )
					{
						Timber.tag( "OkHttp").d(message);
					}
				} ).setLevel( HttpLoggingInterceptor.Level.BODY );
	}
	@Provides
	@Singleton
	static OkHttpClient provideOkHttpClient ( @NonNull RequestInterceptor interceptor, @NonNull HttpLoggingInterceptor loggingInterceptor )
	{
		return new OkHttpClient.Builder()
				.addInterceptor( interceptor )
				.addInterceptor( loggingInterceptor )
				.build();
	}

	@Provides
	@Singleton
	static RequestInterceptor provideRequestInterceptor ()
	{
		return new RequestInterceptor();
	}

	@Provides
	@Singleton
	static PassmanApi providePassmanApi ( @NonNull PassmanService service, @NonNull RequestInterceptor requestInterceptor )
	{
		return new PassmanApi(service, requestInterceptor);
	}
}
