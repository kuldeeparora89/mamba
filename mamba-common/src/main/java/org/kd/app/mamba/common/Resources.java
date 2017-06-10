package org.kd.app.mamba.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by kuldeep on 27-05-2017.
 */
public enum Resources {

    INSTANCE;

    private Env env;
    private Properties props=new Properties();

    Resources() {

        env = Arrays.stream(Env.values()).filter(e -> System.getProperty("env").equals(e.getName())).findFirst().orElseThrow(() -> new RuntimeException("Env is null"));
        try {
            props.load(Resources.class.getResourceAsStream("/"+env.getName() + ".properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Env getEnv() {
        return env;
    }


    public String getProperty(String name) {

        return props.getProperty(name);
    }


}
