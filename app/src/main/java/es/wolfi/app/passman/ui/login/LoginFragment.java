package es.wolfi.app.passman.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import es.wolfi.app.passman.R;
import es.wolfi.app.passman.databinding.FragmentLoginBinding;
import es.wolfi.app.passman.ui.BaseFragment;
import timber.log.Timber;

public
class LoginFragment extends BaseFragment
{

	private LoginViewModel mViewModel;

	private FragmentLoginBinding mBinding;
	//	private HostSpinnerAdapter mHostSpinnerAdapter;

	//	@Inject
	//	DNSSD mDNSSD;

	//	private DNSSDService mDNSSDService;
	//	private ResolveListener mDnssdResolveListener;

	public static
	LoginFragment newInstance ()
	{
		return new LoginFragment();
	}

	@Override
	public
	void onCreate ( @Nullable final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		//		mDnssdResolveListener = new DNSSDResolveListener();
	}

	@Override
	public
	View onCreateView (
			@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState )
	{
		mBinding = FragmentLoginBinding.inflate( inflater, container, false );

		//		mHostSpinnerAdapter = new HostSpinnerAdapter(requireContext());
		//		mBinding.loginHostSpinner.setAdapter( mHostSpinnerAdapter );



		//		mBinding.loginWithClient.setOnClickListener(
		//				Navigation.createNavigateOnClickListener( R.id.nav_login_to_loginClient, args
		//				) );

		// TODO: implement me!
		// mBinding.loginWithNcapp.setOnClickListener( Navigation.createNavigateOnClickListener(R.id
		// .nav_login_to_loginNCApp ) );

		//		browseDNSSD();

		return mBinding.getRoot();
	}

	@Override
	public
	void onViewCreated ( @NonNull final View view, @Nullable final Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );

		LoginFragmentDirections.NavLoginToLoginBasic basicOptions
				= LoginFragmentDirections.navLoginToLoginBasic(
				mBinding.loginHostInput.getText().toString() );
		basicOptions.setHostname( mBinding.loginHostInput.getText().toString() );

		//basicOptions.


		mBinding.loginWithBasic.setOnClickListener(
				v -> {
					Bundle args = new Bundle();
					args.putString( "hostname", mBinding.loginHostInput.getText().toString() );
					navigateTo( R.id.nav_login_to_loginBasic, args );
				} );
	}

	@Override
	public
	void onActivityCreated ( @Nullable Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );
		mViewModel = ViewModelProviders.of( this ).get( LoginViewModel.class );
		// TODO: Use the ViewModel
	}

	@Override
	public
	void onStart ()
	{
		super.onStart();
	}

	@Override
	public
	void onStop ()
	{
		super.onStop();

	}

	@Override
	public
	void onResume ()
	{
		Timber.d( "onResume" );

		super.onResume();

		//		browseDNSSD();
	}

	@Override
	public
	void onPause ()
	{
		Timber.d( "onPause" );
		super.onPause();

		//		mDNSSDService.stop();
		//		mDNSSDService = null;
	}

	//	private
	//	void browseDNSSD()
	//	{
	//		if (mDNSSDService != null)
	//		{
	//			return;
	//		}
	//
	//		try
	//		{
	//			mDNSSDService = mDNSSD.browse( "_nextcloud._tcp", new BrowseListener()
	//			{
	//				@Override
	//				public
	//				void serviceFound (
	//						final DNSSDService browser, final int flags, final int ifIndex,
	//						final String serviceName, final String regType, final String domain )
	//				{
	//					Timber.d( "serviceFound: %s %s %s", serviceName, regType, domain );
	//					try
	//					{
	//						mDNSSD.resolve( flags, ifIndex, serviceName, regType, domain,
	//						mDnssdResolveListener );
	//					}
	//					catch ( DNSSDException e )
	//					{
	//						e.printStackTrace();
	//					}
	//				}
	//
	//				@Override
	//				public
	//				void serviceLost (
	//						final DNSSDService browser, final int flags, final int ifIndex,
	//						final String serviceName, final String regType, final String domain )
	//				{
	//					Timber.d( "serviceLost: %s %s %s", serviceName, regType, domain );
	//				}
	//
	//				@Override
	//				public
	//				void operationFailed ( final DNSSDService service, final int errorCode )
	//				{
	//					Timber.w( "dnssd browse failed: %d", errorCode );
	//				}
	//			} );
	//		}
	//		catch ( DNSSDException e )
	//		{
	//			e.printStackTrace();
	//		}
	//	}
	//	private static
	//	class HostSpinnerAdapter extends BaseAdapter implements Filterable
	//	{
	//		private List<String> mHosts;
	//		private Context mContext;
	//
	//		public HostSpinnerAdapter(@NonNull Context context)
	//		{
	//			mContext = checkNotNull( context, "Null context?!" );
	//			mHosts = new ArrayList<>();
	//		}
	//
	//		public
	//		void addHost(@NonNull String host)
	//		{
	//			mHosts.add( checkNotNull(host, "Null host?!") );
	//			notifyDataSetChanged();
	//		}
	//
	//		public
	//		void removeHost(@NonNull String host)
	//		{
	//			mHosts.remove( checkNotNull( host , "Null host?!" ));
	//			notifyDataSetChanged();
	//		}
	//
	//		@Override
	//		public
	//		int getCount ()
	//		{
	//			return mHosts.size();
	//		}
	//
	//		@Override
	//		public
	//		Object getItem ( final int position )
	//		{
	//			return mHosts.get( position );
	//		}
	//
	//		@Override
	//		public
	//		long getItemId ( final int position )
	//		{
	//			return mHosts.get( position ).hashCode();
	//		}
	//
	//		@Override
	//		public
	//		View getView ( final int position, View convertView, final ViewGroup parent )
	//		{
	//			if (convertView == null)
	//			{
	//				convertView = LayoutInflater.from( mContext ).inflate( R.layout
	//				.login_host_list_item, parent, false);
	//			}
	//
	//			String currentHost = mHosts.get( position );
	//
	//			TextView hostView = convertView.findViewById( R.id.login_host_list_item_text );
	//			hostView.setText( currentHost );
	//
	//			return convertView;
	//		}
	//
	//		@Override
	//		public
	//		Filter getFilter ()
	//		{
	//			return null;
	//		}
	//	}
	//
	//	private
	//	class DNSSDResolveListener implements ResolveListener
	//	{
	//		@Override
	//		public
	//		void serviceResolved (
	//				final DNSSDService resolver, final int flags, final int ifIndex,
	//				final String fullName, final String hostName, final int port,
	//				final Map< String, String > txtRecord )
	//		{
	//			Timber.d( "serviceResolved: %s %s %d", fullName, hostName, port );
	//			mHostSpinnerAdapter.addHost( hostName );
	//		}
	//
	//		@Override
	//		public
	//		void operationFailed (
	//				final DNSSDService service, final int errorCode )
	//		{
	//			Timber.d( "resolve failed: %d", errorCode );
	//		}
	//	}
}
