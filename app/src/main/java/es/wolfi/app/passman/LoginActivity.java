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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.wolfi.passman.API.PassmanApi;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public
class LoginActivity extends BaseActivity
{

	public static final String ACTION_AUTH_RETURN = "es.wolfi.app.passman.AUTH_RETURN";

	public static final String REDIR_SCHEME = "passman";
	public static final String REDIR_URI = REDIR_SCHEME + "://login/oauth_callback";
	private static final int REQUEST_GET_ACCOUNTS = 1;
	private static final int REQ_CHOOSE_ACCOUNT = 2;

	@Inject
	AccountManager mAccountManager;

	@BindView (R.id.login_account_list)
	ListView mListView;

	@Inject
	SharedPreferences mSettings;

	@Inject
	DataStore mDataStore;

	@Inject
	PassmanApi mApi;

	private ItemClickListener mItemClickListener;

	@Override
	protected
	void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_login );
		ButterKnife.bind( this );

		mItemClickListener = new ItemClickListener( this );

		mListView.setOnItemClickListener( mItemClickListener );

		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		Intent intent = getIntent();
		String action = intent.getAction();

		//        if (action == null || !action.contentEquals( ACTION_AUTH_RETURN ))
		//        {
		//            Timber.d( "get host!" );
		//            // show get host page!
		//            return;
		//        }

		getAccount();

		// handle login!

		Timber.d( "data: %s", intent.getDataString() );

	}

	@Override
	protected
	void onActivityResult (
			final int requestCode, final int resultCode, final Intent data )
	{
		switch ( requestCode )
		{
			case REQ_CHOOSE_ACCOUNT:
				if ( data != null )
				{
					Timber.d( "choose account: %s", data.toString() );
				}
				else
				{
					Timber.w( "request failed?!" );
				}
				break;

			default:
				super.onActivityResult( requestCode, resultCode, data );
				break;
		}
	}

	private
	void getAccount ()
	{
		//        Intent chooseIntent = AccountPicker.newChooseAccountIntent( null,
		//        null, new String[]{"nextcloud"},true, null, "org.nextcloud", null,
		//        null );
		//        startActivityForResult( chooseIntent, REQ_CHOOSE_ACCOUNT );

		//		List< Account > accountsImporter = AccountImporter.findAccounts( this );
		//		Timber.d( "accounts Importer: %d", accountsImporter.size() );

		//Intent chooseIntent = AccountManager.newChooseAccountIntent( null,
		// null, new String[] { "nextcloud"} , false,null,"org.nextcloud",
		// null, null);
		//startActivityForResult( chooseIntent, REQ_CHOOSE_ACCOUNT );

		//        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build
		//        .VERSION_CODES.M )
		//        {
		//            Intent intent =
		//                  AccountManager.newChooseAccountIntent( null, null,
		//                                                         new String[] {
		//                                                         "nextcloud" },
		//                                                         false,
		//                                                         null,
		//                                                         "org.nextcloud",
		//                                                         null,
		//                                                         null );
		//
		//            startActivityForResult( intent, REQ_CHOOSE_ACCOUNT );
		//        }

		AccountManager accountManager = AccountManager.get( this );
		Account[] accountsByTypeNextcloud = accountManager.getAccountsByType( "nextcloud" );
		Timber.d( "accounts Nextcloud: %d", accountsByTypeNextcloud.length );

		mItemClickListener.setAccounts( accountsByTypeNextcloud );

		AccountListAdapter listAdapter = new AccountListAdapter( this, R.layout.login_list_item,
																					accountsByTypeNextcloud );
		mListView.setAdapter( listAdapter );

		for ( Account account : accountsByTypeNextcloud )
		{
			Timber.d( "account: %s", account.toString() );
		}
	}

	/**
	 * Displays this activity
	 *
	 * @param c
	 * @param cb
	 */
	public static
	void launch ( Context c, ICallback cb )
	{
		Intent i = new Intent( c, LoginActivity.class );
		i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
		c.startActivity( i );
	}

	@Override
	protected
	void onSaveInstanceState ( final Bundle outState )
	{
		mDataStore.save();

		super.onSaveInstanceState( outState );
	}

	public
	SharedPreferences getSettings ()
	{
		return mSettings;
	}

	public
	void onAuthenticated ( @NonNull String host, @NonNull String user, @NonNull String token )
	{
		mDataStore.setAccountInfo( host, user, token );
		mApi.setHost( host );
		mApi.setUserName( user );
		mApi.setAuthToken( token );
	}

	public
	void onAuthenticationFailed ( Exception e, @NonNull String message )
	{
		Timber.e( e, message );
		Snackbar.make( mListView, message, Snackbar.LENGTH_LONG ).show();
	}

	private static
	class AccountListAdapter extends ArrayAdapter< Account >
	{
		public
		AccountListAdapter (
				@NonNull final Context context, final int resource, @NonNull final Account[] objects )
		{
			super( context, resource, objects );
		}

		@NonNull
		@Override
		public
		View getView (
				final int position, @Nullable final View convertView, @NonNull final ViewGroup parent )
		{
			View listItem = convertView;

			if ( listItem == null )
			{
				listItem = LayoutInflater.from( getContext() )
						.inflate( R.layout.login_list_item, parent, false );
			}

			Account account = getItem( position );

			//            ImageView image = (ImageView) listItem.findViewById( R.id
			//            .imageView_poster );
			//            image.setImageResource( account.getmImageDrawable() );

			boolean haveAt = true;
			int atIndex = account.name.indexOf( '@' );
			if ( atIndex < 0 )
			{
				atIndex = account.name.length() - 1;
				haveAt = false;
			}

			String userName = account.name.substring( 0, atIndex );
			String hostName = haveAt ? account.name.substring( atIndex + 1 ) : account.type;

			TextView name = (TextView) listItem.findViewById( R.id.login_list_name );
			name.setText( userName );

			TextView host = (TextView) listItem.findViewById( R.id.login_list_host );
			String hostNameText = "(" + hostName + ")";
			host.setText( hostNameText );

			ImageView avatarView = (ImageView) listItem.findViewById( R.id.login_list_avatar );

			// avatars!
			// dav: https://cloud.tomasu.org/remote.php/dav/avatars/$userid/96
			//   Doesn't work, dav seems to be authenticated by default?
			// private url: https://host/index.php/avatar/$userid/96
			String uri = String.format( Locale.getDefault(), "https://%s/index.php/avatar/%s/%d",
												 hostName, userName, 96 );
			Timber.d( "Get avatar: %s", uri );

			Glide.with( getContext() ).load( uri ).into( avatarView );

			return listItem;
		}
	}

	private static
	class ItemClickListener implements AdapterView.OnItemClickListener
	{
		private AccountManagerFuture< Bundle > authTokenFuture;
		private AccountManager mAccountManager;
		private LoginActivity mActivity;
		private Account[] mAccounts;

		public
		ItemClickListener ( @NonNull LoginActivity activity )
		{
			mActivity = checkNotNull( activity, "Null activity?!" );
			mAccountManager = AccountManager.get( mActivity );
		}

		private
		void setAccounts ( final Account[] accounts )
		{
			mAccounts = accounts;
		}

		@Override
		public
		void onItemClick (
				final AdapterView< ? > parent, final View view, final int position, final long id )
		{
			authTokenFuture = mAccountManager.getAuthToken( mAccounts[ position ],
																			"nextcloud.password", null, mActivity,
																			new NCAccountManagerCallback( mActivity ),
																			null );
		}
	}
}
