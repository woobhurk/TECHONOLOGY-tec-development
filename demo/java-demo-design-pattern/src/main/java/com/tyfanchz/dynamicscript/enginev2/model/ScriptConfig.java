package com.tyfanchz.dynamicscript.enginev2.model;

import java.io.Serializable;
import java.util.Map;

public class ScriptConfig implements Serializable {
    private String engine;
    private String namespace;
    private Map<String, MethodConfig> methods;

    public String getEngine() {
        return this.engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, MethodConfig> getMethods() {
        return this.methods;
    }

    public void setMethods(Map<String, MethodConfig> methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "ScriptConfig{" +
            "engine='" + this.engine + '\'' +
            ", namespace='" + this.namespace + '\'' +
            ", methods=" + this.methods +
            '}';
    }
}
