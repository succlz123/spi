package org.succlz123.spi.app.impl;

import android.util.Log;

import org.succlz123.spi.SpiImpl;
import org.succlz123.spi.SpiManager;
import org.succlz123.spi.SpiSingleService;
import org.succlz123.spi.app.service.IPlayer;
import org.succlz123.spi.app.service.IWifiService;

@SpiImpl
public class WifiManager implements IWifiService, SpiSingleService {
    //    IPlayer player0 = SpiManager.getInstance().optSingleByName(IPlayer.class, "AvPlayer");
    IPlayer player0;

    @Override
    public void initField() {
        player0 = SpiManager.instance().optSingleByName(IPlayer.class, "AvPlayer");
    }

    @Override
    public void open() {
        Log.e("Spi", "WifiManager is open");
    }

    @Override
    public void close() {

    }
}
