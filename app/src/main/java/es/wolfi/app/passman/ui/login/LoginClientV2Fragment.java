package es.wolfi.app.passman.ui.login;

import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.wolfi.app.passman.databinding.FragmentLoginClientV2Binding;
import timber.log.Timber;

public
class LoginClientV2Fragment extends Fragment
{

	private LoginClientV2ViewModel mViewModel;

	private FragmentLoginClientV2Binding mBinding;

	public static
	LoginClientV2Fragment newInstance ()
	{
		return new LoginClientV2Fragment();
	}

	@Override
	public
	View onCreateView (
			@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState )
	{
		mBinding = FragmentLoginClientV2Binding.inflate( inflater, container, false );

		WebView webView = mBinding.loginClientWebview;

		webView.setWebViewClient( new LoginWebViewClient() );
		webView.getSettings().setJavaScriptEnabled( true );

		Map<String, String> headers = new HashMap<>();
		headers.put( "OCS-APIREQUEST", "true" );
		webView.loadUrl( "https://cloud.tomasu.org/index.php/login/flow", headers );
		return mBinding.getRoot();
	}

	@Override
	public
	void onActivityCreated ( @Nullable Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );
		mViewModel = ViewModelProviders.of( this ).get( LoginClientV2ViewModel.class );
		// TODO: Use the ViewModel
	}

	private
	void onLoginSuccess()
	{

	}

	private
	class LoginWebViewClient extends WebViewClient
	{
		@Override
		public
		void onPageStarted ( final WebView view, final String url, final Bitmap favicon )
		{
			Timber.d( "started: %s", url );
			super.onPageStarted( view, url, favicon );
		}

		@Override
		public
		void onPageFinished ( final WebView view, final String url )
		{
			Timber.d( "finished: %s", url );
			super.onPageFinished( view, url );
		}

		@Override
		public
		void onReceivedError (
				final WebView view, final WebResourceRequest request, final WebResourceError error )
		{
			Timber.d( "onRecievedError: %s %s", request, error );
			super.onReceivedError( view, request, error );
		}

		@Override
		public
		void onReceivedSslError (
				final WebView view, final SslErrorHandler handler, final SslError error )
		{
			Timber.d( "onReceivedSslError: %s", error );
			super.onReceivedSslError( view, handler, error );
		}

		@Override
		public
		boolean shouldOverrideUrlLoading ( final WebView view, final String url )
		{
			Timber.d( "shouldOverrideUrlLoading: %s", url );
			Uri uri = Uri.parse( url );

			if (uri.getScheme().contentEquals( "nc" ))
			{
				List< String > pathSegments = uri.getPathSegments();
				Timber.d( "GOT NC URL! seg0: %s seg1: %s", pathSegments.get( 0 ), pathSegments.get( 1 ) );

				onLoginSuccess();

				return true;
			}

			return false;
		}

		@Nullable
		@Override
		public
		WebResourceResponse shouldInterceptRequest (
				final WebView view, final String url )
		{
			Uri uri = Uri.parse( url );
			if (uri.getScheme().contentEquals( "nc" ))
			{
				Timber.d( "got nc url!" );
				return new WebResourceResponse( "text/plain", "utf8", null);
			}
			return super.shouldInterceptRequest( view, url );
		}
	}
}
