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
package es.wolfi.app.passman;
import android.view.View;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SingleTon {

    protected HashMap<String, View.OnClickListener> _click;
    protected HashMap<String, Object>               _extra;
    protected HashMap<String, String>               _string;
    protected HashMap<String, ICallback>            _callback;

    @Inject
    public SingleTon(){
        _callback   = new HashMap<String, ICallback>();
        _string     = new HashMap<String, String>();
        _click      = new HashMap<String, View.OnClickListener>();
        _extra      = new HashMap<String, Object>();
    }

    public void addClickListener(String name, View.OnClickListener action){
        _click.put(name, action);
    }

    public void addString(String name, String value){
        _string.put(name, value);
    }

    public String getString(String name){
        return _string.get(name);
    }

    public void addCallback(String name, ICallback c){
        _callback.put(name, c);
    }

    public ICallback getCallback(String name){
        return _callback.get(name);
    }

    public void removeCallback(String name){
        _callback.remove(name);
    }

    public void addExtra(String name, Object data){
        _extra.put(name, data);
    }
    public void removeExtra(String name){
        _extra.remove(name);
    }

    public void removeString(String name){
        _string.remove(name);
    }

    public View.OnClickListener getClickListener(String name){
        return _click.get(name);
    }

    public Object getExtra(String name){
        return _extra.get(name);
    }
}
