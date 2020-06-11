package org.succlz123.spi;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbstractMapper {
    HashMap<String, ArrayList<String>> spiMap = new HashMap<>();

    public abstract <T> SpiObtainInfo<T> obtainInstance(String apiClassName);

    public abstract <T> SpiObtainInfo<T> obtainInstanceByName(String apiClassName, String implAnnotationDefinedName);

    public abstract <T> ArrayList<SpiObtainInfo<T>> obtainInstanceByAll(String apiClassName);
}