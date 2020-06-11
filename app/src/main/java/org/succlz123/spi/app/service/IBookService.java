package org.succlz123.spi.app.service;

import org.succlz123.spi.SpiApi;

@SpiApi
public interface IBookService {

    void start();

    void close();
}