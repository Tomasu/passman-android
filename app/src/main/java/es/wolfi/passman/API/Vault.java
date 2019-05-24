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

package es.wolfi.passman.API;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import es.wolfi.app.passman.SJCLCrypto;
import es.wolfi.utils.Filterable;
import es.wolfi.utils.JSONUtils;
import java9.util.stream.StreamSupport;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public
class Vault implements Filterable
{
	public int vault_id;
	public String guid;
	public String name;
	public double created;
	public String public_sharing_key;
	public double last_access;
	public String challenge_password;

	ArrayList< Credential > credentials;

	private String encryption_key = "";

	public
	void setEncryptionKey ( String k )
	{
		encryption_key = checkNotNull( k, "Null key?!" );
	}

	public
	String decryptString ( @NotNull String cryptogram )
	{
		try
		{
			String result = SJCLCrypto.decryptString( checkNotNull( cryptogram, "Null cryptogram?!" ),
														checkNotNull( encryption_key, "Null key?!" ) );

			if (result == null || result.contentEquals( "null" ))
			{
				Timber.w( "decrypt result is null? %s %s", result.getClass().getSimpleName(), result );
			}

			return result;
		}
		catch ( Exception e )
		{
			Timber.e( e );
			e.printStackTrace();
		}

		return "Error decrypting";
	}

	public
	boolean unlock ( @NotNull String key )
	{
		Timber.d( "unlock vault with %s", key );

		encryption_key = checkNotNull( key, "Null key?!" );

		// Check if the key was correct
		if ( is_unlocked() )
		{
			Timber.d( "now unlocked!" );
			return true;
		}

		encryption_key = "";
		return false;
	}

	public
	boolean is_unlocked ()
	{
		try
		{
			Objects.requireNonNull( encryption_key );
			String chpass = credentials != null && credentials.size() > 0
								 ? credentials.get( 0 ).password
								 : this.challenge_password;

			Objects.requireNonNull( chpass );

			if ( !encryption_key.isEmpty() )
			{
				Timber.d( "challenge_pw: %s", chpass );
				SJCLCrypto.decryptString( checkNotNull( chpass, "Null challenge password?!" ),
												  checkNotNull( encryption_key, "Null encryption key?!" ) );
				return true;
			}

			return false;
		}
		catch ( Exception e )
		{
			Timber.w( e );
			return false;
		}
	}

	public
	String encryptString ( String plaintext )
	{
		// TODO: Implement encryption
		return "";
	}

	public
	Date getCreatedTime ()
	{
		return new Date( (long) created * 1000 );
	}

	public
	Credential findCredentialByGUID ( @NonNull String guid )
	{
		checkNotNull( guid, "Null guid?!" );

		Credential found = StreamSupport.stream( credentials )
				.filter( ( cred ) -> cred.guid.contentEquals( guid ) )
				.findFirst()
				.orElseGet( null );

		Timber.e( "GUID: %s Arr pos: %s", guid, String.valueOf( found ) );

		return found;
	}

	public
	Date getLastAccessTime ()
	{
		return new Date( (long) last_access * 1000 );
	}

	public
	ArrayList< Credential > getCredentials ()
	{
		return credentials;
	}

	public
	void setCredentials ( @NonNull ArrayList< Credential > credentials )
	{
		this.credentials = checkNotNull( credentials, "Null credentials?!" );
		StreamSupport.stream( credentials ).forEach( credential -> credential.setVault( this ) );
	}

	//    public static void getVaults (Context c, final FutureCallback<HashMap<String, Vault>> cb
    //    ) {
	//        Vault.requestAPIGET(c, "vaults",new FutureCallback<String>() {
	//            @Override
	//            public void onCompleted(Exception e, String result) {
	//                if (e != null) {
	//                    cb.onCompleted(e, null);
	//                    return;
	//                }
	//
	//                Log.e(Vault.LOG_TAG, result);
	////                cb.onCompleted(e, null);
	//                try {
	//                    JSONArray data = new JSONArray(result);
	//                    HashMap<String, Vault> l = new HashMap<String, Vault>();
	//                    for (int i = 0; i < data.length(); i++) {
	//                        Vault v = Vault.fromJSON(data.getJSONObject(i));
	//                        l.put(v.guid, v);
	//                    }
	//
	//                    cb.onCompleted(e, l);
	//                }
	//                catch (JSONException ex) {
	//                    cb.onCompleted(ex, null);
	//                }
	//            }
	//        });
	//    }
	//
	//    public static void getVault(Context c, String guid, final FutureCallback<Vault> cb) {
	//        Vault.requestAPIGET(c, "vaults/".concat(guid),new FutureCallback<String>() {
	//            @Override
	//            public void onCompleted(Exception e, String result) {
	//                if (e != null) {
	//                    cb.onCompleted(e, null);
	//                    return;
	//                }
	//
	//                try {
	//                    JSONObject data = new JSONObject(result);
	//
	//                    Vault v = Vault.fromJSON(data);
	//
	//                    cb.onCompleted(e, v);
	//                }
	//                catch (JSONException ex) {
	//                    cb.onCompleted(ex, null);
	//                }
	//            }
	//        });
	//    }

	public static
	class Deserializer implements JsonDeserializer< Vault >
	{
		@Override
		public
		Vault deserialize (
				final JsonElement json, final Type typeOfT, final JsonDeserializationContext context )
				throws JsonParseException
		{
			Timber.d( "deserialize!" );
			Vault v = new Vault();

			JsonObject o = json.getAsJsonObject();

			v.vault_id = o.get( "vault_id" ).getAsInt();
			v.guid = JSONUtils.getString( o,"guid" );
			v.name = JSONUtils.getString( o, "name" );
			v.created = o.get( "created" ).getAsDouble();
			v.public_sharing_key = JSONUtils.getString( o, "public_sharing_key" );
			v.last_access = o.get( "last_access" ).getAsDouble();

			if ( o.has( "credentials" ) )
			{
				JsonArray j = o.getAsJsonArray( "credentials" );
				v.credentials = new ArrayList< Credential >();

				JsonObject firstCred = j.get( 1 ).getAsJsonObject();
				Timber.d( "secondCred: %s", firstCred );

				for ( int i = 0; i < j.size(); i++ )
				{
					JsonObject object = j.get(i).getAsJsonObject();
					Credential c = context.deserialize( object, Credential.class );
					if ( c.getDeleteTime() == 0 )
					{
						v.credentials.add( c );
					}

					c.setVault(v);
				}

				v.challenge_password = v.credentials.get( 0 ).password;
			}
			else
			{
				v.challenge_password = JSONUtils.getString( o, "challenge_password" );
			}

			Timber.d( "challenge_password: %s", v.challenge_password );

//			if (o.has( "encryption_key" ))
//			{
//				v.encryption_key = o.get( "encryption_key" ).getAsString();
//				Timber.d( "enckey: %s", v.encryption_key );
//			}

			return v;
		}
	}

//	public static
//	Vault fromJSON ( JSONObject o ) throws JSONException
//	{
//		Vault v = new Vault();
//
//		v.vault_id = o.getInt( "vault_id" );
//		v.guid = o.getString( "guid" );
//		v.name = o.getString( "name" );
//		v.created = o.getDouble( "created" );
//		v.public_sharing_key = o.getString( "public_sharing_key" );
//		v.last_access = o.getDouble( "last_access" );
//
//		if ( o.has( "credentials" ) )
//		{
//			JSONArray j = o.getJSONArray( "credentials" );
//			v.credentials = new ArrayList< Credential >();
//			v.credential_guid = new HashMap<>();
//
//			for ( int i = 0; i < j.length(); i++ )
//			{
//				Credential c = Credential.fromJSON( j.getJSONObject( i ), v );
//				if ( c.getDeleteTime() == 0 )
//				{
//					v.credentials.add( c );
//					v.credential_guid.put( c.getGuid(), v.credentials.size() - 1 );
//				}
//			}
//			v.challenge_password = v.credentials.get( 0 ).password;
//		}
//		else
//		{
//			v.challenge_password = o.getString( "challenge_password" );
//		}
//
//		return v;
//	}

	@Override
	public
	String getFilterableAttribute ()
	{
		return this.name.toLowerCase();
	}
}
