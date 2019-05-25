package es.wolfi.app.passman.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import static com.google.common.base.Preconditions.checkNotNull;

public
class LoginBasicViewModel extends ViewModel
{
	private
	SavedStateHandle mSavedStateHandle;

	private
	MutableLiveData<String> mHost;

	private
	MutableLiveData<String> mUser;

	@SuppressWarnings ("unused")
	public
	LoginBasicViewModel()
	{
		mSavedStateHandle = new SavedStateHandle();
		mHost = mSavedStateHandle.getLiveData( "host" );
		mUser = mSavedStateHandle.getLiveData( "user" );
	}

	@SuppressWarnings ("unused")
	public
	LoginBasicViewModel ( @NonNull SavedStateHandle savedStateHandle )
	{
		mSavedStateHandle = checkNotNull(savedStateHandle, "Null savedStateHandle?!");
		mHost = savedStateHandle.getLiveData( "host" );
		mUser = savedStateHandle.getLiveData( "user" );
	}

	public
	void setHost ( final String host )
	{
		mSavedStateHandle.set( "host", host );
		mHost.postValue( host );
	}

	public
	void setUser ( final String user )
	{
		mSavedStateHandle.set( "user", user );
		mUser.postValue( user );
	}

	public
	MutableLiveData< String > getHostLiveData ()
	{
		return mHost;
	}

	public
	MutableLiveData< String > getUserLiveData ()
	{
		return mUser;
	}

	public
	String getHost ()
	{
		return mHost.getValue();
	}

	public
	String getUser ()
	{
		return mUser.getValue();
	}
}
