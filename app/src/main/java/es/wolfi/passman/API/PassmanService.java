package es.wolfi.passman.API;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public interface PassmanService
{
	@GET("vaults")
	Single< List<Vault> > listVaults ();

	@GET("vaults/{guid}")
	Single< Vault > getVault ( @Path ( "guid" ) String guid );
}
