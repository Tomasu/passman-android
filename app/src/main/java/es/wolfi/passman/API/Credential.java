/**
 *  Passman Android App
 *
 * @copyright Copyright (c) 2016, Sander Brand (brantje@gmail.com)
 * @copyright Copyright (c) 2016, Marcos Zuriaga Miguel (wolfi@wolfi.es)
 * @license GNU AGPL version 3 or any later version
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package es.wolfi.passman.API;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

import es.wolfi.utils.Filterable;
import es.wolfi.utils.JSONUtils;
import timber.log.Timber;

public class Credential implements Filterable {
    @SerializedName( "credential_id" )
    public int id;

    protected String guid;

    @SerializedName( "vault_id" )
    protected int vaultId;

    @SerializedName( "user_id" )
    protected String userId;

    protected String label;
    protected String description;
    protected long created;
    protected long changed;
    protected String tags;
    protected String email;
    protected String username;
    protected String password;
    protected String url;
    protected Icon favicon;

    @SerializedName( "renew_interval" )
    protected long renewInterval;

    @SerializedName( "expire_time" )
    protected long expireTime;

    @SerializedName( "delete_time" )
    protected long deleteTime;

    protected String files;

    @SerializedName( "custom_fields" )
    protected String customFields;

    protected String otp;
    protected int hidden;

    @SerializedName( "shared_key" )
    protected String sharedKey;

    protected transient Vault vault;

    public int getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public int getVaultId() {
        return vaultId;
    }

    public String getUserId() {
        return userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return vault.decryptString(description);
    }

    public void setDescription(String description) {
        this.description = vault.encryptString(description);
    }

    public long getCreated() {
        return created;
    }

    public long getChanged() {
        return changed;
    }

    public void setChanged(long changed) {
        this.changed = changed;
    }

    public String getTags() {
        return vault.decryptString(tags);
    }

    public void setTags(String tags) {
        this.tags = vault.encryptString(tags);
    }

    public String getEmail() {
        return vault.decryptString(email);
    }

    public void setEmail(String email) {
        this.email = vault.encryptString(email);
    }

    public String getUsername() {
        String decrypted = vault.decryptString( username );
        Timber.d( "getUsername: %s / %s", username, decrypted );
        return decrypted;
    }

    public void setUsername(String username) {
        Timber.d( "setUsername: %s", username );
        this.username = vault.encryptString(username);
    }

    public String getPassword() {
        return vault.decryptString(password);
    }

    public void setPassword(String password) {
        this.password = vault.encryptString(password);
    }

    public String getUrl() {
        return vault.decryptString(url);
    }

    public void setUrl(String url) {
        this.url = vault.encryptString(url);
    }

    public Icon getFavicon() {
        return favicon;
    }

    public void setFavicon(Icon favicon) {
        this.favicon = favicon;
    }

    public long getRenewInterval() {
        return renewInterval;
    }

    public void setRenewInterval(long renewInterval) {
        this.renewInterval = renewInterval;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getFiles() {
        return vault.decryptString(files);
    }

    public void setFiles(String files) {
        this.files = vault.encryptString(files);
    }

    public String getCustomFields() {
        return vault.decryptString(customFields);
    }

    public void setCustomFields(String customFields) {
        this.customFields = vault.encryptString(customFields);
    }

    public String getOtp() {
        return vault.decryptString(otp);
    }

    public void setOtp(String otp) {
        this.otp = vault.encryptString(otp);
    }

    public boolean isHidden() {
        return hidden == 1;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden ? 1 : 0;
    }

    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault v) {
        vault = v;
    }

    public static class Deserializer implements JsonDeserializer<Credential>
    {
        @Override
        public
        Credential deserialize (
              final JsonElement json, final Type typeOfT, final JsonDeserializationContext context )
              throws JsonParseException
        {
            Credential c = new Credential();
            JsonObject obj = json.getAsJsonObject();

            c.id = obj.get( "credential_id" ).getAsInt();
            c.guid = JSONUtils.getString( obj, "guid" );
            c.vaultId = obj.get( "vault_id" ).getAsInt();
            c.userId = JSONUtils.getString( obj, "user_id" );
            c.label = JSONUtils.getString( obj, "label" );
            c.description = JSONUtils.getString( obj, "description" );
            c.created = obj.get( "created" ).getAsLong();
            c.changed = obj.get( "changed" ).getAsLong();
            c.tags = JSONUtils.getString( obj, "tags" );
            c.email = JSONUtils.getString( obj, "email" );
            c.username = JSONUtils.getString( obj, "username" );
            c.password = JSONUtils.getString( obj, "password" );
            c.url = JSONUtils.getString( obj, "url" );

            JsonElement icon = null;

            if (obj.has( "favicon" ))
            {
                icon = obj.get( "favicon" );
            }
            else if (obj.has( "icon" ))
            {
                icon = obj.get( "icon" );
            }

            if ( icon != null && !icon.isJsonNull() )
            {
                //Timber.d( "icon: %s", icon.toString() );
                c.favicon = context.deserialize( icon, Icon.class );
            }
            else
            {
                //Timber.d( "favicon is null!");
                c.favicon = new Icon();
            }

            JsonElement renewInterval = obj.get( "renew_interval" );
            if (!renewInterval.isJsonNull())
            {
                c.renewInterval = obj.get( "renew_interval" ).getAsLong();
            }
            else
            {
                c.renewInterval = 0;
            }

            c.expireTime = obj.get( "expire_time" ).getAsLong();
            c.deleteTime = obj.get( "delete_time" ).getAsLong();
            c.files = JSONUtils.getString( obj,  "files" );
            c.customFields = JSONUtils.getString( obj, "custom_fields" );
            c.otp = JSONUtils.getString( obj, "otp" );
            c.hidden = obj.get( "hidden" ).getAsInt();//(j.getInt("hidden") > 0);

            c.sharedKey = JSONUtils.getString( obj, "shared_key" );

            return c;
        }
    }

    public static class Icon
    {
        public String type;
        public String content;

        public
        String getType ()
        {
            return type;
        }

        public
        String getContent ()
        {
            return content;
        }
    }

//    public static Credential fromJSON(JSONObject j) throws JSONException {
//        Credential c = new Credential();
//
//        c.id = j.getInt("credential_id");
//        c.guid = j.getString("guid");
//        c.vaultId = j.getInt("vault_id");
//        c.userId = j.getString("user_id");
//        c.label = j.getString("label");
//        c.description = j.getString("description");
//        c.created = j.getLong("created");
//        c.changed = j.getLong("changed");
//        c.tags = j.getString("tags");
//        c.email = j.getString("email");
//        c.username = j.getString("username");
//        c.password = j.getString("password");
//        c.url = j.getString("url");
//
//        try {
//            c.favicon = j.getString("favicon");
//        }
//        catch (JSONException ex) {
//            try {
//                c.favicon = j.getString("icon");
//            }
//            catch (JSONException ex2) {
//                Log.e("Credential parse", "error, it has no icon or favicon field!", ex2);
//            }
//        }
//
//        if (j.isNull("renew_interval")) {
//            c.renewInterval = 0;
//        }
//        else {
//            c.renewInterval = j.getLong("renew_interval");
//        }
//
//        c.expireTime = j.getLong("expire_time");
//        c.deleteTime = j.getLong("delete_time");
//        c.files = j.getString("files");
//        c.customFields = j.getString("custom_fields");
//        c.otp = j.getString("otp");
//        c.hidden = j.getInt( "hidden" );//(j.getInt("hidden") > 0);
//        c.sharedKey = j.getString("shared_key");
//
//        return c;
//    }
//
//    public static Credential fromJSON(JSONObject j, Vault v) throws JSONException {
//        Credential c = Credential.fromJSON(j);
//        c.setVault(v);
//        return c;
//    }

    @Override
    public String getFilterableAttribute() {
        return getLabel().toLowerCase();
    }
}
