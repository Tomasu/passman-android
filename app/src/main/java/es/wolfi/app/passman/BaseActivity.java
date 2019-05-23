package es.wolfi.app.passman;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

/**
 * @version ${VERSION}
 * @since ${VERSION}
 */
public
abstract class BaseActivity extends AppCompatActivity implements HasSupportFragmentInjector
{
	@Inject
	DispatchingAndroidInjector< Fragment > mFragmentDispatchingAndroidInjector;

	@Override
	protected
	void onCreate (
			@Nullable final Bundle savedInstanceState)
	{
		AndroidInjection.inject( this );
		super.onCreate(savedInstanceState);
	}

	@Override
	public
	AndroidInjector< Fragment > supportFragmentInjector ()
	{
		return mFragmentDispatchingAndroidInjector;
	}
}
