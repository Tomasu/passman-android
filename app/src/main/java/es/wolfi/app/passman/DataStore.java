package es.wolfi.app.passman;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import es.wolfi.passman.API.Credential;
import es.wolfi.passman.API.Vault;
import java9.util.stream.StreamSupport;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a temporary hack before switching to a proper persistence layer
 *
 * @version ${VERSION}
 * @since ${VERSION}
 */
public
class DataStore
{
	private static final String VAULT_KEY_PREFIX = "__vault__";
	public static final String ACTIVE_VAULT_KEY = "ACTIVE_VAULT";
	public static final String VAULT_SET_KEY = "VAULT_SET";
	private static final String VAULT_PASSWORD_KEY_PREFIX = "__vault_password__";
	public static final String HOST_KEY = "HOST";
	public static final String USER_KEY = "USER";
	public static final String TOKEN_KEY = "TOKEN";

	private Gson mGson;

	private SharedPreferences mPreferences;

	private Vault mActiveVault;

	private HashMap< String, Vault > mVaultMap;
	private HashMap< String, String> mVaultPassMap;

	@Inject
	public
	DataStore ( @NonNull Gson gson, @NonNull SharedPreferences preferences )
	{
		mGson = checkNotNull( gson, "Null gson?!" );
		mPreferences = checkNotNull( preferences, "Null preferences?!" );

		mVaultMap = new HashMap<>();
		mVaultPassMap = new HashMap<>();

		Set< String > vaultSet = mPreferences.getStringSet( VAULT_SET_KEY, null );
		if ( vaultSet != null )
		{
			for ( String vaultGuid : vaultSet )
			{
				String vaultString = getString( getVaultKey( vaultGuid ) );
				if ( vaultString != null )
				{
					Vault vault = mGson.fromJson( vaultString, Vault.class );
					Timber.d( "Loaded vault: %s", vault.name );
					mVaultMap.put( vaultGuid, vault );

					String vaultPassKey = getVaultPassKey( vault );
					String vaultPass = getString( vaultPassKey );
					if (vaultPass != null)
					{
						Timber.d( "vault pass: %s", vaultPass);
						mVaultPassMap.put( vault.guid, vaultPass );
					}
					else
					{
						Timber.d( "vault pass empty" );
					}
				}
			}
		}

		String activeVaultGuid = mPreferences.getString( ACTIVE_VAULT_KEY, null );
		if (activeVaultGuid != null)
		{
			mActiveVault = mVaultMap.get( activeVaultGuid );
		}
	}

	private
	String getVaultPassKey(@NonNull Vault vault)
	{
		return VAULT_PASSWORD_KEY_PREFIX + vault.guid;
	}

	private
	String getVaultKey ( @NonNull Vault vault )
	{
		return VAULT_KEY_PREFIX + vault.guid;
	}

	private
	String getVaultKey ( @NonNull String vaultGuid )
	{
		return VAULT_KEY_PREFIX + vaultGuid;
	}

	public
	void setAccountInfo ( @NonNull String host, @NonNull String user, @NonNull String token )
	{
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString( HOST_KEY, host );
		editor.putString( USER_KEY, user );
		editor.putString( TOKEN_KEY, token );
		editor.apply();
	}

	public
	boolean haveHost()
	{
		return mPreferences.contains( HOST_KEY );
	}

	public
	String getHost()
	{
		return mPreferences.getString( HOST_KEY, null );
	}

	public
	String getUserName()
	{
		return mPreferences.getString( USER_KEY, null );
	}

	public
	String getAuthToken()
	{
		return mPreferences.getString( TOKEN_KEY, null );
	}

	private
	String getString( @NonNull String key )
	{
		return mPreferences.getString( key, null );
	}

	private
	void putString ( @NonNull String key, @NonNull String value )
	{
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString( key, value );
		editor.apply();
	}

	public
	void setActiveVault ( @NonNull Vault vault )
	{
		mActiveVault = checkNotNull( vault, "Null vault?!" );
	}

	public
	String getActiveVaultGuid ()
	{
		return mPreferences.getString( ACTIVE_VAULT_KEY, null );
	}

	public
	Vault getActiveVault ()
	{
		return mActiveVault;
	}

	public
	List< Vault > getVaults ()
	{
		return new ArrayList<>( mVaultMap.values() );
	}

	public
	void clear ()
	{
		SharedPreferences.Editor editor = mPreferences.edit();

		for ( HashMap.Entry< String, Vault > entry : mVaultMap.entrySet() )
		{
			Vault vault = entry.getValue();
			editor.remove( getVaultKey( vault ) );
			editor.remove( getVaultPassKey( vault ) );
		}

		editor.remove( ACTIVE_VAULT_KEY );
		editor.remove( VAULT_SET_KEY );
		editor.remove( HOST_KEY );
		editor.remove( USER_KEY );
		editor.remove( TOKEN_KEY );
		editor.apply();

		mActiveVault = null;
		mVaultMap.clear();
		mVaultPassMap.clear();
	}

	public
	void save ()
	{
		Timber.d( "saving datastore" );
		SharedPreferences.Editor editor = mPreferences.edit();

		for ( HashMap.Entry< String, Vault > entry : mVaultMap.entrySet() )
		{
			Vault vault = entry.getValue();
			String vaultString = mGson.toJson( vault, Vault.class );
			editor.putString( getVaultKey( vault ), vaultString );
			editor.putString( getVaultPassKey( vault ),  mVaultPassMap.get( vault.guid ));
		}

		editor.putStringSet( VAULT_SET_KEY, mVaultMap.keySet() );
		if (mActiveVault != null)
		{
			editor.putString( ACTIVE_VAULT_KEY, mActiveVault.guid );
		}

		editor.apply();
	}

	public
	int getNumVaults ()
	{
		return mVaultMap.size();
	}

	public
	Vault getVault ( @NonNull String name )
	{
		return mVaultMap.get( name );
	}

	public
	void putVaultPassword ( @NonNull Vault vault, @NonNull String password )
	{
		// TODO: encrypt me...
		putString( getVaultPassKey( vault ), password ); // save to storage
		//vault.setEncryptionKey( password );
		mVaultPassMap.put( vault.guid, password );
	}

	public
	String getVaultPassword ( @NonNull Vault vault )
	{
		return mVaultPassMap.get( vault.guid );
	}

	public
	void putVault ( @NonNull String guid, @NonNull Vault vault )
	{
		mVaultMap.put( checkNotNull( guid, "Null name?!" ), checkNotNull( vault, "Null vault?!" ) );
		List< Credential > credentials = vault.getCredentials();
		if (credentials != null && credentials.size() > 0)
		{
			for ( Credential cred : credentials )
			{
				cred.setVault( vault );
			}
		}
	}

	public
	void putVaults( @NonNull List<Vault> vaultList )
	{
		StreamSupport.stream( mVaultMap.keySet() )
				.filter( (key) -> !vaultList.contains( key ) )
				.forEach( (key) -> mVaultPassMap.remove( key ) );

		mVaultMap.clear();

		for ( Vault v : vaultList )
		{
			mVaultMap.put( v.guid, v );
		}

		if (mActiveVault != null && !mVaultMap.containsKey( mActiveVault.guid ))
		{
			mActiveVault = null;
		}
	}
}
