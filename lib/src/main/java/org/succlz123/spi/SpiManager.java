package org.succlz123.spi;

import java.util.ArrayList;
import java.util.HashMap;

public class SpiManager {
    private ArrayList<AbstractMapper> allMapper = new ArrayList<>();
    private HashMap<String, SpiObtainInfo<?>> singleInstanceMap = new HashMap<>();

    private SpiManager() {
        register();
    }

    public static SpiManager instance() {
        return _InstanceHolder._sInstance;
    }

    private static class _InstanceHolder {
        private static final SpiManager _sInstance = new SpiManager();
    }

    public <T> T opt(Class<T> clz) {
        if (clz == null) {
            throw new IllegalStateException("clz is null");
        }
        String apiClassName = clz.getName();
        for (AbstractMapper mapper : allMapper) {
            ArrayList<String> classes = mapper.spiMap.get(apiClassName);
            if (classes != null && !classes.isEmpty()) {
                SpiObtainInfo<T> spiObtainInfo = mapper.obtainInstance(apiClassName);
                T data = spiObtainInfo.obtain;
                if (data == null) {
                    throw new IllegalStateException("Can't obtain the impl class < " + apiClassName + " >");
                }
                if (data instanceof SpiSingleService) {
                    ((SpiSingleService) data).initField();
                }
                return spiObtainInfo.obtain;
            }
        }
        throw new IllegalStateException("Can't find the api class < " + apiClassName + " >");
    }

    public <T> T optByName(Class<T> clz, String implAnnotationName) {
        if (clz == null) {
            throw new IllegalStateException("clz is null");
        }
        String apiClassName = clz.getName();
        for (AbstractMapper mapper : allMapper) {
            ArrayList<String> classes = mapper.spiMap.get(apiClassName);
            if (classes != null && !classes.isEmpty()) {
                SpiObtainInfo<T> spiObtainInfo = mapper.obtainInstanceByName(apiClassName, implAnnotationName);
                T data = spiObtainInfo.obtain;
                if (data == null) {
                    throw new IllegalStateException("Can't obtain the impl class < " + apiClassName + " >");
                }
                if (data instanceof SpiSingleService) {
                    ((SpiSingleService) data).initField();
                }
                return spiObtainInfo.obtain;
            }
        }
        throw new IllegalStateException("Can't find the api class < " + apiClassName + " > by name =>>> " + implAnnotationName + " >");
    }

    public <T> ArrayList<T> optAll(Class<T> clz) {
        if (clz == null) {
            throw new IllegalStateException("clz is null");
        }
        String apiClassName = clz.getName();
        for (AbstractMapper mapper : allMapper) {
            ArrayList<String> classes = mapper.spiMap.get(apiClassName);
            if (classes != null && !classes.isEmpty()) {
                ArrayList<SpiObtainInfo<T>> spiObtainInfos = mapper.obtainInstanceByAll(apiClassName);
                if (spiObtainInfos == null || spiObtainInfos.isEmpty()) {
                    throw new IllegalStateException("Can't obtain the impl class < " + apiClassName + " >");
                }
                ArrayList<T> result = new ArrayList<>(spiObtainInfos.size());
                for (SpiObtainInfo<T> obtainInfo : spiObtainInfos) {
                    result.add(obtainInfo.obtain);
                }
                return result;
            }
        }
        throw new IllegalStateException("Can't find the api class < " + apiClassName + " >");
    }

    public <T> T optSingle(Class<T> clz) {
        if (clz == null) {
            throw new IllegalStateException("clz is null");
        }
        String apiClassName = clz.getName();
        SpiObtainInfo<?> instanceObtainInfo = singleInstanceMap.get(apiClassName);
        if (instanceObtainInfo != null) {
            return (T) instanceObtainInfo.obtain;
        }
        for (AbstractMapper mapper : allMapper) {
            ArrayList<String> classes = mapper.spiMap.get(apiClassName);
            if (classes != null && !classes.isEmpty()) {
                SpiObtainInfo<T> spiObtainInfo = mapper.obtainInstance(apiClassName);
                T data = spiObtainInfo.obtain;
                if (data == null) {
                    throw new IllegalStateException("Can't obtain the impl class < " + apiClassName + " >");
                }
                singleInstanceMap.put(apiClassName, spiObtainInfo);
                singleInstanceMap.put(spiObtainInfo.apiClassName + "_" + spiObtainInfo.implAnnotationDefinedName, spiObtainInfo);
                if (data instanceof SpiSingleService) {
                    ((SpiSingleService) data).initField();
                }
                return spiObtainInfo.obtain;
            }
        }
        throw new IllegalStateException("Can't find the api class < " + apiClassName + " >");
    }


    public <T> T optSingleByName(Class<T> clz, String implAnnotationDefinedName) {
        if (clz == null) {
            throw new IllegalStateException("clz is null");
        }
        String apiClassName = clz.getName();
        String keyName = apiClassName + "_" + implAnnotationDefinedName;
        SpiObtainInfo<?> instanceObtainInfo = singleInstanceMap.get(keyName);
        if (instanceObtainInfo != null) {
            return (T) instanceObtainInfo.obtain;
        }
        for (AbstractMapper mapper : allMapper) {
            ArrayList<String> classes = mapper.spiMap.get(apiClassName);
            if (classes != null && !classes.isEmpty()) {
                SpiObtainInfo<T> spiObtainInfo = mapper.obtainInstanceByName(apiClassName, implAnnotationDefinedName);
                T data = spiObtainInfo.obtain;
                if (data == null) {
                    throw new IllegalStateException("Can't obtain the impl class < " + apiClassName + " >");
                }
                if (singleInstanceMap.get(apiClassName) == null) {
                    singleInstanceMap.put(apiClassName, spiObtainInfo);
                }
                singleInstanceMap.put(keyName, spiObtainInfo);
                if (data instanceof SpiSingleService) {
                    ((SpiSingleService) data).initField();
                }
                return spiObtainInfo.obtain;
            }
        }
        throw new IllegalStateException("Can't find the api class < " + apiClassName + " >");
    }

    // auto fill by Gradle plugin
    public void register() {
        // AbstractSpiMapper mapper_a = new AbstractSpiMapper();
        // allMapper.add(mapper_a);
    }
}
