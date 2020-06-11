package org.succlz123.spi.app.service;

import org.succlz123.spi.SpiApi;

@SpiApi()
public interface IPlayer {

    void play();

    void pause();

    void resume();

    void seek(int time);

    void stop();

    void release();
}