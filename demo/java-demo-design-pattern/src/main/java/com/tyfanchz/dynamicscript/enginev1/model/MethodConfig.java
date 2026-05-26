package com.tyfanchz.dynamicscript.enginev1.model;

import java.io.Serializable;
import java.util.Arrays;

public class MethodConfig implements Serializable {
    private String engine;
    private String script;
    private String[] scripts;

    public String getEngine() {
        return this.engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getScript() {
        return this.script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String[] getScripts() {
        return this.scripts;
    }

    public void setScripts(String[] scripts) {
        this.scripts = scripts;
    }

    @Override
    public String toString() {
        return "MethodConfig{" +
            "engine='" + this.engine + '\'' +
            ", script='" + this.script + '\'' +
            ", scripts=" + Arrays.toString(this.scripts) +
            '}';
    }
}
